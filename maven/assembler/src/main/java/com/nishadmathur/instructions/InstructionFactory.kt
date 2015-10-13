package com.nishadmathur.instructions

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:24
 */
interface InstructionFactory<T: Instruction> {
    fun checkIsMatch(instruction: List<String>, ignoreIdentifier: Boolean = false): Boolean
    fun getInstanceIfIsMatch(instruction: List<String>, ignoreIdentifier: Boolean = false): T
    val identifier: String
    val help: String
}
