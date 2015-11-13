package com.nishadmathur.assembler

import com.nishadmathur.errors.UndeclaredLabelError
import com.nishadmathur.util.SizedByteArray
import com.nishadmathur.util.toByteArray

/**
 * User: nishad
 * Date: 12/10/2015
 * Time: 09:13
 */
class Label(val identifier: String, val identifierTable: IdentifierTable) {
    var offset: SizedByteArray? = SizedByteArray((identifierTable.nextIdentifier++).toByteArray(), identifierTable.size)

    val raw: SizedByteArray
        get() {
            return offset ?: throw UndeclaredLabelError(identifier)
        }

    init {
        this.identifierTable.table.put(identifier, this)
    }

    override fun toString(): String {
        return "#$identifier"
    }
}
