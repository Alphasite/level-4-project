package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
class LiteralReference(override val raw: SizedByteArray) : Reference {
    override val size: Int
        get() = raw.bitSize

    override fun toString(): String = "$raw"
}
