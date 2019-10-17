package verification.proof

import org.junit.Test
import verification.ast.arrow
import verification.ast.buildAst
import verification.ast.lit

internal class VerificationTest : VerificationTestBase() {
    @Test
    fun `a - a`() {
        val a = "x"
        val aa = buildAst { arrow {
            lit(a)
            lit(a)
        } }

        val proof = mutableListOf(
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
}