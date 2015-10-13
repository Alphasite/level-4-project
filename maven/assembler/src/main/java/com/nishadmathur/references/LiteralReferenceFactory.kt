package com.nishadmathur.references

import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.errors.DataSourceParseError
import kotlin.text.Regex

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:01
 */
class LiteralReferenceFactory(override val type: String,
                              val literalType: LiteralType,
                              val literalRegex: Regex,
                              val literalExtractionRegex: Regex) : ReferenceFactory<LiteralReference> {

    override fun checkIsMatch(reference: String): Boolean = literalRegex.matches(reference)

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        val match = literalExtractionRegex.match(reference)

        if (match != null) {
            return LiteralReference(32, literalType.convertValue(match.groups.get(0).toString()))
        } else {
            throw DataSourceParseError("Error converting the literal '$reference' into a $literalType")
        }
    }
}

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
            return byteArrayOf(0)
        }
    };

    abstract fun convertValue(value: String): ByteArray
}
