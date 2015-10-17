package com.nishadmathur.assembler

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 19:23
 */
class IdentifierTable(val size: Int) {
    var nextIdentifier = 0
        get() = field++

    val table: MutableMap<String, Label> = hashMapOf()

    operator fun get(index: String): Label? {
        return this.table[index]
    }

    fun add(index: String) {
        this.table[index] = Label(index, this)
            this.table[index] ?: nextIdentifier++
    }
}
