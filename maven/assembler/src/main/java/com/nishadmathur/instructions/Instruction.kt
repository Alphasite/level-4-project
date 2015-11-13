package com.nishadmathur.instructions

import com.nishadmathur.references.Reference
import com.nishadmathur.util.SizedByteArray
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:04
 */
interface Instruction {
    val raw: SizedByteArray
    val size: Int
    val arguments: Map<String, Reference>
    override fun toString(): kotlin.String
}
