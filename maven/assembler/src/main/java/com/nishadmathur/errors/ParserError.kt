package com.nishadmathur.errors

/**
 * User: nishad
 * Date: 24/10/2015
 * Time: 15:08
 */
open class ParserError(message: String) : Exception(message) {

    override fun toString(): String {
        return "An error occurred while parsing the configuration; producing the error:\n\t" +
            "${super.message}\n\n" +
            super.toString() +
            "\n"
    }
}

class IncompleteDeclarationParserError(message: String) : ParserError(message)
class ConfigurationParseError(message: String) : ParserError(message)
class MalformedDeclaration(message: String) : ParserError(message)
class MissingOrMalformedSection(message: String) : ParserError(message)
class InvalidOption(field: String, option: Any?) : ParserError("'$option' is not a valid option for $field or is the wrong type.") {
    constructor(field: String, map: Map<*, *>) : this(field, map[field])
}
