package com.nishadmathur.instructions

import com.nishadmathur.errors.AbstractInstructionInstantiationError
import com.nishadmathur.errors.InstructionParseError
import java.io.Serializable

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:37
 */
class TypePolymorphicInstructionFactory(override val identifier: String,
                                        val factories: Collection<InstructionFactory>) : InstructionFactory, Serializable {

    override val help: String
        get() = throw UnsupportedOperationException()

    val possibleIdentifiers: Set<String>
        get() {
            val identifiers = hashSetOf(identifier)
            identifiers.addAll(factories.map { it.identifier })
            return identifiers
        }

    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        if (name in possibleIdentifiers) {
            return factories any { it.checkIsMatch(name, arguments, ignoreIdentifier = true) }
        } else {
            return false
        }
    }

    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        return factories any { it.checkTypeSignatureIsMatch(instruction) }
    }

    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if ((checkIsMatch(name, arguments) || ignoreIdentifier) && checkTypeSignatureIsMatch(arguments)) {
            return factories
                .first { it.checkIsMatch(name, arguments, ignoreIdentifier = true) && it.checkTypeSignatureIsMatch(arguments)}
                .getInstanceIfIsMatch(name, arguments, ignoreIdentifier = true)
        } else {
            throw AbstractInstructionInstantiationError(
                factories.map { it.help }.join(
                    separator = "\n\t",
                    postfix = "\n\tNot $identifier <${arguments.join("> <")}>\n",
                    prefix = "The polymorphic instruction $identifier should match the form of one of following:\n\t"
                )
            )
        }
    }
}
