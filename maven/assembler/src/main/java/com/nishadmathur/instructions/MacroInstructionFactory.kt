package com.nishadmathur.instructions

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import java.util.*

/**
 * User: nishad
 * Date: 01/02/2016
 * Time: 20:20
 */
class MacroInstructionFactory(
    override val identifier: String,
    val argumentFactories: List<Pair<String, ReferenceFactory>>,
    val argumentAliases: Map<String, String>,
    val instructionFactory: InstructionFactory,
    val configuration: Configuration,
    val macroTemplate: String
) : InstructionFactory {

    override val help: String
        get() = identifier + " " + argumentFactories.map { argument -> "<${argument.first}:${argument.second.type}>" }.joinToString(" ")

    override val factoryMap: Map<String, InstructionFactory>
        get() = hashMapOf(Pair(identifier, this))

    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        return (name == identifier || ignoreIdentifier)
            && arguments.size == argumentFactories.size
    }

    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        return (0 until argumentFactories.size).all { argumentFactories[it].second.checkIsMatch(instruction[it]) }
    }

    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (arguments.size == argumentFactories.size
            && checkIsMatch(name, arguments, ignoreIdentifier)
            && checkTypeSignatureIsMatch(arguments)) {

            val configuration = configuration.nestedConfiguration

            val assembler = Assembler(configuration, instructionFactory, configuration.labelTable)

            val argumentReferences = (0 until argumentFactories.size).map { i ->
                if (argumentFactories[i].second.checkIsMatch(arguments[i])) {
                    val reference = argumentFactories[i].second.getInstanceIfIsMatch(arguments[i])
                    Pair(argumentFactories[i].first, reference)
                } else {
                    throw IncorrectTypeError("For Instruction '$name' argument '${argumentFactories[i].first}' expects its arguments in the form $help")
                }
            }.toMap()

            val aliases = argumentAliases.map {
                Pair(
                    it.value, argumentReferences[it.key] ?: throw InvalidOption(it.key, argumentReferences)
                )
            }

            val argumentReferencesAndAliases = HashMap<String, Reference>()
            argumentReferencesAndAliases.putAll(argumentReferences)
            argumentReferencesAndAliases.putAll(aliases)

            val argumentsMap = HashMap<String, String>()
            (0 until argumentFactories.size).forEach { argumentsMap[argumentFactories[it].first] = arguments[it] }
            argumentAliases.forEach {
                argumentsMap[it.key] = argumentsMap[it.value] ?:
                    throw IncorrectTypeError("Alias ${it.key} to ${it.value} for '$name' is not correctly defined.")
            }

            return MacroInstruction(argumentReferencesAndAliases, argumentsMap, assembler, macroTemplate)
        } else {
            throw InstructionParseError("$identifier with arguments '${arguments.joinToString("', '")}' could not be parsed correctly, it should be in the form '$help'")
        }
    }

    companion object: InstructionParser {
        override fun parse(
            properties: Map<*, *>,
            referenceFactories: Map<String, ReferenceFactory>,
            instructionFormats: Map<String, InstructionFormat>,
            rootInstructionFactory: InstructionFactory,
            configuration: Configuration
        ): InstructionFactory {

            val name = properties["name"] as? String
                ?: throw InvalidOption("name", properties)

            val template = properties["template"] as? String
                ?: throw InvalidOption("template", properties)

            val rawArguments = properties["arguments"] as? Map<*, *>
                ?: if (properties["arguments"] == null) { HashMap<Any, Any>() } else { null }
                ?: throw InvalidOption("arguments", properties["arguments"])

            val rawAliases = (properties["aliases"] as? Map<*, *>)

            val aliases: Map<String, String> = rawAliases?.map {
                Pair(
                    it.key as? String ?: throw InvalidOption(it.key.toString(), rawAliases),
                    it.value as? String ?: throw InvalidOption(it.key.toString(), rawAliases)
                )
            }?.toMap() ?: mapOf()

            val arguments: List<Pair<String, ReferenceFactory>> = rawArguments
                .map {
                    val referenceName = it.key as? String ?: throw InvalidOption("arguments.name", it)
                    val referenceKind = it.value as? String ?: throw InvalidOption("arguments.name", it)
                    val factory = referenceFactories[referenceKind] ?: throw InvalidOption("arguments.type", it)
                    Pair(referenceName, factory)
                }

            return MacroInstructionFactory(
                identifier = name,
                instructionFactory = rootInstructionFactory,
                argumentFactories = arguments,
                argumentAliases = aliases,
                configuration = configuration,
                macroTemplate = template
            )
        }

    }
}
