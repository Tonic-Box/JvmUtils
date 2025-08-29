package com.jvmu.jitcompiler;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Compiler Controller - High-level JIT compilation management
 * 
 * This class provides convenient methods for managing JIT compilation
 * across the application, including batch operations, profiling control,
 * and compilation optimization strategies.
 */
public class CompilerController {
    
    private final Map<String, CompilationStrategy> strategies = new ConcurrentHashMap<>();
    private final Map<Method, CompilationProfile> profiles = new ConcurrentHashMap<>();
    private final AtomicLong operationCounter = new AtomicLong(0);
    
    // Default strategies
    public enum CompilationStrategy {
        AGGRESSIVE,      // Force C2 compilation early
        CONSERVATIVE,    // Let HotSpot decide naturally  
        PROFILE_GUIDED,  // Force profiling then C2
        INTERPRETER_ONLY,// Disable compilation
        C1_ONLY,        // Force C1 compilation only
        MIXED_MODE      // Intelligent level selection
    }
    
    private static final CompilerController INSTANCE = new CompilerController();
    
    public static CompilerController getInstance() {
        return INSTANCE;
    }
    
    private CompilerController() {
        // Initialize default strategies
        setupDefaultStrategies();
    }
    
    private void setupDefaultStrategies() {
        strategies.put("hot-methods", CompilationStrategy.AGGRESSIVE);
        strategies.put("initialization", CompilationStrategy.CONSERVATIVE);
        strategies.put("testing", CompilationStrategy.INTERPRETER_ONLY);
        strategies.put("profiling", CompilationStrategy.PROFILE_GUIDED);
    }
    
    /**
     * Apply compilation strategy to a method
     * @param method target method
     * @param strategy compilation strategy to apply
     * @return true if strategy was applied successfully
     */
    public boolean applyStrategy(Method method, CompilationStrategy strategy) {
        if (!JITCompilerAccess.isAvailable()) {
            return false;
        }
        
        CompilationProfile profile = profiles.computeIfAbsent(method, k -> new CompilationProfile());
        profile.strategy = strategy;
        profile.lastModified = System.currentTimeMillis();
        
        return executeStrategy(method, strategy);
    }
    
    /**
     * Apply compilation strategy to methods matching a pattern
     * @param classPattern class name pattern (supports wildcards)
     * @param methodPattern method name pattern (supports wildcards)
     * @param strategy compilation strategy
     * @return number of methods affected
     */
    public int applyStrategyByPattern(String classPattern, String methodPattern, CompilationStrategy strategy) {
        int count = 0;
        
        // Get all loaded classes
        List<Class<?>> allClasses = getAllLoadedClasses();
        
        for (Class<?> clazz : allClasses) {
            if (matchesPattern(clazz.getName(), classPattern)) {
                Method[] methods = clazz.getDeclaredMethods();
                for (Method method : methods) {
                    if (matchesPattern(method.getName(), methodPattern)) {
                        if (applyStrategy(method, strategy)) {
                            count++;
                        }
                    }
                }
            }
        }
        
        return count;
    }
    
    /**
     * Force compilation of hot methods based on invocation count
     * @param threshold minimum invocation count to consider hot
     * @return number of methods compiled
     */
    public int compileHotMethods(long threshold) {
        int count = 0;
        
        for (Map.Entry<Method, CompilationProfile> entry : profiles.entrySet()) {
            Method method = entry.getKey();
            CompilationProfile profile = entry.getValue();
            
            JITCompilerAccess.CompilationInfo info = JITCompilerAccess.getCompilationInfo(method);
            
            if (info.invocationCount >= threshold && info.compilationLevel < JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION) {
                if (JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION)) {
                    profile.hotMethodCompiled = true;
                    count++;
                }
            }
        }
        
        return count;
    }
    
    /**
     * Disable compilation for specific methods (useful for profiling/debugging)
     * @param methods methods to run in interpreter only
     * @return number of methods successfully disabled
     */
    public int disableCompilation(Method... methods) {
        int count = 0;
        
        for (Method method : methods) {
            if (applyStrategy(method, CompilationStrategy.INTERPRETER_ONLY)) {
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Force recompilation of methods to collect fresh profiling data
     * @param methods methods to recompile
     * @return number of methods successfully recompiled
     */
    public int forceRecompilation(Method... methods) {
        int count = 0;
        
        for (Method method : methods) {
            JITCompilerAccess.clearMethodData(method);
            if (JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_PROFILE)) {
                CompilationProfile profile = profiles.computeIfAbsent(method, k -> new CompilationProfile());
                profile.recompilationCount++;
                count++;
            }
        }
        
        return count;
    }
    
    /**
     * Optimize compilation for startup performance
     * @param essentialMethods methods needed during startup
     * @return optimization report
     */
    public OptimizationReport optimizeForStartup(Method... essentialMethods) {
        OptimizationReport report = new OptimizationReport();
        report.startTime = System.currentTimeMillis();
        
        // Disable compilation for non-essential methods initially
        int nonEssentialCount = applyStrategyByPattern("*", "*", CompilationStrategy.CONSERVATIVE);
        report.nonEssentialMethodsCount = nonEssentialCount;
        
        // Force compilation of essential methods
        for (Method method : essentialMethods) {
            if (JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_SIMPLE)) {
                report.essentialMethodsCompiled++;
            }
        }
        
        report.endTime = System.currentTimeMillis();
        report.success = true;
        
        return report;
    }
    
    /**
     * Generate compilation report for all tracked methods
     * @return detailed compilation report
     */
    public String generateCompilationReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== JIT Compilation Report ===\n");
        sb.append("Generated: ").append(new Date()).append("\n");
        sb.append("Tracked Methods: ").append(profiles.size()).append("\n");
        sb.append("Operations: ").append(operationCounter.get()).append("\n\n");
        
        // Group methods by compilation level
        Map<Integer, List<Method>> byLevel = new HashMap<>();
        
        for (Method method : profiles.keySet()) {
            int level = JITCompilerAccess.getCompilationLevel(method);
            byLevel.computeIfAbsent(level, k -> new ArrayList<>()).add(method);
        }
        
        for (Map.Entry<Integer, List<Method>> entry : byLevel.entrySet()) {
            int level = entry.getKey();
            List<Method> methods = entry.getValue();
            
            sb.append("Compilation Level ").append(level)
              .append(" (").append(JITCompilerAccess.getLevelName(level)).append("): ")
              .append(methods.size()).append(" methods\n");
            
            for (Method method : methods.subList(0, Math.min(5, methods.size()))) {
                sb.append("  - ").append(method.getDeclaringClass().getSimpleName())
                  .append(".").append(method.getName()).append("\n");
            }
            
            if (methods.size() > 5) {
                sb.append("  ... and ").append(methods.size() - 5).append(" more\n");
            }
            sb.append("\n");
        }
        
        // Add compiler statistics
        JITCompilerAccess.CompilerStatistics stats = JITCompilerAccess.getCompilerStatistics();
        sb.append("Compiler Statistics:\n");
        sb.append(stats.toString()).append("\n\n");
        
        return sb.toString();
    }
    
    /**
     * Monitor method performance and suggest optimization
     * @param method method to monitor
     * @return performance analysis
     */
    public PerformanceAnalysis analyzeMethodPerformance(Method method) {
        PerformanceAnalysis analysis = new PerformanceAnalysis();
        analysis.method = method;
        analysis.timestamp = System.currentTimeMillis();
        
        JITCompilerAccess.CompilationInfo info = JITCompilerAccess.getCompilationInfo(method);
        analysis.compilationInfo = info;
        
        CompilationProfile profile = profiles.get(method);
        if (profile != null) {
            analysis.profile = profile;
        }
        
        // Generate recommendations
        analysis.recommendations = generateRecommendations(method, info, profile);
        
        return analysis;
    }
    
    private boolean executeStrategy(Method method, CompilationStrategy strategy) {
        operationCounter.incrementAndGet();
        
        switch (strategy) {
            case AGGRESSIVE:
                return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
                
            case CONSERVATIVE:
                // Let HotSpot decide - no forced compilation
                return true;
                
            case PROFILE_GUIDED:
                // First compile with profiling, then optimize
                JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_PROFILE);
                return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
                
            case INTERPRETER_ONLY:
                JITCompilerAccess.clearMethodData(method);
                return JITCompilerAccess.setCompilationThreshold(method, Integer.MAX_VALUE);
                
            case C1_ONLY:
                return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_SIMPLE);
                
            case MIXED_MODE:
                // Intelligent selection based on method characteristics
                return applyIntelligentStrategy(method);
                
            default:
                return false;
        }
    }
    
    private boolean applyIntelligentStrategy(Method method) {
        // Simple heuristics for demonstration
        if (method.getName().contains("init") || method.getName().contains("setup")) {
            return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_SIMPLE);
        }
        
        if (method.getParameterCount() == 0 && method.getName().length() < 5) {
            return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
        }
        
        // Default to profiling
        return JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_PROFILE);
    }
    
    private List<String> generateRecommendations(Method method, JITCompilerAccess.CompilationInfo info, CompilationProfile profile) {
        List<String> recommendations = new ArrayList<>();
        
        if (info.invocationCount > 1000 && info.compilationLevel < JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION) {
            recommendations.add("Consider forcing C2 compilation for this hot method");
        }
        
        if (info.backedgeCount > 500) {
            recommendations.add("High loop activity detected - ensure OSR compilation is enabled");
        }
        
        if (profile != null && profile.recompilationCount > 3) {
            recommendations.add("Frequent recompilation detected - check for unstable code paths");
        }
        
        if (recommendations.isEmpty()) {
            recommendations.add("Method appears optimally compiled");
        }
        
        return recommendations;
    }
    
    private boolean matchesPattern(String text, String pattern) {
        if (pattern.equals("*")) return true;
        return text.toLowerCase().contains(pattern.toLowerCase().replace("*", ""));
    }
    
    private List<Class<?>> getAllLoadedClasses() {
        // This would typically use JVM internals to get all loaded classes
        // For now, return empty list as placeholder
        return new ArrayList<>();
    }
    
    /**
     * Compilation profile for tracking method compilation history
     */
    public static class CompilationProfile {
        public CompilationStrategy strategy;
        public long lastModified;
        public int recompilationCount = 0;
        public boolean hotMethodCompiled = false;
        public long totalCompilationTime = 0;
        
        @Override
        public String toString() {
            return String.format("CompilationProfile[strategy=%s, recompilations=%d, hot=%b]", 
                strategy, recompilationCount, hotMethodCompiled);
        }
    }
    
    /**
     * Optimization report for bulk operations
     */
    public static class OptimizationReport {
        public long startTime;
        public long endTime;
        public boolean success;
        public int essentialMethodsCompiled = 0;
        public int nonEssentialMethodsCount = 0;
        public String errorMessage;
        
        public long getDurationMs() {
            return endTime - startTime;
        }
        
        @Override
        public String toString() {
            return String.format("OptimizationReport[duration=%d ms, essential=%d, nonEssential=%d, success=%b]",
                getDurationMs(), essentialMethodsCompiled, nonEssentialMethodsCount, success);
        }
    }
    
    /**
     * Performance analysis for individual methods
     */
    public static class PerformanceAnalysis {
        public Method method;
        public long timestamp;
        public JITCompilerAccess.CompilationInfo compilationInfo;
        public CompilationProfile profile;
        public List<String> recommendations;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("PerformanceAnalysis for ").append(method.getName()).append(":\n");
            sb.append("  ").append(compilationInfo).append("\n");
            if (profile != null) {
                sb.append("  ").append(profile).append("\n");
            }
            sb.append("  Recommendations:\n");
            for (String rec : recommendations) {
                sb.append("    - ").append(rec).append("\n");
            }
            return sb.toString();
        }
    }
}