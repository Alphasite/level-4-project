package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.Serializable
import java.lang.Long.decode
import java.text.MessageFormat
import java.util.*

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
            throw DataSourceParseError("Error mapping $reference into th mappings set of '${mappings.keys}'.")
        }
    }

    companion object : ReferenceParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory {
            val type: String = properties["name"] as? String
                ?: throw InvalidOption("name", properties["name"])

            val literalSize = properties["size"] as? Int
                ?: throw InvalidOption("size", properties["size"])

            val mappings: Map<String, SizedByteArray>

            if ("mappings" in properties) {
                mappings = (properties["mappings"] as? Map<*, *>)
                    ?.map {
                        val literal: String = it.key as? String
                            ?: throw InvalidOption("mappings > key", it.toString())

                        val literalValue: ByteArray = when (it.value) {
                            is String -> decode(it.value as String).toByteArray()
                            is Number -> (it.value as Number).toLong().toByteArray()
                            else -> throw InvalidOption("mappings > literalValue", it.toString())
                        }

                        Pair(literal, SizedByteArray(literalValue, literalSize))
                    }?.toMap()
                    ?: throw InvalidOption("mappings", properties)
            } else {
                val range = properties["range"] as? Map<*, *>
                    ?: throw InvalidOption("range", properties)

                val formatString = range["format string"] as? String
                    ?: throw InvalidOption("format string", range)

                val startIndex = (range["start index"] as? Number)?.toLong()
                    ?: throw InvalidOption("start index", range)

                val count = (range["count"] as? Number)?.toLong()
                    ?: throw InvalidOption("count", range)

                var literal = range["start literal"]
                    ?: throw InvalidOption("start literal", range)

                val startLiteral = when (literal) {
                    is String -> decode(literal)
                    is Number -> (literal).toLong()
                    else -> throw InvalidOption("start literal", properties)
                }

                mappings = HashMap()
                for (i in startIndex until (startIndex + count)) {
                    mappings.put(
                        MessageFormat.format(formatString, i),
                        SizedByteArray((startLiteral + i).toByteArray(), literalSize)
                    )
                }
            }

            return MappedReferenceFactory(type, mappings)
        }
    }
}
