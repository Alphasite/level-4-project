package com.nishadmathur.references

import java.io.Serializable

/**
 * User: nishad
 * Date: 04/10/2015
 * Time: 20:54
 */
interface ReferenceFactory {
    val type: String
    fun checkIsMatch(reference: String): Boolean
    fun getInstanceIfIsMatch(reference: String): Reference
}
