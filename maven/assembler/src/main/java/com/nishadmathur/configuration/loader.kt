package com.nishadmathur.configuration

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.errors.IncompleteDeclarationParserError
import com.nishadmathur.errors.MalformedDeclaration
import com.nishadmathur.errors.MissingOrMalformedSection
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.instructions.InstructionFactory
import com.nishadmathur.instructions.MetaInstructionFactory
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import org.yaml.snakeyaml.Yaml
import java.io.Reader
import java.util.*
import kotlin.text.Regex

/**
 * User: nishad
 * Date: 14/10/2015
 * Time: 21:57
 */
fun loadConfiguration(file: Reader): Pair<Configuration, InstructionFactory> {
    val yaml = Yaml();
    val config = yaml.load(file)

    var configuration: Configuration?
    var instructionFactories: InstructionFactory?
    var referenceFactories: Map<String, ReferenceFactory>?

    when (config) {
        is Map<*, *> -> {
            val configurationMap = config.getRaw("configuration") as? Map<*, *>
                    ?: throw IncompleteDeclarationParserError("Configuration is missing or malformed.")

            configuration = parseConfiguration(configurationMap)

            val referenceMap = config.getRaw("references") as? List<*>
                    ?: throw IncompleteDeclarationParserError("Reference declaration is missing or malformed.")

            referenceFactories = parseReference(referenceMap, configuration)

            val instructionMap = config.getRaw("instructions") as? List<*>
                    ?: throw IncompleteDeclarationParserError("Instruction declaration is incomplete or malformed.")

            instructionFactories = parseInstructions(instructionMap, referenceFactories, configuration)

        }

        else ->
            throw MalformedDeclaration("The top level declaration is malformed; it should be a map.")
    }

    configuration ?: throw MissingOrMalformedSection("Configuration is missing or malformed")
    instructionFactories ?: throw MissingOrMalformedSection("Instruction factory is missing or malformed")

    return Pair(configuration, instructionFactories)
}

class Configuration(val identifierBitSize: Int,
                    val argumentSeparator: Regex,
                    val labelRegex: Regex,
                    val commentRegex: Regex,
                    val wordSizeBits: Int) {
    val labelTable: IdentifierTable = IdentifierTable(identifierBitSize)
}

fun parseConfiguration(config: Map<*, *>): Configuration {
    val bitSize = config.getRaw("label bit size") as? Int
            ?: throw InvalidOption("label bit size", config)

    val wordSize = config.getRaw("word size") as? Int
            ?: throw InvalidOption("word size", config)

    val argumentSeparator = (config.getRaw("argument separator") as? String ?: " ").toRegex()

    val labelRegex = (config.getRaw("label regex") as? String)?.toRegex()
            ?: throw InvalidOption("label regex", config)

    val commentRegex = (config.getRaw("comment regex") as? String)?.toRegex()
            ?: throw InvalidOption("comment regex", config)

    val configuration = Configuration(bitSize, argumentSeparator, labelRegex, commentRegex, wordSize)

    return configuration
}

fun parseReference(config: List<*>, configuration: Configuration): Map<String, ReferenceFactory> {
    val references = HashMap<String, ReferenceFactory>()

    for (value in config) {
        val map = value as? Map<*, *> ?: throw MalformedDeclaration("Every child of References should be a map.")
        val name = map.getRaw("name") as? String ?: throw MalformedDeclaration("Field name must be a non-null string.")
        val kind = map.getRaw("kind") as? String ?: throw MalformedDeclaration("Field kind must be a non-null string.")

        when (kind) {
            "meta"-> references.putAll(MetaReferenceFactory.parse(map, references, configuration))
            "indexed"-> references[name] = IndexedReferenceFactory.parse(map, references, configuration)
            "label"-> references[name] = LabelReferenceFactory.parse(map, references, configuration)
            "literal"-> references[name] = LiteralReferenceFactory.parse(map, references, configuration)
            "mapped"-> references[name] = MappedReferenceFactory.parse(map, references, configuration)
            else -> throw InvalidOption("kind", kind.toString())
        }
    }

    return references
}

fun parseInstructions(config: List<*>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): InstructionFactory {
    val instructions = MetaInstructionFactory()

    for (value in config) {
        val map = value as? Map<*, *> ?: throw MalformedDeclaration("Every child of Instructions should be a map.")
        val kind = map.getRaw("kind") as? String ?: "instruction"

        val instruction = when (kind) {
            "meta" -> TypePolymorphicInstructionFactory.parse(map, referenceFactories, configuration)
            "instruction" -> TypedInstructionFactory.parse(map, referenceFactories, configuration)
            else -> throw InvalidOption("kind", kind.toString())
        }

        instructions.addInstruction(instruction)
    }

    return instructions
}

