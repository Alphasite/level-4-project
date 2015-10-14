package com.nishadmathur.references

import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class IndexedReference(val source: Reference, val offset: Reference, override val size: Int) : Reference {

    override val raw: ByteArray
        get() = ByteArray(0)

    override fun toString(): String = "$source[$offset]@${Arrays.toString(raw)}[$size]"
}
