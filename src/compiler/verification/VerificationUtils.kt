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
        if (internalCheckByModusPonens(implication, expression, context))
            return true
    }

    return false
}

private fun internalCheckByModusPonens(implication: ResolvedExpression, expression: ResolvedExpression, context: ProvedContext): Boolean {
    if (implication !is ResolvedArrow)
        return false

    if (!implication.right.deepEquals(expression))
        if (!internalCheckByModusPonens(implication.right, expression, context))
            return false

    return implication.left.verify(context)
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
private val add1 = ResolvedComparison("==", ResolvedAddition(x, y), ResolvedAddition(y, x))
private val add2 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedComparison("<", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val add3 = ResolvedArrow(ResolvedComparison(">", x, y), ResolvedComparison(">", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val add4 = ResolvedArrow(ResolvedComparison("<=", x, y), ResolvedComparison("<=", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val add5 = ResolvedArrow(ResolvedComparison(">=", x, y), ResolvedComparison(">=", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val add6 = ResolvedArrow(ResolvedComparison("==", x, y), ResolvedComparison("==", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val add7 = ResolvedArrow(ResolvedComparison("!=", x, y), ResolvedComparison("!=", ResolvedAddition(x, z), ResolvedAddition(y, z)))
private val sub1 = ResolvedArrow(ResolvedComparison("<", x, y), ResolvedComparison("<", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val sub2 = ResolvedArrow(ResolvedComparison(">", x, y), ResolvedComparison(">", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val sub3 = ResolvedArrow(ResolvedComparison("<=", x, y), ResolvedComparison("<=", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val sub4 = ResolvedArrow(ResolvedComparison(">=", x, y), ResolvedComparison(">=", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val sub5 = ResolvedArrow(ResolvedComparison("==", x, y), ResolvedComparison("==", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val sub6 = ResolvedArrow(ResolvedComparison("!=", x, y), ResolvedComparison("!=", ResolvedSubtraction(x, z), ResolvedSubtraction(y, z)))
private val mul1 = ResolvedComparison("==", ResolvedMultiplication(x, y), ResolvedMultiplication(y, x))
private val mul2 = ResolvedComparison("==", ResolvedMultiplication(x, ResolvedAddition(y, z)), ResolvedAddition(ResolvedMultiplication(x, y), ResolvedMultiplication(x, z)))
private val mul3 = ResolvedComparison("==", ResolvedMultiplication(ResolvedAddition(y, z), x), ResolvedAddition(ResolvedMultiplication(y, x), ResolvedMultiplication(z, x)))
private val mul4 = ResolvedComparison("==", ResolvedMultiplication(x, ResolvedSubtraction(y, z)), ResolvedSubtraction(ResolvedMultiplication(x, y), ResolvedMultiplication(x, z)))
private val mul5 = ResolvedComparison("==", ResolvedMultiplication(ResolvedSubtraction(y, z), x), ResolvedSubtraction(ResolvedMultiplication(y, x), ResolvedMultiplication(z, x)))

private val strictNumericAxiomList = listOf(
    eq1, eq2, eq3,
    neq1, neq2,
    less1, less2, less3, less4,
    greater1, greater2, greater3, greater4,
    lg1, lg2, lg3, lg4, lg5, lg6, lg7, lg8,
    add1, add2, add3, add4, add5, add6, add7,
    sub1, sub2, sub3, sub4, sub5, sub6,
    mul1, mul2, mul3, mul4, mul5
)

private val axiomList = logicAxiomList + strictNumericAxiomList