//: Playground - noun: a place where people can play

import Foundation

var assembly = [
    "start:",
    "load 1[1], r1",
    "load 1, r1",
    "add  r1, r1",
    "cake:",
    "add  r1, r1",
    "load #cake[0], r2",
    "jump #start",
].joinWithSeparator("\n")

do {
    print("Assembly")
    print(assembly)
    print("")

    var offset: UInt = 0
    
    let instructions = try assembly
        .componentsSeparatedByString("\n")
        .enumerate()
        .filter { _, line in line.characters.count > 0 }
        .map { i, line in Line(lineNumber: UInt(i), line: line) }
        .map { try parseLine($0) }
    
    let offsetInstructions = calculateOffsets(instructions)
    
    let bytes = offsetInstructions
        .map { $0.instruction?.raw }
        .enumerate()
        .filter { $1 != nil }
        .map { ($0, $1!) }
    
    let labels = offsetInstructions
        .map { $0.label }
        .filter { $0 != nil }
        .map { $0! }
    
    // Byte Structure
    // asm ops .. 0xFEEBDAEDEADBEEF + [label id word + label offset word]* + 0xFEEBDAEDEADBEEF
    var byteArray = bytes.flatMap { _, array in array }
    byteArray.append(0xFEEBDAEDEADBEEF)
    byteArray.appendContentsOf(labels.map { $0.raw } .flatMap { $0 })
    byteArray.append(0xFEEBDAEDEADBEEF)
    
    print("Intermediate Format")
    offsetInstructions.forEach { print($0) }
    print("")
    
    print("Raw bytes + Basic debugging data")
    bytes.forEach { print($0) }
    print(0xFEEBDAEDEADBEEF)
    labels.forEach { print("(\($0.identifier), \($0.raw))") }
    print(0xFEEBDAEDEADBEEF)
    print("")
    
    print("Byte Array")
    print(byteArray)
    print("")
    
} catch let error {
    print(error)
}
