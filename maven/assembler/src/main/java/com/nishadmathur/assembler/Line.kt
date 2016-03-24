package com.nishadmathur.assembler

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.directives.Segment
import com.nishadmathur.errors.AmbiguousIdentifierMatch
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
    var segment: Segment? = null

    var offset: SizedByteArray? = null
        set(offset) {
            field = offset
            this.instruction?.offset = offset
            this.label?.offset = offset
        }

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

        val potentialSegments = configuration.segments.filter { it.value.matcher.containsMatchIn(line) }

        if (potentialSegments.size > 1) {
            throw AmbiguousIdentifierMatch(potentialSegments.map { it.key }.toList(), "Error occurred when identifying segments.")
        }

        if (potentialSegments.size == 1) {
            val segment = potentialSegments.values.first()

            this.segment = segment

            body = segment.matcher.replace(body, "")
        }

        val label = configuration.labelRegex.find(body)?.groups?.get(1)?.value
        body = configuration.labelRegex.replace(body, "")

        val arguments = body.split(configuration.labelRegex).filter { it.length > 0 }

        if (label == null && arguments.size == 0 && comment == null && segment == null) {
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

            this.instruction = instructionFactory.getInstanceIfIsMatch(name, instructionSegments)
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
