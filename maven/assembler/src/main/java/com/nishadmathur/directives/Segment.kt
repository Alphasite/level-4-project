package com.nishadmathur.directives

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.assembler.Line
import com.nishadmathur.assembler.RawLiteralConvertible
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 24/03/2016
 * Time: 13:07
 */
class Segment(
    val name: String,
    val offset: Long,
    val matcher: Regex
): RawLiteralConvertible {

    val lines = ArrayList<Line>()

    override val raw: SizedByteArray
        get() = SizedByteArray.join(
            lines.map { Assembler.annotateError(it) { it.instruction?.raw } }
                .filterNotNull()
                .filter { it.bitSize > 0 }
        )

    companion object {
        fun parseSegments(segmentMap: List<*>): Map<String, Segment> {
            val segmentMapPartiallyParsed = segmentMap
                .map { it as? Map<*, *> ?: throw InvalidOption(it.toString(), segmentMap) }
                .map { map -> map.entries.map {
                    Pair(
                        it.key as? String ?: throw InvalidOption(it.key.toString(), map),
                        it.value
                    )
                }.toMap() }

            return segmentMapPartiallyParsed.map {
                val name = it["name"] as? String
                    ?: throw InvalidOption("name", it)

                val rawOffset = it["offset"]
                val offset = when (rawOffset) {
                    is String -> java.lang.Long.decode(rawOffset)
                    is Number -> (rawOffset).toLong()
                    else -> throw InvalidOption("offset", it)
                }

                val regex = it["regex"] as? String
                    ?: throw InvalidOption("regex", it)

                Pair(
                    name,
                    Segment(name, offset, Regex(regex))
                )
            }.toMap()
        }
    }
}
