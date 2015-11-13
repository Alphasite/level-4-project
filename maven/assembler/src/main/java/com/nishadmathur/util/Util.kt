package com.nishadmathur.util

import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.print.attribute.standard.MediaSize

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:38
 */

fun String.decode(): Long {
    return java.lang.Long.decode(this)
}

fun Int.toByteArray(): ByteArray {
    return ByteBuffer.allocate(4).putInt(this).array();
}

fun Long.toByteArray(): ByteArray {
    return ByteBuffer.allocate(8).putLong(this).array();
}

fun intToByteArray(words: Int, withWidth: Int): ByteArray {
    val bytes = ByteArray(withWidth)

    for (i in 0..(withWidth/8)) {
        bytes[i] = ((words shl i * 8) and 0x0F).toByte()
    }

    return bytes
}

fun join(vararg byteArrays: ByteArray): ByteArray {
    val size = byteArrays.map { it.size }.sumBy { it }

    var offset = 0
    var outputArray = ByteArray(size)
    for (byteArray in byteArrays) {
        System.arraycopy(byteArray, 0, outputArray, offset, byteArray.size)
        offset += byteArray.size
    }

    return outputArray
}

fun join(byteArrays: Collection<ByteArray>): ByteArray {
    val size = byteArrays.map { it.size }.sumBy { it }

    var offset = 0
    var outputArray = ByteArray(size)
    for (byteArray in byteArrays) {
        System.arraycopy(byteArray, 0, outputArray, offset, byteArray.size)
        offset += byteArray.size
    }

    return outputArray
}

fun Sequence<Byte>.toByteArray(): ByteArray {
    val list = this.toArrayList()
    val byteArray = ByteArray(list.size)

    for (i in 0 until list.size) {
        byteArray[i] = list[i]
    }

    return byteArray
}

fun <T> Iterable<T>.enumerate(): Iterable<Pair<Int, T>> {
    return (0..Int.MAX_VALUE).zip(this)
}

//fun flipEndianess(byte: Byte): Byte {
//    byte.
//}
//
//fun flipEndianess(byteArray: SizedByteArray): SizedByteArray {
//    val truncatedByteArray = SizedByteArray.join(byteArray)
//    val buffer = ByteBuffer.allocate(truncatedByteArray.byteArray.size)
//    buffer.order(if (buffer.order() == ByteOrder.BIG_ENDIAN) { ByteOrder.LITTLE_ENDIAN } else { ByteOrder.BIG_ENDIAN })
//
//}
