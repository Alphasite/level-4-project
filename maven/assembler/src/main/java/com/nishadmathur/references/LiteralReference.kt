package com.nishadmathur.references

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
class LiteralReference(override val size: Int,
                       val value: ByteArray) : AbstractReference(size) {

    override val raw: ByteArray
        get() = ByteArray(0)

}
