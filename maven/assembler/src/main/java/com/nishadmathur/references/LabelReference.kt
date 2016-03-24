package com.nishadmathur.references

import com.nishadmathur.assembler.IdentifierTable
import com.nishadmathur.errors.PathResolutionError
import com.nishadmathur.errors.UndeclaredLabelError
import com.nishadmathur.util.SizedByteArray
import java.math.BigInteger

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class LabelReference(
    val label: String,
    val labelTable: IdentifierTable,
    val addressingMode: AddressingModes,
    override val size: Int
) : Reference {

    override var offset: SizedByteArray? = null
        set(offset) {
            field = offset
        }

    override fun resolvePath(path: String): SizedByteArray {
        return if (path == "") {
            this.raw
        } else {
            throw PathResolutionError(path)
        }
    }

    // Size must be <32bits
    // Its an easy limitation to change but it doest seem to be an issue atm.
    override val raw: SizedByteArray
        get() {
            val globalOffset = labelTable[label]?.raw
                ?: throw UndeclaredLabelError("Label '$label' is used but never defined.")

            // This is currently broken, due to the code base using raw.bitsize everywhere (including offset calculations)
//            if (offset == null) {
//                throw InternalError("Offset not initialised")
//            }

            return when (addressingMode) {
                AddressingModes.global -> globalOffset
                AddressingModes.pc_relative -> SizedByteArray(
                    globalOffset.bigInteger.subtract(offset?.bigInteger ?: BigInteger.ZERO).toByteArray(),
                    globalOffset.bitSize
                )
            }
        }

    override fun toString(): String = "$label#$raw"
}
