package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */

class Main {
    val referenceFactory = MetaReferenceFactory()
    val instructionFactory = MetaInstructionFactory()

    init {
        referenceFactory.addReference(
            IndexedReferenceFactory("memory", "(\\d+|r\\d+|#\\w+)\\[(\\d+|r\\d+)\\]".toRegex(), referenceFactory),
            1
        )

        referenceFactory.addReference(
            MappedReferenceFactory("register", mapOf(
                Pair("r0", byteArrayOf(0)),
                Pair("r1", byteArrayOf(1)),
                Pair("r2", byteArrayOf(2)),
                Pair("r3", byteArrayOf(3)),
                Pair("r4", byteArrayOf(4)),
                Pair("r5", byteArrayOf(5)),
                Pair("r6", byteArrayOf(6)),
                Pair("r7", byteArrayOf(7))
            )),
            2
        )

        referenceFactory.addReference(
            LiteralReferenceFactory("number", LiteralType.INTEGER, "\\d+".toRegex(), "(\\d+)".toRegex()),
            3
        )

        referenceFactory.addReference(
            LabelReferenceFactory("label", referenceFactory, "^#\\w+$".toRegex(), "(?<=#)\\w+".toRegex()),
            3
        )


        instructionFactory.addInstruction(
            TypedInstructionFactory(
                "load",
                listOf(
                    Pair("value", referenceFactory["number"]!!),
                    Pair("destination", referenceFactory["register"]!!)
                ),
                byteArrayOf(0.toByte()),
                8
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
                byteArrayOf(1.toByte()),
                8
            )
        )
    }
}

fun main(args: Array<String>) {
    val main = Main()

    Assembler(instructionFactory = main.instructionFactory).assemble("test.asm")
}

