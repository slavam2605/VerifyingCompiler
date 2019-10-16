package verification.ast

import kotlin.random.Random

private val notThreadSafeRandom = Random.Default

fun randomName(startLength: Int = 1, endLength: Int = 10): String {
    val length = notThreadSafeRandom.nextInt(startLength, endLength + 1)
    val buffer = CharArray(length) { notThreadSafeRandom.nextInt(32, 128).toChar() }
    return String(buffer)
}