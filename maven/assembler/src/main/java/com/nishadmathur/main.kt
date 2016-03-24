package com.nishadmathur

import com.nishadmathur.assembler.Assembler
import com.nishadmathur.configuration.loadConfiguration
import com.nishadmathur.errors.IncorrectArgument
import org.apache.commons.cli.*
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
        val formatter = HelpFormatter()
        formatter.printHelp( "ant", parsedArgs.options)
        return
    }

    if (parsedArgs.config == null) {
        throw IncorrectArgument("config", parsedArgs.parsedArgs)
    }

    val (configuration, instructionFactory) = loadConfiguration(FileReader(parsedArgs.config))

    val assembler = Assembler(
        instructionFactory = instructionFactory,
        identifierTable = configuration.labelTable,
        configuration = configuration
    )

    if (!parsedArgs.input.isNullOrEmpty()) {
        val file = assembler.assemble(Scanner(File(parsedArgs.input)), isTopLevel = true)

        if (parsedArgs.listings) {
            println("Listings:")
            println(assembler.listings)
            println()
        }

        if (parsedArgs.output != null) {
            File(parsedArgs.output).outputStream().use { out ->
                // TODO ensure that it only outputs the correct number of bytes
                out.write(file.byteArray)
            }
        }
    }
}

class CommandLineArgs(args: Array<String>) {
    val options: Options = Options()
    val parser = DefaultParser()

    val parsedArgs: CommandLine

    val listings: Boolean
    val input: String?
    val output: String?
    val config: String?
    val help: Boolean

    init {
        var option: Option

        option = Option("c", "config", true, "The configuration file which describes the assembly language.")
        //option.isRequired = true // I need to figure out how to handle --help
        options.addOption(option)

        option = Option("i", "input", true, "Sets the file from which the assembly language file is loaded.")
        options.addOption(option)

        option = Option("o", "output", true, "Sets the file to which the assembled output is written.")
        options.addOption(option)

        option = Option("l", "listings", false, "Generate an assembly listing instead of a binary file.")
        options.addOption(option)

        option = Option("h", "help", false, "Lists out the arguments.")
        options.addOption(option)

        parsedArgs = parser.parse(options, args)

        help = parsedArgs.hasOption('h')
        listings = parsedArgs.hasOption('l')

        input = parsedArgs.getOptionValue('i')
        output = parsedArgs.getOptionValue('o')
        config = parsedArgs.getOptionValue('c')
    }

    fun printHelp() = HelpFormatter().printHelp("ant", options)
}
