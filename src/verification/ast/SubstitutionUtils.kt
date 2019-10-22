package verification.ast

import compiler.ast.*

fun ProofAstNode.toPattern(): PatternAst {
    return when (this) {
        is ProofLiteralNode -> LiteralPattern(name)
        is ProofNotNode -> NotPattern(child.toPattern())
        is ProofOrNode -> OrPattern(left.toPattern(), right.toPattern())
        is ProofAndNode -> AndPattern(left.toPattern(), right.toPattern())
        is ProofArrowNode -> ArrowPattern(left.toPattern(), right.toPattern())
    }
}

fun PatternAst.replace(leafName: String, ast: ProofAstNode): PatternAst {
    return when (this) {
        is LiteralPattern -> this
        is LeafPattern -> if (name == leafName) ast.toPattern() else this
        is NotPattern -> NotPattern(child.replace(leafName, ast))
        is OrPattern -> OrPattern(left.replace(leafName, ast), right.replace(leafName, ast))
        is AndPattern -> AndPattern(left.replace(leafName, ast), right.replace(leafName, ast))
        is ArrowPattern -> ArrowPattern(left.replace(leafName, ast), right.replace(leafName, ast))
    }
}