package com.nishadmathur.instructions

import com.nishadmathur.assembler.RawLiteralConvertible
import com.nishadmathur.references.Reference
import com.nishadmathur.util.OffsetAssignable
import com.nishadmathur.util.SegmentAssignable

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:04
 */
interface Instruction : RawLiteralConvertible, OffsetAssignable, SegmentAssignable {
    val size: Int
    val arguments: Map<String, Reference>

    override fun toString(): kotlin.String
}
