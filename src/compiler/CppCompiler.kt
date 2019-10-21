package compiler

import compiler.ast.*
import utils.exhaustive

class CppCompiler {
    private val builder = CppBuilder()

    val result: String
        get() = builder.toString()

    init {
        builder.appendHeader("cstdint")
        builder.appendNewLine()
    }

    fun compileFunction(ast: FunctionDeclarationAstNode) {
        builder.appendFunctionHeader(
            ast.name,
            ast.returnType.resolved.type.formatCppType(),
            ast.parameters.map { it.name to it.type.resolved.type.formatCppType() }
        )
        compileBlock(ast.body)
    }

    private fun compileBlock(ast: CodeBlockAstNode) {
        builder.withBlock {
            ast.statements.forEach { compileStatement(it) }
        }
    }

    private fun compileStatement(ast: CodeStatementAstNode) {
        when (ast) {
            is VarDeclarationAstNode -> compileVarDeclaration(ast)
        }.exhaustive
    }

    private fun compileVarDeclaration(ast: VarDeclarationAstNode) {
        val varType = ast.type.resolved.type
        builder.appendVarDeclaration(
            varType.formatVarDeclaration(ast.name),
            ast.initializer?.let { compileExpression(it) }
        )
    }

    private fun compileExpression(ast: CodeExpressionAstNode): String {
        return when (ast) {
            is IntegerLiteralAstNode -> compileIntLiteral(ast)
        }
    }

    private fun compileIntLiteral(ast: IntegerLiteralAstNode): String {
        return ast.value
    }
}