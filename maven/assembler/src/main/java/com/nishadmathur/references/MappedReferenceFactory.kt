package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.Serializable
import java.lang.Long

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 21:02
 */
class MappedReferenceFactory(override val type: String, val mappings: Map<String, SizedByteArray>) : ReferenceFactory, Serializable {
    override fun checkIsMatch(reference: String): Boolean {
        return mappings.containsKey(reference)
    }

    override fun getInstanceIfIsMatch(reference: String): LiteralReference {
        if (checkIsMatch(reference)) {
            return LiteralReference(mappings[reference]!!)
        } else {
            throw DataSourceParseError("Error mapping $reference into th mappings set of '${mappings.keySet()}'.")
        }
    }

    companion object : ReferenceParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory {
            val type: String = properties.getRaw("name") as? String
                    ?: throw InvalidOption("name", properties.getRaw("name"))

            val literalSize = properties.getRaw("size") as? Int
                    ?: throw InvalidOption("size", properties.getRaw("size"))

            val mappings: Map<String, SizedByteArray> = (properties.getRaw("mappings") as? Map<*, *>)
                    ?.map {
                        val literal: String = it.key as? String
                                ?: throw InvalidOption("mappings > key", it.toString())

                        val literalValue: ByteArray = when (it.value) {
                            is String -> Long.decode(it.value as String).toByteArray()
                            is Number -> (it.value as Number).toLong().toByteArray()
                            else -> throw InvalidOption("mappings > literalValue", it.toString())
                        }

                        Pair(literal, SizedByteArray(literalValue, literalSize))
                    }?.toMap()
                    ?: throw InvalidOption("mappings", properties.getRaw("mappings"))

            return MappedReferenceFactory(type, mappings)
        }
    }
}
