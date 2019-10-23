package compiler.resolved

fun ResolvedExpression.substitute(transform: (ResolvedExpression) -> ResolvedExpression?): ResolvedExpression = transform(this)
    ?: when (this) {
        is ResolvedInvocation -> ResolvedInvocation(functionDescriptor, arguments.map { it.substitute(transform) })
        is ResolvedSymbolReference -> this
        is ResolvedIntegerLiteral -> this
        is ResolvedNot -> ResolvedNot(child.substitute(transform))
        is ResolvedOr -> ResolvedOr(left.substitute(transform), right.substitute(transform))
        is ResolvedAnd -> ResolvedAnd(left.substitute(transform), right.substitute(transform))
        is ResolvedArrow -> ResolvedArrow(left.substitute(transform), right.substitute(transform))
    }

fun ResolvedExpression.deepEquals(other: ResolvedExpression): Boolean = when (this) {
    is ResolvedInvocation -> other is ResolvedInvocation && functionDescriptor == other.functionDescriptor // TODO correct comparison of descriptors
    is ResolvedSymbolReference -> other is ResolvedSymbolReference && descriptor == other.descriptor // TODO correct comparison of descriptors
    is ResolvedIntegerLiteral -> other is ResolvedIntegerLiteral && value == other.value
    is ResolvedNot -> other is ResolvedNot && child.deepEquals(other.child)
    is ResolvedOr -> other is ResolvedOr && left.deepEquals(other.left) && right.deepEquals(other.right)
    is ResolvedAnd -> other is ResolvedAnd && left.deepEquals(other.left) && right.deepEquals(other.right)
    is ResolvedArrow -> other is ResolvedArrow && left.deepEquals(other.left) && right.deepEquals(other.right)
}