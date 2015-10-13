package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:02
 */
class MappedReferenceFactory(override val type: String, val mappings: Map<String, ByteArray>): ReferenceFactory<LiteralReference> {
    override fun checkIsMatch(reference: String): Boolean {
        return mappings.containsKey(reference)
    }

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        if (checkIsMatch(reference)) {
            return LiteralReference(32, mappings.get(reference)!!)
        } else {
            throw DataSourceParseError("Error mapping $reference into th mappings set of '${mappings.keySet()}'.")
        }
    }
}
