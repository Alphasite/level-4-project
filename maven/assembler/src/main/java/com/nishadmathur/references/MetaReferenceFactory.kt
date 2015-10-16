package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError
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
}

