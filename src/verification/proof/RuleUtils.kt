package verification.proof

import verification.ast.PatternAst
import verification.ast.arrow
import verification.ast.buildPattern
import verification.ast.leaf

class InferenceRule(
    vararg val prepositions: PatternAst,
    val consequence: PatternAst
)

val modusPonens = InferenceRule(
    buildPattern { leaf("a") }, buildPattern { arrow {
        leaf("a")
        leaf("b")
    } },
    consequence = buildPattern { leaf("b") }
)