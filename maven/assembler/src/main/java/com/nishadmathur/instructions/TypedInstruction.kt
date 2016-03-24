package com.nishadmathur.instructions

import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.references.Reference
import com.nishadmathur.util.OffsetAssignable
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:08
 */
class TypedInstruction(
    override val arguments: Map<String, Reference>,
    val instructionFormat: InstructionFormat
) : Instruction {
    override var offset: SizedByteArray? = null
        set(value) {
            field = value

            for (argument in arguments.values) {
                if (argument is OffsetAssignable) {
                    argument.offset = value
                }
            }
        }

    override val raw: SizedByteArray
        get() = instructionFormat.applyTo(arguments)

    override val size: Int
        get() = raw.bitSize

    override fun toString(): String = "$raw Args:{${arguments.values.joinToString(", ")}}"
}


