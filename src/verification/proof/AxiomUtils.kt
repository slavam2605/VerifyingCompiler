package verification.proof

import verification.ast.*

private val arrow1 = buildPattern { arrow {
    leaf("a")
    arrow {
        leaf("b")
        leaf("a")
    }
} }

private val arrow2 = buildPattern { arrow {
    arrow {
        leaf("a")
        leaf("b")
    }
    arrow {
        arrow {
            leaf("a")
            arrow {
                leaf("b")
                leaf("c")
            }
        }
        arrow {
            leaf("a")
            leaf("c")
        }
    }
} }

private val and1 = buildPattern { arrow {
    leaf("a")
    arrow {
        leaf("b")
        and {
            leaf("a")
            leaf("b")
        }
    }
} }

private val and2 = buildPattern { arrow {
    and {
        leaf("a")
        leaf("b")
    }
    leaf("a")
} }

private val and3 = buildPattern { arrow {
    and {
        leaf("a")
        leaf("b")
    }
    leaf("b")
} }

private val or1 = buildPattern { arrow {
    leaf("a")
    or {
        leaf("a")
        leaf("b")
    }
} }

private val or2 = buildPattern { arrow {
    leaf("b")
    or {
        leaf("a")
        leaf("b")
    }
} }

private val or3 = buildPattern { arrow {
    arrow {
        leaf("a")
        leaf("c")
    }
    arrow {
        arrow {
            leaf("b")
            leaf("c")
        }
        arrow {
            or {
                leaf("a")
                leaf("b")
            }
            leaf("c")
        }
    }
} }

private val not1 = buildPattern { arrow {
    arrow {
        leaf("a")
        leaf("b")
    }
    arrow {
        arrow {
            leaf("a")
            not { leaf("b") }
        }
        not { leaf("a") }
    }
} }

private val not2 = buildPattern { arrow {
    not { not { leaf("a") } }
    leaf("a")
} }

val basicAxioms = listOf(arrow1, arrow2, and1, and2, and3, or1, or2, or3, not1, not2)