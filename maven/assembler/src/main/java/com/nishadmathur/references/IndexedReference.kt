package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class IndexedReference(val source: Reference, val offset: Reference) : Reference {

    override val size: Int
        get() = source.raw.bitSize + offset.raw.bitSize

    override val raw: SizedByteArray
        get() = SizedByteArray.join(listOf(source.raw, offset.raw))

    override fun toString(): String = "$source[$offset]#$raw"
}
