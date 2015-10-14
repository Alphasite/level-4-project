package com.nishadmathur.assembler

import com.nishadmathur.errors.AssemblerError
import com.nishadmathur.errors.LineParseError
import com.nishadmathur.instructions.Instruction
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.references.MetaReferenceFactory
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import sun.tools.asm.Assembler
import java.io.BufferedInputStream
import java.io.File
import java.io.FileReader
import java.util.*
import java.util.stream.IntStream
import javax.inject.Inject

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 09:08
 */
class Assembler(var instructionFactory: MetaInstructionFactory) {
    var identifier: Int = 0
    val identifierTable = IdentifierTable()
    val lines = ArrayList<Line>()
    val word_size = 32

    fun loadFile(path: String) {
        Scanner(FileReader(path)).use {
            var stringLines: MutableList<String> = ArrayList()

            while (it.hasNextLine()) {
                stringLines.add(it.nextLine())
            }

            for (i in 0 until stringLines.size()) {
                if (stringLines[i].length() > 0) {
                    lines.add(Line(i, stringLines[i]))
                }
            }
        }

    }

    fun assemble(file: String): ByteArray {
        this.loadFile(file)
        this.lines.forEach { it.parseLine(instructionFactory = instructionFactory, labelTable = identifierTable) }

        var instructionBytes = this.lines map { it.instruction?.raw } filter { (it?.size() ?: 0) > 0 }
        var labels = this.lines map { it.label } filter { it != null }

        println("Instructions:")
        lines.forEach { println(it) }
        println()

        println("Raw Bytes:")
        instructionBytes.forEach { println(Arrays.toString(it)) }
        println()

        println("Labels:")
        println(labels)
        println()

        return ByteArray(0) // TODO do!
    }

    fun calculateOffsets(lines: List<Line>): List<Line> {
        var offset: Int = 0
        var offsetLines = arrayListOf<Line>()

        for (line in lines) {
            line.offset = intToByteArray(offset, word_size)
            offsetLines.add(line)
            offset += line.size
        }

        return offsetLines
    }
}
