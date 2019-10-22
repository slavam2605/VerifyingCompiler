package verification.proof

import compiler.ast.ProofAstNode

sealed class VerificationResult {
    class Success(val provedResult: ProofAstNode) : VerificationResult()

    class Failure : VerificationResult()
}