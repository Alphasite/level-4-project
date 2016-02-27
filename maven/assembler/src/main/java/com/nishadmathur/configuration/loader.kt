package com.nishadmathur.configuration

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.errors.IncompleteDeclarationParserError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.errors.MalformedDeclaration
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.references.*
import org.yaml.snakeyaml.Yaml
import java.io.Reader
import java.util.*

/**
 * User: nishad
 * Date: 14/10/2015
 * Time: 21:57
 */
fun loadConfiguration(file: Reader): Pair<Configuration, InstructionFactory> {
    val yaml = Yaml()
    val config = yaml.load(file)

    var configuration: Configuration
    var instructionFactories: InstructionFactory
    var referenceFactories: Map<String, ReferenceFactory>
    var instructionFormats: Map<String, InstructionFormat>

    when (config) {
        is Map<*, *> -> {
            val configurationMap = config["configuration"] as? Map<*, *>
                ?: throw IncompleteDeclarationParserError("Configuration is missing or malformed.")

            configuration = parseConfiguration(configurationMap)

            val referenceMap = config["references"] as? List<*>
                ?: throw IncompleteDeclarationParserError("Reference declaration is missing or malformed.")

            val rawInstructionFormats = (config["instruction formats"] as? Map<*, *>)

            if (rawInstructionFormats != null) {
                instructionFormats = InstructionFormat.parseInstructionFormats(rawInstructionFormats)
            } else {
                instructionFormats = mapOf()
            }

            referenceFactories = parseReference(referenceMap, configuration)

            val instructionMap = config.get<Any?, Any?>("instructions" as Any?) as? List<*>
                ?: throw IncompleteDeclarationParserError("Instruction declaration is incomplete or malformed.")

            instructionFactories = parseInstructions(
                instructionMap,
                referenceFactories,
                instructionFormats,
                configuration
            )

        }

        else ->
            throw MalformedDeclaration("The top level declaration is malformed; it should be a map.")
    }

    return Pair(configuration, instructionFactories)
}

class Configuration(val identifierBitSize: Long,
                    val argumentSeparator: Regex,
                    val labelRegex: Regex,
                    val commentRegex: Regex,
                    val wordSizeBits: Int,
                    val smallSegmentSize: Int?,
                    val largeSegmentSize: Int?,
                    val labelTable: IdentifierTable = IdentifierTable(identifierBitSize)
) {

    val nestedConfiguration: Configuration
        get() = Configuration(
            identifierBitSize,
            argumentSeparator,
            labelRegex,
            commentRegex,
            wordSizeBits,
            smallSegmentSize,
            largeSegmentSize,
            labelTable.childTable
        )
}

fun parseConfiguration(config: Map<*, *>): Configuration {
    val bitSize = config["label bit size"] as? Int
        ?: throw InvalidOption("label bit size", config)

    val wordSize = config["word size"] as? Int
        ?: throw InvalidOption("word size", config)

    val argumentSeparator = (config["argument separator"] as? String ?: " ").toRegex()

    val labelRegex = (config["label regex"] as? String)?.toRegex()
        ?: throw InvalidOption("label regex", config)

    val commentRegex = (config["comment regex"] as? String)?.toRegex()
        ?: throw InvalidOption("comment regex", config)

    val (smallSegmentSize, largeSegmentSize) = if ("endian change" in config) {
        val endianSwitch = (config["endian change"] as? Map<*, *>)?.map {
            Pair(
                it.key as? String ?: throw InvalidOption(it.key.toString(), config["endian change"] as Map<*, *>),
                it.value as? Int ?: throw InvalidOption(it.key.toString(), config["endian change"] as Map<*, *>)
            )
        }?.toMap() ?: throw InvalidOption("endian change", config)

        Pair(
            endianSwitch["small segment size"] ?: throw InvalidOption("small segment size", endianSwitch),
            endianSwitch["large segment size"] ?: throw InvalidOption("large segment size", endianSwitch)
        )
    } else {
        Pair(null, null)
    }

    val configuration = Configuration(
        bitSize.toLong(),
        argumentSeparator,
        labelRegex,
        commentRegex,
        wordSize,
        smallSegmentSize,
        largeSegmentSize
    )

    return configuration
}

fun parseReference(config: List<*>, configuration: Configuration): Map<String, ReferenceFactory> {
    val references = HashMap<String, ReferenceFactory>()

    for (value in config) {
        val map = value as? Map<*, *> ?: throw MalformedDeclaration("Every child of References should be a map.")
        val name = map["name"] as? String ?: throw MalformedDeclaration("Field name must be a non-null string.")
        val kind = map["kind"] as? String ?: throw MalformedDeclaration("Field kind must be a non-null string.")

        when (kind) {
            "meta" -> references.putAll(MetaReferenceFactory.parse(map, references, configuration))
            "indexed" -> references[name] = IndexedReferenceFactory.parse(map, references, configuration)
            "label" -> references[name] = LabelReferenceFactory.parse(map, references, configuration)
            "literal" -> references[name] = LiteralReferenceFactory.parse(map, references, configuration)
            "mapped" -> references[name] = MappedReferenceFactory.parse(map, references, configuration)
            else -> throw InvalidOption("kind", kind.toString())
        }
    }

    return references
}

fun parseInstructions(
    config: List<*>,
    referenceFactories: Map<String, ReferenceFactory>,
    instructionFormats: Map<String, InstructionFormat>,
    configuration: Configuration
): InstructionFactory {
    val instructions = MetaInstructionFactory()

    for (value in config) {
        val map = value as? Map<*, *> ?: throw MalformedDeclaration("Every child of Instructions should be a map.")
        val kind = map["kind"] as? String ?: "instruction"

        val instruction = when (kind) {
            "meta" -> TypePolymorphicInstructionFactory.parse(map, referenceFactories, instructionFormats, configuration)
            "instruction" -> TypedInstructionFactory.parse(map, referenceFactories, instructionFormats, configuration)
            else -> throw InvalidOption("kind", kind.toString())
        }

        instructions.addInstruction(instruction)
    }

    return instructions
}

