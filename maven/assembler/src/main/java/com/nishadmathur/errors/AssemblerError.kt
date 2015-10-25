package com.nishadmathur.errors

import com.nishadmathur.assembler.Line

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:10
 */

open class AssemblerError(message: String): Exception(message) {
    public var line: Line? = null

    override fun toString(): String {
        return "An error occurred while parsing line: \n\t"+
            "${line?.lineNumber} '${line?.line}' \n\n" +
            "Producing the error:\n\t" +
            "${super.message}\n\n" +
            super.toString() +
            "\n"
    }
}

class InstructionParseError(message: String): AssemblerError(message)
class DataSourceParseError(message: String): AssemblerError(message)
class LineParseError(message: String): AssemblerError(message)
class UndeclaredLabelError(message: String): AssemblerError(message)
class IncorrectTypeError(message: String): AssemblerError(message)
class AbstractInstructionInstantiationError(message: String): AssemblerError(message)
