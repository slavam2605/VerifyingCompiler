package compiler.resolved

import compiler.ast.FunctionDeclarationAstNode
import compiler.types.Type

sealed class ResolvedDescriptor

interface TypedDescriptor {
    val type: Type
}

class TypeDescriptor(val type: Type) : ResolvedDescriptor()

class FunctionDescriptor(
    val name: String,
    val inputContract: List<ExpressionDescriptor>,
    val outputContract: List<ExpressionDescriptor>,
    val ast: FunctionDeclarationAstNode
) : ResolvedDescriptor()

/**
 * Marker interface to mark expression descriptors that can't be used
 * outside of proofs or contracts (e.g. [ProofReturnValueDescriptor])
 */
interface ProofExpressionOnly

sealed class ExpressionDescriptor : ResolvedDescriptor(), TypedDescriptor

class FunctionParameterDescriptor(val name: String, override val type: Type) : ExpressionDescriptor()

class LocalVariableDescriptor(val name: String, override val type: Type) : ExpressionDescriptor()

class IntegerLiteralDescriptor(val value: String, override val type: Type) : ExpressionDescriptor()

class ProofReturnValueDescriptor(override val type: Type) : ExpressionDescriptor(), ProofExpressionOnly

class NotDescriptor(val child: ExpressionDescriptor) : ExpressionDescriptor() {
    override val type: Type
        get() = Type.BooleanType
}

class OrDescriptor(
    val left: ExpressionDescriptor,
    val right: ExpressionDescriptor
) : ExpressionDescriptor() {
    override val type: Type
        get() = Type.BooleanType
}

class AndDescriptor(
    val left: ExpressionDescriptor,
    val right: ExpressionDescriptor
) : ExpressionDescriptor() {
    override val type: Type
        get() = Type.BooleanType
}

class ArrowDescriptor(
    val left: ExpressionDescriptor,
    val right: ExpressionDescriptor
) : ExpressionDescriptor() {
    override val type: Type
        get() = Type.BooleanType
}
