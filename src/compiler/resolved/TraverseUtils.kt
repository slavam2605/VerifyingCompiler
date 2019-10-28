package compiler.resolved

fun ResolvedExpression.substitute(transform: (ResolvedExpression) -> ResolvedExpression?): ResolvedExpression = transform(this)
    ?: when (this) {
        is ResolvedInvocation -> ResolvedInvocation(functionDescriptor, arguments.map { it.substitute(transform) }, inputContract.map { it.substitute(transform) })
        is ResolvedSymbolReference -> this
        is ResolvedIntegerLiteral -> this
        is ResolvedBooleanLiteral -> this
        is ResolvedComparison -> ResolvedComparison(op, left.substitute(transform), right.substitute(transform))
        is ResolvedNot -> ResolvedNot(child.substitute(transform))
        is ResolvedOr -> ResolvedOr(left.substitute(transform), right.substitute(transform))
        is ResolvedAnd -> ResolvedAnd(left.substitute(transform), right.substitute(transform))
        is ResolvedArrow -> ResolvedArrow(left.substitute(transform), right.substitute(transform))
    }

fun ResolvedExpression.deepEquals(other: ResolvedExpression): Boolean = when (this) {
    is ResolvedInvocation -> other is ResolvedInvocation && functionDescriptor == other.functionDescriptor // TODO correct comparison of descriptors
    is ResolvedSymbolReference -> other is ResolvedSymbolReference && descriptor == other.descriptor // TODO correct comparison of descriptors
    is ResolvedIntegerLiteral -> other is ResolvedIntegerLiteral && value == other.value
    is ResolvedBooleanLiteral -> other is ResolvedBooleanLiteral && value == other.value
    is ResolvedComparison -> other is ResolvedComparison && op == other.op && left.deepEquals(other.left) && right.deepEquals(other.right)
    is ResolvedNot -> other is ResolvedNot && child.deepEquals(other.child)
    is ResolvedOr -> other is ResolvedOr && left.deepEquals(other.left) && right.deepEquals(other.right)
    is ResolvedAnd -> other is ResolvedAnd && left.deepEquals(other.left) && right.deepEquals(other.right)
    is ResolvedArrow -> other is ResolvedArrow && left.deepEquals(other.left) && right.deepEquals(other.right)
}

fun ResolvedExpression.prettyPrint(level: Int = 0): String = when (this) {
    is ResolvedSymbolReference -> when (descriptor) {
        is FunctionParameterDescriptor -> descriptor.name
        is LocalVariableDescriptor -> descriptor.name
        is ProofReturnValueDescriptor -> "_"
    }
    is ResolvedInvocation -> "${functionDescriptor.name}(${arguments.joinToString { it.prettyPrint(0) }})"
    is ResolvedIntegerLiteral -> value
    is ResolvedBooleanLiteral -> value.toString()
    is ResolvedNot -> "!${child.prettyPrint(4)}"
    is ResolvedComparison -> embrace("${left.prettyPrint(3)} $op ${right.prettyPrint(3)}", level, 3)
    is ResolvedOr -> embrace("${left.prettyPrint(1)} | ${right.prettyPrint(1)}", level, 1)
    is ResolvedAnd -> embrace("${left.prettyPrint(2)} & ${right.prettyPrint(2)}", level, 2)
    is ResolvedArrow -> embrace("${left.prettyPrint(0)} -> ${right.prettyPrint(1)}", level, 0)
}

private fun embrace(string: String, value: Int, threshold: Int): String {
    if (value <= threshold)
        return string
    return "($string)"
}