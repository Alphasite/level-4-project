package com.nishadmathur.instructions

import com.nishadmathur.references.Reference
import java.util.*

/**
 * User: nishad
 * Date: 05/10/2015
 * Time: 09:04
 */
interface Instruction {
    val raw: ByteArray
    val size: Int
    val arguments: List<Reference>

    override fun toString(): String {
        return "${Arrays.toString(raw)} Args@{${arguments.joinToString(", ")}}"
    }
}
