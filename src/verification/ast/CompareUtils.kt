package verification.ast

import compiler.ast.*
import verification.ast.MatchErrorDescription.*

fun ProofAstNode.deepEquals(other: ProofAstNode): Boolean {
    if (javaClass != other.javaClass)
        return false

    return when (this) {
        is ProofLiteralNode -> name == (other as ProofLiteralNode).name
        is ProofNotNode -> child.deepEquals((other as ProofNotNode).child)
        is ProofOrNode -> left.deepEquals((other as ProofOrNode).left) && right.deepEquals(other.right)
        is ProofAndNode -> left.deepEquals((other as ProofAndNode).left) && right.deepEquals(other.right)
        is ProofArrowNode -> left.deepEquals((other as ProofArrowNode).left) && right.deepEquals(other.right)
    }
}

fun PatternAst.match(ast: ProofAstNode): MatchResult {
    val patternMap = mutableMapOf<String, ProofAstNode>()
    fun internalMatch(patternAst: PatternAst, ast: ProofAstNode): MatchErrorDescription? {
        return when (patternAst) {
            is LiteralPattern -> {
                if (ast !is ProofLiteralNode || ast.name != patternAst.name)
                    WrongTopLevelNode("LiteralNode[${patternAst.name}]", ast)
                else
                    null
            }
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
            is NotPattern -> {
                if (ast !is ProofNotNode)
                    WrongTopLevelNode("!", ast)
                else
                    internalMatch(patternAst.child, ast.child)
            }
            is OrPattern -> {
                if (ast !is ProofOrNode)
                    WrongTopLevelNode("||", ast)
                else
                    internalMatch(patternAst.left, ast.left) ?: internalMatch(patternAst.right, ast.right)
            }
            is AndPattern -> {
                if (ast !is ProofAndNode)
                    WrongTopLevelNode("&&", ast)
                else
                    internalMatch(patternAst.left, ast.left) ?: internalMatch(patternAst.right, ast.right)
            }
            is ArrowPattern -> {
                if (ast !is ProofArrowNode)
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
    class Success(val patternMap: Map<String, ProofAstNode>) : MatchResult()
    class Failure(val description: MatchErrorDescription) : MatchResult()
}