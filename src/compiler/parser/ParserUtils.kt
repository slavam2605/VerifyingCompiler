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
        ctx.name.text,
        ctx.parameters.map { visitFunctionParameter(it) },
        ctx.inputContract?.let { visitFunctionContract(it) },
        visitType(ctx.returnType),
        ctx.outputContract?.let { visitFunctionContract(it) },
        visitCodeBlock(ctx.codeBlock())
    )

    override fun visitCodeBlock(ctx: CodeBlockContext) = CodeBlockAstNode(ctx.statements.map { visitCodeStatement(it) })

    override fun visitVarDeclaration(ctx: VarDeclarationContext) = VarDeclarationAstNode(
        ctx.name.text, visitType(ctx.type()), ctx.codeExpression()?.let { visitCodeExpression(it) }
    )

    override fun visitIntLiteral(ctx: IntLiteralContext) = IntegerLiteralAstNode(ctx.INT().text)

    override fun visitSymbolReference(ctx: SymbolReferenceContext) = SymbolReferenceAstNode(ctx.name.text)

    override fun visitFunctionContract(ctx: FunctionContractContext) = FunctionContractAstNode(
        ctx.expressions.map { visitCodeExpression(it) }
    )

    override fun visitNegate(ctx: NegateContext) = NotNode(
        visitCodeExpression(ctx.codeExpression())
    )

    override fun visitParen(ctx: ParenContext) =
        visitCodeExpression(ctx.codeExpression())

    override fun visitOr(ctx: OrContext) = OrNode(
        visitCodeExpression(ctx.left),
        visitCodeExpression(ctx.right)
    )

    override fun visitAnd(ctx: AndContext) = AndNode(
        visitCodeExpression(ctx.left),
        visitCodeExpression(ctx.right)
    )

    override fun visitArrow(ctx: ArrowContext) = ArrowNode(
        visitCodeExpression(ctx.left),
        visitCodeExpression(ctx.right)
    )

    fun visitCodeStatement(tree: ParseTree) = visit(tree) as CodeStatementAstNode
    fun visitCodeExpression(tree: ParseTree) = visit(tree) as CodeExpressionAstNode
}