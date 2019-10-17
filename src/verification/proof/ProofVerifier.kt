package verification.proof

import verification.ast.*

class ProofVerifier(private val proof: List<AstNode>) {
    private val axioms = basicAxioms
    private val rules = listOf(modusPonens)

    fun verify(): VerificationResult {
        for (index in 0 until proof.size) {
            val statement = proof[index]
            if (checkIsAxiom(statement))
                continue

            if (checkByRule(statement, index))
                continue

            System.err.println("Statement is not verified: '${statement.prettyFormat()}'")
            return VerificationResult.Failure()
        }

        return VerificationResult.Success()
    }

    private fun checkByRule(statement: AstNode, untilIndex: Int): Boolean {
        for (rule in rules) {
            val resultResult = rule.consequence.match(statement) as? MatchResult.Success
                ?: continue

            val resultMap = resultResult.patternMap
            val prepCount = rule.prepositions.size
            val prepositionCandidates = IntArray(prepCount)
            val matchResults = Array(prepCount) { MatchResult.Success(mapOf()) }

            fun findNextCandidate(index: Int): Boolean {
                if (index >= prepCount)
                    return true

                val pattern = run {
                    var pattern = rule.prepositions[index]
                    for (resultIndex in 0 until index) {
                        val result = matchResults[resultIndex]
                        for ((key, value) in result.patternMap) {
                            pattern = pattern.replace(key, value)
                        }
                    }
                    for ((key, value) in resultMap) {
                        pattern = pattern.replace(key, value)
                    }
                    pattern
                }

                for (statementIndex in 0 until untilIndex) {
                    val matchResult = pattern.match(proof[statementIndex]) as? MatchResult.Success
                        ?: continue

                    matchResults[index] = matchResult
                    prepositionCandidates[index] = statementIndex
                    if (findNextCandidate(index + 1))
                        return true
                }

                return false
            }

            if (findNextCandidate(0)) {
                println("Inference rule matched: '${statement.prettyFormat()}' was inferred from:")
                for (candidateIndex in prepositionCandidates) {
                    println("\t${proof[candidateIndex].prettyFormat()}")
                }
                return true
            }
        }

        return false
    }

    private fun checkIsAxiom(statement: AstNode): Boolean {
        for (axiom in axioms) {
            if (axiom.match(statement) is MatchResult.Success) {
                println("Successful match: '${statement.prettyFormat()}' is '${axiom.prettyFormat()}'")
                return true
            }
        }
        return false
    }
}