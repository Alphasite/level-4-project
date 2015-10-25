package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.configuration.parseReference
import com.nishadmathur.errors.DataSourceParseError
import com.nishadmathur.errors.InvalidOption
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import java.io.Serializable
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 05:57
 */
class MetaReferenceFactory(override val type: String): ReferenceFactory, Serializable {

    internal var referenceFactories: TreeSet<Pair<Int, ReferenceFactory>>
    internal var referenceFactoriesMapping: MutableMap<String, ReferenceFactory>

    init {
        this.referenceFactories = TreeSet<Pair<Int, ReferenceFactory>>(comparator { a, b -> a.first.compareTo(b.first) })
        this.referenceFactoriesMapping = HashMap()
    }

    override fun checkIsMatch(reference: String): Boolean {
        return referenceFactories.descendingIterator().asSequence()
            .map { it.second }
            .any { it.checkIsMatch(reference) }
    }

    override fun getInstanceIfIsMatch(reference: String): Reference {
        try {
            return referenceFactories.descendingIterator().asSequence()
                .map { it.second }
                .first { it.checkIsMatch(reference) }
                .getInstanceIfIsMatch(reference)
        } catch (_: NoSuchElementException) {
            throw DataSourceParseError("Error parsing data source; no matching type was found for '$reference'.")
        }
    }

    fun addReference(referenceFactory: ReferenceFactory, priority: Int) {
        this.referenceFactories.add(Pair(priority, referenceFactory))
        this.referenceFactoriesMapping[referenceFactory.type] = referenceFactory
    }

    operator fun get(factoryName: String): ReferenceFactory? {
        return referenceFactoriesMapping[factoryName]
    }

    companion object {
        fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): Map<String, ReferenceFactory> {
            val name = properties.getRaw("name") as? String
                    ?: throw InvalidOption("name", properties)

            val references = (properties.getRaw("references") as? List<*>)
                    ?.map { it as? Map<*, *> }
                    ?.requireNoNulls()
                    ?: throw InvalidOption("references", properties)

            val meta = MetaReferenceFactory(name)
            val newReferenceFactories = parseReference(references, configuration) as MutableMap

            newReferenceFactories.values.mapIndexed { i, factory -> meta.addReference(factory, i)}

            newReferenceFactories[name] = meta
            newReferenceFactories.putAll(newReferenceFactories)

            return newReferenceFactories
        }
    }
}

