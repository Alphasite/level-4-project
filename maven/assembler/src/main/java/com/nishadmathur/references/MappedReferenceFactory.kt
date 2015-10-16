package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.util.SizedByteArray
import java.io.Serializable

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:02
 */
class MappedReferenceFactory: ReferenceFactory, Serializable {
    lateinit override var type: String
    lateinit var mappings: Map<String, SizedByteArray>

    constructor(type: String, mappings: Map<String, SizedByteArray>) {
        this.type = type
        this.mappings = mappings
    }

    override fun checkIsMatch(reference: String): Boolean {
        return mappings.containsKey(reference)
    }

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        if (checkIsMatch(reference)) {
            return LiteralReference(mappings.get(reference)!!)
        } else {
            throw DataSourceParseError("Error mapping $reference into th mappings set of '${mappings.keySet()}'.")
        }
    }
}
