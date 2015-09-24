//: Playground - noun: a place where people can play

import Cocoa

enum AssemblerError: ErrorType {
    case InstructionParseError(error: String)
    case DataSourceParseError(error: String)
    case LineParseError(error: String)
    case UndeclaredLabelError(error: String)
    case IncorrectTypeError(error: String)
    case AbstractInstructionInstantiationError(error: String)
}

enum DataSource {
    case register(UInt)
    case literal(UInt)
    case label(String)
    indirect case memory(source: DataSource, offset: DataSource)
    
    private static let memoryRegex = "(\\d+|r\\d+|#\\w+)\\[(\\d+|r\\d+)\\]"
    
    var raw: [UInt]? {
        switch self {
        case .register(let identifier):
            return [UInt(identifier)]
            
        case .literal(let value):
            return [UInt(value)]
            
        case .label(let label):
            if let id = Label.IdentifierTable[label] {
                return [id]
            } else {
                return nil
            }
            
        case .memory(let source, let offset):
            if let sourceValue = source.raw?[0], offsetValue = offset.raw?[0] {
                return [sourceValue, offsetValue]
            } else {
                return nil
            }
        }
    }
    
    static func get(source: String) throws -> DataSource {
        if source.isMatch(DataSource.memoryRegex) {
            let matches = source.getMatches(DataSource.memoryRegex)
            
            guard matches.count == 1 else {
                throw AssemblerError.DataSourceParseError(error: "Error extracting memory address from \(source)")
            }
            
            let match = matches[0]
            
            guard !source.isMatch("\\[.*\(memoryRegex).*\\]") else {
                throw AssemblerError.DataSourceParseError(
                    error: "Memory data sources cannot be nested; '\(source)'"
                )
            }
            
            let sourceAddress = try DataSource.get((source as NSString).substringWithRange(match.rangeAtIndex(1)))
            let offsetAddress = try DataSource.get((source as NSString).substringWithRange(match.rangeAtIndex(2)))
            
            switch sourceAddress {
            case .memory(_, _):
                throw AssemblerError.DataSourceParseError(error: "Memory data sources cannot be nested")
            default:
                break
            }
            
            switch offsetAddress {
            case .memory(_, _):
                throw AssemblerError.DataSourceParseError(error: "Memory data sources cannot be nested")
            default:
                break
            }
            
            return DataSource.memory(
                source: sourceAddress,
                offset: offsetAddress
            )
        }
        
        if source.lowercaseString.isMatch("^r\\d+$") {
            guard let number = UInt(source[1...source.length]) else {
                throw AssemblerError.DataSourceParseError(error: "Error extracting register number from \(source)")
            }
            
            return DataSource.register(number)
        }

        if source.isMatch("^\\d+$") {
            guard let number = UInt(source) else {
                throw AssemblerError.DataSourceParseError(error: "Error extracting literal from \(source)")
            }
            
            return DataSource.literal(number)
        }
        
        if source.isMatch("^#\\w+$") {
            let label = source.getMatchesAsStrings("(?<=#)\\w+")
            return DataSource.label(label[0])
        }
        
        throw AssemblerError.DataSourceParseError(error: "Error determing type for literal in reference \(source)")
    }
    
}

enum Instruction {
    // load <from:address> <to:register>
    case load(source: DataSource, destination: DataSource)
    case loadr(source: DataSource, destination: DataSource)
    case loadm(source: DataSource, destination: DataSource)
    case loadl(source: DataSource, destination: DataSource)
    
    // save <from:register> <to:address>
    case save(source: DataSource, destination: DataSource)
    case saver(source: DataSource, destination: DataSource)
    case savem(source: DataSource, destination: DataSource)
    
    // add <value:register> <to:register>
    case add(source: DataSource, destination: DataSource)
    
    // sub <value:register> <from:register>
    case sub(source: DataSource, destination: DataSource)
    
    case jump(destination: DataSource)
    case jumpt(condition: DataSource, destination: DataSource)
    case jumpf(condition: DataSource, destination: DataSource)
    
    case cmpeq(left: DataSource, right: DataSource, destination:DataSource)
    case cmpneq(left: DataSource, right: DataSource, destination:DataSource)
    case cmpgt(left: DataSource, right: DataSource, destination:DataSource)
    case cmplt(left: DataSource, right: DataSource, destination:DataSource)
    
    case not(boolean: DataSource, destination:DataSource)
    
    var raw: [UInt]? {
        switch self {
        case .load(let source, let destination):
            switch source {
            case .register(_):
                return Instruction.loadr(source: source, destination: destination).raw
            case .memory(_, _):
                return Instruction.loadm(source: source, destination: destination).raw
            case .literal(_):
                return Instruction.loadl(source: source, destination: destination).raw
            default:
                print("The system attemtped to get the value for an abstract instruction, not sure what kind exactly.")
                return nil
            }
        case .loadr(let source, let destination):
            return [[0], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .loadm(let source, let destination):
            return [[1], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .loadl(let source, let destination):
            return [[2], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .save(let source, let destination):
            switch destination {
            case .register(_):
                return Instruction.saver(source: source, destination: destination).raw
            case .memory(_, _):
                return Instruction.savem(source: source, destination: destination).raw
            default:
                print("The system attemtped to get the value for an abstract instruction, not sure what kind exactly.")
                return nil
            }
        case .saver(let source, let destination):
            return [[3], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .savem(let source, let destination):
            return [[4], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .add(let source, let destination):
            return [[5], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case .sub(let source, let destination):
            return [[6], source.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case jump(let destination):
            return [[7], destination.raw].flatMap { $0 } .flatMap { $0 }
        case jumpt(let condition, let destination):
            return [[8], condition.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case jumpf(let condition, let destination):
            return [[9], condition.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case cmpeq(let left, let right, let destination):
            return [[10], left.raw, right.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case cmpneq(let left, let right, let destination):
            return [[11], left.raw, right.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case cmpgt(let left, let right, let destination):
            return [[12], left.raw, right.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case cmplt(let left, let right, let destination):
            return [[13], left.raw, right.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        case not(let boolean, let destination):
            return [[14], boolean.raw, destination.raw].flatMap { $0 } .flatMap { $0 }
        }
    }
    
    var size: UInt {
        switch self {
        case .load(_, _):
            print("Attempted to get the size for an abstract instruction load, this should never occur.")
            return 0 // This case should never occur
        case .loadr(_, _):
            return 3
        case .loadm(_, _):
            return 4
        case .loadl(_, _):
            return 3
        case .save(_, _):
            return 4
        case .saver(_, _):
            return 3
        case .savem(_, _):
            return 4
        case .add(_, _):
            return 3
        case .sub(_, _):
            return 3
        case jump(_):
            return 1
        case jumpt(_, _):
            return 3
        case jumpf(_, _):
            return 3
        case cmpeq(_, _, _):
            return 4
        case cmpneq(_, _, _):
            return 4
        case cmpgt(_, _, _):
            return 4
        case cmplt(_, _, _):
            return 4
        case not(_, _):
            return 3
        }
    }
    
    static var help: [String: String] = [
        "loadr": "loadr <from:register> <to:register>",
        "loadm": "loadm <from:memory-access> <to:register>",
        "loadl": "loadl <from:literal> <to:register>",
        "load":  "load <from:register | memory-access | literal> <to:register>",
        "saver": "saver <from:register> <to:register>",
        "savem": "savem <from:register> <to:memory-access>",
        "save":  "save <from:register> <to:register | memory-access | literal>",
        "add": "add <value:register> <from:register>",
        "sub": "sub <value:register> <from:register>",
        "jump": "jump <destination:label>",
        "jumpt": "jumpt <condition:register> <destination:label>",
        "jumpf": "jumpf <condition:register> <destination:label>",
        "cmpeq": "cmpeq <left:register> <right:register> <destination:register>",
        "cmpneq": "cmpneq <left:register> <right:register> <destination:register>",
        "cmpgt": "cmpgt <left:register> <right:register> <destination:register>",
        "cmplt": "cmplt <left:register> <right:register> <destination:register>",
        "not": "not <value:register> <destination:label>",
    ]
    
    static func get(var instructionSegments: [String]) throws -> Instruction {
        
        guard instructionSegments.count > 0 else {
            throw AssemblerError.InstructionParseError(error: "No values in instruction, this should not be reachable")
        }
        
        for var i = 1; i < instructionSegments.count; i++ {
            instructionSegments[i] = instructionSegments[i].stringByTrimmingCharactersInSet(NSCharacterSet(charactersInString: ", "))
        }
        
        
        let instructionString = instructionSegments[0]
        switch instructionString {
        
        case "loadl": fallthrough
        case "loadm": fallthrough
        case "loadr": fallthrough
        case "load":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Load must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)\'"
                )
            }
            
            let source = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (instructionString, source, destination) {
            
            case (let instruction, .literal(_), .register(_)) where instruction == "load" || instruction == "loadl":
                return Instruction.loadl(
                    source: source,
                    destination: destination
                )
                
            case (let instruction, .memory(_, _), .register(_)) where instruction == "load" || instruction == "loadm":
                return Instruction.loadm(
                    source: source,
                    destination: destination
                )
                
            case (let instruction, .register(_), .register(_)) where instruction == "load" || instruction == "loadr":
                return Instruction.loadr(
                    source: source,
                    destination: destination
                )
                
            default:
                print(source, destination)
                throw AssemblerError.IncorrectTypeError(error: "Load expects its arguments in the form '\(Instruction.help[instructionString])', not '\(source, destination)'")
            }
            
        case "saver": fallthrough
        case "savel": fallthrough
        case "save":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Load must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)\'"
                )
            }
            
            let source = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (instructionString, source, destination) {
            
            case (let instruction, .register(_), .register(_)) where instruction == "save" || instruction == "saver":
                return Instruction.saver(
                    source: source,
                    destination: destination
                )
                
            case (let instruction, .memory(_, _), .register(_)) where instruction == "save" || instruction == "savem":
                return Instruction.savem(
                    source: source,
                    destination: destination
                )
                
            default:
                throw AssemblerError.IncorrectTypeError(error: "Save expects its arguments in the form '\(Instruction.help[instructionString])', not '\(source, destination)'")
            }
        
        case "add":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Add must be in the form '\(Instruction.help[instructionString]), not '\(instructionSegments)'"
                )
            }
            
            let source = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (source, destination) {
            case (.register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Add expects its arguments in the form '\(Instruction.help[instructionString])', not '\(source, destination)'")
            }
            
            return Instruction.add(
                source: source,
                destination: destination
            )
            
        
        case "sub":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Subtract must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let source = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (source, destination) {
            case (.memory(_, _), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Subtract expects its arguments in the form '\(Instruction.help[instructionString])', not '\(source, destination)'")
            }
            
            return Instruction.sub(
                source: source,
                destination: destination
            )
        case "jump":
            guard instructionSegments.count == 2 else {
                throw AssemblerError.InstructionParseError(
                    error: "Jump must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let destination = try DataSource.get(instructionSegments[1])
            
            switch destination {
            case .label:
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Jump expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.jump(
                destination: destination
            )
            
        case "jumpt":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Jumpt must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let condition = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (condition, destination) {
            case (.register(_), .label):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Jumpt expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.jumpt(
                condition: condition,
                destination: destination
            )
            
        case "jumpf":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Jumpf must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let condition = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (condition, destination) {
            case (.register(_), .label):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Jumpf expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.jumpf(
                condition: condition,
                destination: destination
            )
            
        case "cmpeq":
            guard instructionSegments.count == 4 else {
                throw AssemblerError.InstructionParseError(
                    error: "Compare Equals must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let left = try DataSource.get(instructionSegments[1])
            let right = try DataSource.get(instructionSegments[2])
            let destination = try DataSource.get(instructionSegments[3])
            
            switch (left, right, destination) {
            case (.register(_), .register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Cmpeq expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.cmpeq(
                left: left,
                right: right,
                destination: destination
            )
            
        case "cmpneq":
            guard instructionSegments.count == 4 else {
                throw AssemblerError.InstructionParseError(
                    error: "Compare not equals must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let left = try DataSource.get(instructionSegments[1])
            let right = try DataSource.get(instructionSegments[2])
            let destination = try DataSource.get(instructionSegments[3])
            
            switch (left, right, destination) {
            case (.register(_), .register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Cmpneq expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.cmpneq(
                left: left,
                right: right,
                destination: destination
            )
            
        case "cmpgt":
            guard instructionSegments.count == 4 else {
                throw AssemblerError.InstructionParseError(
                    error: "Compare greater than must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let left = try DataSource.get(instructionSegments[1])
            let right = try DataSource.get(instructionSegments[2])
            let destination = try DataSource.get(instructionSegments[3])
            
            switch (left, right, destination) {
            case (.register(_), .register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Cmpgt expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.cmpgt(
                left: left,
                right: right,
                destination: destination
            )
            
        case "cmplt":
            guard instructionSegments.count == 4 else {
                throw AssemblerError.InstructionParseError(
                    error: "Compare less than must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let left = try DataSource.get(instructionSegments[1])
            let right = try DataSource.get(instructionSegments[2])
            let destination = try DataSource.get(instructionSegments[3])
            
            switch (left, right, destination) {
            case (.register(_), .register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "Cmplt expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.cmplt(
                left: left,
                right: right,
                destination: destination
            )
            
        case "not":
            guard instructionSegments.count == 3 else {
                throw AssemblerError.InstructionParseError(
                    error: "Not must be in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'"
                )
            }
            
            let boolean = try DataSource.get(instructionSegments[1])
            let destination = try DataSource.get(instructionSegments[2])
            
            switch (boolean, destination) {
            case (.register(_), .register(_)):
                break
            default:
                throw AssemblerError.IncorrectTypeError(error: "not expects its arguments in the form '\(Instruction.help[instructionString])', not '\(instructionSegments)'")
            }
            
            return Instruction.not(
                boolean: boolean,
                destination: destination
            )
            
        default:
            throw AssemblerError.InstructionParseError(error: "Unrecognised instruction '\(instructionSegments)'")
            
        }
    }
}

class Label {
    // Look into using a class and removing this.
    static var NextIdentifier: UInt = 0
    static var IdentifierTable = [String: UInt]()
    
    let identifier: String
    var offset: UInt?

    var linkIdentifier: UInt? {
        return Label.IdentifierTable[self.identifier]
    }
    
    var raw: [UInt] {
        return [self.linkIdentifier ?? UInt.max, self.offset ?? UInt.max]
    }
    
    init(identifier: String) {
        self.identifier = identifier
        Label.IdentifierTable[identifier] = Label.NextIdentifier++
    }
}

struct Line {
    let lineNumber: UInt
    let line: String
    var label: Label?
    var instruction: Instruction?
    var offset: UInt? {
        didSet {
            label?.offset = offset
        }
    }
    
    var size: UInt {
        return instruction?.size ?? 0
    }
    
    init(lineNumber: UInt, line: String) {
        self.lineNumber = lineNumber
        self.line = line
    }
}

func parseLine(var line: Line) throws -> Line {
    let sections = line.line.componentsSeparatedByString(":").filter { $0 != "" }
    
    guard sections.count == 1 else {
        throw AssemblerError.LineParseError(
            error: "Line has more than 1 segment, you can have at most 1 label or 1 instruction per line; '\(sections)'"
        )
    }
    
    if line.line.isMatch("\\w:$") {
        let label = line.line
            .stringByTrimmingCharactersInSet(NSCharacterSet(charactersInString: ":"))
            .stringByTrimmingCharactersInSet(NSCharacterSet.whitespaceAndNewlineCharacterSet())
        
        guard label.isMatch("^\\w+$") else {
            throw AssemblerError.LineParseError(error: "The label has no text or contains non text characters; '\(label)'")
        }
        
        line.label = Label(identifier: label)
    } else {
        let instructionSegments = line.line.componentsSeparatedByString(" ").filter { $0.characters.count > 0 }
        
        line.instruction = try Instruction.get(instructionSegments)
    }
    
    return line
}

func calculateOffsets(lines: [Line]) -> [Line] {
    var offset: UInt = 0
    var offsetLines = [Line]()
    for var line in lines {
        line.offset = offset
        offsetLines.append(line)
        offset += line.size
    }
    
    return offsetLines
}

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

    let byteArray = bytes.flatMap { _, array in array }
        + [0xFEEBDAEDEADBEEF]
        + labels.map {$0.raw }
        + [0xFEEBDAEDEADBEEF]
    
    print("Intermediate Format")
    offsetInstructions.forEach { print($0) }
    print("")
    
    print("Raw bytes")
    bytes.forEach { print($0) }
    print(0xFEEBDAEDEADBEEF)
    labels.forEach { print("(\($0.identifier), \($0.raw))") }
    print(0xFEEBDAEDEADBEEF)
    print("")
    
    print(byteArray)
    
} catch let error {
    print(error)
}
