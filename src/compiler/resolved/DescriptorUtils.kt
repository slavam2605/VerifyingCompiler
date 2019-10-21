package compiler.resolved

import compiler.types.Type

sealed class ResolvedDescriptor

sealed class TypedDescriptor {
    private var type: Type? = null

    fun getType(): Type {
        return type ?: resolveType().also { type = it }
    }

    protected abstract fun resolveType(): Type
}

class TypeDescriptor(val type: Type) : ResolvedDescriptor()