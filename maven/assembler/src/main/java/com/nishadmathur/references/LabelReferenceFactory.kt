package com.nishadmathur.references

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import java.io.Serializable

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
            val label = memoryExtractionRegex.find(reference)
            return LabelReference(label!!.groups[0]!!.value, labelTable, size)
        } else {
            throw DataSourceParseError("Error extracting label from $reference")
        }
    }

    companion object : ReferenceParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory {
            val type: String = properties["name"] as? String
                ?: throw InvalidOption("name", properties["name"])

            val size: Int = properties["size"] as? Int
                ?: throw InvalidOption("size", properties["size"])

            val labelRegex: Regex = (properties["validation regex"] as? String)?.toRegex()
                ?: throw InvalidOption("validation regex", properties["validation regex"])

            val labelExtractionRegex: Regex = (properties["extraction regex"] as? String)?.toRegex()
                ?: throw InvalidOption("extraction regex", properties["extraction regex"])

            return LabelReferenceFactory(
                type,
                configuration.labelTable,
                size,
                labelRegex,
                labelExtractionRegex
            )
        }
    }
}
