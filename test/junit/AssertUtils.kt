package junit

import org.junit.Assert
import kotlin.contracts.contract

internal fun assertTrue(message: String, condition: Boolean) {
    contract { returns() implies condition }
    Assert.assertTrue(message, condition)
}

internal fun assertNotNull(message: String, value: Any?) {
    contract { returns() implies (value != null) }
    Assert.assertNotNull(message, value)
}