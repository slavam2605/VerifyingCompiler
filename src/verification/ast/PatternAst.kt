package verification.ast

sealed class PatternAst

class LiteralPattern(val name: String) : PatternAst()

class LeafPattern(val name: String) : PatternAst()

class NotPattern(val child: PatternAst) : PatternAst()

class OrPattern(val left: PatternAst, val right: PatternAst) : PatternAst()

class AndPattern(val left: PatternAst, val right: PatternAst) : PatternAst()

class ArrowPattern(val left: PatternAst, val right: PatternAst) : PatternAst()