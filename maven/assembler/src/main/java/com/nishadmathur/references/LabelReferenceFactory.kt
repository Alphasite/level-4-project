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
class LabelReferenceFactory(override val type: String,
                            val labelTable: IdentifierTable,
                            val size: Int,
                            labelRegex: Regex,
                            labelExtractionRegex: Regex) : ReferenceFactory, Serializable {

    private val memoryRegex = labelRegex
    private val memoryExtractionRegex = labelExtractionRegex

    override fun checkIsMatch(reference: String): Boolean {
        return memoryRegex.matches(reference)
    }

    override fun getInstanceIfIsMatch(reference: String): LabelReference {
        if (checkIsMatch(reference)) {
            val label = memoryExtractionRegex.match(reference)
            return LabelReference(label!!.groups.get(0).toString(), labelTable, size)
        } else {
            throw DataSourceParseError("Error extracting label from $reference")
        }
    }
}
