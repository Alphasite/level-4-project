package com.nishadmathur.references

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
interface Reference {
    val raw: ByteArray
    val size: Int

    override fun toString(): String
}
