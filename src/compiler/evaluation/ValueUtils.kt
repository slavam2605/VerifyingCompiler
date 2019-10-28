package compiler.evaluation

import compiler.EvaluationException
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

class StrictInt64Value private constructor(private val value: BigInteger) : Value() {
    constructor(stringValue: String) : this(BigInteger(stringValue))

    override val type: Type
        get() = Type.StrictInteger.Int64

    fun eq(other: StrictInt64Value) = BooleanValue(value == other.value)
    fun neq(other: StrictInt64Value) = BooleanValue(value != other.value)
    fun gt(other: StrictInt64Value) = BooleanValue(value > other.value)
    fun lt(other: StrictInt64Value) = BooleanValue(value < other.value)
    fun geq(other: StrictInt64Value) = BooleanValue(value >= other.value)
    fun leq(other: StrictInt64Value) = BooleanValue(value <= other.value)

    fun add(other: StrictInt64Value) = StrictInt64Value(assertRange(value + other.value))
    fun sub(other: StrictInt64Value) = StrictInt64Value(assertRange(value - other.value))
    fun mul(other: StrictInt64Value) = StrictInt64Value(assertRange(value * other.value))
    fun div(other: StrictInt64Value) = StrictInt64Value(assertRange(value / other.value))

    private fun assertRange(value: BigInteger): BigInteger {
        val minValue = BigInteger.valueOf(Long.MIN_VALUE)
        val maxValue = BigInteger.valueOf(Long.MAX_VALUE)
        if (value < minValue || value > maxValue) {
            throw EvaluationException("Overflow for strict int64 type")
        }
        return value
    }
}