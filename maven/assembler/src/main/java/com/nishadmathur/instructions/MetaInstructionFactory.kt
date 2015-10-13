package com.nishadmathur.instructions

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:24
 */
class MetaInstructionFactory: InstructionFactory<Instruction> {
    val factories = arrayListOf<InstructionFactory<Instruction>>()

    override fun checkIsMatch(instruction: List<String>): Boolean {
        return factories.find { it.checkIsMatch(instruction) } != null
    }

    override fun getInstanceIfIsMatch(instruction: List<String>): Instruction? {
        return factories
                .first { it.checkIsMatch(instruction) }
                .getInstanceIfIsMatch(instruction)
    }

    fun addInstruction(referenceFactory: InstructionFactory<Instruction>) {
        this.factories.add(referenceFactory)
    }
}
