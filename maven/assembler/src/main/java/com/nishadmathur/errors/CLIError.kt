package com.nishadmathur.errors

import org.apache.commons.cli.CommandLine

/**
 * User: nishad
 * Date: 25/02/2016
 * Time: 13:32
 */

open class CLIError(message: String?) : Exception(message)

class IncorrectArgument(option: String, arguments: CommandLine): CLIError("Warning option $option with value '${arguments.getOptionValue(option)}' is invalid. try --help for details.")
