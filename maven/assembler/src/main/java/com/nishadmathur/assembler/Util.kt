package com.nishadmathur.assembler

import javax.print.attribute.standard.MediaSize

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:38
 */

fun intToByteArray(words: Int, withWidth: Int): ByteArray {
    val bytes = ByteArray(withWidth)

    for (i in 0..(withWidth/8)) {
        bytes[i] = ((words shl i * 8) and 0x0F).toByte()
    }

    return bytes
}

fun join(vararg byteArrays: ByteArray): ByteArray {
    val size = byteArrays map { it.size() } sumBy { it }

    var offset = 0
    var outputArray = ByteArray(size)
    for (byteArray in byteArrays) {
        System.arraycopy(byteArray, 0, outputArray, offset, byteArray.size())
        offset += byteArray.size()
    }

    return outputArray
}

fun <T> Iterable<T>.enumerate(): Iterable<Pair<Int, T>> {
    return (0..Int.MAX_VALUE).zip(this)
}
