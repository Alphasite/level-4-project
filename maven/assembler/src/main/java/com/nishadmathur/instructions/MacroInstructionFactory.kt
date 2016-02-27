package com.nishadmathur.instructions

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.errors.InvalidOption
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
}
