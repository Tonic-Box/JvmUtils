package com.jvmu.classredefinition;

/**
 * BytecodeParser - Utility for parsing and analyzing bytecode for redefinition
 * 
 * Provides basic bytecode parsing capabilities for redefinition strategies
 * that need to analyze the structure of new bytecode.
 */
public class BytecodeParser {
    
    private byte[] currentBytecode;
    private boolean validClassFile;
    private int classVersion;
    
    /**
     * Parse the given bytecode
     * 
     * @param bytecode The bytecode to parse
     * @return true if parsing was successful
     */
    public boolean parse(byte[] bytecode) {
        this.currentBytecode = bytecode;
        this.validClassFile = false;
        this.classVersion = 0;
        
        try {
            if (bytecode == null || bytecode.length < 10) {
                return false;
            }
            
            // Check for Java class file magic number (0xCAFEBABE)
            if (bytecode.length >= 4) {
                int magic = ((bytecode[0] & 0xFF) << 24) |
                           ((bytecode[1] & 0xFF) << 16) |
                           ((bytecode[2] & 0xFF) << 8) |
                           (bytecode[3] & 0xFF);
                
                if (magic == 0xCAFEBABE) {
                    this.validClassFile = true;
                    
                    // Extract class version if available
                    if (bytecode.length >= 8) {
                        this.classVersion = ((bytecode[6] & 0xFF) << 8) | (bytecode[7] & 0xFF);
                    }
                    
                    return true;
                } else {
                    // Not a valid class file, but might be test data
                    // Allow parsing to continue for compatibility
                    this.validClassFile = false;
                    return true;
                }
            }
            
            return false;
            
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Check if the parsed bytecode represents a valid Java class file
     * 
     * @return true if this is a valid class file
     */
    public boolean isValidClassFile() {
        return validClassFile;
    }
    
    /**
     * Get the class file version
     * 
     * @return class file version, or 0 if not available
     */
    public int getClassVersion() {
        return classVersion;
    }
    
    /**
     * Get the size of the parsed bytecode
     * 
     * @return bytecode size in bytes
     */
    public int getBytecodeSize() {
        return currentBytecode != null ? currentBytecode.length : 0;
    }
    
    /**
     * Get the raw bytecode
     * 
     * @return the raw bytecode array
     */
    public byte[] getRawBytecode() {
        return currentBytecode;
    }
    
    /**
     * Get a description of the parsed bytecode
     * 
     * @return human-readable description
     */
    public String getDescription() {
        if (currentBytecode == null) {
            return "No bytecode parsed";
        }
        
        StringBuilder desc = new StringBuilder();
        desc.append("Bytecode: ").append(currentBytecode.length).append(" bytes");
        
        if (validClassFile) {
            desc.append(", valid class file");
            if (classVersion > 0) {
                desc.append(", version ").append(classVersion);
            }
        } else {
            desc.append(", not a class file (possibly test data)");
        }
        
        return desc.toString();
    }
    
    /**
     * Check if the bytecode contains specific patterns or signatures
     * 
     * @param pattern the pattern to search for
     * @return true if pattern is found
     */
    public boolean containsPattern(byte[] pattern) {
        if (currentBytecode == null || pattern == null) {
            return false;
        }
        
        for (int i = 0; i <= currentBytecode.length - pattern.length; i++) {
            boolean match = true;
            for (int j = 0; j < pattern.length; j++) {
                if (currentBytecode[i + j] != pattern[j]) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return true;
            }
        }
        
        return false;
    }
}