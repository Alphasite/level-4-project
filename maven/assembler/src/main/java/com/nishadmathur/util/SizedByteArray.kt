package com.nishadmathur.util

import java.io.ByteArrayOutputStream
import java.util.*

/**
 * User: nishad
 * Date: 15/10/2015
 * Time: 09:32
 */
class SizedByteArray(val byteArray: ByteArray, val bitSize: Int) {

    constructor(bytes: Collection<Byte>) : this(bytes.asSequence().toByteArray(), bytes.size() * 8)

    override fun toString(): String {
        val bytes = byteArray.withIndex()
            .dropWhile { it.index < (byteArray.size() - Math.ceil(bitSize / 8.0).toInt()) }
            .map { Integer.toBinaryString(it.value.toInt()).padStart(8, '0') }
            .join(", ")

        return "[$bytes]@$bitSize"
    }

    companion object {
        fun join(byteArrays: List<SizedByteArray>): SizedByteArray {
            var totalSize = byteArrays.map { it.bitSize } .sum()

            val byteStream = ByteArrayOutputStream();

            var byte: Byte = 0

            // This buffer needs to start a few bits right shifted, so that whole array is right aligned, not left.
            var currentByteBitsRead = 8 - (totalSize % 8) // i.e. XX11 0011 instead of 1100 11XX

            for (i in 0 until byteArrays.size()) {
                val byteArray = byteArrays[i].byteArray
                val bitSize = byteArrays[i].bitSize
                val bytesToSkip = byteArray.size() - Math.ceil(bitSize / 8.0).toInt()

                for (j in bytesToSkip until byteArray.size()) {
                    var (word, length) = currentByte(byteArray, bitSize, j, bytesToSkip)

                    if (byteArrays[i].bitSize < 8) {
                        word = (word.toInt() shl (8 - byteArrays[i].bitSize)).toByte()
                    }

                    byte = ((byte.toInt() shl (8 - currentByteBitsRead)) or (word.toInt() ushr currentByteBitsRead)).toByte()

                    currentByteBitsRead += length

                    if (currentByteBitsRead >= 8) {
                        byteStream.write(byteArrayOf(byte))
                        currentByteBitsRead %= 8
                        byte = word
                    }
                }
            }

            if (currentByteBitsRead > 0) {
                byteStream.write(byteArrayOf(byte))
            }

            return SizedByteArray(byteStream.toByteArray(), totalSize)
        }

        fun currentByte(byteArray: ByteArray, bitSize: Int, j: Int, bytesToSkip: Int): Pair<Byte, Int> {
            return Pair(byteArray[j], bytesToSkip * 8 + bitSize - j * 8)
        }
    }
}
