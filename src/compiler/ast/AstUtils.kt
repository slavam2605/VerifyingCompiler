package compiler.ast

import compiler.resolved.FunctionParameterDescriptor
import compiler.resolved.LocalVariableDescriptor
import compiler.resolved.ResolvedDescriptor
import compiler.resolved.TypeDescriptor
import compiler.types.Type
import verification.proof.ProofElement
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

sealed class CodeExpressionAstNode() : AstNode()

class IntegerLiteralAstNode(val value: String) : CodeExpressionAstNode()

class SymbolReferenceAstNode(val name: String) : CodeExpressionAstNode()

class FunctionDeclarationAstNode(
    val name: String,
    val parameters: List<FunctionParameterAstNode>,
    val returnType: TypeAstNode,
    val body: CodeBlockAstNode
) : AstNode()

/* ======================================== proof ast ======================================== */

sealed class ProofAstNode : AstNode(), ProofElement

class ProofLiteralNode(val name: String) : ProofAstNode()

class ProofNotNode(val child: ProofAstNode) : ProofAstNode()

class ProofOrNode(val left: ProofAstNode, val right: ProofAstNode) : ProofAstNode()

class ProofAndNode(val left: ProofAstNode, val right: ProofAstNode) : ProofAstNode()

class ProofArrowNode(val left: ProofAstNode, val right: ProofAstNode) : ProofAstNode()