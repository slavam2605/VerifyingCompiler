package compiler.resolved

import compiler.types.Type

sealed class ResolvedDescriptor

sealed class TypedDescriptor : ResolvedDescriptor() {
    abstract val type: Type
}

class TypeDescriptor(val type: Type) : ResolvedDescriptor()

class FunctionDescriptor(
    val name: String,
    val inputContract: List<ResolvedExpression>,
    val outputContract: List<ResolvedExpression>,
    val parameters: List<FunctionParameterDescriptor>,
    val returnType: Type
) : ResolvedDescriptor()

class FunctionParameterDescriptor(val name: String, override val type: Type) : TypedDescriptor()

class LocalVariableDescriptor(val name: String, override val type: Type) : TypedDescriptor()

class ProofReturnValueDescriptor(override val type: Type) : TypedDescriptor()