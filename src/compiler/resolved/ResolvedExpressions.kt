package compiler.resolved

import compiler.types.Type

sealed class ResolvedExpression {
    abstract val type: Type
}

class ResolvedInvocation(
    val functionDescriptor: FunctionDescriptor,
    val arguments: List<ResolvedExpression>
) : ResolvedExpression() {
    override val type: Type
        get() = functionDescriptor.returnType
}

class ResolvedSymbolReference(val descriptor: TypedDescriptor) : ResolvedExpression() {
    override val type: Type
        get() = descriptor.type
}

class ResolvedIntegerLiteral(val value: String, override val type: Type) : ResolvedExpression()

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
