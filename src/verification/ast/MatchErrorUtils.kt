package verification.ast

import compiler.ast.ProofAstNode

sealed class MatchErrorDescription {
    class TwoValuesForPattern(
        val patternName: String,
        val firstValue: ProofAstNode,
        val secondValue: ProofAstNode
    ) : MatchErrorDescription() {
        override fun description() =
            "Two different values for a pattern '$patternName': '${firstValue.prettyFormat()}' and '${secondValue.prettyFormat()}'"
    }

    class WrongTopLevelNode(
        val expectedOp: String,
        val actualValue: ProofAstNode
    ) : MatchErrorDescription() {
        override fun description() =
            "Expected '$expectedOp', found: '${actualValue.prettyFormat()}'"
    }

    abstract fun description(): String
    override fun toString() = description()
}