package verification.ast

import org.junit.Test
import verification.ast.MatchErrorDescription.*

internal class PatternMatchTest : PatternMatchTestBase() {
    @Test
    fun simpleTest1() {
        val ast1 = buildAst { lit("a") }
        val ast2 = buildAst { or {
            lit("a")
            lit("b")
        } }
        val ast3 = buildAst { and {
            lit("a")
            lit("b")
        } }
        val ast4 = buildAst { arrow {
            lit("a")
            lit("b")
        } }
        val pattern1 = buildPattern { leaf("#1") }
        val pattern2 = buildPattern { or {
            leaf("#1")
            leaf("#2")
        } }
        val pattern3 = buildPattern { and {
            leaf("#1")
            leaf("#2")
        } }
        val pattern4 = buildPattern { arrow {
            leaf("#1")
            leaf("#2")
        } }

        assertSucceeded(ast1, pattern1)
        assertSucceeded(ast2, pattern1)
        assertSucceeded(ast3, pattern1)
        assertSucceeded(ast4, pattern1)

        assertFailed<WrongTopLevelNode>(ast1, pattern2) { expectedOp == "||" }
        assertSucceeded(ast2, pattern2)
        assertFailed<WrongTopLevelNode>(ast3, pattern2) { expectedOp == "||" }
        assertFailed<WrongTopLevelNode>(ast4, pattern2) { expectedOp == "||" }

        assertFailed<WrongTopLevelNode>(ast1, pattern3) { expectedOp == "&&" }
        assertFailed<WrongTopLevelNode>(ast2, pattern3) { expectedOp == "&&" }
        assertSucceeded(ast3, pattern3)
        assertFailed<WrongTopLevelNode>(ast4, pattern3) { expectedOp == "&&" }

        assertFailed<WrongTopLevelNode>(ast1, pattern4) { expectedOp == "->" }
        assertFailed<WrongTopLevelNode>(ast2, pattern4) { expectedOp == "->" }
        assertFailed<WrongTopLevelNode>(ast3, pattern4) { expectedOp == "->" }
        assertSucceeded(ast4, pattern4)
    }

    @Test
    fun simpleTest2() {
        val ast1 = buildAst { or {
            lit("a")
            lit("b")
        } }
        val ast2 = buildAst { or {
            lit("a")
            lit("a")
        } }
        val pattern = buildPattern { or {
            leaf("#1")
            leaf("#1")
        } }

        assertFailed<TwoValuesForPattern>(ast1, pattern)
        assertSucceeded(ast2, pattern) { expectValue("#1", buildAst { lit("a") }) }
    }

    @Test
    fun testMappings1() {
        val a = randomName()
        val b = randomName()
        val c = randomName()
        val p1 = randomName()
        val p2 = randomName()
        val p3 = randomName()
        val p4 = randomName()

        val ast1 = buildAst { arrow {
            or {
                lit(a)
                lit(b)
            }
            and {
                lit(a)
                lit(c)
            }
        } }
        val ast2 = buildAst { arrow {
            or {
                lit(a)
                lit(a)
            }
            and {
                lit(a)
                lit(a)
            }
        } }
        val ast3 = buildAst { arrow {
            or {
                lit(c)
                lit(a)
            }
            and {
                lit(b)
                lit(a)
            }
        } }

        val pattern1 = buildPattern { arrow {
            or {
                leaf(p1)
                leaf(p2)
            }
            and {
                leaf(p3)
                leaf(p4)
            }
        } }
        val pattern2 = buildPattern { arrow {
            or {
                leaf(p1)
                leaf(p2)
            }
            and {
                leaf(p1)
                leaf(p3)
            }
        } }
        val pattern3 = buildPattern { arrow {
            or {
                leaf(p2)
                leaf(p2)
            }
            and {
                leaf(p2)
                leaf(p2)
            }
        } }
        val pattern4 = buildPattern { arrow {
            or {
                leaf(p3)
                leaf(p4)
            }
            and {
                leaf(p1)
                leaf(p4)
            }
        } }

        assertSucceeded(ast1, pattern1) {
            expectValue(p1, buildAst { lit(a) })
            expectValue(p2, buildAst { lit(b) })
            expectValue(p3, buildAst { lit(a) })
            expectValue(p4, buildAst { lit(c) })
        }
        assertSucceeded(ast2, pattern1) {
            expectValue(p1, buildAst { lit(a) })
            expectValue(p2, buildAst { lit(a) })
            expectValue(p3, buildAst { lit(a) })
            expectValue(p4, buildAst { lit(a) })
        }
        assertSucceeded(ast3, pattern1) {
            expectValue(p1, buildAst { lit(c) })
            expectValue(p2, buildAst { lit(a) })
            expectValue(p3, buildAst { lit(b) })
            expectValue(p4, buildAst { lit(a) })
        }

        assertSucceeded(ast1, pattern2) {
            expectValue(p1, buildAst { lit(a) })
            expectValue(p2, buildAst { lit(b) })
            expectValue(p3, buildAst { lit(c) })
        }
        assertSucceeded(ast2, pattern2) {
            expectValue(p1, buildAst { lit(a) })
            expectValue(p2, buildAst { lit(a) })
            expectValue(p3, buildAst { lit(a) })
        }
        assertFailed<TwoValuesForPattern>(ast3, pattern2) { patternName == p1 }

        assertFailed<TwoValuesForPattern>(ast1, pattern3) { patternName == p2 }
        assertSucceeded(ast2, pattern3) {
            expectValue(p2, buildAst { lit(a) })
        }
        assertFailed<TwoValuesForPattern>(ast3, pattern3) { patternName == p2 }

        assertFailed<TwoValuesForPattern>(ast1, pattern4) { patternName == p4 }
        assertSucceeded(ast2, pattern4) {
            expectValue(p1, buildAst { lit(a) })
            expectValue(p3, buildAst { lit(a) })
            expectValue(p4, buildAst { lit(a) })
        }
        assertSucceeded(ast3, pattern4) {
            expectValue(p1, buildAst { lit(b) })
            expectValue(p3, buildAst { lit(c) })
            expectValue(p4, buildAst { lit(a) })
        }
    }
}