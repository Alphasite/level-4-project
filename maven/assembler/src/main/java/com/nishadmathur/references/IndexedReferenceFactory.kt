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
                              val factory: ReferenceFactory): ReferenceFactory, Serializable {

    override fun checkIsMatch(reference: String): Boolean {
        if (reference.matches(memoryRegex)) {
            val matches = memoryRegex.matchAll(reference).toList()

            if (matches.count() != 1) {
                return false
            }

            val match = matches[0]

            if (!reference.matches("\\[.*$memoryRegex.*\\]".toRegex())) {
                return false
            }

            return true
        } else {
            return false
        }
    }

    override fun getInstanceIfIsMatch(reference: String): IndexedReference {
        if (reference.matches(memoryRegex)) {
            val matches = memoryRegex.matchAll(reference).toList()

            if (matches.count() != 1) {
                throw DataSourceParseError("Error extracting memory address from $reference")
            }

            val match = matches[0]

            if (!reference.matches("\\[.*$memoryRegex.*\\]".toRegex())) {
                throw DataSourceParseError("Memory data sources cannot be nested; '$reference'")
            }

            val sourceAddress = factory.getInstanceIfIsMatch(match.groups.get(1)!!.value)
            val offsetAddress = factory.getInstanceIfIsMatch(match.groups.get(2)!!.value)

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
