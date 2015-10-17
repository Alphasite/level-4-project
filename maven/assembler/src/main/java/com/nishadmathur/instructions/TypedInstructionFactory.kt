package com.nishadmathur.instructions

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
class TypedInstructionFactory(override val identifier: String,
                              val argumentFactories: List<Pair<String, ReferenceFactory>>,
                              val rawLiteral: SizedByteArray): InstructionFactory, Serializable {

    override val help: String
        get() = identifier + " " + argumentFactories.map { argument -> "<${argument.first}:${argument.second.type}>"}.join(" ")

    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        return (name == identifier || ignoreIdentifier)
            && arguments.size() == argumentFactories.size()
    }

    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        return (0 until argumentFactories.size()) all { argumentFactories[it].second.checkIsMatch(instruction[it]) }
    }

    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (arguments.size() == argumentFactories.size()
            && checkIsMatch(name, arguments, ignoreIdentifier)
            && checkTypeSignatureIsMatch(arguments)) {

            val argumentReferences = (0 until argumentFactories.size()) map { i ->
                if (argumentFactories[i].second.checkIsMatch(arguments[i])) {
                    argumentFactories[i].second.getInstanceIfIsMatch(arguments[i])
                } else {
                    throw IncorrectTypeError("${argumentFactories[i].first} expects its arguments in the form $help")
                }
            }

            return TypedInstruction(argumentReferences, rawLiteral)
        } else {
            throw InstructionParseError("$identifier could not be parsed correctly, it should be in the form '$help'")
        }
    }
}
