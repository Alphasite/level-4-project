package com.nishadmathur.configuration

import com.esotericsoftware.yamlbeans.YamlWriter
import com.nishadmathur.instructions.TypePolymorphicInstructionFactory
import com.nishadmathur.instructions.TypedInstructionFactory
import com.nishadmathur.references.*
import java.io.Writer

/**
 * User: nishad
 * Date: 14/10/2015
 * Time: 21:57
 */
fun load(file: Writer) {
    val writer = YamlWriter(file);
    writer.config.setClassTag("MetaInstruction", TypePolymorphicInstructionFactory::class.java)
    writer.config.setClassTag("TypedInstruction", TypedInstructionFactory::class.java)

    writer.config.setClassTag("MetaReference", MetaReferenceFactory::class.java)
    writer.config.setClassTag("IndexedReference", IndexedReferenceFactory::class.java)
    writer.config.setClassTag("LabelReference", LabelReferenceFactory::class.java)
    writer.config.setClassTag("LiteralReference", LiteralReferenceFactory::class.java)
    writer.config.setClassTag("MappedReference", MappedReferenceFactory::class.java)

}
