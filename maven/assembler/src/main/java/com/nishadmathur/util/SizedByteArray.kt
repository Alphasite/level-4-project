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

    constructor(size: Int) : this (
        ByteArray(Math.ceil(size / 8.0).toInt()),
        size,
        false
    )

    constructor(value: Int, bytes: Collection<Byte>) : this(
        bytes.asSequence().toByteArray(),
        bytes.size * 8,
        value < 0
    )

    fun rightAlign(): SizedByteArray {
        return SizedByteArray.join(this, SizedByteArray(ByteArray(1), 4))
    }

    fun range(min: Int = 0, max: Int = bitSize): SizedByteArray {

        val binaryString = byteArray
                .map { it.toInt() }
                .map { Integer.toBinaryString(it) }
                .map { it.substring(21, 32) }
                .joinToString("")
                .substring(min, max)
                .padStart(Math.ceil((max - min) / 8.0).toInt() * 8)

        val byteArray = binaryString.splitEqually(8)
            .map { Integer.parseInt(it, 2) }
            .map { it.toByte() }
            .toByteArray()

        return SizedByteArray(byteArray, max - min)
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

            val binaryString = byteArrays.map {
                it.byteArray
                    .map { it.toInt() }
                    .map { Integer.toBinaryString(it) }
                    .map { it.padStart(8, '0') }
                    .map { it.substring(it.length - 8, it.length) }
                    .joinToString("")
                    .substring(it.byteArray.size * 8 - it.bitSize)
            }.joinToString("").padStart(Math.ceil(totalSize / 8.0).toInt() * 8)

            val byteArray = binaryString.splitEqually(8)
                .map { Integer.parseInt(it, 2) }
                .map { it.toByte() }
                .toByteArray()

            return SizedByteArray(byteArray, totalSize)
        }
    }
}
