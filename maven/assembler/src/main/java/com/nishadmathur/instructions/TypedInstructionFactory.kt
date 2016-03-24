package com.nishadmathur.instructions

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.IncompleteDeclarationParserError
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.instructions.format.TypedLiteral
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.parseNumber
import java.io.Serializable
import java.util.*

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:51
 */
class TypedInstructionFactory(
    override val identifier: String,
    val argumentFactories: List<Pair<String, ReferenceFactory>>,
    val argumentAliases: Map<String, String>,
    var instructionFormat: InstructionFormat
) : InstructionFactory, Serializable {
    
    override val help: String
        get() = identifier + " " + argumentFactories.map { argument -> "<${argument.first}:${argument.second.type}>" }.joinToString(" ")
    
    override val factoryMap: Map<String, InstructionFactory>
        get() = hashMapOf(Pair(identifier, this))
    
    override fun checkIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Boolean {
        return (name == identifier || ignoreIdentifier)
            && arguments.size == argumentFactories.size
    }
    
    override fun checkTypeSignatureIsMatch(instruction: List<String>): Boolean {
        return (0 until argumentFactories.size).all { argumentFactories[it].second.checkIsMatch(instruction[it]) }
    }
    
    override fun getInstanceIfIsMatch(name: String, arguments: List<String>, ignoreIdentifier: Boolean): Instruction {
        if (arguments.size == argumentFactories.size
            && checkIsMatch(name, arguments, ignoreIdentifier)
            && checkTypeSignatureIsMatch(arguments)) {
            
            val argumentReferences = (0 until argumentFactories.size).map { i ->
                if (argumentFactories[i].second.checkIsMatch(arguments[i])) {
                    val reference = argumentFactories[i].second.getInstanceIfIsMatch(arguments[i])
                    Pair(argumentFactories[i].first, reference)
                } else {
                    throw IncorrectTypeError("${argumentFactories[i].first} expects its arguments in the form $help")
                }
            }.toMap()
            
            val aliases = argumentAliases.map {
                Pair(
                    it.value, argumentReferences[it.key] ?: throw InvalidOption(it.key, argumentReferences)
                )
            }
            
            val argumentReferencesAndAliases = HashMap<String, Reference>()
            argumentReferencesAndAliases.putAll(argumentReferences)
            argumentReferencesAndAliases.putAll(aliases)
            
            return TypedInstruction(argumentReferencesAndAliases, instructionFormat)
        } else {
            throw InstructionParseError("$identifier with arguments '${arguments.joinToString("', '")}' could not be parsed correctly, it should be in the form '$help'")
        }
    }
    
    companion object : InstructionParser {
        override fun parse(
            properties: Map<*, *>,
            referenceFactories: Map<String, ReferenceFactory>,
            instructionFormats: Map<String, InstructionFormat>,
            configuration: Configuration
        ): InstructionFactory {
            
            val name = properties["name"] as? String
                ?: throw InvalidOption("name", properties)
            
            val instructionFormat: InstructionFormat
            
            val rawArguments = properties["arguments"] as? Map<*, *>
                ?: if (properties["arguments"] == null) { HashMap<Any, Any>() } else { null }
                ?: throw InvalidOption("arguments", properties["arguments"])
            
            val rawAliases = (properties["aliases"] as? Map<*, *>)
            
            val aliases: Map<String, String> = rawAliases?.map {
                Pair(
                    it.key as? String ?: throw InvalidOption(it.key.toString(), rawAliases),
                    it.value as? String ?: throw InvalidOption(it.key.toString(), rawAliases)
                )
            }?.toMap() ?: mapOf()
            
            val arguments: List<Pair<String, ReferenceFactory>> = rawArguments
                .map {
                    val referenceName = it.key as? String ?: throw InvalidOption("arguments.name", it)
                    val referenceKind = it.value as? String ?: throw InvalidOption("arguments.name", it)
                    val factory = referenceFactories[referenceKind] ?: throw InvalidOption("arguments.type", it)
                    Pair(referenceName, factory)
                }
            
            val instructionFormatName = properties["instruction format"] as? String
            if (instructionFormatName != null) {
                instructionFormat = instructionFormats[instructionFormatName]
                    ?: throw IncompleteDeclarationParserError("The declaration for the instruction format '$instructionFormatName' is missing.")
            } else {
                
                val rawByteSequence = properties["byte sequence"]
                    ?: throw InvalidOption("byte sequence", properties)
                
                val literalSize = properties["size"] as? Int
                if (literalSize != null) {
                    if (rawByteSequence is Map<*, *>) {
                        throw InvalidOption("byte sequence", properties)
                    }
                    
                    val literalName = properties["name"] as? String?
                    
                    val rawStructureLiterals = listOf(
                        TypedLiteral.literal(literalName, SizedByteArray(parseNumber(rawByteSequence), literalSize)),
                        *arguments.map { TypedLiteral.path(literalName, it.first) }.toTypedArray()
                    )
                    
                    instructionFormat = InstructionFormat(rawStructureLiterals)
                } else {
                    
                    val untypedLiterals = rawByteSequence as? List<*>
                        ?: throw InvalidOption("byte sequence", rawByteSequence)
                    
                    instructionFormat = InstructionFormat(TypedLiteral.parseList(untypedLiterals))
                }
            }
            
            return TypedInstructionFactory(
                name,
                arguments,
                aliases,
                instructionFormat
            )
        }
        
        
    }
}
