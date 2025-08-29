package com.jvmu.demos;

import com.jvmu.jitcompiler.JITCompilerAccess;
import com.jvmu.jitcompiler.CompilerController;

import java.lang.reflect.Method;

/**
 * JIT Compiler Access Demo - Demonstrates advanced JIT compilation control
 * 
 * This demo shows how to use the JITCompilerAccess API to:
 * - Control compilation levels for specific methods
 * - Monitor compilation statistics
 * - Apply compilation strategies
 * - Analyze method performance
 * - Force recompilation and deoptimization
 */
public class JITCompilerDemo {
    
    private static int counter = 0;
    private static long result = 0;
    
    public static void main(String[] args) {
        printHeader("JIT Compiler Access Demo");
        
        try {
            // Check if JIT compiler access is available
            if (!JITCompilerAccess.isAvailable()) {
                System.err.println("JIT Compiler access is not available!");
                System.err.println("Make sure ModuleBootstrap is properly initialized.");
                return;
            }
            
            System.out.println("SUCCESS: JIT Compiler Access Available!");
            System.out.println(JITCompilerAccess.getCompilationStatus());
            
            // Demo 1: Basic compilation control
            demonstrateBasicCompilationControl();
            
            // Demo 2: Compilation level management
            demonstrateCompilationLevels();
            
            // Demo 3: Method profiling and optimization
            demonstrateMethodProfiling();
            
            // Demo 4: Compiler controller usage
            demonstrateCompilerController();
            
            // Demo 5: Performance analysis
            demonstratePerformanceAnalysis();
            
            printFooter("JIT Compiler Demo Completed Successfully!");
            
        } catch (Exception e) {
            System.err.println("ERROR: Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateBasicCompilationControl() {
        printSection("Basic Compilation Control");
        
        try {
            Method testMethod = JITCompilerDemo.class.getDeclaredMethod("computeHeavy", int.class);
            
            // Check initial compilation state
            int initialLevel = JITCompilerAccess.getCompilationLevel(testMethod);
            System.out.println("Initial compilation level: " + initialLevel + " (" + 
                JITCompilerAccess.getLevelName(initialLevel) + ")");
            
            // Force compilation at different levels
            System.out.println("Forcing C1 compilation...");
            boolean c1Success = JITCompilerAccess.compileMethod(testMethod, JITCompilerAccess.COMP_LEVEL_SIMPLE);
            System.out.println("C1 compilation result: " + c1Success);
            
            System.out.println("Forcing C2 compilation...");
            boolean c2Success = JITCompilerAccess.compileMethod(testMethod, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
            System.out.println("C2 compilation result: " + c2Success);
            
            // Check final state
            int finalLevel = JITCompilerAccess.getCompilationLevel(testMethod);
            System.out.println("Final compilation level: " + finalLevel + " (" + 
                JITCompilerAccess.getLevelName(finalLevel) + ")");
                
        } catch (Exception e) {
            System.err.println("ERROR: Basic compilation control failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateCompilationLevels() {
        printSection("Compilation Level Management");
        
        try {
            Method loopMethod = JITCompilerDemo.class.getDeclaredMethod("loopingMethod", int.class);
            Method simpleMethod = JITCompilerDemo.class.getDeclaredMethod("simpleCalculation", double.class, double.class);
            
            // Test different compilation strategies
            System.out.println("Testing compilation levels:");
            
            // Level 1: C1 Simple
            JITCompilerAccess.compileMethod(loopMethod, JITCompilerAccess.COMP_LEVEL_SIMPLE);
            System.out.println("Loop method compiled at level 1: " + 
                JITCompilerAccess.getLevelName(JITCompilerAccess.getCompilationLevel(loopMethod)));
            
            // Level 3: C1 with profiling
            JITCompilerAccess.compileMethod(simpleMethod, JITCompilerAccess.COMP_LEVEL_FULL_PROFILE);
            System.out.println("Simple method compiled at level 3: " + 
                JITCompilerAccess.getLevelName(JITCompilerAccess.getCompilationLevel(simpleMethod)));
            
            // Run methods to generate activity
            warmupMethods();
            
            // Check compilation info after warmup
            printMethodCompilationInfo(loopMethod);
            printMethodCompilationInfo(simpleMethod);
            
        } catch (Exception e) {
            System.err.println("ERROR: Compilation level management failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateMethodProfiling() {
        printSection("Method Profiling and Data Management");
        
        try {
            Method profileMethod = JITCompilerDemo.class.getDeclaredMethod("profileTestMethod", int.class);
            
            System.out.println("Initial method state:");
            printMethodCompilationInfo(profileMethod);
            
            // Clear method data to start fresh
            System.out.println("Clearing method profiling data...");
            JITCompilerAccess.clearMethodData(profileMethod);
            
            // Set don't inline to prevent inlining optimization
            System.out.println("Setting dont-inline flag...");
            boolean dontInlineResult = JITCompilerAccess.setDontInline(profileMethod, true);
            System.out.println("Dont-inline set: " + dontInlineResult);
            
            // Run method multiple times to generate profiling data
            System.out.println("Generating profiling data...");
            for (int i = 0; i < 1000; i++) {
                profileTestMethod(i % 10);
            }
            
            System.out.println("Method state after profiling:");
            printMethodCompilationInfo(profileMethod);
            
        } catch (Exception e) {
            System.err.println("ERROR: Method profiling failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateCompilerController() {
        printSection("Compiler Controller Usage");
        
        try {
            CompilerController controller = CompilerController.getInstance();
            
            Method testMethod1 = JITCompilerDemo.class.getDeclaredMethod("testMethod1");
            Method testMethod2 = JITCompilerDemo.class.getDeclaredMethod("testMethod2");
            
            // Apply different strategies
            System.out.println("Applying compilation strategies...");
            
            boolean strategy1 = controller.applyStrategy(testMethod1, CompilerController.CompilationStrategy.AGGRESSIVE);
            System.out.println("Aggressive strategy applied to testMethod1: " + strategy1);
            
            boolean strategy2 = controller.applyStrategy(testMethod2, CompilerController.CompilationStrategy.PROFILE_GUIDED);
            System.out.println("Profile-guided strategy applied to testMethod2: " + strategy2);
            
            // Apply strategy by pattern
            int patternCount = controller.applyStrategyByPattern("*Demo*", "test*", 
                CompilerController.CompilationStrategy.C1_ONLY);
            System.out.println("Methods affected by pattern strategy: " + patternCount);
            
            // Generate compilation report
            System.out.println("Compilation Report:");
            System.out.println(controller.generateCompilationReport());
            
        } catch (Exception e) {
            System.err.println("ERROR: Compiler controller demo failed: " + e.getMessage());
        }
    }
    
    private static void demonstratePerformanceAnalysis() {
        printSection("Performance Analysis");
        
        try {
            Method heavyMethod = JITCompilerDemo.class.getDeclaredMethod("computeHeavy", int.class);
            CompilerController controller = CompilerController.getInstance();
            
            // Warm up the method
            System.out.println("Warming up method for performance analysis...");
            for (int i = 0; i < 500; i++) {
                computeHeavy(i);
            }
            
            // Analyze performance
            CompilerController.PerformanceAnalysis analysis = controller.analyzeMethodPerformance(heavyMethod);
            System.out.println("Performance Analysis Results:");
            System.out.println(analysis.toString());
            
            // Test optimization for startup
            Method[] essentialMethods = {
                JITCompilerDemo.class.getDeclaredMethod("simpleCalculation", double.class, double.class),
                JITCompilerDemo.class.getDeclaredMethod("testMethod1")
            };
            
            CompilerController.OptimizationReport report = controller.optimizeForStartup(essentialMethods);
            System.out.println("Startup Optimization Report:");
            System.out.println(report.toString());
            
        } catch (Exception e) {
            System.err.println("ERROR: Performance analysis failed: " + e.getMessage());
        }
    }
    
    // Test methods for compilation experiments
    
    public static long computeHeavy(int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(i * i + 1) * Math.sin(i);
            if (i % 100 == 0) {
                sum *= 1.001; // Prevent over-optimization
            }
        }
        result = sum; // Store to prevent dead code elimination
        return sum;
    }
    
    public static long loopingMethod(int count) {
        long total = 0;
        for (int i = 0; i < count; i++) {
            total += i * i;
            counter++; // Side effect to prevent optimization
        }
        return total;
    }
    
    public static double simpleCalculation(double a, double b) {
        return (a + b) * Math.PI / (a - b + 0.1);
    }
    
    public static int profileTestMethod(int input) {
        if (input < 5) {
            return input * 2;
        } else {
            return input * input;
        }
    }
    
    public static String testMethod1() {
        return "Test method 1 executed - counter: " + counter++;
    }
    
    public static String testMethod2() {
        return "Test method 2 executed - result: " + (result % 1000);
    }
    
    // Helper methods
    
    private static void warmupMethods() {
        System.out.println("Warming up methods...");
        
        for (int i = 0; i < 100; i++) {
            loopingMethod(50);
            simpleCalculation(i, i + 1);
            computeHeavy(10);
        }
        
        System.out.println("Warmup completed.");
    }
    
    private static void printMethodCompilationInfo(Method method) {
        JITCompilerAccess.CompilationInfo info = JITCompilerAccess.getCompilationInfo(method);
        System.out.println(method.getName() + ": " + info.toString());
    }
    
    private static void printHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("    " + title);
        System.out.println("    Advanced JIT Compilation Control Demo");
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println();
    }
    
    private static void printSection(String title) {
        System.out.println("\\n" + "-".repeat(50));
        System.out.println(">>> " + title);
        System.out.println("-".repeat(50));
    }
    
    private static void printFooter(String message) {
        System.out.println("\\n" + "=".repeat(60));
        System.out.println("SUCCESS: " + message);
        System.out.println("JIT Compiler control functionality validated!");
        System.out.println("=".repeat(60));
    }
}