package com.nishadmathur.instructions

import com.nishadmathur.errors.InstructionParseError

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:24
 */
class MetaInstructionFactory: InstructionFactory {
    override val factoryMap: Map<String, InstructionFactory>
        get() = factories.map { it.factoryMap.entries.map { it.toPair() } }.flatten().toMap()

    val factories = arrayListOf<InstructionFactory>()

    override val identifier: String
        get() = throw UnsupportedOperationException()

    override val help: String
        get() = throw UnsupportedOperationException()

    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        return factories.find { it.checkIsMatch(name, arguments, ignoreIdentifier = ignoreIdentifier) } != null
    }

    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        throw UnsupportedOperationException(
            "The meta instruction factory does not support type checking, but i could be convinced."
        )
    }

    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (checkIsMatch(name, arguments, ignoreIdentifier)) {
            return factories
                .first { it.checkIsMatch(name, arguments) }
                .getInstanceIfIsMatch(name, arguments)
        } else {
            throw InstructionParseError("Error parsing instruction '$name' with arguments: '$arguments', it doesnt appear to match any known instructions.")
        }
    }

    fun addInstruction(referenceFactory: InstructionFactory) {
        this.factories.add(referenceFactory)
    }
}
