package com.nishadmathur.assembler

import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.errors.LineParseError
import com.nishadmathur.instructions.Instruction
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.references.MetaReferenceFactory
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.intToByteArray
import com.nishadmathur.util.toByteArray
import sun.tools.asm.Assembler
import java.io.BufferedInputStream
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.stream.IntStream

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 09:08
 */
class Assembler(val instructionFactory: InstructionFactory, val identifierTable: IdentifierTable) {
    val lines = ArrayList<Line>()
    val word_size = 32

    fun loadFile(path: String) {
        Scanner(FileReader(path)).use {

            var stringLines: MutableList<String> = ArrayList()

            while (it.hasNextLine()) {
                stringLines.add(it.nextLine())
            }

            for (i in 0 until stringLines.size) {
                if (stringLines[i].length() > 0) {
                    lines.add(Line(i, stringLines[i]))
                }
            }
        }

    }

    fun assemble(file: String): SizedByteArray {
        this.loadFile(file)

        for (line in this.lines) {
            annotateError(line) {
                line.parseLine(instructionFactory = instructionFactory, labelTable = identifierTable)
            }
        }

        calculateOffsets(lines)

        var instructionBytes = this.lines
            .map { annotateError(it) { it.instruction?.raw } }
            .filterNotNull()
            .filter { it.bitSize > 0 }

        var labels = this.lines map { it.label } filter { it != null }

        println("Instructions:")
        lines.forEach { println(it) }
        println()

        println("Raw Bytes:")
        instructionBytes.forEach { println(it) }
        println()

        println("Labels:")
        println(labels)
        println()

        return SizedByteArray.join(instructionBytes) // TODO do!
    }

    fun calculateOffsets(lines: List<Line>): List<Line> {
        var offset: Int = 0
        var offsetLines = arrayListOf<Line>()

        for (line in lines) {
            annotateError(line) {
                line.offset = SizedByteArray(offset.toByteArray(), 8)
                offsetLines.add(line)
                offset += line.size / 8 // Size is bit size not byte size.
            }
        }

        return offsetLines
    }

    companion object {
        fun bail(error: Exception) {
            System.err.println(error)
            System.exit(-1)
        }

        fun annotateError<T>(line: Line, function: (Line) -> T): T {
            try {
                return function(line)
            } catch (e: AssemblerError) {
                e.line = line
                throw e
            }
        }
    }
}
