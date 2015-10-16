package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.assembler.Label
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */

class Main {
    val referenceFactory = MetaReferenceFactory("All")
    val instructionFactory = MetaInstructionFactory()
    val identiferTable = IdentifierTable()

    init {
        referenceFactory.addReference(
            IndexedReferenceFactory("memory", "(\\d+|r\\d+|#\\w+)\\[(\\d+|r\\d+)\\]".toRegex(), referenceFactory),
            1
        )

        referenceFactory.addReference(
            MappedReferenceFactory("register", mapOf(
                Pair("r0", SizedByteArray(0.toByteArray(), 4)),
                Pair("r1", SizedByteArray(1.toByteArray(), 4)),
                Pair("r2", SizedByteArray(2.toByteArray(), 4)),
                Pair("r3", SizedByteArray(3.toByteArray(), 4)),
                Pair("r4", SizedByteArray(4.toByteArray(), 4)),
                Pair("r5", SizedByteArray(5.toByteArray(), 4)),
                Pair("r6", SizedByteArray(6.toByteArray(), 4)),
                Pair("r7", SizedByteArray(7.toByteArray(), 4))
            )),
            2
        )

        referenceFactory.addReference(
            LiteralReferenceFactory("number", LiteralType.INTEGER, 8, "\\d+".toRegex(), "(\\d+)".toRegex()),
            3
        )

        referenceFactory.addReference(
            LabelReferenceFactory("label", referenceFactory, identiferTable, 8, "^#\\w+$".toRegex(), "(?<=#)\\w+".toRegex()),
            3
        )


        instructionFactory.addInstruction(
            TypedInstructionFactory(
                "load",
                listOf(
                    Pair("value", referenceFactory["number"]!!),
                    Pair("destination", referenceFactory["register"]!!)
                ),
                SizedByteArray(1.toByteArray(), 8)
            )
        )

        instructionFactory.addInstruction(
            TypedInstructionFactory(
                "add",
                listOf(
                    Pair("lhs", referenceFactory["register"]!!),
                    Pair("rhs", referenceFactory["register"]!!),
                    Pair("destination", referenceFactory["register"]!!)
                ),
                SizedByteArray(2.toByteArray(), 8)
            )
        )
    }
}

fun main(args: Array<String>) {
    val main = Main()

    Assembler(instructionFactory = main.instructionFactory, identifierTable = main.identiferTable).assemble("test.asm")
}

