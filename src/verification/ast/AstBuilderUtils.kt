package verification.ast

import compiler.ast.*

sealed class AstBuilder {
    abstract fun append(node: ProofAstNode)
    abstract fun build(): ProofAstNode

    abstract class UnaryAstBuilder : AstBuilder() {
        var child: ProofAstNode? = null

        override fun append(node: ProofAstNode) {
            when (child) {
                null -> child = node
                else -> throw IllegalStateException("UnaryAstBuilder can accept only 1 child")
            }
        }

        fun checkFull(): ProofAstNode {
            check(child != null) { "No child was provided for ${this::class.java}" }
            return child!!
        }
    }

    abstract class BinaryAstBuilder : AstBuilder() {
        var left: ProofAstNode? = null
        var right: ProofAstNode? = null

        override fun append(node: ProofAstNode) {
            when {
                left == null -> left = node
                right == null -> right = node
                else -> throw IllegalStateException("BinaryAstBuilder can accept only 2 children")
            }
        }

        fun checkFull(): Pair<ProofAstNode, ProofAstNode> {
            check(left != null) { "No child was provided for ${this::class.java}" }
            check(right != null) { "Only one child was provided for ${this::class.java}" }
            return left!! to right!!
        }
    }

    class RootAstBuilder : UnaryAstBuilder() {
        override fun build() = checkFull()
    }

    class NotBuilder : UnaryAstBuilder() {
        override fun build() = ProofNotNode(checkFull())
    }

    class OrBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> ProofOrNode(left, right) }
    }

    class AndBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> ProofAndNode(left, right) }
    }

    class ArrowBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> ProofArrowNode(left, right) }
    }

    operator fun ProofAstNode.unaryPlus() {
        append(this)
    }
}

fun AstBuilder.lit(name: String) {
    append(ProofLiteralNode(name))
}

fun AstBuilder.not(block: AstBuilder.NotBuilder.() -> Unit) {
    val builder = AstBuilder.NotBuilder()
    builder.block()
    append(builder.build())
}

fun AstBuilder.or(block: AstBuilder.OrBuilder.() -> Unit) {
    val builder = AstBuilder.OrBuilder()
    builder.block()
    append(builder.build())
}

fun AstBuilder.and(block: AstBuilder.AndBuilder.() -> Unit) {
    val builder = AstBuilder.AndBuilder()
    builder.block()
    append(builder.build())
}

fun AstBuilder.arrow(block: AstBuilder.ArrowBuilder.() -> Unit) {
    val builder = AstBuilder.ArrowBuilder()
    builder.block()
    append(builder.build())
}

fun buildAst(block: AstBuilder.() -> Unit): ProofAstNode {
    val builder = AstBuilder.RootAstBuilder()
    builder.block()
    return builder.build()
}