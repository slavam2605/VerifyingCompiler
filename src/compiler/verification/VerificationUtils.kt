package compiler.verification

import compiler.ProvedContext
import compiler.resolved.*
import compiler.types.Type

fun ResolvedExpression.verify(context: ProvedContext): Boolean {
    if (checkByAxiom(this))
        return true

    if (checkByModusPonens(this, context))
        return true

    if (checkByContext(this, context))
        return true

    return false
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

private val axiomList = listOf(arrow1, arrow2, and1, and2, and3, or1, or2, or3, not1, not2)