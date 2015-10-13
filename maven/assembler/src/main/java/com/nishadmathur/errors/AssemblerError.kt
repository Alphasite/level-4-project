package com.nishadmathur.errors

import com.nishadmathur.assembler.Line

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 12:10
 */

open class AssemblerError(message: String): Exception(message) {
    var line: Line? = null
}

class InstructionParseError(message: String): AssemblerError(message)
class DataSourceParseError(message: String): AssemblerError(message)
class LineParseError(message: String): AssemblerError(message)
class UndeclaredLabelError(message: String): AssemblerError(message)
class IncorrectTypeError(message: String): AssemblerError(message)
class AbstractInstructionInstantiationError(message: String): AssemblerError(message)
