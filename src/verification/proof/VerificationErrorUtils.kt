package verification.proof

import verification.ast.AstNode

sealed class VerificationResult {
    class Success(val provedResult: AstNode) : VerificationResult()

    class Failure : VerificationResult()
}