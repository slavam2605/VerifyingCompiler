package compiler.resolved

import compiler.types.Type

sealed class ResolvedExpression {
    abstract val type: Type
}

class ResolvedInvocation(
    val functionDescriptor: FunctionDescriptor,
    val arguments: List<ResolvedExpression>,
    val inputContract: List<ResolvedExpression>
) : ResolvedExpression() {
    override val type: Type
        get() = functionDescriptor.returnType
}

class ResolvedSymbolReference(val descriptor: TypedDescriptor) : ResolvedExpression() {
    override val type: Type
        get() = descriptor.type
}

class ResolvedIntegerLiteral(val value: String, override val type: Type) : ResolvedExpression()

class ResolvedBooleanLiteral(val value: Boolean) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}

class ResolvedMultiplication(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.StrictInteger.Int64
}

class ResolvedDivision(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.StrictInteger.Int64
}

class ResolvedAddition(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.StrictInteger.Int64
}

class ResolvedSubtraction(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.StrictInteger.Int64
}

class ResolvedComparison(val op: String, val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}

class ResolvedNot(val child: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}

class ResolvedOr(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}

class ResolvedAnd(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}

class ResolvedArrow(val left: ResolvedExpression, val right: ResolvedExpression) : ResolvedExpression() {
    override val type: Type
        get() = Type.BooleanType
}
