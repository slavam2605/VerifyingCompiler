package compiler.verification

import compiler.ProvedContext
import compiler.evaluation.BooleanValue
import compiler.evaluation.evaluate
import compiler.resolved.*
import compiler.types.Type

fun ResolvedExpression.verify(context: ProvedContext): Boolean {
    if (checkByEvaluation(this))
        return true

    if (checkByAxiom(this))
        return true

    if (checkByModusPonens(this, context))
        return true

    if (checkByContext(this, context))
        return true

    return false
}

private fun checkByEvaluation(expression: ResolvedExpression): Boolean {
    val value = expression.evaluate()
        ?: return false

    check(value is BooleanValue)
    return value.value
}

private fun checkByContext(expression: ResolvedExpression, context: ProvedContext): Boolean {
    return context.contains(expression)
}

private fun checkByModusPonens(expression: ResolvedExpression, context: ProvedContext): Boolean {
    context.forEach { implication ->
        if (implication !is ResolvedArrow || !implication.right.deepEquals(expression))
            return@forEach

        if (context.contains(implication.left))
            return true
    }

    return false
}

private fun checkByAxiom(expression: ResolvedExpression): Boolean {
    for (axiom in axiomList) {
        if (expression.match(axiom)) {
            return true
        }
    }
    return false
}

private val a = ResolvedSymbolReference(LocalVariableDescriptor("a", Type.BooleanType))
private val b = ResolvedSymbolReference(LocalVariableDescriptor("b", Type.BooleanType))
private val c = ResolvedSymbolReference(LocalVariableDescriptor("c", Type.BooleanType))

private val arrow1 = ResolvedArrow(a, ResolvedArrow(b, a))
private val arrow2 = ResolvedArrow(ResolvedArrow(a, b), ResolvedArrow(ResolvedArrow(a, ResolvedArrow(b, c)), ResolvedArrow(a, c)))
private val and1 = ResolvedArrow(a, ResolvedArrow(b, ResolvedAnd(a, b)))
private val and2 = ResolvedArrow(ResolvedAnd(a, b), a)
private val and3 = ResolvedArrow(ResolvedAnd(a, b), b)
private val or1 = ResolvedArrow(a, ResolvedOr(a, b))
private val or2 = ResolvedArrow(b, ResolvedOr(a, b))
private val or3 = ResolvedArrow(ResolvedArrow(a, c), ResolvedArrow(ResolvedArrow(b, c), ResolvedArrow(ResolvedOr(a, b), c)))
private val not1 = ResolvedArrow(ResolvedArrow(a, b), ResolvedArrow(ResolvedArrow(a, ResolvedNot(b)), ResolvedNot(a)))
private val not2 = ResolvedArrow(ResolvedNot(ResolvedNot(a)), a)

private val logicAxiomList = listOf(arrow1, arrow2, and1, and2, and3, or1, or2, or3, not1, not2)

private val x = ResolvedSymbolReference(LocalVariableDescriptor("x", Type.StrictInteger.Int64))
private val y = ResolvedSymbolReference(LocalVariableDescriptor("y", Type.StrictInteger.Int64))
private val z = ResolvedSymbolReference(LocalVariableDescriptor("z", Type.StrictInteger.Int64))

private val eq1 = ResolvedComparison("==", x, x)
private val eq2 = ResolvedArrow(ResolvedComparison("==", x, y), ResolvedComparison("==", y, x))
private val eq3 = ResolvedArrow(ResolvedComparison("==", x, y), ResolvedArrow(ResolvedComparison("==", y, z), ResolvedComparison("==", x, z)))
private val neq1 = ResolvedArrow(ResolvedComparison("!=", x, y), ResolvedNot(ResolvedComparison("==", x, y)))
private val neq2 = ResolvedArrow(ResolvedNot(ResolvedComparison("==", x, y)), ResolvedComparison("!=", x, y))
private val less1 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedArrow(ResolvedComparison("<", y, z), ResolvedComparison("<", x, z)))
private val less2 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedArrow(ResolvedComparison("<=", y, z), ResolvedComparison("<", x, z)))
private val less3 = ResolvedArrow(ResolvedComparison("<=", x, y), ResolvedArrow(ResolvedComparison("<", y, z), ResolvedComparison("<", x, z)))
private val less4 = ResolvedArrow(ResolvedComparison("<=", x, y), ResolvedArrow(ResolvedComparison("<=", y, z), ResolvedComparison("<=", x, z)))
private val greater1 = ResolvedArrow(ResolvedComparison(">", x, y), ResolvedArrow(ResolvedComparison(">", y, z), ResolvedComparison(">", x, z)))
private val greater2 = ResolvedArrow(ResolvedComparison(">", x, y), ResolvedArrow(ResolvedComparison(">=", y, z), ResolvedComparison(">", x, z)))
private val greater3 = ResolvedArrow(ResolvedComparison(">=", x, y), ResolvedArrow(ResolvedComparison(">", y, z), ResolvedComparison(">", x, z)))
private val greater4 = ResolvedArrow(ResolvedComparison(">=", x, y), ResolvedArrow(ResolvedComparison(">=", y, z), ResolvedComparison(">=", x, z)))
private val lg1 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedComparison(">", y, x))
private val lg2 = ResolvedArrow(ResolvedComparison(">", y, x), ResolvedComparison("<", x, y))
private val lg3 = ResolvedArrow(ResolvedComparison("<=", y, x), ResolvedComparison(">=", x, y))
private val lg4 = ResolvedArrow(ResolvedComparison(">=", y, x), ResolvedComparison("<=", x, y))
private val lg5 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedNot(ResolvedComparison(">=", x, y)))
private val lg6 = ResolvedArrow(ResolvedNot(ResolvedComparison(">=", x, y)), ResolvedComparison("<", x, y))
private val lg7 = ResolvedArrow(ResolvedComparison(">", x, y), ResolvedNot(ResolvedComparison("<=", x, y)))
private val lg8 = ResolvedArrow(ResolvedNot(ResolvedComparison("<=", x, y)), ResolvedComparison(">", x, y))

private val strictNumericAxiomList = listOf(
    eq1, eq2, eq3,
    neq1, neq2,
    less1, less2, less3, less4,
    greater1, greater2, greater3, greater4,
    lg1, lg2, lg3, lg4, lg5, lg6, lg7, lg8
)

private val axiomList = logicAxiomList + strictNumericAxiomList