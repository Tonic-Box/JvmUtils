package com.jvmu.jdi.types;

import com.jvmu.jdi.vm.SelfVirtualMachine;

/**
 * Self Location - JDI-like Location for code position representation
 */
public class SelfLocation {
    
    private final SelfVirtualMachine vm;
    private final SelfMethod method;
    private final int lineNumber;
    private final long codeIndex;
    
    public SelfLocation(SelfVirtualMachine vm, SelfMethod method, int lineNumber) {
        this(vm, method, lineNumber, 0);
    }
    
    public SelfLocation(SelfVirtualMachine vm, SelfMethod method, int lineNumber, long codeIndex) {
        this.vm = vm;
        this.method = method;
        this.lineNumber = lineNumber;
        this.codeIndex = codeIndex;
    }
    
    /**
     * Get the method containing this location
     */
    public SelfMethod method() {
        return method;
    }
    
    /**
     * Get the declaring type
     */
    public SelfReferenceType declaringType() {
        return method.declaringType();
    }
    
    /**
     * Get line number
     */
    public int lineNumber() {
        return lineNumber;
    }
    
    /**
     * Get code index (bytecode offset)
     */
    public long codeIndex() {
        return codeIndex;
    }
    
    /**
     * Get source name
     */
    public String sourceName() {
        return method.declaringType().sourceName();
    }
    
    /**
     * Get source path
     */
    public String sourcePath() {
        return method.declaringType().sourcePaths("Java").get(0);
    }
    
    /**
     * Get source path for stratum
     */
    public String sourcePath(String stratum) {
        return method.declaringType().sourcePaths(stratum).get(0);
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfLocation)) return false;
        SelfLocation other = (SelfLocation) obj;
        return method.equals(other.method) && 
               lineNumber == other.lineNumber && 
               codeIndex == other.codeIndex;
    }
    
    @Override
    public int hashCode() {
        return method.hashCode() ^ Integer.hashCode(lineNumber) ^ Long.hashCode(codeIndex);
    }
    
    @Override
    public String toString() {
        return String.format("SelfLocation[%s:%d @%d]", 
            method.declaringType().name() + "." + method.name(), 
            lineNumber, 
            codeIndex);
    }
}