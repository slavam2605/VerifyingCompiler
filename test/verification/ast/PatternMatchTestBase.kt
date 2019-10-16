package verification.ast

import junit.assertNotNull
import junit.assertTrue

internal abstract class PatternMatchTestBase {
    protected fun assertSucceeded(ast: AstNode, pattern: PatternAst, assertBlock: MatchResult.Success.() -> Unit = { }) {
        val result = pattern.match(ast)

        assertTrue("Result must be a success", result is MatchResult.Success)
        result.assertBlock()

        println("Successfully matched: patternMap = ${result.patternMap.mapValues { it.value.prettyFormat() }}")
    }

    protected inline fun <reified T> assertFailed(ast: AstNode, pattern: PatternAst, assertBlock: T.() -> Boolean = { true }) {
        val result = pattern.match(ast)

        assertTrue("Result must be a failure", result is MatchResult.Failure)
        assertTrue("Failure description must have type ${T::class.java}", result.description is T)
        assertTrue("Assert block must be true", (result.description as T).assertBlock())

        println("Match successfully failed with the following description: ${result.description}")
    }

    protected fun MatchResult.Success.expectValue(name: String, value: AstNode) {
        val actualValue = patternMap[name]

        assertNotNull("Value must be in the patternMap", actualValue)
        assertTrue("Value must be equal to expected value", actualValue.deepEquals(value))
    }
}