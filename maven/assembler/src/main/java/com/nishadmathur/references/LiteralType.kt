package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.math.BigInteger

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 22:20
 */
enum class LiteralType {
    BINARY {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            return SizedByteArray(BigInteger(value, 2).toByteArray(), size)
        }
    },

    HEXADECIMAL {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            val hexString = value[2, value.length].toString()
            return SizedByteArray(BigInteger(hexString, 16).toByteArray(), size)
        }
    },

    INTEGER {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            return SizedByteArray(BigInteger(value).toByteArray(), size)
        }
    };

    abstract fun convertValue(value: String, size: Int): SizedByteArray
}
