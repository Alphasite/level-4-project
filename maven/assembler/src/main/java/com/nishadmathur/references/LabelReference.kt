package com.nishadmathur.references

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.errors.UndeclaredLabelError
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray
import java.util.*

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class LabelReference(val label: String, val labelTable: IdentifierTable, override val size: Int) : Reference {

    // Size must be <32bits
    // Its an easy limitation to change but it doest seem to be an issue atm.
    override val raw: SizedByteArray
        get() = labelTable[label]?.raw ?: throw UndeclaredLabelError("Label $label is used but never defined.")

    override fun toString(): String = "$label#$raw"
}
