//
//  StringExtensions.swift
//  Core
//
//  Based on https://gist.github.com/albertbori/0faf7de867d96eb83591
//  But rewritten with to function in swift 2
//

import Foundation

public extension String {
    
    init(seperator: String = "\n", _ lines: String...){
        self = lines.joinWithSeparator(seperator)
        print(self)
    }
    
    public var length: Int {
        get {
            return self.characters.count
        }
    }
    
    public func contains(s: String) -> Bool {
        return self.rangeOfString(s) != nil
    }
    
    public func replace(target: String, withString: String) -> String {
        return self.stringByReplacingOccurrencesOfString(
            target,
            withString: withString,
            options: NSStringCompareOptions.LiteralSearch,
            range: nil
        )
    }
    
    public subscript (i: Int) -> Character {
        get {
            let index = startIndex.advancedBy(i)
            return self[index]
        }
    }
    
    public subscript (r: Range<Int>) -> String {
        get {
            let startIndex = self.startIndex.advancedBy(r.startIndex)
            let endIndex = self.startIndex.advancedBy(r.endIndex - 1)
            
            return self[Range(start: startIndex, end: endIndex)]
        }
    }
    
    public func subString(startIndex: Int, length: Int) -> String {
        let start = self.startIndex.advancedBy(startIndex)
        let end = self.startIndex.advancedBy(startIndex + length)
        return self.substringWithRange(Range<String.Index>(start: start, end: end))
    }
    
    public func indexOf(target: String) -> Int {
        if let range = self.rangeOfString(target) {
            return self.startIndex.distanceTo(range.startIndex)
        } else {
            return -1
        }
    }
    
    public func indexOf(target: String, startIndex: Int) -> Int {
        let startRange = self.startIndex.advancedBy(startIndex)
        
        let range = self.rangeOfString(target, options: NSStringCompareOptions.LiteralSearch, range: Range<String.Index>(start: startRange, end: self.endIndex))
        
        if let range = range {
            return self.startIndex.distanceTo(range.startIndex)
        } else {
            return -1
        }
    }
    
    public func lastIndexOf(target: String) -> Int {
        var index = -1
        var stepIndex = self.indexOf(target)
        
        while stepIndex > -1 {
            index = stepIndex
            
            if stepIndex + target.length < self.length {
                stepIndex = indexOf(target, startIndex: stepIndex + target.length)
            } else {
                stepIndex = -1
            }
        }
        return index
    }
    
    public func isMatch(regex: String, options: NSRegularExpressionOptions = NSRegularExpressionOptions()) -> Bool {
        
        guard let exp = try? NSRegularExpression(pattern: regex, options: options) else {
            print("Error parsing regex")
            return false
        }
        
        let matchCount = exp.numberOfMatchesInString(
            self,
            options: NSMatchingOptions(),
            range: NSMakeRange(0, self.length)
        )
        
        return matchCount > 0
    }
    
    public func getMatches(regex: String, options: NSRegularExpressionOptions = NSRegularExpressionOptions()) -> [NSTextCheckingResult] {
        
        guard let exp = try? NSRegularExpression(pattern: regex, options: options) else {
            print("Error compiling regex")
            return []
        }
        
        let matches = exp.matchesInString(self, options: NSMatchingOptions(), range: NSMakeRange(0, self.length))
        return matches as [NSTextCheckingResult]
    }
    
    public func getMatchesAsStrings(regex: String, options: NSRegularExpressionOptions = NSRegularExpressionOptions()) -> [String] {
        return self
            .getMatches(regex, options: options)
            .map { (result: NSTextCheckingResult) in return (self as NSString).substringWithRange(result.rangeAtIndex(0)) }
    }
}