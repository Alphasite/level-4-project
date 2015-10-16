package com.nishadmathur.errors

import com.nishadmathur.assembler.Line

/**
 * User: nishad
 * Date: 16/10/2015
 * Time: 17:38
 */
open class ConfigurationError(message: String): Exception(message) {
    var line: Line? = null
}

class ValueParseError(message: String): ConfigurationError(message) {

}
