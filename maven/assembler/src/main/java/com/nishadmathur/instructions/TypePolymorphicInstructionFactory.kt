package com.nishadmathur.instructions

import com.nishadmathur.errors.AbstractInstructionInstantiationError
import com.nishadmathur.errors.InstructionParseError

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:37
 */
class TypePolymorphicInstructionFactory(override val identifier: String,
                                        val factories: Collection<InstructionFactory<Instruction>>) : InstructionFactory<Instruction> {
    override val help: String
        get() = throw UnsupportedOperationException()

    override fun checkIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Boolean {
        if (instruction.size() > 0 && instruction[0] == identifier) {
            return factories.any { it.checkIsMatch(instruction, ignoreIdentifier = true) }
        } else {
            return false
        }
    }

    override fun getInstanceIfIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (checkIsMatch(instruction) || ignoreIdentifier) {
            return factories
                .first { it.checkIsMatch(instruction, ignoreIdentifier = true) }
                .getInstanceIfIsMatch(instruction)
        } else {
            throw AbstractInstructionInstantiationError(
                factories.map { it.help }.join(
                    separator = "\n\t",
                    postfix = "\n",
                    prefix = "The polymorphic instruction $identifier should match the form of one of following:\n\t"
                )
            )
        }
    }
}
