package com.nishadmathur.assembler

/**
 * User: nishad
 * Date: 13/10/2015
 * Time: 19:23
 */
class IdentifierTable(val size: Long, val parent: IdentifierTable? = null) {

    var nextIdentifier = 0
        get() {
            field += 1
            return field
        }

    val table: MutableMap<String, Label> = hashMapOf()

    val childTable: IdentifierTable get() = IdentifierTable(size, this)

    operator fun get(index: String): Label? {
        return this.table[index] ?: parent?.get(index)
    }

    fun add(index: String) {
        this.table[index] = Label(index, this)
        this.table[index] ?: nextIdentifier++
    }
}
