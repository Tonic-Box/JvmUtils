package com.jvmu.classredefinition.strategies;

import com.jvmu.classredefinition.BytecodeParser;

/**
 * RedefinitionStrategyImpl - Base interface for all class redefinition strategies
 * 
 * Each strategy implements a different approach to class redefinition,
 * allowing the system to try multiple methods until one succeeds.
 */
public interface RedefinitionStrategyImpl {
    
    /**
     * Get the name of this strategy
     * 
     * @return strategy name for identification and logging
     */
    String getStrategyName();
    
    /**
     * Check if this strategy is available on the current JVM
     * 
     * @return true if this strategy can be used
     */
    boolean isAvailable();
    
    /**
     * Check if this strategy requires bytecode analysis
     * 
     * @return true if the BytecodeParser should parse bytecode before calling redefineClass
     */
    boolean requiresBytecodeAnalysis();
    
    /**
     * Attempt to redefine a single class using this strategy
     * 
     * @param targetClass the class to redefine
     * @param newBytecode the new bytecode for the class
     * @param parser parsed bytecode information (may be null if not required)
     * @return true if redefinition succeeded
     */
    boolean redefineClass(Class<?> targetClass, byte[] newBytecode, BytecodeParser parser);
    
    /**
     * Get additional information about this strategy
     * 
     * @return description of what this strategy does
     */
    String getDescription();
}