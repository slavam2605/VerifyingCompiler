package compiler

import compiler.resolved.ResolvedExpression
import compiler.resolved.deepEquals
import java.util.NoSuchElementException

class ProvedContext(vararg initialExpressions: ResolvedExpression): Iterable<ResolvedExpression> {
    private var parent: ProvedContext? = null
    private val expressions = initialExpressions.toMutableList()

    fun createNested(vararg newExpressions: ResolvedExpression) = ProvedContext(*newExpressions).also { it.parent = this }

    fun addExpression(expression: ResolvedExpression) {
        expressions.add(expression)
    }

    fun contains(expression: ResolvedExpression): Boolean {
        for (provedExpression in expressions) {
            if (provedExpression.deepEquals(expression)) {
                return true
            }
        }

        return parent?.contains(expression) ?: false
    }

    override fun iterator(): Iterator<ResolvedExpression> = object : Iterator<ResolvedExpression> {
        private val selfIterator = expressions.iterator()
        private val parentIterator = parent?.iterator()

        override fun hasNext(): Boolean {
            return selfIterator.hasNext() || (parentIterator?.hasNext() ?: false)
        }

        override fun next(): ResolvedExpression {
            if (selfIterator.hasNext())
                return selfIterator.next()

            return parentIterator?.next()
                ?: throw NoSuchElementException()
        }
    }
}