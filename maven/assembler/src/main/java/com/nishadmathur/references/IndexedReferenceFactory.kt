package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import java.io.Serializable

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:59
 */
class IndexedReferenceFactory(override val type: String,
                              val memoryRegex: Regex,
                              val sourceBeforeOffset: Boolean,
                              val validLeftSideReferenceTypes: List<ReferenceFactory>,
                              val validRightSideReferenceStrings: List<ReferenceFactory>) : ReferenceFactory, Serializable {

    override fun checkIsMatch(reference: String): Boolean {
        val matches = memoryRegex.findAll(reference)

        if (matches.count() != 1) {
            return false
        }

        val matcher = matches.first()
        val groups = matcher.groups

        if (groups.size < 2) {
            return false
        }

        val leftSide = validLeftSideReferenceTypes.any { it.checkIsMatch(groups[1]!!.value) }
        val rightSide = validRightSideReferenceStrings.any { it.checkIsMatch(groups[2]!!.value) }

        return leftSide && rightSide
    }

    override fun getInstanceIfIsMatch(reference: String): IndexedReference {
        if (reference.matches(memoryRegex)) {
            val matches = memoryRegex.findAll(reference).toList()

            if (matches.count() != 1) {
                throw DataSourceParseError("Error extracting memory address from $reference")
            }

            val match = matches[0]
            val groups = match.groups

            if (groups.size < 2) {
                throw DataSourceParseError("Memory reference appears to be incomplete: '$reference'")
            }

            val sourceAddress = validLeftSideReferenceTypes
                .first { it.checkIsMatch(groups[1]!!.value) }
                .getInstanceIfIsMatch(groups[1]!!.value)

            val offsetAddress = validRightSideReferenceStrings
                .first { it.checkIsMatch(groups[2]!!.value) }
                .getInstanceIfIsMatch(groups[2]!!.value)

            if (sourceAddress is IndexedReference) {
                throw DataSourceParseError("Memory data sources cannot be nested")
            }

            if (offsetAddress is IndexedReference) {
                throw DataSourceParseError("Memory data sources cannot be nested")
            }

            return IndexedReference(sourceAddress, offsetAddress, sourceBeforeOffset)
        } else {
            throw DataSourceParseError("Error extracting memory reference from $reference")
        }
    }

    companion object : ReferenceParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory {
            val type: String = properties["name"] as? String
                ?: throw InvalidOption("name", properties["name"])

            val memoryRegex: Regex = (properties["regex"] as? String)?.toRegex()
                ?: throw InvalidOption("regex", properties["regex"])

            val sourceBeforeOffset: Boolean = properties["source before offset"] as? Boolean
                ?: false

            val validLeftSideReferenceTypes: List<ReferenceFactory> = (properties["valid left hand types"] as? List<*>)
                ?.map { it as? String }
                ?.map { referenceFactories[it] ?: throw InvalidOption(it.toString(), "Not a known option.") }
                ?.requireNoNulls()
                ?: throw InvalidOption("valid left hand types", properties["valid left hand types"])

            val validRightSideReferenceStrings: List<ReferenceFactory> = (properties["valid right hand types"] as? List<*>)
                ?.map { it as? String }
                ?.map { referenceFactories[it] ?: throw InvalidOption(it.toString(), "Not a known option.") }
                ?.requireNoNulls()
                ?: throw InvalidOption("valid right hand types", properties["valid right hand types"])

            return IndexedReferenceFactory(
                type,
                memoryRegex,
                sourceBeforeOffset,
                validLeftSideReferenceTypes,
                validRightSideReferenceStrings
            )
        }
    }
}
