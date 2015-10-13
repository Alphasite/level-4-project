import Foundation

public enum DataSource {
    case register(UInt)
    case literal(UInt)
    case label(String)
    indirect case memory(source: DataSource, offset: DataSource)
    
    private static let memoryRegex = "(\\d+|r\\d+|#\\w+)\\[(\\d+|r\\d+)\\]"
    
    public var raw: [UInt]? {
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
    
    public static func get(source: String) throws -> DataSource {
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
            

            if case .memory = sourceAddress {
                throw AssemblerError.DataSourceParseError(error: "Memory data sources cannot be nested")
            }
            

            if case .memory = offsetAddress {
                throw AssemblerError.DataSourceParseError(error: "Memory data sources cannot be nested")
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
