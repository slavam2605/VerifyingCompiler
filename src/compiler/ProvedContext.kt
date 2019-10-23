package compiler

import compiler.resolved.ResolvedExpression
import compiler.resolved.deepEquals

class ProvedContext(vararg initialExpressions: ResolvedExpression) {
    private var parent: ProvedContext? = null
    private val expressions = initialExpressions.toMutableList()

    fun createNested(vararg newExpressions: ResolvedExpression) = ProvedContext(*newExpressions).also { it.parent = this }

    fun contains(expression: ResolvedExpression): Boolean {
        for (provedExpression in expressions) {
            if (provedExpression.deepEquals(expression)) {
                return true
            }
        }

        return parent?.contains(expression) ?: false
    }
}