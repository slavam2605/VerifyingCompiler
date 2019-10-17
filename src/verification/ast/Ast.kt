package verification.ast

import verification.proof.ProofElement

sealed class AstNode : ProofElement

class LiteralNode(val name: String) : AstNode()

class NotNode(val child: AstNode) : AstNode()

class OrNode(val left: AstNode, val right: AstNode) : AstNode()

class AndNode(val left: AstNode, val right: AstNode) : AstNode()

class ArrowNode(val left: AstNode, val right: AstNode) : AstNode()

//class CustomFunctionNode(val functionName: String, vararg val children: AstNode) : AstNode()