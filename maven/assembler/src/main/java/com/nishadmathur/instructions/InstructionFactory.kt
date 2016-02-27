package com.nishadmathur.instructions

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:24
 */
interface InstructionFactory {
    fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean = false): Boolean
    fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean
    fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean = false): Instruction
    val identifier: String
    val help: String

    val factoryMap: Map<String, InstructionFactory>
}


