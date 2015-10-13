package com.nishadmathur.assembler

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 09:13
 */
class Label(val identifier: String, val IdentifierTable: IdentifierTable) {
    var offset: ByteArray? = null

    val linkIdentifier: Byte?
        get() = this.IdentifierTable[this.identifier]?.toByte()

    val raw: ByteArray
        get() {
            val bytes = ByteArray(1 + (offset?.size() ?: 0))
            bytes[0] = linkIdentifier ?: 0

            if (offset != null) {
                System.arraycopy(offset, 0, bytes, 1, offset!!.size())
            }

            return bytes
        }

    init {
        this.IdentifierTable.table.put(identifier, IdentifierTable.nextIdentifier++)
    }
}
