package com.nishadmathur.errors

/**
 * User: nishad
 * Date: 24/10/2015
 * Time: 21:46
 */
open class InternalError(message: String?) : Exception(message)

class InvalidImplementation(message: String?) : InternalError(message)
