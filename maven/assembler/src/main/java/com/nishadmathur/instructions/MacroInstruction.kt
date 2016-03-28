package com.nishadmathur.instructions

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.directives.Segment
import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 01/02/2016
 * Time: 20:28
 */
class MacroInstruction(
    override val arguments: Map<String, Reference>,
    val stringArguments: Map<String, String>,
    val assembler: Assembler,
    val macroTemplate: String
) : Instruction {
    override var segment: Segment? = null
        set(value) {
            field = value

            if (value != null) {
                assembler.defaultSegment = value
            }
        }

    override var offset: SizedByteArray? = null
        set(value) {
            field = value
            val intOffset = value?.long?.times(assembler.configuration.wordSizeBits)
            assembler.offset = intOffset ?: throw AssemblerError("offset isn't initialised correctly for macro.")
        }

    override val raw: SizedByteArray
        get() {
            var macroTemplate = macroTemplate

            stringArguments.forEach { macroTemplate = macroTemplate.replace("$${it.key}", it.value) }

            return assembler.assemble(Scanner(macroTemplate), isTopLevel = false)
        }

    override val size: Int
        get() = this.raw.bitSize

    override fun toString(): String {
        throw UnsupportedOperationException()
    }
}
