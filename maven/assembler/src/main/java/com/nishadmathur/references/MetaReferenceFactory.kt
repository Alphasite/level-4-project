package com.nishadmathur.references

import com.nishadmathur.errors.DataSourceParseError
import dagger.Module
import dagger.Provides
import java.util.*

import javax.inject.Singleton

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 05:57
 */
@Module
class MetaReferenceFactory<T : Reference, U : ReferenceFactory<T>> : ReferenceFactory<T> {

    override val type: String
        get() = throw UnsupportedOperationException()

    internal var referenceFactories: TreeSet<Pair<Int, U>>

    init {
        this.referenceFactories = TreeSet<Pair<Int, U>>(comparator { a, b -> a.first.compareTo(b.first) })
    }

    override fun checkIsMatch(reference: String): Boolean {
        return referenceFactories.descendingIterator().asSequence()
            .map { it.second }
            .any { it.checkIsMatch(reference) }
    }

    override fun getInstanceIfIsMatch(reference: String): T {
        try {
            return referenceFactories.descendingIterator().asSequence()
                .map { it.second }
                .first { it.checkIsMatch(reference) }
                .getInstanceIfIsMatch(reference)
        } catch (_: NoSuchElementException) {
            throw DataSourceParseError("Error parsing data source; no matching type was found for '$reference'.")
        }
    }

    fun addReference(referenceFactory: U, priority: Int) {
        this.referenceFactories.add(Pair(priority, referenceFactory))
    }

    @Provides @Singleton private fun provideMetaReferenceFactor(): MetaReferenceFactory<Reference, ReferenceFactory<Reference>> {
        return MetaReferenceFactory()
    }
}

