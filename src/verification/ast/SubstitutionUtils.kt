package verification.ast

fun AstNode.toPattern(): PatternAst {
    return when (this) {
        is LiteralNode -> LiteralPattern(name)
        is NotNode -> NotPattern(child.toPattern())
        is OrNode -> OrPattern(left.toPattern(), right.toPattern())
        is AndNode -> AndPattern(left.toPattern(), right.toPattern())
        is ArrowNode -> ArrowPattern(left.toPattern(), right.toPattern())
    }
}

fun PatternAst.replace(leafName: String, ast: AstNode): PatternAst {
    return when (this) {
        is LiteralPattern -> this
        is LeafPattern -> if (name == leafName) ast.toPattern() else this
        is NotPattern -> NotPattern(child.replace(leafName, ast))
        is OrPattern -> OrPattern(left.replace(leafName, ast), right.replace(leafName, ast))
        is AndPattern -> AndPattern(left.replace(leafName, ast), right.replace(leafName, ast))
        is ArrowPattern -> ArrowPattern(left.replace(leafName, ast), right.replace(leafName, ast))
    }
}