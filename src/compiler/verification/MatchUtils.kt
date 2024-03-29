package compiler.verification

import compiler.resolved.*

fun ResolvedExpression.match(pattern: ResolvedExpression): Boolean {
    val matchMap = mutableMapOf<String, ResolvedExpression>()
    fun internalMatch(expression: ResolvedExpression, pattern: ResolvedExpression): Boolean {
        return when (pattern) {
            is ResolvedSymbolReference -> {
                check(pattern.descriptor is LocalVariableDescriptor)
                val name = pattern.descriptor.name
                val match = matchMap[name]
                if (match != null) {
                    match.deepEquals(expression)
                } else {
                    matchMap[name] = expression
                    true
                }
            }
            is ResolvedInvocation -> expression is ResolvedInvocation
                    && expression.functionDescriptor == pattern.functionDescriptor // TODO correct comparison of descriptors
                    && expression.arguments.zip(pattern.arguments).all { internalMatch(it.first, it.second) }
            is ResolvedIntegerLiteral -> expression is ResolvedIntegerLiteral && expression.value == pattern.value
            is ResolvedBooleanLiteral -> expression is ResolvedBooleanLiteral && expression.value == pattern.value
            is ResolvedComparison -> expression is ResolvedComparison && expression.op == pattern.op && internalMatch(expression.left, pattern.left) && internalMatch(expression.right, pattern.right)
            is ResolvedNot -> expression is ResolvedNot && internalMatch(expression.child, pattern.child)
            is ResolvedOr -> expression is ResolvedOr && internalMatch(expression.left, pattern.left) && internalMatch(expression.right, pattern.right)
            is ResolvedAnd -> expression is ResolvedAnd && internalMatch(expression.left, pattern.left) && internalMatch(expression.right, pattern.right)
            is ResolvedArrow -> expression is ResolvedArrow && internalMatch(expression.left, pattern.left) && internalMatch(expression.right, pattern.right)
            is ResolvedMultiplication -> expression is ResolvedMultiplication && internalMatch(expression.left, pattern.left) && internalMatch(expression.left, expression.right)
            is ResolvedDivision -> expression is ResolvedDivision && internalMatch(expression.left, pattern.left) && internalMatch(expression.left, expression.right)
            is ResolvedAddition -> expression is ResolvedAddition && internalMatch(expression.left, pattern.left) && internalMatch(expression.left, expression.right)
            is ResolvedSubtraction -> expression is ResolvedSubtraction && internalMatch(expression.left, pattern.left) && internalMatch(expression.left, expression.right)
        }
    }

    return internalMatch(this, pattern)
}