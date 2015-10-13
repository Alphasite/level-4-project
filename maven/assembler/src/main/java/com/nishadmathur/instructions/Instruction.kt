package com.nishadmathur.instructions

import com.nishadmathur.references.Reference

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:04
 */
interface Instruction {
    val raw: ByteArray
    val size: Int
    val arguments: List<Reference>
}
