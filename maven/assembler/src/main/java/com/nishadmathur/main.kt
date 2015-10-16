package com.nishadmathur

import com.esotericsoftware.yamlbeans.YamlReader
import com.esotericsoftware.yamlbeans.YamlWriter
import com.nishadmathur.assembler.Assembler
import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.assembler.Label
import com.nishadmathur.configuration.SerializablePair
import com.nishadmathur.configuration.SerializableTriple
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.FileReader
import java.io.FileWriter
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */

class Main {
    val referenceFactory = MetaReferenceFactory("All")
    val instructionFactory = MetaInstructionFactory()
    val identifierTable = IdentifierTable()

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
            LabelReferenceFactory(8, "label", identifierTable, "^#\\w+$".toRegex(), "(?<=#)\\w+".toRegex()),
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

    val reader = YamlReader(FileReader("test.yml"));
    reader.config.setClassTag("instruction", TypedInstructionFactory::class.javaClass)
    reader.config.setClassTag("instruction.polymorphic", TypePolymorphicInstructionFactory::class.javaClass)
    reader.config.setClassTag("instruction.meta", MetaInstructionFactory::class.javaClass)

    reader.config.setClassTag("reference.meta", MetaReferenceFactory::class.javaClass)
    reader.config.setClassTag("reference.indexed", IndexedReferenceFactory::class.javaClass)
    reader.config.setClassTag("reference.label", LabelReferenceFactory::class.javaClass)
    reader.config.setClassTag("reference.literal", LiteralReferenceFactory::class.javaClass)
    reader.config.setClassTag("reference.mapped", MappedReferenceFactory::class.javaClass)

    reader.config.setClassTag("pair", SerializablePair::class.javaClass)
    reader.config.setClassTag("tripe", SerializableTriple::class.javaClass)

    reader.read()

    Assembler(instructionFactory = main.instructionFactory, identifierTable = main.identifierTable).assemble("test.asm")
}

