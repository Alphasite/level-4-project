package com.nishadmathur.references

import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class LabelReference(val label: String, override val size: Int) : Reference {

    override val raw: ByteArray
        get() = ByteArray(0)

    override fun toString(): String = "$label@${Arrays.toString(raw)}"
}
