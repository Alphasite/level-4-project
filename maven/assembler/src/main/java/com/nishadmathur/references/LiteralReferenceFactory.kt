package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import java.io.Serializable

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:01
 */
class LiteralReferenceFactory(override val type: String,
                              val literalType: LiteralType,
                              val literalSize: Int,
                              val literalRegex: Regex,
                              val literalExtractionRegex: Regex) : ReferenceFactory, Serializable {

    override fun checkIsMatch(reference: String): Boolean = literalRegex.matches(reference)

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        val match = literalExtractionRegex.find(reference)

        if (match != null) {
            return LiteralReference(literalType.convertValue(match.groups[1]!!.value, literalSize))
        } else {
            throw DataSourceParseError("Error converting the literal '$reference' into a $literalType")
        }
    }

    companion object : ReferenceParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory {
            val type: String = properties["name"] as? String
                ?: throw InvalidOption("name", properties["name"])

            val rawLiteralType = properties["literal type"] as? String
                ?: throw InvalidOption("literal type", properties["literal type"])

            val literalType: LiteralType = try {
                LiteralType.valueOf(rawLiteralType)
            } catch (e: IllegalArgumentException) {
                throw InvalidOption("literal type", rawLiteralType)
            }

            val literalSize: Int = properties["literal size"] as? Int
                ?: throw InvalidOption("literal size", properties["literal size"])

            val literalIdentificationRegex: Regex = (properties["validation regex"] as? String)?.toRegex()
                ?: throw InvalidOption("validation regex", properties["validation regex"])

            val literalExtractionRegex: Regex = (properties["extraction regex"] as? String)?.toRegex()
                ?: throw InvalidOption("extraction regex", properties["extraction regex"])

            return LiteralReferenceFactory(
                type,
                literalType,
                literalSize,
                literalIdentificationRegex,
                literalExtractionRegex
            )
        }
    }
}
