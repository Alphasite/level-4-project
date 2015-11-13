package com.nishadmathur.instructions

import com.nishadmathur.errors.PathResolutionError
import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.join
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:08
 */
class TypedInstruction (
        override val arguments: Map<String, Reference>,
        var rawStructure: List<RawStructureLiteral>
) : Instruction {

    override val raw: SizedByteArray
        get() {
            val bytes = ArrayList<SizedByteArray>()

            bytes.addAll(rawStructure.map {
                when (it) {
                    is RawStructureLiteral.literal -> it.literal
                    is RawStructureLiteral.path -> {
                        val segments = it.path.split('.', limit = 2)
                        when (segments.size) {
                            2 -> arguments[segments[0]]?.resolvePath(segments[1])
                            1 -> arguments[segments[0]]?.raw
                            else -> null
                        } ?: throw PathResolutionError(it.path)
                    }
                }
            })

            return SizedByteArray.join(bytes)
        }

    override val size: Int
        get() = raw.bitSize

    override fun toString(): String = "$raw Args:{${arguments.values.joinToString(", ")}}"
}

sealed class RawStructureLiteral {
    class literal(val literal: SizedByteArray): RawStructureLiteral()
    class path(val path: String): RawStructureLiteral()
}
