package com.nishadmathur.references

import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.errors.DataSourceParseError
import java.io.Serializable
import kotlin.text.Regex

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:01
 */
class LiteralReferenceFactory : ReferenceFactory, Serializable {
    var literalSize: Int = 0

    lateinit override var type: String
    lateinit var literalType: LiteralType
    lateinit var literalRegex: Regex
    lateinit var literalExtractionRegex: Regex

    constructor(type: String,
                literalType: LiteralType,
                literalSize: Int,
                literalRegex: Regex,
                literalExtractionRegex: Regex) {
        this.type = type
        this.literalType = literalType
        this.literalSize = literalSize
        this.literalRegex = literalRegex
        this.literalExtractionRegex = literalExtractionRegex
    }

    override fun checkIsMatch(reference: String): Boolean = literalRegex.matches(reference)

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        val match = literalExtractionRegex.match(reference)

        if (match != null) {
            return LiteralReference(literalType.convertValue(match.groups.get(0)!!.value, literalSize))
        } else {
            throw DataSourceParseError("Error converting the literal '$reference' into a $literalType")
        }
    }
}
