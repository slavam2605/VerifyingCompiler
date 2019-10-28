package compiler.ast

import compiler.resolved.FunctionParameterDescriptor
import compiler.resolved.LocalVariableDescriptor
import compiler.resolved.ResolvedDescriptor
import compiler.resolved.TypeDescriptor
import compiler.types.Type
import kotlin.test.fail

sealed class AstNode()

interface DescriptorProvider<T: ResolvedDescriptor> {
    val descriptor: T
}

class FunctionParameterAstNode(val name: String, val type: TypeAstNode) : AstNode(), DescriptorProvider<FunctionParameterDescriptor> {
    override val descriptor by lazy {
        FunctionParameterDescriptor(name, type.descriptor.type)
    }
}

class TypeAstNode(val typeName: String) : AstNode(), DescriptorProvider<TypeDescriptor> {
    override val descriptor by lazy {
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
) : CodeStatementAstNode(), DescriptorProvider<LocalVariableDescriptor> {
    override val descriptor by lazy {
        LocalVariableDescriptor(name, type.descriptor.type)
    }
}

class ProofElementAstNode(val expression: CodeExpressionAstNode) : CodeStatementAstNode()

sealed class CodeExpressionAstNode : AstNode()

class IntegerLiteralAstNode(val value: String) : CodeExpressionAstNode()

class BooleanLiteralAstNode(val value: Boolean) : CodeExpressionAstNode()

class InvocationAstNode(val name: String, val arguments: List<CodeExpressionAstNode>) : CodeExpressionAstNode()

class SymbolReferenceAstNode(val name: String) : CodeExpressionAstNode()

class MultiplicationAstNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class DivisionAstNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class AdditionAstNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class SubtractionAstNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class ComparisonNode(val op: String, val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class NotNode(val child: CodeExpressionAstNode) : CodeExpressionAstNode()

class OrNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class AndNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class ArrowNode(val left: CodeExpressionAstNode, val right: CodeExpressionAstNode) : CodeExpressionAstNode()

class FunctionDeclarationAstNode(
    val name: String,
    val parameters: List<FunctionParameterAstNode>,
    val inputContract: FunctionContractAstNode?,
    val returnType: TypeAstNode,
    val outputContract: FunctionContractAstNode?,
    val body: CodeBlockAstNode
) : AstNode()

class FunctionContractAstNode(
    val expressions: List<CodeExpressionAstNode>
) : AstNode()