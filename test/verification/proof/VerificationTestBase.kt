package verification.proof

import junit.assertTrue
import verification.ast.AstNode
import verification.ast.arrow
import verification.ast.buildAst
import verification.ast.lit

internal abstract class VerificationTestBase {
    protected fun assertSucceeded(proof: List<ProofElement>) {
        val result = ProofVerifier(listOf(), proof).verify()

        assertTrue("Proof must be verified", result is VerificationResult.Success)
    }

    protected fun assertFailed(proof: List<ProofElement>) {
        val result = ProofVerifier(listOf(), proof).verify()

        assertTrue("Proof must not be verified", result is VerificationResult.Failure)
    }

    protected fun assertSucceededStrong(proof: MutableList<ProofElement>) {
        assertSucceeded(proof)

        for (index in 0 until proof.size - 1) {
            val statement = proof[index]
            proof[index] = buildAst { arrow {
                lit("_")
                arrow {
                    lit("_")
                    lit("_")
                }
            } }

            assertFailed(proof)
            proof[index] = statement
        }
    }
}