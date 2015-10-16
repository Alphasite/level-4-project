package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.Serializable
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:02
 */
class MappedReferenceFactory : ReferenceFactory, Serializable {
    lateinit override var type: String
    lateinit var mappings: Map<String, SizedByteArray>

    constructor(type: String, mappings: Map<String, SizedByteArray>) {
        this.type = type
        this.mappings = mappings
    }

    constructor(type: String, mappings: List<Triple<String, Any, Int>>) {
        this.type = type

        var mappings: List<Triple<String, Double, Int>> = mappings
            .map {
                when (it.second) {
                    is String ->
                        return Triple(it.first, Integer.decode(it.second as String), it.third)
                    is Number ->
                        return Triple(it.first, (it.second as Number).toDouble(), it.third)
                    else ->
                        throw DataSourceParseError("Error parsing the literal ${it.second}it")
                }
            }

        val tmpMappings = HashMap<String, SizedByteArray>()
        mappings.forEach { tmpMappings[it.first] = SizedByteArray(it.second.toByteArray(), it.third) }

        this.mappings = tmpMappings
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
