package verification.ast

sealed class AstNode

class LiteralNode(val name: String) : AstNode()

class OrNode(val left: AstNode, val right: AstNode) : AstNode()

class AndNode(val left: AstNode, val right: AstNode) : AstNode()

class ArrowNode(val left: AstNode, val right: AstNode) : AstNode()

//class CustomFunctionNode(val functionName: String, vararg val children: AstNode) : AstNode()