package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class IndexedReference(val source: Reference, val offset: Reference, val sourceFirst: Boolean) : Reference {

    override val size: Int
        get() = source.raw.bitSize + offset.raw.bitSize

    override val raw: SizedByteArray
        get() {
            if (sourceFirst) {
                return SizedByteArray.join(listOf(source.raw, offset.raw))
            } else {
                return SizedByteArray.join(listOf(offset.raw, source.raw))
            }
        }

    override fun toString(): String = "$source[$offset]#$raw"
}
