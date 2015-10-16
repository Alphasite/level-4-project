package com.nishadmathur.instructions

import com.nishadmathur.configuration.SerializablePair
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import com.nishadmathur.util.SizedByteArray
import java.io.Serializable

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:51
 */
class TypedInstructionFactory: InstructionFactory<Instruction>, Serializable {

    lateinit override var identifier: String
    lateinit var arguments: List<SerializablePair<String, ReferenceFactory>>
    lateinit var rawLiteral: SizedByteArray

    constructor(identifier: String, arguments: List<SerializablePair<String, ReferenceFactory>>, rawLiteral: SizedByteArray) {
        this.identifier = identifier
        this.arguments = arguments
        this.rawLiteral = rawLiteral
    }

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

            return TypedInstruction(argumentReferences, rawLiteral)
        } else {
            throw InstructionParseError("$identifier could not be parsed correctly, it should be in the form '$help'")
        }
    }
}
