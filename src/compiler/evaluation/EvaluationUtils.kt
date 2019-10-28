package compiler.evaluation

import compiler.EvaluationException
import compiler.resolved.*

fun ResolvedExpression.evaluate(): Value? = when (this) {
    is ResolvedInvocation -> null
    is ResolvedSymbolReference -> null
    is ResolvedIntegerLiteral -> StrictInt64Value(value)
    is ResolvedBooleanLiteral -> BooleanValue(value)
    is ResolvedComparison -> evaluateComparison(this)
    is ResolvedNot -> evaluateNot(this)
    is ResolvedOr -> evaluateOr(this)
    is ResolvedAnd -> evaluateAnd(this)
    is ResolvedArrow -> evaluateArrow(this)
    is ResolvedMultiplication -> evaluateMultiplication(this)
    is ResolvedDivision -> evaluateDivision(this)
    is ResolvedAddition -> evaluateAddition(this)
    is ResolvedSubtraction -> evaluateSubtraction(this)
}

private fun evaluateMultiplication(expression: ResolvedMultiplication): Value? {
    val left = (expression.left.evaluate() ?: return null) as StrictInt64Value
    val right = (expression.right.evaluate() ?: return null) as StrictInt64Value
    return left.mul(right)
}

private fun evaluateDivision(expression: ResolvedDivision): Value? {
    val left = (expression.left.evaluate() ?: return null) as StrictInt64Value
    val right = (expression.right.evaluate() ?: return null) as StrictInt64Value
    return left.div(right)
}

private fun evaluateAddition(expression: ResolvedAddition): Value? {
    val left = (expression.left.evaluate() ?: return null) as StrictInt64Value
    val right = (expression.right.evaluate() ?: return null) as StrictInt64Value
    return left.add(right)
}

private fun evaluateSubtraction(expression: ResolvedSubtraction): Value? {
    val left = (expression.left.evaluate() ?: return null) as StrictInt64Value
    val right = (expression.right.evaluate() ?: return null) as StrictInt64Value
    return left.sub(right)
}

private fun evaluateNot(expression: ResolvedNot): Value? {
    val child = (expression.child.evaluate() ?: return null) as BooleanValue
    return child.not()
}

private fun evaluateOr(expression: ResolvedOr): Value? {
    val left = (expression.left.evaluate() ?: return null) as BooleanValue
    val right = (expression.right.evaluate() ?: return null) as BooleanValue
    return left.or(right)
}

private fun evaluateAnd(expression: ResolvedAnd): Value? {
    val left = (expression.left.evaluate() ?: return null) as BooleanValue
    val right = (expression.right.evaluate() ?: return null) as BooleanValue
    return left.and(right)
}

private fun evaluateArrow(expression: ResolvedArrow): Value? {
    val left = (expression.left.evaluate() ?: return null) as BooleanValue
    val right = (expression.right.evaluate() ?: return null) as BooleanValue
    return left.arrow(right)
}

private fun evaluateComparison(expression: ResolvedComparison): Value? {
    val left = (expression.left.evaluate() ?: return null) as StrictInt64Value
    val right = (expression.right.evaluate() ?: return null) as StrictInt64Value
    return when (expression.op) {
        "==" -> left.eq(right)
        "!=" -> left.neq(right)
        ">=" -> left.geq(right)
        "<=" -> left.leq(right)
        ">" -> left.gt(right)
        "<" -> left.lt(right)
        else -> throw EvaluationException("Unknown comparison op: ${expression.op}")
    }
}