package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 22:20
 */
enum class LiteralType {
    BINARY {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            return SizedByteArray(ByteArray(0), size)
        }
    },

    HEXADECIMAL {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            return SizedByteArray(ByteArray(0), size)
        }
    },

    INTEGER {
        override fun convertValue(value: String, size: Int): SizedByteArray {
            return SizedByteArray(value.toInt().toByteArray(), size)
        }
    };

    abstract fun convertValue(value: String, size: Int): SizedByteArray
}
