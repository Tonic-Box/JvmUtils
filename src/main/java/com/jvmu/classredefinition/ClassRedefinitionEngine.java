package com.jvmu.classredefinition;

import com.jvmu.classredefinition.strategies.*;
import com.jvmu.internals.InternalUnsafe;
import java.lang.instrument.ClassDefinition;
import java.util.ArrayList;
import java.util.List;

/**
 * ClassRedefinitionEngine - Core engine that coordinates redefinition strategies
 * 
 * This class manages the execution of different redefinition strategies and
 * provides fallback mechanisms for maximum compatibility.
 */
class ClassRedefinitionEngine {
    
    private final List<RedefinitionStrategyImpl> strategies;
    private final BytecodeParser bytecodeParser;
    
    public ClassRedefinitionEngine() {
        this.strategies = initializeStrategies();
        this.bytecodeParser = new BytecodeParser();
    }
    
    /**
     * Initialize all available redefinition strategies in order of preference
     */
    private List<RedefinitionStrategyImpl> initializeStrategies() {
        List<RedefinitionStrategyImpl> strategyList = new ArrayList<>();
        
        // Add strategies in order of preference (most powerful first)
        strategyList.add(new DirectMethodReplacementStrategy());
        strategyList.add(new ConstantPoolManipulationStrategy());
        strategyList.add(new JvmInternalReplacementStrategy());
        strategyList.add(new MemoryLevelReplacementStrategy());
        strategyList.add(new CompatibilityModeStrategy());
        
        return strategyList;
    }
    
    /**
     * Perform redefinition using automatic strategy selection
     */
    public ClassRedefinitionAPI.RedefinitionResult performRedefinition(ClassDefinition[] definitions) {
        return performRedefinition(definitions, ClassRedefinitionAPI.RedefinitionStrategy.AUTO_SELECT);
    }
    
    /**
     * Perform redefinition with specific strategy preference
     */
    public ClassRedefinitionAPI.RedefinitionResult performRedefinition(ClassDefinition[] definitions, 
                                                                       ClassRedefinitionAPI.RedefinitionStrategy preferredStrategy) {
        
        System.out.println("[*] === ClassRedefinitionAPI: Starting Redefinition ===");
        System.out.println("[*] Classes to redefine: " + definitions.length);
        System.out.println("[*] Preferred strategy: " + preferredStrategy);
        
        int classesProcessed = 0;
        int classesSucceeded = 0;
        ClassRedefinitionAPI.RedefinitionStrategy strategyUsed = null;
        StringBuilder details = new StringBuilder();
        Exception lastError = null;
        
        try {
            // If specific strategy requested, try it first
            if (preferredStrategy != ClassRedefinitionAPI.RedefinitionStrategy.AUTO_SELECT) {
                RedefinitionStrategyImpl strategy = getStrategyByType(preferredStrategy);
                if (strategy != null && strategy.isAvailable()) {
                    RedefinitionResult result = executeStrategy(strategy, definitions);
                    if (result.success) {
                        return new ClassRedefinitionAPI.RedefinitionResult(
                            true, definitions.length, definitions.length, 
                            preferredStrategy, result.details, null);
                    }
                    details.append("Preferred strategy failed: ").append(result.details).append("; ");
                    lastError = result.error;
                }
            }
            
            // Try strategies in order until one succeeds
            for (RedefinitionStrategyImpl strategy : strategies) {
                if (!strategy.isAvailable()) {
                    continue;
                }
                
                System.out.println("[*] Attempting strategy: " + strategy.getStrategyName());
                RedefinitionResult result = executeStrategy(strategy, definitions);
                
                classesProcessed = result.classesProcessed;
                classesSucceeded += result.classesSucceeded;
                
                if (result.success && result.classesSucceeded > 0) {
                    strategyUsed = mapStrategyToEnum(strategy);
                    details.append(result.details);
                    
                    System.out.println("[+] SUCCESS: Strategy '" + strategy.getStrategyName() + "' succeeded");
                    return new ClassRedefinitionAPI.RedefinitionResult(
                        true, classesProcessed, classesSucceeded, 
                        strategyUsed, details.toString(), null);
                }
                
                details.append(strategy.getStrategyName()).append(": ").append(result.details).append("; ");
                if (result.error != null) {
                    lastError = result.error;
                }
                
                System.out.println("[*] Strategy '" + strategy.getStrategyName() + "' completed with " + 
                                 result.classesSucceeded + "/" + result.classesProcessed + " successes");
            }
            
            // If we get here, no strategy fully succeeded but we may have partial success
            boolean partialSuccess = classesSucceeded > 0;
            String finalDetails = details.toString() + " Processed: " + classesProcessed + ", Succeeded: " + classesSucceeded;
            
            System.out.println(partialSuccess ? "[+] PARTIAL SUCCESS" : "[!] ALL STRATEGIES FAILED");
            System.out.println("[*] Final result: " + classesSucceeded + "/" + classesProcessed + " classes succeeded");
            
            return new ClassRedefinitionAPI.RedefinitionResult(
                partialSuccess, classesProcessed, classesSucceeded, 
                strategyUsed != null ? strategyUsed : ClassRedefinitionAPI.RedefinitionStrategy.COMPATIBILITY_MODE, 
                finalDetails, lastError);
            
        } catch (Exception e) {
            System.err.println("[!] ClassRedefinitionEngine error: " + e.getMessage());
            return new ClassRedefinitionAPI.RedefinitionResult(
                false, classesProcessed, classesSucceeded, strategyUsed, 
                "Engine error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Execute a specific strategy on the given class definitions
     */
    private RedefinitionResult executeStrategy(RedefinitionStrategyImpl strategy, ClassDefinition[] definitions) {
        int processed = 0;
        int succeeded = 0;
        StringBuilder details = new StringBuilder();
        Exception error = null;
        
        try {
            for (ClassDefinition def : definitions) {
                processed++;
                
                Class<?> targetClass = def.getDefinitionClass();
                byte[] newBytecode = def.getDefinitionClassFile();
                
                // Parse bytecode if needed
                if (strategy.requiresBytecodeAnalysis()) {
                    if (!bytecodeParser.parse(newBytecode)) {
                        details.append("Bytecode parsing failed for ").append(targetClass.getName()).append("; ");
                        continue;
                    }
                }
                
                // Execute the strategy
                boolean result = strategy.redefineClass(targetClass, newBytecode, bytecodeParser);
                
                if (result) {
                    succeeded++;
                    details.append("SUCCESS: ").append(targetClass.getName()).append("; ");
                } else {
                    details.append("FAILED: ").append(targetClass.getName()).append("; ");
                }
            }
            
        } catch (Exception e) {
            error = e;
            details.append("Strategy exception: ").append(e.getMessage()).append("; ");
        }
        
        return new RedefinitionResult(succeeded > 0, processed, succeeded, details.toString(), error);
    }
    
    /**
     * Get strategy implementation by enum type
     */
    private RedefinitionStrategyImpl getStrategyByType(ClassRedefinitionAPI.RedefinitionStrategy strategyType) {
        for (RedefinitionStrategyImpl strategy : strategies) {
            if (mapStrategyToEnum(strategy) == strategyType) {
                return strategy;
            }
        }
        return null;
    }
    
    /**
     * Map strategy implementation to enum
     */
    private ClassRedefinitionAPI.RedefinitionStrategy mapStrategyToEnum(RedefinitionStrategyImpl strategy) {
        String name = strategy.getStrategyName();
        switch (name) {
            case "DirectMethodReplacement": return ClassRedefinitionAPI.RedefinitionStrategy.DIRECT_METHOD_REPLACEMENT;
            case "ConstantPoolManipulation": return ClassRedefinitionAPI.RedefinitionStrategy.CONSTANT_POOL_MANIPULATION;
            case "JvmInternalReplacement": return ClassRedefinitionAPI.RedefinitionStrategy.JVM_INTERNAL_REPLACEMENT;
            case "MemoryLevelReplacement": return ClassRedefinitionAPI.RedefinitionStrategy.MEMORY_LEVEL_REPLACEMENT;
            case "CompatibilityMode": return ClassRedefinitionAPI.RedefinitionStrategy.COMPATIBILITY_MODE;
            default: return ClassRedefinitionAPI.RedefinitionStrategy.AUTO_SELECT;
        }
    }
    
    /**
     * Check if redefinition is available
     */
    public boolean isAvailable() {
        // Check if we have InternalUnsafe and at least one working strategy
        if (!InternalUnsafe.isAvailable()) {
            return false;
        }
        
        for (RedefinitionStrategyImpl strategy : strategies) {
            if (strategy.isAvailable()) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Get capabilities of this engine
     */
    public ClassRedefinitionAPI.RedefinitionCapabilities getCapabilities() {
        boolean directMethod = false;
        boolean constantPool = false;
        boolean jvmInternal = false;
        boolean memoryLevel = false;
        boolean compatibility = false;
        
        for (RedefinitionStrategyImpl strategy : strategies) {
            if (!strategy.isAvailable()) continue;
            
            String name = strategy.getStrategyName();
            switch (name) {
                case "DirectMethodReplacement": directMethod = true; break;
                case "ConstantPoolManipulation": constantPool = true; break;
                case "JvmInternalReplacement": jvmInternal = true; break;
                case "MemoryLevelReplacement": memoryLevel = true; break;
                case "CompatibilityMode": compatibility = true; break;
            }
        }
        
        return new ClassRedefinitionAPI.RedefinitionCapabilities(
            directMethod, constantPool, jvmInternal, memoryLevel, compatibility);
    }
    
    /**
     * Internal result class for strategy execution
     */
    private static class RedefinitionResult {
        final boolean success;
        final int classesProcessed;
        final int classesSucceeded;
        final String details;
        final Exception error;
        
        RedefinitionResult(boolean success, int classesProcessed, int classesSucceeded, 
                          String details, Exception error) {
            this.success = success;
            this.classesProcessed = classesProcessed;
            this.classesSucceeded = classesSucceeded;
            this.details = details;
            this.error = error;
        }
    }
}