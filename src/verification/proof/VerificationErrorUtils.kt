package verification.proof

sealed class VerificationResult {
    class Success : VerificationResult()

    class Failure : VerificationResult() {

    }
}