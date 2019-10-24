package compiler.evaluation

import compiler.types.Type
import java.math.BigInteger

sealed class Value {
    abstract val type: Type
}

class BooleanValue(val value: Boolean) : Value() {
    override val type: Type
        get() = Type.BooleanType

    fun not() = BooleanValue(!value)
    fun and(other: BooleanValue) = BooleanValue(value && other.value)
    fun or(other: BooleanValue) = BooleanValue(value || other.value)
    fun arrow(other: BooleanValue) = BooleanValue(!value || other.value)
}

class StrictInt64Value(stringValue: String) : Value() {
    private val value = BigInteger(stringValue)

    override val type: Type
        get() = Type.StrictInteger.Int64

    fun eq(other: StrictInt64Value) = BooleanValue(value == other.value)
    fun neq(other: StrictInt64Value) = BooleanValue(value != other.value)
    fun gt(other: StrictInt64Value) = BooleanValue(value > other.value)
    fun lt(other: StrictInt64Value) = BooleanValue(value < other.value)
    fun geq(other: StrictInt64Value) = BooleanValue(value >= other.value)
    fun leq(other: StrictInt64Value) = BooleanValue(value <= other.value)
}