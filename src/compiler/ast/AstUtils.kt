package compiler.ast

import compiler.resolved.ResolvedDescriptor
import compiler.resolved.TypeDescriptor
import compiler.types.Type
import kotlin.test.fail

sealed class AstNode()

interface ResolvableAstNode<T: ResolvedDescriptor> {
    val resolved: T
}

class ParameterAstNode(val name: String, val type: TypeAstNode) : AstNode()

class TypeAstNode(val typeName: String) : AstNode(), ResolvableAstNode<TypeDescriptor> {
    override val resolved by lazy {
        TypeDescriptor(
            when (typeName) {
                "bool" -> Type.BooleanType
                "int64" -> Type.StrictInteger.Int64
                else -> fail("Unresolved type: $typeName")
            }
        )
    }
}

class CodeBlockAstNode(val statements: List<CodeStatementAstNode>) : AstNode()

sealed class CodeStatementAstNode() : AstNode()

class VarDeclarationAstNode(
    val name: String,
    val type: TypeAstNode,
    val initializer: CodeExpressionAstNode?
) : CodeStatementAstNode()

sealed class CodeExpressionAstNode() : AstNode()

class IntegerLiteralAstNode(val value: String) : CodeExpressionAstNode()

class FunctionDeclarationAstNode(
    val name: String,
    val parameters: List<ParameterAstNode>,
    val returnType: TypeAstNode,
    val body: CodeBlockAstNode
) : AstNode()