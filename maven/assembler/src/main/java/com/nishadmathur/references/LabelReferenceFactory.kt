package com.nishadmathur.references

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.errors.DataSourceParseError
import java.io.Serializable
import kotlin.text.Regex

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:01
 */
class LabelReferenceFactory: ReferenceFactory, Serializable {
    var size: Int = 0
    lateinit override var type: String
    lateinit var labelTable: IdentifierTable
    lateinit var labelRegex: Regex
    lateinit var labelExtractionRegex: Regex

    constructor(size: Int, type: String, labelTable: IdentifierTable, labelRegex: Regex, labelExtractionRegex: Regex) {
        this.size = size
        this.type = type
        this.labelTable = labelTable
        this.labelRegex = labelRegex
        this.labelExtractionRegex = labelExtractionRegex
    }

    override fun checkIsMatch(reference: String): Boolean {
        return labelRegex.matches(reference)
    }

    override fun getInstanceIfIsMatch(reference: String): LabelReference {
        if (checkIsMatch(reference)) {
            val label = labelExtractionRegex.match(reference)
            return LabelReference(label!!.groups.get(0).toString(), labelTable, size)
        } else {
            throw DataSourceParseError("Error extracting label from $reference")
        }
    }
}
