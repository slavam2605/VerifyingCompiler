package compiler.resolved

import compiler.types.Type

sealed class ResolvedDescriptor

interface TypedDescriptor {
    val type: Type
}

class TypeDescriptor(val type: Type) : ResolvedDescriptor()

sealed class VariableDescriptor : ResolvedDescriptor(), TypedDescriptor

class FunctionParameterDescriptor(val name: String, override val type: Type) : VariableDescriptor()

class LocalVariableDescriptor(val name: String, override val type: Type) : VariableDescriptor()