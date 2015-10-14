package com.nishadmathur.instructions

import com.nishadmathur.assembler.join
import com.nishadmathur.references.Reference
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:08
 */
class TypedInstruction(override val arguments: List<Reference>,
                       val rawLiteral: ByteArray,
                       val instructionIdentifierWordSize: Int): Instruction {

    override val raw: ByteArray
        get() {
            val rawArguments = join(arguments.map { argument -> argument.raw })
            val destinationArray = ByteArray(this.rawLiteral.size() + rawArguments.size())

            System.arraycopy(this.rawLiteral, 0, destinationArray, 0, this.rawLiteral.size())
            System.arraycopy(rawArguments, 0, destinationArray, this.rawLiteral.size(), rawArguments.size())

            return destinationArray
        }

    override val size: Int
        get() = instructionIdentifierWordSize + arguments.map { argument -> argument.size }.sum()
}
