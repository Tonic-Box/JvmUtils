package com.jvmu.classredefinition;

import java.lang.instrument.ClassDefinition;

/**
 * ClassRedefinitionAPI - Clean, modular API for class redefinition operations
 * 
 * Provides a unified interface for multiple class redefinition strategies including:
 * - Direct method replacement
 * - Constant pool manipulation 
 * - JVM internal replacement
 * - Memory-level bytecode replacement
 * - Compatibility mode with graceful fallbacks
 */
public class ClassRedefinitionAPI {
    
    private final ClassRedefinitionEngine engine;
    
    /**
     * Create a new ClassRedefinitionAPI instance
     */
    public ClassRedefinitionAPI() {
        this.engine = new ClassRedefinitionEngine();
    }
    
    /**
     * Perform class redefinition using the most appropriate strategy
     * 
     * @param definitions Array of class definitions to redefine
     * @return RedefinitionResult containing success status and details
     */
    public RedefinitionResult redefineClasses(ClassDefinition[] definitions) {
        return engine.performRedefinition(definitions);
    }
    
    /**
     * Perform class redefinition with specific strategy preference
     * 
     * @param definitions Array of class definitions to redefine
     * @param strategy Preferred redefinition strategy
     * @return RedefinitionResult containing success status and details
     */
    public RedefinitionResult redefineClasses(ClassDefinition[] definitions, RedefinitionStrategy strategy) {
        return engine.performRedefinition(definitions, strategy);
    }
    
    /**
     * Check if class redefinition is available and working
     * 
     * @return true if redefinition capabilities are functional
     */
    public boolean isRedefinitionAvailable() {
        return engine.isAvailable();
    }
    
    /**
     * Get detailed information about available redefinition strategies
     * 
     * @return RedefinitionCapabilities describing available functionality
     */
    public RedefinitionCapabilities getCapabilities() {
        return engine.getCapabilities();
    }
    
    /**
     * Enum defining available redefinition strategies
     */
    public enum RedefinitionStrategy {
        DIRECT_METHOD_REPLACEMENT,
        CONSTANT_POOL_MANIPULATION,
        JVM_INTERNAL_REPLACEMENT,
        MEMORY_LEVEL_REPLACEMENT,
        COMPATIBILITY_MODE,
        AUTO_SELECT
    }
    
    /**
     * Result of a class redefinition operation
     */
    public static class RedefinitionResult {
        private final boolean success;
        private final int classesProcessed;
        private final int classesSucceeded;
        private final RedefinitionStrategy strategyUsed;
        private final String details;
        private final Exception error;
        
        public RedefinitionResult(boolean success, int classesProcessed, int classesSucceeded, 
                                RedefinitionStrategy strategyUsed, String details, Exception error) {
            this.success = success;
            this.classesProcessed = classesProcessed;
            this.classesSucceeded = classesSucceeded;
            this.strategyUsed = strategyUsed;
            this.details = details;
            this.error = error;
        }
        
        public boolean isSuccess() { return success; }
        public int getClassesProcessed() { return classesProcessed; }
        public int getClassesSucceeded() { return classesSucceeded; }
        public RedefinitionStrategy getStrategyUsed() { return strategyUsed; }
        public String getDetails() { return details; }
        public Exception getError() { return error; }
        
        @Override
        public String toString() {
            return String.format("RedefinitionResult{success=%s, processed=%d, succeeded=%d, strategy=%s}", 
                               success, classesProcessed, classesSucceeded, strategyUsed);
        }
    }
    
    /**
     * Describes the capabilities of the redefinition system
     */
    public static class RedefinitionCapabilities {
        private final boolean directMethodReplacement;
        private final boolean constantPoolManipulation;
        private final boolean jvmInternalReplacement;
        private final boolean memoryLevelReplacement;
        private final boolean compatibilityMode;
        
        public RedefinitionCapabilities(boolean directMethodReplacement, boolean constantPoolManipulation,
                                      boolean jvmInternalReplacement, boolean memoryLevelReplacement,
                                      boolean compatibilityMode) {
            this.directMethodReplacement = directMethodReplacement;
            this.constantPoolManipulation = constantPoolManipulation;
            this.jvmInternalReplacement = jvmInternalReplacement;
            this.memoryLevelReplacement = memoryLevelReplacement;
            this.compatibilityMode = compatibilityMode;
        }
        
        public boolean hasDirectMethodReplacement() { return directMethodReplacement; }
        public boolean hasConstantPoolManipulation() { return constantPoolManipulation; }
        public boolean hasJvmInternalReplacement() { return jvmInternalReplacement; }
        public boolean hasMemoryLevelReplacement() { return memoryLevelReplacement; }
        public boolean hasCompatibilityMode() { return compatibilityMode; }
        
        @Override
        public String toString() {
            return String.format("RedefinitionCapabilities{direct=%s, constantPool=%s, jvmInternal=%s, memory=%s, compatibility=%s}",
                               directMethodReplacement, constantPoolManipulation, jvmInternalReplacement, 
                               memoryLevelReplacement, compatibilityMode);
        }
    }
}