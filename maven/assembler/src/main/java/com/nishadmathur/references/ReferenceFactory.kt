package com.nishadmathur.references

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:54
 */
interface ReferenceFactory<T : Reference> {
    val type: String
    fun checkIsMatch(reference: String): Boolean
    fun getInstanceIfIsMatch(reference: String): T
}
