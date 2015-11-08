package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.configuration.Configuration
import com.nishadmathur.configuration.loadConfiguration
import com.nishadmathur.configuration.parseConfiguration
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.net.URI
import java.net.URL

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */
//fun configure(): Triple<InstructionFactory, IdentifierTable, Configuration> {
//
//    val addressWidth = 8
//    val referenceFactory = MetaReferenceFactory("All")
//    val instructionFactory = MetaInstructionFactory()
//    val identifierTable = IdentifierTable(addressWidth)
//
//    referenceFactory.addReference(
//            MappedReferenceFactory("register", mapOf(
//                    Pair("r0", SizedByteArray(0.toByteArray(), 4)),
//                    Pair("r1", SizedByteArray(1.toByteArray(), 4)),
//                    Pair("r2", SizedByteArray(2.toByteArray(), 4)),
//                    Pair("r3", SizedByteArray(3.toByteArray(), 4)),
//                    Pair("r4", SizedByteArray(4.toByteArray(), 4)),
//                    Pair("r5", SizedByteArray(5.toByteArray(), 4)),
//                    Pair("r6", SizedByteArray(6.toByteArray(), 4)),
//                    Pair("r7", SizedByteArray(7.toByteArray(), 4))
//            )),
//            2
//    )
//
//    referenceFactory.addReference(
//            LiteralReferenceFactory("number", LiteralType.INTEGER, 8, "\\d+".toRegex(), "(\\d+)".toRegex()),
//            3
//    )
//
//    referenceFactory.addReference(
//            LabelReferenceFactory("label", identifierTable, 8, "^#\\w+$".toRegex(), "(?<=#)\\w+".toRegex()),
//            4
//    )
//
//    referenceFactory.addReference(
//            IndexedReferenceFactory(
//                    "memory",
//                    "(.*?)\\[(.*?)\\]".toRegex(),
//                    sourceFirst = true,
//                    validLeftSideReferenceTypes = listOf(referenceFactory["label"]!!),
//                    validRightSideReferenceStrings = listOf(referenceFactory["register"]!!, referenceFactory["number"]!!)
//            ),
//            1
//    )
//
//    instructionFactory.addInstruction(
//            TypePolymorphicInstructionFactory("load",
//                    listOf(
//                            TypedInstructionFactory(
//                                    "loadr",
//                                    listOf(
//                                            Pair("value", referenceFactory["register"]!!),
//                                            Pair("destination", referenceFactory["register"]!!)
//                                    ),
//                                    SizedByteArray(1.toByteArray(), 8),
//                                    0
//                            ),
//                            TypedInstructionFactory(
//                                    "loadl",
//                                    listOf(
//                                            Pair("value", referenceFactory["number"]!!),
//                                            Pair("destination", referenceFactory["register"]!!)
//                                    ),
//                                    SizedByteArray(2.toByteArray(), 8),
//                                    0
//                            )
//                    )
//            )
//    )
//
//    instructionFactory.addInstruction(
//            TypedInstructionFactory(
//                    "jump",
//                    listOf(
//                            Pair("destination", referenceFactory["memory"]!!)
//                    ),
//                    SizedByteArray(3.toByteArray(), 8),
//                    0
//            )
//    )
//
//    instructionFactory.addInstruction(
//            TypedInstructionFactory(
//                    "add",
//                    listOf(
//                            Pair("lhs", referenceFactory["register"]!!),
//                            Pair("rhs", referenceFactory["register"]!!),
//                            Pair("destination", referenceFactory["register"]!!)
//                    ),
//                    SizedByteArray(4.toByteArray(), 8),
//                    0
//            )
//    )
//
//    val configuration = Configuration(8, " ".toRegex(), "(\\w+):".toRegex())
//
//    return Triple(instructionFactory, identifierTable, configuration)
//}

fun main(args: Array<String>) {
//    val (instructionFactory, identifierTable, configuration) = configure()
//
//    val file1 = Assembler(
//            instructionFactory = instructionFactory,
//            identifierTable = identifierTable,
//            configuration = configuration
//    ).assemble("test.asm")
//
//
//    println("File:")
//    println(file1.rightAlign())
//    println()
//
//    println("Config File:")
//    println(configuration)
//    println()

    val (configuration, instructionFactory) = loadConfiguration(FileReader(args[0]))

    val file2 = Assembler(
            instructionFactory = instructionFactory,
            identifierTable = configuration.labelTable,
            configuration = configuration
    ).assemble(args[1])

    println("File:")
    println(file2.rightAlign())
    println()

    println("Config File:")
    println(configuration)
    println()
}
