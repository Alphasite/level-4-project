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
                       val rawLiteral: SizedByteArray,
                       val paddingBits: Int) : Instruction {

    override val raw: SizedByteArray
        get() {
            val bytes = arrayListOf(rawLiteral)
            bytes.addAll(arguments.map { argument -> argument.raw })
            bytes.add(SizedByteArray(ByteArray(Math.ceil(paddingBits / 1.0).toInt()), paddingBits))

            return SizedByteArray.join(bytes)
        }

    override val size: Int
        get() = rawLiteral.bitSize + arguments.map { argument -> argument.size }.sum() + paddingBits

    override fun toString(): String = "$raw Args:{${arguments.joinToString(", ")}}"
}
