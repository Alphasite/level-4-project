package com.nishadmathur.instructions

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.errors.IncorrectTypeError
import com.nishadmathur.errors.InstructionParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.references.Reference
import com.nishadmathur.references.ReferenceFactory
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.io.Serializable
import java.lang.Long

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 15:51
 */
class TypedInstructionFactory(override val identifier: String,
                              val argumentFactories: List<Pair<String, ReferenceFactory>>,
                              var rawStructure: List<RawStructureLiteral>) : InstructionFactory, Serializable {

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

            return TypedInstruction(argumentReferences, rawStructure)
        } else {
            throw InstructionParseError("$identifier with arguments '${arguments.joinToString("', '")}' could not be parsed correctly, it should be in the form '$help'")
        }
    }

    companion object : InstructionParser {
        override fun parse(properties: Map<*, *>,
                           referenceFactories: Map<String, ReferenceFactory>,
                           configuration: Configuration): InstructionFactory {

            val name = properties.getRaw("name") as? String
                    ?: throw InvalidOption("name", properties)

            val rawByteSequence = properties.getRaw("byte sequence")
                    ?: throw InvalidOption("byte sequence", properties)
            val rawStructureLiterals: List<RawStructureLiteral>

            val rawArguments = properties.getRaw("arguments") as? Map<*, *>
                    ?: throw InvalidOption("arguments", properties.getRaw("arguments"))

            val arguments: List<Pair<String, ReferenceFactory>> = rawArguments
                    .map {
                        val referenceName = it.key as? String ?: throw InvalidOption("arguments > name", it)
                        val referenceKind = it.value as? String ?: throw InvalidOption("arguments > name", it)
                        val factory = referenceFactories[referenceKind] ?: throw InvalidOption("arguments > type", it)
                        Pair(referenceName, factory)
                    }

            val literalSize = properties.getRaw("size") as? Int
            if (literalSize != null) {
                if (rawByteSequence is Map<*, *>) {
                    throw InvalidOption("byte sequence", properties)
                }

                rawStructureLiterals = listOf(
                        RawStructureLiteral.literal(SizedByteArray(parseNumber(rawByteSequence), literalSize)),
                        *arguments.map { RawStructureLiteral.path(it.first) }.toTypedArray()
                )
            } else {
                val structureLiterals = (rawByteSequence as? List<*>)
                        ?.map { it as? Map<*, *> }
                        ?.requireNoNulls()
                        ?.map {
                            it.entries.map { entry ->
                                Pair(
                                        entry.key as? String
                                                ?: throw InvalidOption(entry.key.toString(), it),
                                        entry.value as? String ?: entry.value as? Number
                                                ?: throw InvalidOption(entry.key as String, it)
                                )
                            }.toMap()
                        }
                        ?: throw InvalidOption("byte sequence", rawByteSequence)

                rawStructureLiterals = structureLiterals.map {
                    if ("path" in it) {
                        val path = it["path"] as? String
                                ?: throw InvalidOption("path", it)
                        RawStructureLiteral.path(path)
                    } else {
                        val literal = parseNumber(it["literal"]!!)
                        val size = (it["size"] as? Number)?.toInt()
                            ?: throw InvalidOption("size", it)
                        RawStructureLiteral.literal(SizedByteArray(literal, size))
                    }
                }
            }

            return TypedInstructionFactory(
                    name,
                    arguments,
                    rawStructureLiterals
            )
        }

        fun parseNumber(rawByteSequence: Any): ByteArray {
            return when (rawByteSequence) {
                is String -> Long.decode(rawByteSequence).toByteArray()
                is Number -> rawByteSequence.toLong().toByteArray()
                else -> throw InvalidOption("byte sequence", rawByteSequence)
            }
        }
    }
}
