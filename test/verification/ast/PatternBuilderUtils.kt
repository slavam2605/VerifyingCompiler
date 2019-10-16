package verification.ast

sealed class PatternBuilder {
    abstract fun append(node: PatternAst)
    abstract fun build(): PatternAst

    class RootPatternBuilder : PatternBuilder() {
        var child: PatternAst? = null

        override fun append(node: PatternAst) {
            when (child) {
                null -> child = node
                else -> throw IllegalStateException("RootAstBuilder can accept only 1 child")
            }
        }

        override fun build() = child ?: throw IllegalStateException("No child was provided for ${this::class.java}")
    }

    abstract class BinaryPatternBuilder : PatternBuilder() {
        var left: PatternAst? = null
        var right: PatternAst? = null

        override fun append(node: PatternAst) {
            when {
                left == null -> left = node
                right == null -> right = node
                else -> throw IllegalStateException("BinaryAstBuilder can accept only 2 children")
            }
        }

        fun checkFull(): Pair<PatternAst, PatternAst> {
            check(left != null) { "No child was provided for ${this::class.java}" }
            check(right != null) { "Only one child was provided for ${this::class.java}" }
            return left!! to right!!
        }
    }

    class OrBuilder : BinaryPatternBuilder() {
        override fun build() = checkFull().let { (left, right) -> OrPattern(left, right) }
    }

    class AndBuilder : BinaryPatternBuilder() {
        override fun build() = checkFull().let { (left, right) -> AndPattern(left, right) }
    }

    class ArrowBuilder : BinaryPatternBuilder() {
        override fun build() = checkFull().let { (left, right) -> ArrowPattern(left, right) }
    }
}

fun PatternBuilder.leaf(name: String) {
    append(LeafPattern(name))
}

fun PatternBuilder.or(block: PatternBuilder.OrBuilder.() -> Unit) {
    val builder = PatternBuilder.OrBuilder()
    builder.block()
    append(builder.build())
}

fun PatternBuilder.and(block: PatternBuilder.AndBuilder.() -> Unit) {
    val builder = PatternBuilder.AndBuilder()
    builder.block()
    append(builder.build())
}

fun PatternBuilder.arrow(block: PatternBuilder.ArrowBuilder.() -> Unit) {
    val builder = PatternBuilder.ArrowBuilder()
    builder.block()
    append(builder.build())
}

fun buildPattern(block: PatternBuilder.() -> Unit): PatternAst {
    val builder = PatternBuilder.RootPatternBuilder()
    builder.block()
    return builder.build()
}