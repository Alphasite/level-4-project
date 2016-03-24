package com.nishadmathur.references

import com.nishadmathur.assembler.RawLiteralConvertible
import com.nishadmathur.util.OffsetAssignable
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
interface Reference : RawLiteralConvertible, OffsetAssignable {

    val size: Int

    override var offset: SizedByteArray?

    fun resolvePath(path: String): SizedByteArray

    override fun toString(): String
}
