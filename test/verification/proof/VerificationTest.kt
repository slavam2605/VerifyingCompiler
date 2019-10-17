package verification.proof

import org.junit.Test
import verification.ast.arrow
import verification.ast.buildAst
import verification.ast.lit
import verification.ast.not

internal class VerificationTest : VerificationTestBase() {
    @Test
    fun `a - a`() {
        val a = "x"
        val aa = buildAst { arrow {
            lit(a)
            lit(a)
        } }

        val proof = mutableListOf<ProofElement>(
            buildAst { arrow {
                lit(a)
                +aa
            } },
            buildAst { arrow {
                lit(a)
                arrow {
                    +aa
                    lit(a)
                }
            } },
            buildAst { arrow {
                arrow {
                    lit(a)
                    +aa
                }
                arrow {
                    arrow {
                        lit(a)
                        arrow {
                            +aa
                            lit(a)
                        }
                    }
                    +aa
                }
            } },
            buildAst { arrow {
                arrow {
                    lit(a)
                    arrow {
                        +aa
                        lit(a)
                    }
                }
                +aa
            } },
            aa
        )

        assertSucceededStrong(proof)
    }

    @Test
    fun `(a - b) - (!b - !a)`() {
        val a = "x"
        val b = "y"

        val proof = mutableListOf(
            buildAst { arrow {
                arrow {
                    lit(a)
                    lit(b)
                }
                arrow {
                    arrow {
                        lit(a)
                        not { lit(b) }
                    }
                    not { lit(a) }
                }
            } },
            ProofBlock(buildAst { arrow {
                lit(a)
                lit(b)
            } }, listOf(
                buildAst { arrow {
                    arrow {
                        lit(a)
                        not { lit(b) }
                    }
                    not { lit(a) }
                } },
                buildAst { arrow {
                    not { lit(b) }
                    arrow {
                        lit(a)
                        not { lit(b) }
                    }
                } },
                ProofBlock(buildAst { not { lit(b) } }, listOf(
                    buildAst { arrow {
                        lit(a)
                        not { lit(b) }
                    } },
                    buildAst { not { lit(a) } }
                ))
            ))
        )

        assertSucceededStrong(proof)
    }
}