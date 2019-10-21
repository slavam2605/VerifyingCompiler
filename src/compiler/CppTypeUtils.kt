package compiler

import compiler.types.Type

fun Type.formatCppType(): String {
    return when (this) {
        Type.BooleanType -> "bool"
        Type.StrictInteger.Int64 -> "int64_t"
    }
}

fun Type.formatVarDeclaration(varName: String): String {
    return "${formatCppType()} $varName"
}