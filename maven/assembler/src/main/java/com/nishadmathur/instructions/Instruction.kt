package com.nishadmathur.instructions

import com.nishadmathur.assembler.RawLiteralConvertible
import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:04
 */
interface Instruction : RawLiteralConvertible {
    val size: Int
    val arguments: Map<String, Reference>
    var offset: SizedByteArray? set
    override fun toString(): kotlin.String
}
