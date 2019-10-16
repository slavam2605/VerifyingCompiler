package verification.ast

sealed class PatternAst

class LeafPattern(val name: String) : PatternAst()

class OrPattern(val left: PatternAst, val right: PatternAst) : PatternAst()

class AndPattern(val left: PatternAst, val right: PatternAst) : PatternAst()

class ArrowPattern(val left: PatternAst, val right: PatternAst) : PatternAst()