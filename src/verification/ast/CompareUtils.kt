package verification.ast

import verification.ast.MatchErrorDescription.*

fun AstNode.deepEquals(other: AstNode): Boolean {
    if (javaClass != other.javaClass)
        return false

    return when (this) {
        is LiteralNode -> name == (other as LiteralNode).name
        is OrNode -> left.deepEquals((other as OrNode).left) && right.deepEquals(other.right)
        is AndNode -> left.deepEquals((other as AndNode).left) && right.deepEquals(other.right)
        is ArrowNode -> left.deepEquals((other as ArrowNode).left) && right.deepEquals(other.right)
    }
}

fun PatternAst.match(ast: AstNode): MatchResult {
    val patternMap = mutableMapOf<String, AstNode>()
    fun internalMatch(patternAst: PatternAst, ast: AstNode): MatchErrorDescription? {
        return when (patternAst) {
            is LeafPattern -> {
                val leafValue = patternMap[patternAst.name]
                if (leafValue == null) {
                    patternMap[patternAst.name] = ast
                    return null
                }
                if (!leafValue.deepEquals(ast))
                    TwoValuesForPattern(patternAst.name, leafValue, ast)
                else
                    null
            }
            is OrPattern -> {
                if (ast !is OrNode)
                    WrongTopLevelNode("||", ast)
                else
                    internalMatch(patternAst.left, ast.left) ?: internalMatch(patternAst.right, ast.right)
            }
            is AndPattern -> {
                if (ast !is AndNode)
                    WrongTopLevelNode("&&", ast)
                else
                    internalMatch(patternAst.left, ast.left) ?: internalMatch(patternAst.right, ast.right)
            }
            is ArrowPattern -> {
                if (ast !is ArrowNode)
                    WrongTopLevelNode("->", ast)
                else
                    internalMatch(patternAst.left, ast.left) ?: internalMatch(patternAst.right, ast.right)
            }
        }
    }

    internalMatch(this, ast)?.let {
        return MatchResult.Failure(it)
    }
    return MatchResult.Success(patternMap)
}

sealed class MatchResult {
    class Success(val patternMap: Map<String, AstNode>) : MatchResult()
    class Failure(val description: MatchErrorDescription) : MatchResult()
}