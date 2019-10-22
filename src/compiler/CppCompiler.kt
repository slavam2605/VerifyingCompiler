package compiler

import compiler.ast.*
import compiler.resolved.FunctionParameterDescriptor
import compiler.resolved.LocalVariableDescriptor
import compiler.resolved.VariableDescriptor
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
        resolutionContext.withLayer {
            ast.parameters.forEach { parameter ->
                resolutionContext.addDeclaration(parameter.name, parameter.descriptor)
            }
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
        val compiledExpression = ast.initializer?.let { compileExpression(it) }?.also {
            if (!TypeChecker.areEqual(varType, it.type)) {
                throw CompilationException("Wrong type: expected ${varType.prettyPrint()}, actual: ${it.type.prettyPrint()}")
            }
        }
        builder.appendVarDeclaration(
            varType.formatVarDeclaration(ast.name),
            compiledExpression?.compiled
        )
        resolutionContext.addDeclaration(ast.name, ast.descriptor)
    }

    private fun compileExpression(ast: CodeExpressionAstNode): CompiledExpression {
        return when (ast) {
            is IntegerLiteralAstNode -> compileIntLiteral(ast)
            is SymbolReferenceAstNode -> compileSymbolReference(ast)
        }
    }

    private fun compileSymbolReference(ast: SymbolReferenceAstNode): CompiledExpression {
        val descriptor = resolutionContext.findDeclaration(ast.name)
            ?: throw CompilationException("Unresolved symbol reference: ${ast.name}")

        if (descriptor !is VariableDescriptor) {
            throw CompilationException("Expected a reference to a variable, found: ${descriptor.javaClass}")
        }

        return when (descriptor) {
            is FunctionParameterDescriptor -> CompiledExpression(descriptor.name, descriptor.type)
            is LocalVariableDescriptor -> CompiledExpression(descriptor.name, descriptor.type)
        }
    }

    private fun compileIntLiteral(ast: IntegerLiteralAstNode): CompiledExpression {
        return CompiledExpression(ast.value, Type.StrictInteger.Int64)
    }
}