package verification.ast

import compiler.ast.*

private fun embraceFrom(value: String, from: Int, level: Int) =
    if (level >= from) "($value)" else value

fun ProofAstNode.prettyFormat(level: Int = 0): String {
    return when (this) {
        is ProofLiteralNode -> name
        is ProofNotNode -> "!${child.prettyFormat(3)}"
        is ProofOrNode -> embraceFrom("${left.prettyFormat(1)} | ${right.prettyFormat(1)}", 2, level)
        is ProofAndNode -> embraceFrom("${left.prettyFormat(2)} & ${right.prettyFormat(2)}", 3, level)
        is ProofArrowNode -> embraceFrom("${left.prettyFormat(1)} -> ${right.prettyFormat(0)}", 1, level)
    }
}

fun PatternAst.prettyFormat(level: Int = 0): String {
    return when (this) {
        is LiteralPattern -> "<$name>"
        is LeafPattern -> name
        is NotPattern -> "!${child.prettyFormat(3)}"
        is OrPattern -> embraceFrom("${left.prettyFormat(1)} | ${right.prettyFormat(1)}", 2, level)
        is AndPattern -> embraceFrom("${left.prettyFormat(2)} & ${right.prettyFormat(2)}", 3, level)
        is ArrowPattern -> embraceFrom("${left.prettyFormat(1)} -> ${right.prettyFormat(0)}", 1, level)
    }
}