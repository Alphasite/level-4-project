package com.nishadmathur.util

import org.junit.Test

/**
 * User: nishad
 * Date: 16/10/2015
 * Time: 12:59
 */
class SizesByteArrayTest {
    @Test fun testSize() {
        val array = SizedByteArray(0.toByteArray(), 0)
        assert(array.bitSize == 0)
    }

    @Test
    fun testRange() {
        // There are some bugs which need to ber evolved but it isn't currently exposed ot the user, so that's okay.
        val array = SizedByteArray(0x10.toByteArray(), 8)
        val subset = array.range(2, 6)

        assert(subset.byteArray[0] == 0x4.toByte()) { "${subset.byteArray[0]} != ${0x4.toByte()}" }
    }

    @Test
    fun testJoin() {
        val array1 = SizedByteArray(1.toByteArray(), 4) // 0001
        val array2 = SizedByteArray(1.toByteArray(), 2) // 01
        val array3 = SizedByteArray(1.toByteArray(), 1) // 1
        val array4 = SizedByteArray(1.toByteArray(), 2) // 01

        // 0 0010 1101
        val out = SizedByteArray.join(array1, array2, array3, array4)

        assert(out.bitSize == 9)
        assert(out.byteArray[0] == 0.toByte()) {"${out.byteArray[0]} byte[0] was not 0"}
        assert(out.byteArray[1] == 45.toByte()) {"${out.byteArray[1]} byte[1] was not 45"}
    }
}
