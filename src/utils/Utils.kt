package utils

/**
 * General extension property to assert that `when` statement is exhaustive
 */
val <T> T.exhaustive: Unit
    get() = Unit