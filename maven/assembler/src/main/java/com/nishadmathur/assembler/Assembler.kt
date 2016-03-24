package com.nishadmathur.assembler

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.directives.Segment
import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.lang.Math.ceil
import java.lang.Math.log10
import java.util.*

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 09:08
 */
class Assembler(
    val configuration: Configuration,
    val instructionFactory: InstructionFactory,
    val identifierTable: IdentifierTable
): RawLiteralConvertible {

    val lines = ArrayList<Line>()

    var offset: Long = configuration.startOffset

    var compiledArray = SizedByteArray(0.toByteArray(), 0)

    val listings: String
        get() {
            val maxLineNumberLength = ceil(log10(lines.map { it.lineNumber }.max()?.toDouble() ?: 0.0)).toInt()

            val hexLines = lines.map {
                val instruction = it.instruction
                instruction
                    ?.raw
                    ?.hex
                    ?.padStart(Math.ceil(instruction.size / 4.0).toInt(), '0')
                    ?: ""
            }

            val lengthLines = lines.map { it.offset?.hex ?: "" }

            val maxHexLength = hexLines.map { it.length }.max() ?: 0
            val maxLenLength = lengthLines.map{ it.length }.max() ?: 0

            return lines.zip(hexLines).zip(lengthLines).map {
                val (tup, length) = it
                val (line, hex) = tup

                val lineNumber = line.lineNumber.toString().padEnd(maxLineNumberLength)
                "$lineNumber | ${length.padEnd(maxLenLength)} | ${hex.padEnd(maxHexLength)} | ${line.line}"
            }.joinToString("\n")
        }

    init {
        configuration.segments.put("default", Segment("default", configuration.startOffset, Regex("^.default$")))
    }

    override val raw: SizedByteArray get() {
        return compiledArray
    }

    fun loadFile(file: Scanner) {
        file.use {

            var stringLines = ArrayList<String>()

            while (it.hasNextLine()) {
                stringLines.add(it.nextLine())
            }

            for (i in 0 until stringLines.size) {
                lines.add(Line(i, stringLines[i]))
            }
        }

    }

    fun assemble(file: Scanner, isTopLevel: Boolean): SizedByteArray {
        this.loadFile(file)

        for (line in this.lines) {
            annotateError(line) {
                line.parseLine(instructionFactory = instructionFactory, labelTable = identifierTable, configuration = configuration)
            }
        }

        var currentSegment = configuration.segments["default"]!!
        for (line in lines) {
            currentSegment = line.segment ?: currentSegment

            currentSegment.lines.add(line)
        }

        calculateOffsets(configuration.segments.values)

        var orderedSegments = configuration.segments.values.sortedBy { it.offset }

        var segmentsWithPadding = ArrayList<Pair<Segment, SizedByteArray>>()

        for (i in 0 until orderedSegments.size - 1) {
            val paddingBits = orderedSegments[i + 1].offset - orderedSegments[i].offset + orderedSegments[0].raw.bitSize
            val padding = SizedByteArray(paddingBits.toInt())
            segmentsWithPadding.add(Pair(orderedSegments[i], padding))
        }

        segmentsWithPadding.add(Pair(orderedSegments.last(), SizedByteArray(0)))

        var instructionBytes = segmentsWithPadding
            .map {
                listOf(it.first.raw, it.second)
            }.flatten()

        compiledArray = SizedByteArray.join(instructionBytes)

        if (isTopLevel && configuration.smallSegmentSize != null && configuration.largeSegmentSize != null) {
            compiledArray = compiledArray.reverseEndianess(configuration.smallSegmentSize, configuration.largeSegmentSize)
        }

        return compiledArray
    }

    fun calculateOffsets(segments: Collection<Segment>) {
        for (segment in segments) {
            var offset: Long = segment.offset
            var offsetLines = arrayListOf<Line>()

            for (line in segment.lines) {
                annotateError(line) {
                    line.offset = SizedByteArray(offset.toByteArray(), configuration.identifierBitSize.toInt())
                    offsetLines.add(line)
                    offset += line.size / configuration.wordSizeBits // Size is bit size not byte size.
                }
            }
        }
    }

    companion object {
        fun bail(error: Exception) {
            System.err.println(error)
            System.exit(-1)
        }

        fun <T> annotateError(line: Line, function: (Line) -> T): T {
            try {
                return function(line)
            } catch (e: AssemblerError) {
                e.line = line
                throw e
            }
        }
    }
}
