package verification.ast

private fun embraceFrom(value: String, from: Int, level: Int) =
    if (level >= from) "($value)" else value

fun AstNode.prettyFormat(level: Int = 0): String {
    return when (this) {
        is LiteralNode -> name
        is OrNode -> embraceFrom("${left.prettyFormat(1)} | ${right.prettyFormat(1)}", 2, level)
        is AndNode -> embraceFrom("${left.prettyFormat(2)} & ${right.prettyFormat(2)}", 3, level)
        is ArrowNode -> embraceFrom("${left.prettyFormat(1)} -> ${right.prettyFormat(0)}", 1, level)
    }
}

fun PatternAst.prettyFormat(level: Int = 0): String {
    return when (this) {
        is LeafPattern -> name
        is OrPattern -> embraceFrom("${left.prettyFormat(1)} | ${right.prettyFormat(1)}", 2, level)
        is AndPattern -> embraceFrom("${left.prettyFormat(2)} & ${right.prettyFormat(2)}", 3, level)
        is ArrowPattern -> embraceFrom("${left.prettyFormat(1)} -> ${right.prettyFormat(0)}", 1, level)
    }
}