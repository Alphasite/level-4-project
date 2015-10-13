import Foundation

public enum AssemblerError: ErrorType {
    case InstructionParseError(error: String)
    case DataSourceParseError(error: String)
    case LineParseError(error: String)
    case UndeclaredLabelError(error: String)
    case IncorrectTypeError(error: String)
    case AbstractInstructionInstantiationError(error: String)
}

public class Label {
    // Look into using a class and removing this.
    static var NextIdentifier: UInt = 0
    static var IdentifierTable = [String: UInt]()
    
    public let identifier: String
    public var offset: UInt?
    
    public var linkIdentifier: UInt? {
        return Label.IdentifierTable[self.identifier]
    }
    
    public var raw: [UInt] {
        return [self.linkIdentifier ?? UInt.max, self.offset ?? UInt.max]
    }
    
    public init(identifier: String) {
        self.identifier = identifier
        Label.IdentifierTable[identifier] = Label.NextIdentifier++
    }
}

public struct Line {
    public let lineNumber: UInt
    public let line: String
    public var label: Label?
    public var instruction: Instruction?
    public var offset: UInt? {
        didSet {
            label?.offset = offset
        }
    }
        
    public var size: UInt {
        return instruction?.size ?? 0
    }
    
    public init(lineNumber: UInt, line: String) {
        self.lineNumber = lineNumber
        self.line = line
    }
}

public func parseLine(var line: Line) throws -> Line {
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

public func calculateOffsets(lines: [Line]) -> [Line] {
    var offset: UInt = 0
    var offsetLines = [Line]()
    for var line in lines {
        line.offset = offset
        offsetLines.append(line)
        offset += line.size
    }
    
    return offsetLines
}