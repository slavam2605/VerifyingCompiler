package verification.proof

import compiler.ast.ProofLiteralNode
import compiler.ast.ProofAstNode
import verification.ast.*
import kotlin.test.fail

interface ProofElement

class ProofBlock(val preposition: ProofAstNode, val subProof: List<ProofElement>) : ProofElement

class ProofVerifier(private val context: List<ProofAstNode>, private val proof: List<ProofElement>) {
    private val axioms = basicAxioms
    private val rules = listOf(modusPonens)
    private val verifiedProof = mutableListOf<ProofAstNode>()

    fun verify(): VerificationResult {
        loop@ for (index in 0 until proof.size) {
            when (val statement = proof[index]) {
                is ProofAstNode -> {
                    if (checkInContext(statement) || checkIsAxiom(statement) || checkByRule(statement)) {
                        verifiedProof.add(statement)
                        continue@loop
                    }

                    System.err.println("Statement is not verified: '${statement.prettyFormat()}'")
                }
                is ProofBlock -> {
                    val subResult = ProofVerifier(context + verifiedProof + listOf(statement.preposition), statement.subProof).verify()
                    if (subResult is VerificationResult.Success) {
                        verifiedProof.add(buildAst { arrow {
                            +statement.preposition
                            +subResult.provedResult
                        } })
                        continue@loop
                    }
                }
                else -> fail()
            }
            return VerificationResult.Failure()
        }

        return VerificationResult.Success(verifiedProof.last())
    }

    private fun checkByRule(statement: ProofAstNode): Boolean {
        for (rule in rules) {
            val resultResult = rule.consequence.match(statement) as? MatchResult.Success
                ?: continue

            val resultMap = resultResult.patternMap
            val prepCount = rule.prepositions.size
            val prepositionCandidates = Array<ProofAstNode>(prepCount) { ProofLiteralNode("!failure!") }
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

                for (verifiedStatement in context.asSequence() + verifiedProof.asSequence()) {
                    val matchResult = pattern.match(verifiedStatement) as? MatchResult.Success
                        ?: continue

                    matchResults[index] = matchResult
                    prepositionCandidates[index] = verifiedStatement
                    if (findNextCandidate(index + 1))
                        return true
                }

                return false
            }

            if (findNextCandidate(0)) {
                println("Inference rule matched: '${statement.prettyFormat()}' was inferred from:")
                for (candidate in prepositionCandidates) {
                    println("\t${candidate.prettyFormat()}")
                }
                return true
            }
        }

        return false
    }

    private fun checkIsAxiom(statement: ProofAstNode): Boolean {
        for (axiom in axioms) {
            if (axiom.match(statement) is MatchResult.Success) {
                println("Successful match: '${statement.prettyFormat()}' is '${axiom.prettyFormat()}'")
                return true
            }
        }
        return false
    }

    private fun checkInContext(statement: ProofAstNode): Boolean {
        for (element in context) {
            if (element.deepEquals(statement)) {
                println("Successfully found in context: '${statement.prettyFormat()}'")
                return true
            }
        }
        return false
    }
}