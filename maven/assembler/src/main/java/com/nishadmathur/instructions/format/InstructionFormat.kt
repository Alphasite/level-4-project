package com.nishadmathur.instructions.format

import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.errors.MalformedDeclaration
import com.nishadmathur.errors.PathResolutionError
import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import org.springframework.expression.spel.standard.SpelExpressionParser
import org.springframework.expression.spel.support.StandardEvaluationContext
import java.lang.Math.min
import java.util.*

/**
 * User: nishad
 * Date: 13/01/2016
 * Time: 21:57
 */
public class InstructionFormat(val typedSegments: List<TypedLiteral>) {
    public fun applyTo(arguments: Map<String, Reference>): SizedByteArray {
        val bytes = HashMap<TypedLiteral, SizedByteArray>()

        val nonPathLiterals = typedSegments.filter {
            it !is TypedLiteral.expression
        }.map {
            when (it) {
                is TypedLiteral.literal -> Pair(it, it.literal)
                is TypedLiteral.path -> {
                    val segments = it.path.split('.', limit = 2)
                    val segment = when (segments.size) {
                        2 -> arguments[segments[0]]?.resolvePath(segments[1])
                        1 -> arguments[segments[0]]?.raw
                        else -> null
                    } ?: throw PathResolutionError(it.path)

                    if (it.size != null) {
                        Pair(it, segment.range(it.drop, min(it.drop + it.size, segment.bitSize)))
                    } else {
                        Pair(it, segment.range(it.drop))
                    }
                }
                is TypedLiteral.expression -> throw InternalError("Somehow an expression got here.")
            }
        }.toMap()

        val namedLiterals = nonPathLiterals
            .filter { it.component1().name != null }
            .map { Pair(it.component1().name!!, it.component2()) }
            .toMap()

        // I need to consider how to handle ordered references between expressions.
        typedSegments
            .map { it as? TypedLiteral.expression }
            .filterNotNull()
            .forEach {
                val parser = SpelExpressionParser()
                val context = StandardEvaluationContext()
                val exp = parser.parseExpression(it.expression)

                context.setVariables(namedLiterals)

                bytes[it] = SizedByteArray((exp.getValue(context) as Number).toLong().toByteArray(), it.size)
            }

        val orderedBytes = typedSegments.map { it ->
            bytes[it]
                ?: nonPathLiterals[it]
                ?: it.default
                ?: throw MalformedDeclaration("Instruction is missing an argument or a default or something. '$it'")
        }

        return SizedByteArray.join(orderedBytes)
    }

    companion object {
        public fun parseInstructionFormats(configuration: Map<*, *>): Map<String, InstructionFormat> {
            return configuration.map { entry ->
                val key = entry.key as? String
                    ?: throw InvalidOption(entry.key.toString(), configuration)

                val untypedList = entry.value as? List<*>
                    ?: throw InvalidOption(entry.key as String, configuration)

                Pair(key, InstructionFormat(TypedLiteral.parseList(untypedList)))
            }.toMap()
        }
    }
}
