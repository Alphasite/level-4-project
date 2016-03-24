package com.nishadmathur.instructions

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.AbstractInstructionInstantiationError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.references.ReferenceFactory
import java.io.Serializable

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:37
 */
class TypeOverloadedInstructionFactory(
    override val identifier: String,
    val factories: Collection<InstructionFactory>
) : InstructionFactory, Serializable {

    override val help: String
        get() = throw UnsupportedOperationException()

    override val factoryMap: Map<String, InstructionFactory>
        get() = hashMapOf(
            Pair(identifier, this),
            *factories
                .map { it.factoryMap.entries }
                .flatten()
                .map { Pair(it.key, it.value) }
                .toTypedArray()
        )

    val possibleIdentifiers: Set<String>
        get() {
            val identifiers = hashSetOf(identifier)
            identifiers.addAll(factories.map { it.identifier })
            return identifiers
        }

    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        if (name in possibleIdentifiers) {
            return factories.any { it.checkIsMatch(name, arguments, ignoreIdentifier = true) }
        } else {
            return false
        }
    }

    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        return factories.any { it.checkTypeSignatureIsMatch(instruction) }
    }

    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if ((checkIsMatch(name, arguments) || ignoreIdentifier) && checkTypeSignatureIsMatch(arguments)) {
            return factories
                .first { it.checkIsMatch(name, arguments, ignoreIdentifier = true) && it.checkTypeSignatureIsMatch(arguments) }
                .getInstanceIfIsMatch(name, arguments, ignoreIdentifier = true)
        } else {
            throw AbstractInstructionInstantiationError(
                factories.map { it.help }.joinToString(
                    separator = "\n\t",
                    prefix = "The polymorphic instruction $identifier should match the form of one of following:\n\t",
                    postfix = "\n\tNot $identifier <${arguments.joinToString("> <")}>\n")
            )
        }
    }

    companion object : InstructionParser {
        override fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, instructionFormats: Map<String, InstructionFormat>, configuration: Configuration): InstructionFactory {
            val name = properties["name"] as? String
                ?: throw InvalidOption("name", properties)

            val instructions = (properties["instructions"] as? List<*>)
                ?.map { it as? Map<*, *> }
                ?.requireNoNulls()
                ?: throw InvalidOption("instructions", properties)

            val instructionFactories = instructions.map { TypedInstructionFactory.parse(it, referenceFactories, instructionFormats, configuration) }

            return TypeOverloadedInstructionFactory(name, instructionFactories)
        }
    }
}
