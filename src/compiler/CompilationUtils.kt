package compiler

import compiler.types.Type

class CompilationException(message: String) : Exception(message)

class CompiledExpression(val compiled: String, val type: Type)