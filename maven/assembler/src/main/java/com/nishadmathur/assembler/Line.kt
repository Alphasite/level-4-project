package com.nishadmathur.assembler

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.LineParseError
import com.nishadmathur.instructions.Instruction
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 11:29
 */
class Line(val lineNumber: Int, val line: String) {

    var comment: String? = null
    var label: Label? = null
    var instruction: Instruction? = null

    var offset: SizedByteArray? = null
        set(offset) {
            field = offset

            // TODO implement this.
            //            if (instruction == null) {
            //                this.label?.offset = offset + 1 // Otherwise
            //            } else {
            this.instruction?.offset = offset
            this.label?.offset = offset
            //            }
        }

    // For what ever reason un-backed fields cant access local fields
    val size: Int
        get() = this.instruction?.size ?: 0

    fun parseLine(
        configuration: Configuration,
        instructionFactory: InstructionFactory,
        labelTable: IdentifierTable
    ) {
        if (line.length == 0) {
            return
        }

        comment = configuration.commentRegex.find(line)?.value
        var body = configuration.commentRegex.replace(line, "").trim()

        val label = configuration.labelRegex.find(body)?.groups?.get(1)?.value
        body = configuration.labelRegex.replace(body, "")

        val arguments = body.split(configuration.labelRegex).filter { it.length > 0 }

        if (label == null && arguments.size == 0 && comment == null) {
            throw LineParseError("Line isn't formatted correctly; $line")
        }

        if (label != null) {
            this.label = Label(label.trim(' ', '\n', '\r'), labelTable)
        }

        if (arguments.size > 0) {
            val instructionSection = arguments[0].trim()

            val name = instructionSection.substringBefore(" ")

            val instructionSegments = instructionSection.replace("$name", "").trim()
                .split(configuration.argumentSeparator)
                .map { it.trim() }
                .filter { it.length > 0 }

            // This needs to be investigated and potentially fixed in the future.
//            if (instructionSegments.size > 0) {
                this.instruction = instructionFactory.getInstanceIfIsMatch(name, instructionSegments)
//            } else {
//                throw LineParseError("Error parsing the instruction segment of line: '$line'")
//            }
        }
    }

    override fun toString(): String {
        val line = StringBuilder()

        line.append(lineNumber + 1)
        line.append(" '")
        line.append(this.line)
        line.append("': ")

        if (this.label != null) {
            line.append(this.label).append(" ")
        }

        if (this.instruction != null) {
            line.append(this.instruction).append(" ")
        }

        return line.toString()
    }
}
