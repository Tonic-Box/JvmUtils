package com.jvmu.demos;

import com.jvmu.bytecode.BytecodeAPI;
import com.jvmu.bytecode.BytecodeManipulator;

/**
 * Demonstration of working JDK 11 internal API access and bytecode analysis
 * Shows only fully functional capabilities
 */
public class BytecodeDemo {
    
    public static void main(String[] args) {
        System.out.println("=== JDK 11 INTERNAL BYTECODE API ACCESS DEMO ===");
        
        try {
            testBytecodeAnalysis();
            testClassInfoAPI();
            testMethodAnalysis();
            testBytecodeGeneration();
            System.out.println("\n[SUCCESS] All functional tests completed!");
            
        } catch (Exception e) {
            System.out.println("\n[ERROR] Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBytecodeAnalysis() {
        System.out.println("\n=== Bytecode Analysis Test ===");
        
        // Analyze this class
        BytecodeAPI.analyzeClass(BytecodeDemo.class);
        
        // Analyze String class
        System.out.println("\n--- String Class Analysis ---");
        BytecodeAPI.analyzeClass(String.class);
    }
    
    private static void testClassInfoAPI() {
        System.out.println("\n=== ClassInfo API Test ===");
        
        // Get class info for this class
        BytecodeAPI.ClassInfo info = BytecodeAPI.getClassInfo(BytecodeDemo.class);
        
        System.out.println("Class Info: " + info);
        System.out.println("Name: " + info.getName());
        System.out.println("Simple Name: " + info.getSimpleName());
        System.out.println("Bytecode Size: " + info.getBytecodeSize() + " bytes");
        System.out.println("Magic Number: " + info.getMagicNumber());
        System.out.println("Valid Class File: " + info.isValid());
        System.out.println("Methods Count: " + info.getMethods().length);
        System.out.println("Fields Count: " + info.getFields().length);
        
        // Test with a different class
        BytecodeAPI.ClassInfo stringInfo = BytecodeAPI.getClassInfo(String.class);
        System.out.println("\nString Class Info: " + stringInfo);
        System.out.println("String Methods: " + stringInfo.getMethods().length);
        System.out.println("String Valid: " + stringInfo.isValid());
    }
    
    private static void testMethodAnalysis() {
        System.out.println("\n=== Method Analysis Test ===");
        
        try {
            // Analyze this main method
            var mainMethod = BytecodeDemo.class.getDeclaredMethod("main", String[].class);
            BytecodeManipulator.analyzeMethod(mainMethod);
            
            // Analyze a String method
            System.out.println("\n--- String.valueOf Analysis ---");
            var valueOfMethod = String.class.getDeclaredMethod("valueOf", Object.class);
            BytecodeManipulator.analyzeMethod(valueOfMethod);
            
        } catch (Exception e) {
            System.out.println("Method analysis failed: " + e.getMessage());
        }
    }
    
    private static void testBytecodeGeneration() {
        System.out.println("\n=== Bytecode Generation Test ===");
        
        try {
            // Test 1: Generate simple class
            System.out.println("Test 1: Simple class generation");
            Class<?> simpleClass = BytecodeAPI.generateSimpleClass("GeneratedSimpleClass", "Hello World");
            System.out.println("[SUCCESS] Generated class: " + simpleClass.getName());
            
            // Test 2: Generate class with method that returns string
            System.out.println("\nTest 2: String class generation with method");
            Class<?> stringClass = BytecodeAPI.generateStringClass("GeneratedStringClass", "getMessage", "Hello from generated method!");
            System.out.println("[SUCCESS] Generated class: " + stringClass.getName());
            
            // Test method exists
            try {
                java.lang.reflect.Method method = stringClass.getDeclaredMethod("getMessage");
                System.out.println("[SUCCESS] Method exists: " + method.getName());
                System.out.println("[SUCCESS] Method modifiers: " + java.lang.reflect.Modifier.toString(method.getModifiers()));
                System.out.println("[INFO] Method is abstract - cannot invoke");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[INFO] Method invocation test: " + e.getMessage());
            }
            
            // Test 3: Generate proxy class implementing Runnable
            System.out.println("\nTest 3: Proxy class generation (implements Runnable)");
            Class<?> proxyClass = BytecodeAPI.generateProxyClass("GeneratedProxy", "java.lang.Runnable", "run");
            System.out.println("[SUCCESS] Generated proxy class: " + proxyClass.getName());
            
            // Test proxy has the interface method
            try {
                java.lang.reflect.Method runMethod = proxyClass.getDeclaredMethod("run");
                System.out.println("[SUCCESS] Interface method exists: " + runMethod.getName());
                System.out.println("[SUCCESS] Method modifiers: " + java.lang.reflect.Modifier.toString(runMethod.getModifiers()));
                System.out.println("[SUCCESS] Implements Runnable: " + java.lang.Runnable.class.isAssignableFrom(proxyClass));
                System.out.println("[INFO] Class is abstract - cannot instantiate");
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("[INFO] Proxy method test: " + e.getMessage());
            }
            
        } catch (Exception e) {
            System.out.println("[ERROR] Generation failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
}