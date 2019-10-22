package compiler.parser

import compiler.ast.*
import compiler.parser.MainParser.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.tree.ParseTree

fun parseString(text: String): FunctionDeclarationAstNode {
    val input = CharStreams.fromString(text)
    val lexer = MainLexer(input)
    val tokens = CommonTokenStream(lexer)
    val parser = MainParser(tokens)
    return AstBuildingVisitor().visitFunction(parser.function())
}

private class AstBuildingVisitor : MainParserBaseVisitor<AstNode>() {
    override fun visitType(ctx: TypeContext) = TypeAstNode(ctx.name.text)

    override fun visitFunctionParameter(ctx: FunctionParameterContext) = FunctionParameterAstNode(
        ctx.name.text, visitType(ctx.type())
    )

    override fun visitFunction(ctx: FunctionContext) = FunctionDeclarationAstNode(
        ctx.name.text, ctx.parameters.map { visitFunctionParameter(it) }, visitType(ctx.returnType), visitCodeBlock(ctx.codeBlock())
    )

    override fun visitCodeBlock(ctx: CodeBlockContext) = CodeBlockAstNode(ctx.statements.map { visitCodeStatement(it) })

    override fun visitVarDeclaration(ctx: VarDeclarationContext) = VarDeclarationAstNode(
        ctx.name.text, visitType(ctx.type()), ctx.codeExpression()?.let { visitCodeExpression(it) }
    )

    override fun visitIntLiteral(ctx: IntLiteralContext) = IntegerLiteralAstNode(ctx.INT().text)

    override fun visitSymbolReference(ctx: SymbolReferenceContext) = SymbolReferenceAstNode(ctx.name.text)

    fun visitCodeStatement(tree: ParseTree) = visit(tree) as CodeStatementAstNode
    fun visitCodeExpression(tree: ParseTree) = visit(tree) as CodeExpressionAstNode
}