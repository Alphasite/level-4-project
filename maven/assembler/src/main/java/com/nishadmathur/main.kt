package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */
fun configure(): Pair<InstructionFactory, IdentifierTable> {

    val addressWidth = 8
    val referenceFactory = MetaReferenceFactory("All")
    val instructionFactory = MetaInstructionFactory()
    val identifierTable = IdentifierTable(addressWidth)

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
        LabelReferenceFactory("label", identifierTable, 8, "^#\\w+$".toRegex(), "(?<=#)\\w+".toRegex()),
        4
    )

    referenceFactory.addReference(
        IndexedReferenceFactory(
            "memory",
            "(.*?)\\[(.*?)\\]".toRegex(),
            listOf(referenceFactory["label"]!!),
            listOf(referenceFactory["register"]!!, referenceFactory["number"]!!)
        ),
        1
    )

    instructionFactory.addInstruction(
        TypePolymorphicInstructionFactory("load",
            listOf(
                TypedInstructionFactory(
                    "loadr",
                    listOf(
                        Pair("value", referenceFactory["register"]!!),
                        Pair("destination", referenceFactory["register"]!!)
                    ),
                    SizedByteArray(1.toByteArray(), 8)
                ),
                TypedInstructionFactory(
                    "loadl",
                    listOf(
                        Pair("value", referenceFactory["number"]!!),
                        Pair("destination", referenceFactory["register"]!!)
                    ),
                    SizedByteArray(2.toByteArray(), 8)
                )
            )
        )
    )

    instructionFactory.addInstruction(
        TypedInstructionFactory(
            "jump",
            listOf(
                Pair("destination", referenceFactory["memory"]!!)
            ),
            SizedByteArray(3.toByteArray(), 8)
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
            SizedByteArray(4.toByteArray(), 8)
        )
    )

    return Pair(instructionFactory, identifierTable)
}

fun main(args: Array<String>) {
    val (instructionFactory, identifierTable) = configure()

    Assembler(
        instructionFactory = instructionFactory,
        identifierTable = identifierTable
    ).assemble("test.asm")
}

