package com.nishadmathur.references

import com.nishadmathur.util.SizedByteArray

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:48
 */
interface Reference {
    val raw: SizedByteArray
    val size: Int

    override fun toString(): String
}
