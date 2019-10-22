package compiler

import compiler.ast.*
import compiler.resolved.*
import compiler.types.Type
import compiler.types.TypeChecker
import compiler.types.prettyPrint
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

        val resolvedInputContract = mutableListOf<ExpressionDescriptor>()
        val resolvedOutputContract = mutableListOf<ExpressionDescriptor>()
        val functionDescriptor = FunctionDescriptor(ast.name, resolvedInputContract, resolvedOutputContract, ast)
        resolutionContext.addDeclaration(ast.name, functionDescriptor)

        resolutionContext.withFunctionLayer(functionDescriptor) {
            resolvedInputContract.addAll(ast.inputContract?.expressions?.map { resolveExpression(it) } ?: emptyList())
            resolvedOutputContract.addAll(ast.outputContract?.expressions?.map { resolveExpression(it) } ?: emptyList())

            compileBlock(ast.body)
        }
    }

    private fun compileBlock(ast: CodeBlockAstNode) {
        resolutionContext.withLayer {
            builder.withBlock {
                ast.statements.forEach { compileStatement(it) }
            }
        }
    }

    private fun compileStatement(ast: CodeStatementAstNode) {
        when (ast) {
            is VarDeclarationAstNode -> compileVarDeclaration(ast)
        }.exhaustive
    }

    private fun compileVarDeclaration(ast: VarDeclarationAstNode) {
        val varType = ast.type.descriptor.type
        if (ast.initializer == null) {
            builder.appendVarDeclaration(varType.formatVarDeclaration(ast.name))
        } else {
            val expressionDescriptor = resolveExpression(ast.initializer)
            if (!TypeChecker.areEqual(varType, expressionDescriptor.type)) {
                throw CompilationException("Wrong type: expected ${varType.prettyPrint()}, actual: ${expressionDescriptor.type.prettyPrint()}")
            }

            val compiledExpression = compileExpression(expressionDescriptor)
            builder.appendVarDeclaration(
                varType.formatVarDeclaration(ast.name),
                compiledExpression
            )
        }
        resolutionContext.addDeclaration(ast.name, ast.descriptor)
    }

    private fun compileExpression(expression: ExpressionDescriptor): String {
        if (expression is ProofExpressionOnly) {
            throw CompilationException("Proof only expression could not be compiled: $expression")
        }

        return when (expression) {
            is FunctionParameterDescriptor -> expression.name
            is LocalVariableDescriptor -> expression.name
            is IntegerLiteralDescriptor -> expression.value
            is NotDescriptor -> "!(${compileExpression(expression.child)})"
            is OrDescriptor -> "(${compileExpression(expression.left)}) | (${compileExpression(expression.right)})"
            is AndDescriptor -> "(${compileExpression(expression.left)}) & (${compileExpression(expression.right)})"
            is ArrowDescriptor -> "!(${compileExpression(expression.left)}) | (${compileExpression(expression.right)})"

            // proof only expression
            is ProofReturnValueDescriptor -> error("")
        }
    }

    /* ============================== expression resolution ============================== */

    private fun resolveExpression(ast: CodeExpressionAstNode): ExpressionDescriptor {
        return when (ast) {
            is IntegerLiteralAstNode -> resolveIntLiteral(ast)
            is SymbolReferenceAstNode -> resolveSymbolReference(ast)
            is NotNode -> resolveNot(ast)
            is OrNode -> resolveOr(ast)
            is AndNode -> resolveAnd(ast)
            is ArrowNode -> resolveArrow(ast)
        }
    }

    private fun resolveExpressionAssertType(ast: CodeExpressionAstNode, type: Type): ExpressionDescriptor {
        val resolved = resolveExpression(ast)
        if (!TypeChecker.areEqual(resolved.type, type)) {
            throw CompilationException("Expected type ${type.prettyPrint()}, found: ${resolved.type.prettyPrint()}")
        }

        return resolved
    }

    private fun resolveNot(ast: NotNode): NotDescriptor {
        val resolvedChild = resolveExpressionAssertType(ast.child, Type.BooleanType)
        return NotDescriptor(resolvedChild)
    }

    private fun resolveOr(ast: OrNode): OrDescriptor {
        val resolvedLeft = resolveExpressionAssertType(ast.left, Type.BooleanType)
        val resolvedRight = resolveExpressionAssertType(ast.right, Type.BooleanType)
        return OrDescriptor(resolvedLeft, resolvedRight)
    }

    private fun resolveAnd(ast: AndNode): AndDescriptor {
        val resolvedLeft = resolveExpressionAssertType(ast.left, Type.BooleanType)
        val resolvedRight = resolveExpressionAssertType(ast.right, Type.BooleanType)
        return AndDescriptor(resolvedLeft, resolvedRight)
    }

    private fun resolveArrow(ast: ArrowNode): ArrowDescriptor {
        val resolvedLeft = resolveExpressionAssertType(ast.left, Type.BooleanType)
        val resolvedRight = resolveExpressionAssertType(ast.right, Type.BooleanType)
        return ArrowDescriptor(resolvedLeft, resolvedRight)
    }

    private fun resolveSymbolReference(ast: SymbolReferenceAstNode): ExpressionDescriptor {
        if (ast.name == "_")
            return ProofReturnValueDescriptor(resolutionContext.currentFunctionNotNull.ast.returnType.descriptor.type)

        val descriptor = resolutionContext.findDeclaration(ast.name)
            ?: throw CompilationException("Unresolved symbol reference: ${ast.name}")

        if (descriptor !is ExpressionDescriptor) {
            throw CompilationException("Expected a reference to an expression, found: ${descriptor.javaClass}")
        }

        return descriptor
    }

    private fun resolveIntLiteral(ast: IntegerLiteralAstNode): IntegerLiteralDescriptor {
        return IntegerLiteralDescriptor(ast.value, Type.StrictInteger.Int64)
    }
}