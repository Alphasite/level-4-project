package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.configuration.loadConfiguration
import com.nishadmathur.errors.IncorrectArgument
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Option
import org.apache.commons.cli.Options
import java.io.File
import java.io.FileReader
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:47
 */
fun main(args: Array<String>) {
    val parsedArgs = CommandLineArgs(args)

    if (parsedArgs.help) {
        parsedArgs.help
        return
    }

    val (configuration, instructionFactory) = loadConfiguration(FileReader(parsedArgs.config))

    val file2 = Assembler(
        instructionFactory = instructionFactory,
        identifierTable = configuration.labelTable,
        configuration = configuration
    ).assemble(Scanner(File(parsedArgs.input)), isTopLevel = true)


    if (parsedArgs.listings) {
        println("File:")
        println(file2.rightAlign().hex)
        println()
    }

    println("Config File:")
    println(configuration)
    println()
}

class CommandLineArgs(args: Array<String>) {
    val options: Options = Options()
    val parser = DefaultParser()

    val listings: Boolean
    val input: String?
    val output: String?
    val config: String
    val help: Boolean

    init {
        var option: Option

        option = Option("c", "config", true, "The configuration file which describes the assembly language.")
        option.isRequired = true
        options.addOption(option)

        option = Option("i", "input", true, "Sets the file from which the assembly language file is loaded.")
        options.addOption(option)

        option = Option("o", "output", true, "Sets the file to which the assembled output is written.")
        options.addOption(option)

        option = Option("l", "listings", false, "Generate an assembly listing instead of a binary file.")
        options.addOption(option)

        option = Option("h", "help", false, "Lists out the arguments.")
        options.addOption(option)

        val parsedArgs = parser.parse(options, args)

        help = parsedArgs.hasOption('h')
        listings = parsedArgs.hasOption('l')

        input = parsedArgs.getOptionValue('i')
        output = parsedArgs.getOptionValue('o')
        config = parsedArgs.getOptionValue('c') ?: throw IncorrectArgument("config", parsedArgs)
    }

    fun printHelp() = HelpFormatter().printHelp("ant", options)
}
