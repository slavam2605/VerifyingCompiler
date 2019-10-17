package verification.ast

sealed class MatchErrorDescription {
    class TwoValuesForPattern(
        val patternName: String,
        val firstValue: AstNode,
        val secondValue: AstNode
    ) : MatchErrorDescription() {
        override fun description() =
            "Two different values for a pattern '$patternName': '${firstValue.prettyFormat()}' and '${secondValue.prettyFormat()}'"
    }

    class WrongTopLevelNode(
        val expectedOp: String,
        val actualValue: AstNode
    ) : MatchErrorDescription() {
        override fun description() =
            "Expected '$expectedOp', found: '${actualValue.prettyFormat()}'"
    }

    abstract fun description(): String
    override fun toString() = description()
}