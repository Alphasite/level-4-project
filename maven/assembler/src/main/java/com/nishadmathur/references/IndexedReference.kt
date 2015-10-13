package com.nishadmathur.references

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:49
 */
class IndexedReference(val source: Reference, val offset: Reference, size: Int) : AbstractReference(size) {

    override val raw: ByteArray
        get() = ByteArray(0)

}
