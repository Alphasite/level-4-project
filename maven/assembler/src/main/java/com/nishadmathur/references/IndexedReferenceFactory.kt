package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError
import java.io.Serializable
import kotlin.text.Regex

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:59
 */
class IndexedReferenceFactory(override val type: String,
                              val memoryRegex: Regex,
                              val validLeftSideReferenceTypes: List<ReferenceFactory>,
                              val validRightSideReferenceStrings: List<ReferenceFactory>): ReferenceFactory, Serializable {

    override fun checkIsMatch(reference: String): Boolean {
        val matches = memoryRegex.matchAll(reference)

        if (matches.count() != 1) {
            return false
        }

        val matcher = matches.first()
        val groups = matcher.groups

        if (groups.size() < 2) {
            return false
        }

        val leftSide = validLeftSideReferenceTypes any { it.checkIsMatch(groups.get(1)!!.value) }
        val rightSide = validRightSideReferenceStrings any { it.checkIsMatch(groups.get(2)!!.value) }

        return leftSide && rightSide
    }

    override fun getInstanceIfIsMatch(reference: String): IndexedReference {
        if (reference.matches(memoryRegex)) {
            val matches = memoryRegex.matchAll(reference).toList()

            if (matches.count() != 1) {
                throw DataSourceParseError("Error extracting memory address from $reference")
            }

            val match = matches[0]
            val groups = match.groups

            if (groups.size() < 2) {
                throw DataSourceParseError("Memory reference appears to be incomplete: '$reference'")
            }

            val sourceAddress = validLeftSideReferenceTypes
                .first { it.checkIsMatch(groups.get(1)!!.value) }
                .getInstanceIfIsMatch(groups.get(1)!!.value)

            val offsetAddress = validRightSideReferenceStrings
                .first { it.checkIsMatch(groups.get(2)!!.value) }
                .getInstanceIfIsMatch(groups.get(2)!!.value)

            if (sourceAddress is IndexedReference) {
                throw DataSourceParseError("Memory data sources cannot be nested")
            }

            if (offsetAddress is IndexedReference) {
                throw DataSourceParseError("Memory data sources cannot be nested")
            }

            return IndexedReference(sourceAddress, offsetAddress)
        } else {
            throw DataSourceParseError("Error extracting memory reference from $reference")
        }
    }
}
