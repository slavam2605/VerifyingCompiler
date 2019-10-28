package compiler

import compiler.ast.*
import compiler.resolved.*
import compiler.types.Type
import compiler.types.TypeChecker
import compiler.types.prettyPrint
import compiler.verification.verify
import utils.exhaustive

class CppCompiler {
    private val builder = CppBuilder()
    private val resolutionContext = ResolutionContext()

    val result: String
        get() = builder.toString()

    init {
        builder.appendHeader("cstdint")
        builder.appendNewLine()
    }

    fun compileFunction(ast: FunctionDeclarationAstNode) {
        builder.appendFunctionHeader(
            ast.name,
            ast.returnType.descriptor.type.formatCppType(),
            ast.parameters.map { it.name to it.descriptor.type.formatCppType() }
        )

        val resolvedInputContract = mutableListOf<ResolvedExpression>()
        val resolvedOutputContract = mutableListOf<ResolvedExpression>()
        val functionDescriptor = FunctionDescriptor(
            ast.name,
            resolvedInputContract,
            resolvedOutputContract,
            ast.parameters.map { it.descriptor },
            ast.returnType.descriptor.type
        )
        resolutionContext.addDeclaration(ast.name, functionDescriptor)

        resolutionContext.withFunctionLayer(functionDescriptor) {
            resolvedInputContract.addAll(ast.inputContract?.expressions?.map { resolveExpression(it) } ?: emptyList())
            resolvedOutputContract.addAll(ast.outputContract?.expressions?.map { resolveExpression(it) } ?: emptyList())

            val provedContext = ProvedContext(*resolvedInputContract.toTypedArray())
            compileBlock(ast.body, provedContext)
        }
    }

    private fun compileBlock(ast: CodeBlockAstNode, provedContext: ProvedContext) {
        resolutionContext.withLayer {
            builder.withBlock {
                ast.statements.forEach {
                    compileStatement(it, provedContext)
                }
            }
        }
    }

    private fun compileStatement(ast: CodeStatementAstNode, provedContext: ProvedContext) {
        when (ast) {
            is VarDeclarationAstNode -> compileVarDeclaration(ast, provedContext)
            is ProofElementAstNode -> compileProofElement(ast, provedContext)
        }.exhaustive
    }

    private fun compileProofElement(ast: ProofElementAstNode, provedContext: ProvedContext) {
        val resolvedExpression = resolveExpressionAssertType(ast.expression, Type.BooleanType)
        if (!resolvedExpression.verify(provedContext)) {
            throw CompilationException("Proof element couldn't be verified: ${resolvedExpression.prettyPrint()}")
        }

        provedContext.addExpression(resolvedExpression)
    }

    private fun compileVarDeclaration(ast: VarDeclarationAstNode, provedContext: ProvedContext) {
        val varType = ast.type.descriptor.type
        if (ast.initializer == null) {
            builder.appendVarDeclaration(varType.formatVarDeclaration(ast.name))
        } else {
            val expressionDescriptor = resolveExpression(ast.initializer)
            if (!TypeChecker.areEqual(varType, expressionDescriptor.type)) {
                throw CompilationException("Wrong type: expected ${varType.prettyPrint()}, actual: ${expressionDescriptor.type.prettyPrint()}")
            }

            val compiledExpression = compileExpression(expressionDescriptor, provedContext)
            builder.appendVarDeclaration(
                varType.formatVarDeclaration(ast.name),
                compiledExpression
            )
        }
        resolutionContext.addDeclaration(ast.name, ast.descriptor)
    }

    private fun compileExpression(expression: ResolvedExpression, provedContext: ProvedContext): String {
        return when (expression) {
            is ResolvedSymbolReference -> when (expression.descriptor) {
                is LocalVariableDescriptor -> expression.descriptor.name
                is FunctionParameterDescriptor -> expression.descriptor.name
                is ProofReturnValueDescriptor -> throw CompilationException("ProofReturnValueDescriptor can't be compiled")
            }
            is ResolvedIntegerLiteral -> expression.value
            is ResolvedBooleanLiteral -> expression.value.toString()
            is ResolvedComparison -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext
            ) { left, right ->
                "($left) ${expression.op} ($right)"
            }
            is ResolvedNot -> "!(${compileExpression(expression.child, provedContext)})"
            is ResolvedOr -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext,
                { it.createNested(ResolvedNot(expression.left)) }
            ) { left, right ->
                "($left) || ($right)"
            }
            is ResolvedAnd -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext,
                { it.createNested(expression.left) }
            ) { left, right ->
                "($left) && ($right)"
            }
            is ResolvedArrow -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext,
                { it.createNested(expression.left) }
            ) { left, right ->
                "!($left) || ($right)"
            }
            is ResolvedInvocation -> {
                val compilerArguments = expression.arguments.map { compileExpression(it, provedContext) }
                expression.inputContract.forEach {
                    if (!provedContext.contains(it)) {
                        throw CompilationException("Expression was not proved: ${it.prettyPrint()}")
                    }
                }
                "${expression.functionDescriptor.name}(${compilerArguments.joinToString()})"
            }
            is ResolvedMultiplication -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext
            ) { left, right ->
                "($left) * ($right)"
            }
            is ResolvedDivision -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext
            ) { left, right ->
                "($left) / ($right)"
            }
            is ResolvedAddition -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext
            ) { left, right ->
                "($left) + ($right)"
            }
            is ResolvedSubtraction -> compileBinaryExpression(
                expression.left,
                expression.right,
                provedContext
            ) { left, right ->
                "($left) - ($right)"
            }
        }
    }

    private fun compileBinaryExpression(
        left: ResolvedExpression,
        right: ResolvedExpression,
        provedContext: ProvedContext,
        pcTransform: (ProvedContext) -> ProvedContext = { it },
        result: (String, String) -> String
    ): String {
        val compiledLeft = compileExpression(left, provedContext)
        val compiledRight = compileExpression(right, pcTransform(provedContext))
        return result(compiledLeft, compiledRight)
    }

    /* ============================== expression resolution ============================== */

    private fun resolveExpression(ast: CodeExpressionAstNode): ResolvedExpression {
        return when (ast) {
            is IntegerLiteralAstNode -> resolveIntLiteral(ast)
            is BooleanLiteralAstNode -> ResolvedBooleanLiteral(ast.value)
            is InvocationAstNode -> resolveInvocation(ast)
            is SymbolReferenceAstNode -> resolveSymbolReference(ast)
            is ComparisonNode -> resolveBinaryOperation(ast.left, ast.right, Type.StrictInteger.Int64, Type.StrictInteger.Int64) { left, right -> ResolvedComparison(ast.op, left, right) }
            is NotNode -> resolveNot(ast)
            is OrNode -> resolveBinaryOperation(ast.left, ast.right, Type.BooleanType, Type.BooleanType) { left, right -> ResolvedOr(left, right) }
            is AndNode -> resolveBinaryOperation(ast.left, ast.right, Type.BooleanType, Type.BooleanType) { left, right -> ResolvedOr(left, right) }
            is ArrowNode -> resolveBinaryOperation(ast.left, ast.right, Type.BooleanType, Type.BooleanType) { left, right -> ResolvedOr(left, right) }
            is MultiplicationAstNode -> resolveBinaryOperation(ast.left, ast.right, Type.StrictInteger.Int64, Type.StrictInteger.Int64) { left, right -> ResolvedMultiplication(left, right) }
            is DivisionAstNode -> resolveBinaryOperation(ast.left, ast.right, Type.StrictInteger.Int64, Type.StrictInteger.Int64) { left, right -> ResolvedDivision(left, right) }
            is AdditionAstNode -> resolveBinaryOperation(ast.left, ast.right, Type.StrictInteger.Int64, Type.StrictInteger.Int64) { left, right -> ResolvedAddition(left, right) }
            is SubtractionAstNode -> resolveBinaryOperation(ast.left, ast.right, Type.StrictInteger.Int64, Type.StrictInteger.Int64) { left, right -> ResolvedSubtraction(left, right) }
        }
    }

    private fun resolveExpressionAssertType(ast: CodeExpressionAstNode, type: Type): ResolvedExpression {
        val resolved = resolveExpression(ast)
        if (!TypeChecker.areEqual(resolved.type, type)) {
            throw CompilationException("Expected type ${type.prettyPrint()}, found: ${resolved.type.prettyPrint()}")
        }

        return resolved
    }

    private fun <T: ResolvedExpression> resolveBinaryOperation(
        left: CodeExpressionAstNode,
        right: CodeExpressionAstNode,
        leftType: Type,
        rightType: Type,
        constructor: (ResolvedExpression, ResolvedExpression) -> T
    ): T {
        val resolvedLeft = resolveExpressionAssertType(left, leftType)
        val resolvedRight = resolveExpressionAssertType(right, rightType)
        return constructor(resolvedLeft, resolvedRight)
    }

    private fun resolveInvocation(ast: InvocationAstNode): ResolvedInvocation {
        val functionDescriptor = resolutionContext.findDeclaration(ast.name)
            ?: throw CompilationException("Unresolved symbol reference: ${ast.name}")

        if (functionDescriptor !is FunctionDescriptor) {
            throw CompilationException("Expected a callable reference, found: ${functionDescriptor.javaClass}")
        }

        val resolvedArguments = ast.arguments.mapIndexed { index, node ->
            resolveExpressionAssertType(node, functionDescriptor.parameters[index].type)
        }

        val resolvedInputContract = functionDescriptor.inputContract.map { it.substitute { expr ->
            if (expr !is ResolvedSymbolReference)
                return@substitute null

            functionDescriptor.parameters.forEachIndexed { index, param ->
                if (expr.descriptor == param) { // TODO correct comparison of descriptors
                    return@substitute resolvedArguments[index]
                }
            }

            null
        } }

        return ResolvedInvocation(functionDescriptor, resolvedArguments, resolvedInputContract)
    }

    private fun resolveNot(ast: NotNode): ResolvedNot {
        val resolvedChild = resolveExpressionAssertType(ast.child, Type.BooleanType)
        return ResolvedNot(resolvedChild)
    }

    private fun resolveSymbolReference(ast: SymbolReferenceAstNode): ResolvedSymbolReference {
        if (ast.name == "_")
            return ResolvedSymbolReference(ProofReturnValueDescriptor(resolutionContext.currentFunctionNotNull.returnType))

        val descriptor = resolutionContext.findDeclaration(ast.name)
            ?: throw CompilationException("Unresolved symbol reference: ${ast.name}")

        if (descriptor !is TypedDescriptor) {
            throw CompilationException("Expected a reference to an expression, found: ${descriptor.javaClass}")
        }

        return ResolvedSymbolReference(descriptor)
    }

    private fun resolveIntLiteral(ast: IntegerLiteralAstNode): ResolvedIntegerLiteral {
        return ResolvedIntegerLiteral(ast.value, Type.StrictInteger.Int64)
    }
}