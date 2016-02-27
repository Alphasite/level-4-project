package com.nishadmathur.instructions.format

import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.errors.ParserError
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.ensureKeysAreStrings
import com.nishadmathur.util.parseNumber

/**
 * User: nishad
 * Date: 13/01/2016
 * Time: 22:41
 */
sealed class TypedLiteral(val name: String?, val default: SizedByteArray?) {

    class literal(name: String?, val literal: SizedByteArray) : TypedLiteral(name, null)

    class path(name: String?, val path: String, val size: Int? = null, default: SizedByteArray? = null) : TypedLiteral(name, default)

    class expression(name: String?, val expression: String, val size: Int, default: SizedByteArray? = null) : TypedLiteral(name, default)

    companion object {
        public fun deduceTypeOfLiteral(untypedLiteral: Map<String, *>): TypedLiteral {
            if ("path" in untypedLiteral) {
                val name = untypedLiteral["name"] as? String?
                val path = untypedLiteral["path"] as? String
                    ?: throw InvalidOption("path", untypedLiteral)
                val size = (untypedLiteral["size"] as? Number)?.toInt()

                val defaultMap = (untypedLiteral["default"] as? Map<*, *>)?.ensureKeysAreStrings()
                val default: SizedByteArray?
                if (defaultMap != null) {
                    default = parseLiteral(defaultMap)
                } else {
                    default = null
                }

                return TypedLiteral.path(name, path, size, default)
            } else {
                val name = untypedLiteral["name"] as? String?
                val literal = parseLiteral(untypedLiteral)

                return TypedLiteral.literal(name, literal)
            }
        }

        private fun parseLiteral(untypedLiteral: Map<String, *>): SizedByteArray {
            val literal = parseNumber(untypedLiteral["literal"]!!)
            val size = (untypedLiteral["size"] as? Number)?.toInt()
                ?: throw InvalidOption("size", untypedLiteral)
            return SizedByteArray(literal, size)
        }

        public fun parseList(unparsedLiterals: List<*>): List<TypedLiteral> {
            val untypedLiterals = unparsedLiterals
                .map { it as? Map<*, *> }
                .requireNoNulls()
                .map { it.ensureKeysAreStrings() ?: throw ParserError("Error parsing the config '$it'.") }

            val typedLiterals = untypedLiterals.map { deduceTypeOfLiteral(it) }

            return typedLiterals
        }
    }
}
