package com.nishadmathur.instructions

import com.nishadmathur.configuration.Configuration
import com.nishadmathur.instructions.format.InstructionFormat
import com.nishadmathur.references.ReferenceFactory

/**
 * User: nishad
 * Date: 21/10/2015
 * Time: 18:31
 */
interface InstructionParser {
    fun parse(
        properties: Map<*, *>,
        referenceFactories: Map<String, ReferenceFactory>,
        instructionFormats: Map<String, InstructionFormat>,
        rootInstructionFactory: InstructionFactory,
        configuration: Configuration
    ): InstructionFactory
}
