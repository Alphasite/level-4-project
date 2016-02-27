package com.nishadmathur.assembler

import com.nishadmathur.configuration.Configuration
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
class Assembler(val configuration: Configuration, val instructionFactory: InstructionFactory, val identifierTable: IdentifierTable) {
    val lines = ArrayList<Line>()
    var offset: Long = 0

    val listings: String
        get() {
            val maxLineNumberLength = ceil(log10(lines.map { it.lineNumber }.max()?.toDouble() ?: 0.0)).toInt()

            val hexLines = lines.map { it.instruction?.raw?.hex ?: "" }

            val maxLength = hexLines.map { it.length }.max() ?: 0

            return lines.zip(hexLines).map {
                val (line, hex) = it
                val lineNumber = line.lineNumber.toString().padEnd(maxLineNumberLength)
                "$lineNumber | ${hex.padEnd(maxLength)} | ${line.line}"
            }.joinToString("\n")
        }

    fun loadFile(file: Scanner) {
        file.use {

            var stringLines: MutableList<String> = ArrayList()

            while (it.hasNextLine()) {
                stringLines.add(it.nextLine())
            }

            for (i in 0 until stringLines.size) {
                if (stringLines[i].length > 0) {
                    lines.add(Line(i, stringLines[i]))
                }
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

        calculateOffsets(lines)

        var instructionBytes = this.lines
            .map { annotateError(it) { it.instruction?.raw } }
            .filterNotNull()
            .filter { it.bitSize > 0 }

        println("Listings:")
        println(listings)
        println()

        val bytes = SizedByteArray.join(instructionBytes)
        if (isTopLevel && configuration.smallSegmentSize != null && configuration.largeSegmentSize != null) {
            return bytes.reverseEndianess(configuration.smallSegmentSize, configuration.largeSegmentSize)
        } else {
            return bytes
        }
    }

    fun calculateOffsets(lines: List<Line>): List<Line> {
        var offset: Long = this.offset
        var offsetLines = arrayListOf<Line>()

        for (line in lines) {
            annotateError(line) {
                line.offset = SizedByteArray(offset.toByteArray(), configuration.identifierBitSize.toInt())
                offsetLines.add(line)
                offset += line.size / configuration.wordSizeBits // Size is bit size not byte size.
            }
        }

        return offsetLines
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
