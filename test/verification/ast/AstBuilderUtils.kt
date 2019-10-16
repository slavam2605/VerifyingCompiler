package verification.ast

sealed class AstBuilder {
    abstract fun append(node: AstNode)
    abstract fun build(): AstNode

    class RootAstBuilder : AstBuilder() {
        var child: AstNode? = null

        override fun append(node: AstNode) {
            when (child) {
                null -> child = node
                else -> throw IllegalStateException("RootAstBuilder can accept only 1 child")
            }
        }

        override fun build() = child ?: throw IllegalStateException("No child was provided for ${this::class.java}")
    }

    abstract class BinaryAstBuilder : AstBuilder() {
        var left: AstNode? = null
        var right: AstNode? = null

        override fun append(node: AstNode) {
            when {
                left == null -> left = node
                right == null -> right = node
                else -> throw IllegalStateException("BinaryAstBuilder can accept only 2 children")
            }
        }

        fun checkFull(): Pair<AstNode, AstNode> {
            check(left != null) { "No child was provided for ${this::class.java}" }
            check(right != null) { "Only one child was provided for ${this::class.java}" }
            return left!! to right!!
        }
    }

    class OrBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> OrNode(left, right) }
    }

    class AndBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> AndNode(left, right) }
    }

    class ArrowBuilder : BinaryAstBuilder() {
        override fun build() = checkFull().let { (left, right) -> ArrowNode(left, right) }
    }
}

fun AstBuilder.lit(name: String) {
    append(LiteralNode(name))
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

fun buildAst(block: AstBuilder.() -> Unit): AstNode {
    val builder = AstBuilder.RootAstBuilder()
    builder.block()
    return builder.build()
}