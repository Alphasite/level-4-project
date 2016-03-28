package com.nishadmathur.references

import com.nishadmathur.directives.Segment
import com.nishadmathur.errors.PathResolutionError
import com.nishadmathur.util.OffsetAssignable
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class IndexedReference(
    val sourceReference: Reference,
    val offsetReference: Reference,
    val sourceBeforeOffset: Boolean
) : Reference {

    override var segment: Segment? = null

    override var offset: SizedByteArray? = null
        set(offset) {
            field = offset

            for (argument in arrayOf(sourceReference, offsetReference)) {
                if (argument is OffsetAssignable) {
                    argument.offset = offset
                }
            }
        }

    override val size: Int
        get() = sourceReference.raw.bitSize + offsetReference.raw.bitSize

    override val raw: SizedByteArray
        get() {
            if (sourceBeforeOffset) {
                return SizedByteArray.join(listOf(sourceReference.raw, offsetReference.raw))
            } else {
                return SizedByteArray.join(listOf(offsetReference.raw, sourceReference.raw))
            }
        }

    override fun resolvePath(path: String): SizedByteArray {
        return when (path) {
            "source" -> sourceReference.raw
            "offset" -> offsetReference.raw
            else -> throw PathResolutionError(path)
        }
    }

    override fun toString(): String = "$sourceReference[$offsetReference]#$raw"
}
