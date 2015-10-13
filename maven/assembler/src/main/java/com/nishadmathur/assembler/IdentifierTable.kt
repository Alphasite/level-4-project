package com.nishadmathur.assembler

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 19:23
 */
class IdentifierTable {
    var nextIdentifier = 0
    val table: MutableMap<String, Int> = hashMapOf()

    operator fun get(index: String): Int? {
        return this.table[index]
    }

    operator fun set(index: String, value: Int) {
        this.table[index] = value
    }
}
