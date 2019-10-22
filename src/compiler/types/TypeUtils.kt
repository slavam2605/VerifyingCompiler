package compiler.types

sealed class Type {
    object BooleanType : Type()
    sealed class StrictInteger : Type() {
        object Int64 : StrictInteger()
    }
}

fun Type.prettyPrint(): String {
    return when (this) {
        Type.BooleanType -> "bool"
        Type.StrictInteger.Int64 -> "int64"
    }
}