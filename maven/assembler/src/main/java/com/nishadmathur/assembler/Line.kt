package com.nishadmathur.assembler

import com.nishadmathur.errors.LineParseError
import com.nishadmathur.instructions.Instruction
import com.nishadmathur.instructions.InstructionFactory

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 11:29
 */
class Line(val lineNumber: Int, val line: String) {

    var label: Label? = null
    var instruction: Instruction? = null

    var offset: ByteArray? = null
        set(offset) {
            this.offset = offset
            this.label?.offset = offset
        }

    // For what ever reason un-backed fields cant access local fields
    val size: Int
        get() = this.instruction?.size ?: 0

    fun parseLine(instructionFactory: InstructionFactory<Instruction>, labelTable: IdentifierTable) {
        val sections = line.split(":").filter { it != "" }

        if (sections.size() != 1) {
            throw LineParseError(
                    "Line has more than 1 segment, you can have at most 1 label or 1 instruction per line; '$sections'"
            )
        }

        if (line.matches("\\w:$".toRegex())) {
            val label = line
                    .removeSurrounding(":")
                    .trim()

            if (!label.matches("^\\w+$".toRegex())) {
                throw LineParseError("The label has no text or contains non text characters; '$label'")
            }

            this.label = Label(label, labelTable)

        } else {
            val instructionSegments = this.line.split(" ").filter { it.length() > 0 }
            this.instruction = instructionFactory.getInstanceIfIsMatch(instructionSegments)
        }
    }
}
