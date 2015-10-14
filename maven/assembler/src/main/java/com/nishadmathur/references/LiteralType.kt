package com.nishadmathur.references

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 22:20
 */
enum class LiteralType {
    BINARY {
        override fun convertValue(value: String): ByteArray {
            return byteArrayOf(0)
        }
    },

    HEXADECIMAL {
        override fun convertValue(value: String): ByteArray {
            return byteArrayOf(0)
        }
    },

    INTEGER {
        override fun convertValue(value: String): ByteArray {
            return byteArrayOf(value.toInt().toByte())
        }
    };

    abstract fun convertValue(value: String): ByteArray
}
