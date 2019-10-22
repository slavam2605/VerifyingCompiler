package compiler.types

object TypeChecker {
    fun areEqual(left: Type, right: Type): Boolean {
        return when (left) {
            Type.BooleanType -> right == Type.BooleanType
            Type.StrictInteger.Int64 -> right == Type.StrictInteger.Int64
        }
    }
}