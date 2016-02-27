package com.nishadmathur.references

import com.nishadmathur.configuration.Configuration

/**
 * User: nishad
 * Date: 24/10/2015
 * Time: 17:49
 */
interface ReferenceParser {
    fun parse(properties: Map<*, *>, referenceFactories: Map<String, ReferenceFactory>, configuration: Configuration): ReferenceFactory
}
