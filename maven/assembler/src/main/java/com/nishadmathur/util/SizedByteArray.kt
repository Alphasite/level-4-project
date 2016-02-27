package com.nishadmathur.util

import java.io.ByteArrayOutputStream
import java.math.BigInteger

/**
 * User: nishad
 * Date: 15/10/2015
 * Time: 09:32
 */
class SizedByteArray(byteArray: ByteArray, val bitSize: Int, val isNegative: Boolean = false) {
    val byteArray: ByteArray

    val bigInteger: BigInteger
        get() = BigInteger(byteArray)

    val long: Long
        get() = this.bigInteger.toLong()

    val hex: String
        get() {
            val hexString = BigInteger(1, byteArray).toString(16)
            return hexString.padStart((Math.ceil(hexString.length / 4.0) * 4).toInt(), '0')
        }

    val bytes: String
        get() {
            return byteArray.withIndex()
                .dropWhile { it.index < (byteArray.size - Math.ceil(bitSize / 8.0).toInt()) }
                .map { Integer.toBinaryString(it.value.toInt()).padStart(8, '0') }
                .joinToString(", ")
        }

    init {
        val bytesToAdd = (bitSize / 8) - byteArray.size
        val byteStream = ByteArrayOutputStream()

        if (bytesToAdd > 0) {
            if ((byteArray[byteArray.lastIndex].toInt() and 0x80) > 0) {
                (1..bytesToAdd).forEach { byteStream.write(0xFF) }
            } else {
                (1..bytesToAdd).forEach { byteStream.write(0x00) }
            }
        }

        byteStream.write(byteArray)

        this.byteArray = byteStream.toByteArray()

        assert(byteArray.size >= Math.ceil(bitSize / 8.0).toInt()) { "The array was not sized correctly!" }
    }

    constructor(value: Int, bytes: Collection<Byte>) : this(
        bytes.asSequence().toByteArray(),
        bytes.size * 8,
        value < 0
    )

    fun rightAlign(): SizedByteArray {
        return SizedByteArray.join(this, SizedByteArray(ByteArray(1), 4))
    }

    // TODO check this to make sure it works...
    fun range(min: Int = 0, max: Int = bitSize): SizedByteArray {
        val bitsToSkip: Int = min % 8
        val bytesToSkip: Int = min - bitsToSkip

        val byteStream = ByteArrayOutputStream()

        for (i in 0 until Math.ceil(max / 8.0).toInt()) {
            val previous: Int = currentByte(byteArray, bitSize, i - 1, bytesToSkip).first.toInt()
            val current: Int = currentByte(byteArray, bitSize, i, bytesToSkip).first.toInt()
            val result = (current ushr bitsToSkip) and (previous shl (8 - bitsToSkip))
            byteStream.write(result)
        }

        return SizedByteArray(byteStream.toByteArray(), max - min)
    }

    fun reverseEndianess(smallSegmentSize: Int, largeSegmentSize: Int): SizedByteArray {
        var string = this.byteArray.map { Integer.toBinaryString(it.toInt()) }.joinToString(separator = "")

        val smallSegments = string.split("(?<=\\G.{$smallSegmentSize})")

        if (this.bitSize % largeSegmentSize != 0) {
            string += "0".repeat(this.bitSize % largeSegmentSize)
            System.err.println(
                "Warning, the reverseEndianess function encountered a " +
                "non aligned word, it is being padded by 0's to the correct length."
            )
        }

        var outString = StringBuilder()
        var segment = ""
        for (i in smallSegments.size downTo 0) {
            segment = smallSegments[i] + segment

            if (segment.length >= largeSegmentSize) {
                outString.append(segment)
                segment = ""
            }
        }

        outString.append(segment)

        return SizedByteArray(outString.split("(?<=\\G.{8})").map { it.toByte() }.toByteArray(), bitSize)
    }

    override fun toString(): String {
        return "[$hex][$bytes]@$bitSize"
    }

    companion object {

        fun join(vararg byteArrays: SizedByteArray): SizedByteArray {
            return join(byteArrays.toList())
        }

        fun join(byteArrays: List<SizedByteArray>): SizedByteArray {
            var totalSize = byteArrays.map { it.bitSize }.sum()

            val byteStream = ByteArrayOutputStream();

            var byte: Byte = 0

            // This buffer needs to start a few bits right shifted, so that whole array is right aligned, not left.
            var currentByteBitsRead = (8 - (totalSize % 8)) % 8// i.e. XX11 0011 instead of 1100 11XX

            for (i in 0 until byteArrays.size) {
                val byteArray = byteArrays[i].byteArray
                val bitSize = byteArrays[i].bitSize
                val bytesToSkip = byteArray.size - Math.ceil(bitSize / 8.0).toInt()

                for (j in bytesToSkip until byteArray.size) {
                    var (word, length) = currentByte(byteArray, bitSize, j, bytesToSkip)

                    if (bitSize < 8 && currentByteBitsRead != 0) {
                        word = (word.toInt() shl (8 - bitSize)).toByte()
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

        fun currentByte(byteArray: ByteArray, bitSize: Int, offset: Int, bytesToSkip: Int): Pair<Byte, Int> {
            if (offset >= 0) {
                return Pair(byteArray[offset], bytesToSkip * 8 + bitSize - offset * 8)
            } else {
                return Pair(
                    0.toByte(),
                    8 - Math.abs(Math.abs(byteArray.size * 8 - bitSize) + offset * 8) % 8
                )
            }
        }
    }
}
