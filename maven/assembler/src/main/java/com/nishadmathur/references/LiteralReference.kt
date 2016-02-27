package com.nishadmathur.references

import com.nishadmathur.errors.PathResolutionError
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
class LiteralReference(override val raw: SizedByteArray) : Reference {
    override val size: Int
        get() = raw.bitSize

    override fun resolvePath(path: String): SizedByteArray {
        return if (path == "") {
            this.raw
        } else {
            throw PathResolutionError(path)
        }
    }

    override fun toString(): String = "$raw"
}
