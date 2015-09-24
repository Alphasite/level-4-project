//
//  StringExtensions.swift
//  Core
//
//  Created by Nishad Mathur on 07/09/2015.
//  Copyright © 2015 Nishad Mathur. All rights reserved.
//

import Foundation

public extension String {
    
    public var length: Int {
        get {
            return self.characters.count
        }
    }
    
    func contains(s: String) -> Bool {
        return (self.rangeOfString(s) != nil) ? true : false
    }
    
    func replace(target: String, withString: String) -> String {
        return self.stringByReplacingOccurrencesOfString(target, withString: withString, options: NSStringCompareOptions.LiteralSearch, range: nil)
    }
    
    subscript (i: Int) -> Character {
        get {
            let index = startIndex.advancedBy(i)
            return self[index]
        }
    }
    
    subscript (r: Range<Int>) -> String {
        get {
            let startIndex = self.startIndex.advancedBy(r.startIndex)
            let endIndex = self.startIndex.advancedBy(r.endIndex - 1)
            
            return self[Range(start: startIndex, end: endIndex)]
        }
    }
    
    func subString(startIndex: Int, length: Int) -> String {
        let start = self.startIndex.advancedBy(startIndex)
        let end = self.startIndex.advancedBy(startIndex + length)
        return self.substringWithRange(Range<String.Index>(start: start, end: end))
    }
    
    func indexOf(target: String) -> Int {
        if let range = self.rangeOfString(target) {
            return self.startIndex.distanceTo(range.startIndex)
        } else {
            return -1
        }
    }
    
    func indexOf(target: String, startIndex: Int) -> Int {
        let startRange = self.startIndex.advancedBy(startIndex)
        
        let range = self.rangeOfString(target, options: NSStringCompareOptions.LiteralSearch, range: Range<String.Index>(start: startRange, end: self.endIndex))
        
        if let range = range {
            return self.startIndex.distanceTo(range.startIndex)
        } else {
            return -1
        }
    }
    
    func lastIndexOf(target: String) -> Int {
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
    
    func isMatch(regex: String, options: NSRegularExpressionOptions = NSRegularExpressionOptions()) -> Bool {
        
        guard let exp = try? NSRegularExpression(pattern: regex, options: options) else {
            print("Error parsing regex")
            return false
        }
        
        let matchCount = exp.numberOfMatchesInString(self, options: NSMatchingOptions(), range: NSMakeRange(0, self.length))
        return matchCount > 0
    }
    
    func getMatches(regex: String, options: NSRegularExpressionOptions = NSRegularExpressionOptions()) -> [NSTextCheckingResult] {
        guard let exp = try? NSRegularExpression(pattern: regex, options: options) else {
            print("Error compiling regex")
            return []
        }
        
        let matches = exp.matchesInString(self, options: NSMatchingOptions(), range: NSMakeRange(0, self.length))
        return matches as [NSTextCheckingResult]
    }
    
    private var vowels: [String] {
        get {
            return ["a", "e", "i", "o", "u"]
        }
    }
    
    private var consonants: [String] {
        get {
            return ["b", "c", "d", "f", "g", "h", "j", "k", "l", "m", "n", "p", "q", "r", "s", "t", "v", "w", "x", "z"]
        }
    }
    
    func pluralize(count: Int) -> String {
        if count == 1 {
            return self
        } else {
            let lastChar = self.subString(self.length - 1, length: 1)
            let secondToLastChar = self.subString(self.length - 2, length: 1)

            let prefix: String
            let suffix: String
            if lastChar.lowercaseString == "y" && vowels.filter({x in x == secondToLastChar}).count == 0 {
                prefix = self[0...self.length - 1]
                suffix = "ies"
            } else if lastChar.lowercaseString == "s" || (lastChar.lowercaseString == "o" && consonants.filter({x in x == secondToLastChar}).count > 0) {
                prefix = self[0...self.length]
                suffix = "es"
            } else {
                prefix = self[0...self.length]
                suffix = "s"
            }
            
            return prefix + (lastChar != lastChar.uppercaseString ? suffix : suffix.uppercaseString)
        }
    }
}