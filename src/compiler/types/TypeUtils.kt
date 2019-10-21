package compiler.types

sealed class Type {
    object BooleanType : Type()
    sealed class StrictInteger : Type() {
        object Int64 : StrictInteger()
    }
}