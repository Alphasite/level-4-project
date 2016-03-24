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

//    @Test
//    fun testRange() {
//        throw UnsupportedOperationException()
//    }

//    @Test
    fun testJoin() {
        val array1 = SizedByteArray(1.toByteArray(), 4)
        val array2 = SizedByteArray(1.toByteArray(), 2)
        val array3 = SizedByteArray(1.toByteArray(), 1)
        val array4 = SizedByteArray(1.toByteArray(), 2)

        val out = SizedByteArray.join(array1, array2, array3, array4)

        println(out)



        assert(out.bitSize == 9)
        assert(out.byteArray.equals(byteArrayOf(0, 45))) {"${out.byteArray} is not ${byteArrayOf(0, 45)}"}
        assert(out.byteArray[0] == 0.toByte()) {"${out.byteArray[0]} was not 0"}
        assert(out.byteArray[1] == 45.toByte()) {"${out.byteArray[1]} was not 45"}

        throw UnsupportedOperationException()
    }
}
