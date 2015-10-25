package com.nishadmathur.instructions

import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.join
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:08
 */
class TypedInstruction(override val arguments: List<Reference>,
                       val rawLiteral: SizedByteArray) : Instruction {

    override val raw: SizedByteArray
        get() {
            val bytes = arrayListOf(rawLiteral)
            bytes.addAll(arguments.map { argument -> argument.raw })

            return SizedByteArray.join(bytes)
        }

    override val size: Int
        get() = rawLiteral.bitSize + arguments.map { argument -> argument.size }.sum()

    override fun toString(): String = "$raw Args:{${arguments.joinToString(", ")}}"
}
