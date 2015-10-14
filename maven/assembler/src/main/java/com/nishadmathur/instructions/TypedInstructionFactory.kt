package com.nishadmathur.instructions

import com.nishadmathur.assembler.enumerate
import com.nishadmathur.assembler.join
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:51
 */
class TypedInstructionFactory(override val identifier: String,
                              val arguments: List<Pair<String, ReferenceFactory>>,
                              val rawLiteral: ByteArray,
                              val instructionIdentifierWordSize: Int): InstructionFactory<Instruction> {

    override val help: String
        get() = identifier + " " + arguments.map { argument -> "<${argument.first}:${argument.second.type}>"}.join(" ")

    override fun checkIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Boolean {
        return (instruction[0] == identifier || ignoreIdentifier) && instruction.size() == arguments.size() + 1
    }

    override fun getInstanceIfIsMatch(instruction: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (instruction.size() == arguments.size() + 1 && (checkIsMatch(instruction) || !ignoreIdentifier)) {
            val argumentReferences = (0 until arguments.size()) map { i ->
                if (arguments[i].second.checkIsMatch(instruction[i + 1])) {
                    arguments[i].second.getInstanceIfIsMatch(instruction[i + 1])
                } else {
                    throw IncorrectTypeError("${arguments[i].first} expects its arguments in the form $help")
                }
            }

            return TypedInstruction(argumentReferences, rawLiteral, instructionIdentifierWordSize)
        } else {
            throw InstructionParseError("$identifier could not be parsed correctly, it should be in the form '$help'")
        }
    }
}
