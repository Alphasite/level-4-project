package com.nishadmathur.references

import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
class LiteralReference(override val size: Int,
                       val value: ByteArray) : Reference {

    override val raw: ByteArray
        get() = value //TODO resizing.

    override fun toString(): String = "${Arrays.toString(value)}@$size"
}
