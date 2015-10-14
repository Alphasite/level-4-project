package com.nishadmathur.instructions

import com.nishadmathur.errors.InstructionParseError
import sun.tools.jstat.Identifier

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:24
 */
class MetaInstructionFactory: InstructionFactory<Instruction> {
    override val identifier: String
        get() = throw UnsupportedOperationException()

    override val help: String
        get() = throw UnsupportedOperationException()

    val factories = arrayListOf<InstructionFactory<Instruction>>()

    override fun checkIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Boolean {
        return factories.find { it.checkIsMatch(instruction, ignoreIdentifier = ignoreIdentifier) } != null
    }

    override fun getInstanceIfIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (checkIsMatch(instruction, ignoreIdentifier)) {
            return factories
                .first { it.checkIsMatch(instruction) }
                .getInstanceIfIsMatch(instruction)
        } else {
            throw InstructionParseError("Error parsing instruction '$instruction', it doesnt appear to match any known instructions.")
        }
    }

    fun addInstruction(referenceFactory: InstructionFactory<Instruction>) {
        this.factories.add(referenceFactory)
    }
}
