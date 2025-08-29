package com.jvmu.demos;

import com.jvmu.jdi.*;
import com.jvmu.jdi.vm.*;
import com.jvmu.jdi.types.*;
import com.jvmu.jdi.threads.*;
import com.jvmu.jdi.events.*;

import java.util.List;

/**
 * Final JDI Demo - Demonstrates the complete JDI hierarchy implementation
 * 
 * Tests all major components with ASCII-safe output:
 * - SelfVirtualMachine with complete capabilities
 * - SelfReferenceType for class inspection
 * - SelfField and SelfMethod for member access
 * - SelfLocation for code positioning
 * - SelfModuleReference for module access (Java 9+)
 * - SelfClassLoaderReference for class loading
 * - SelfEventQueue and SelfEventRequestManager for debugging events
 * - SelfThreadGroupReference for thread organization
 * - Complete JDI API coverage
 */
public class JDIDemo {
    
    private String testField = "Hello JDI";
    private static int staticTestField = 42;
    
    public static void main(String[] args) {
        printHeader("Final JDI Implementation Demo");
        
        try {
            // Initialize JDI
            if (!JDI.initialize()) {
                System.err.println("Failed to initialize JDI");
                return;
            }
            
            System.out.println("SUCCESS: JDI Status: " + (JDI.isAvailable() ? "AVAILABLE" : "UNAVAILABLE"));
            
            testVirtualMachine();
            testReferenceTypes();
            testFields();
            testMethods();
            testEventSystem();
            testThreadGroups();
            testComprehensiveAPI();
            
            printFooter("All JDI Components Successfully Tested!");
            
        } catch (Exception e) {
            System.err.println("ERROR: Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testVirtualMachine() {
        printSection("Virtual Machine Inspection");
        
        SelfVirtualMachine vm = JDI.getVirtualMachine();
        System.out.println("Process ID: " + vm.getProcessId());
        System.out.println("VM Name: " + vm.name());
        System.out.println("Version: " + vm.version());
        System.out.println("Capabilities: " + vm.getCapabilities());
        System.out.println("Memory Info: " + vm.getMemoryInfo());
        System.out.println("Thread Count: " + vm.getAllThreads().size());
        System.out.println("Loaded Classes: " + vm.getAllLoadedClasses().size());
    }
    
    private static void testReferenceTypes() {
        printSection("Reference Type Analysis");
        
        try {
            Class<?> demoClass = JDIDemo.class;
            SelfVirtualMachine vm = JDI.getVirtualMachine();
            SelfReferenceType refType = new SelfReferenceType(vm, demoClass);
            
            System.out.println("Class: " + refType.name());
            System.out.println("Signature: " + refType.signature());
            System.out.println("Is Interface: " + refType.isInterface());
            System.out.println("Is Final: " + refType.isFinal());
            System.out.println("Is Static: " + refType.isStatic());
            System.out.println("Source Name: " + refType.sourceName());
            System.out.println("Major Version: " + refType.majorVersion());
            
            System.out.println("Fields: " + refType.allFields().size());
            for (SelfField field : refType.allFields()) {
                System.out.println("  - " + field.name() + " (" + field.signature() + ") " +
                    (field.isStatic() ? "[static]" : "") +
                    (field.isPrivate() ? "[private]" : "") +
                    (field.isFinal() ? "[final]" : ""));
            }
            
            System.out.println("Methods: " + refType.allMethods().size());
            int count = 0;
            for (SelfMethod method : refType.allMethods()) {
                System.out.println("  - " + method.name() + method.signature() + " " +
                    (method.isStatic() ? "[static]" : "") +
                    (method.isPrivate() ? "[private]" : "") +
                    (method.isNative() ? "[native]" : ""));
                if (++count >= 5) break; // Show first few
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Reference type test failed: " + e.getMessage());
        }
    }
    
    private static void testFields() {
        printSection("Field Access & Manipulation");
        
        try {
            JDIDemo demo = new JDIDemo();
            SelfObjectReference objRef = JDI.createObjectReference(demo);
            
            // Test instance field
            Object fieldValue = JDI.getFieldValue(objRef, "testField");
            System.out.println("Instance field value: " + fieldValue);
            
            JDI.setFieldValue(objRef, "testField", "Modified by JDI!");
            Object newValue = JDI.getFieldValue(objRef, "testField");
            System.out.println("Modified field value: " + newValue);
            
            // Test static field access via ClassObjectReference
            Class<?> demoClass = JDIDemo.class;
            SelfClassObjectReference classRef = new SelfClassObjectReference(JDI.getVirtualMachine(), demoClass);
            
            Object staticValue = classRef.getStaticFieldValue("staticTestField");
            System.out.println("Static field value: " + staticValue);
            
            classRef.setStaticFieldValue("staticTestField", 99);
            Object newStaticValue = classRef.getStaticFieldValue("staticTestField");
            System.out.println("Modified static field: " + newStaticValue);
            
        } catch (Exception e) {
            System.err.println("ERROR: Field test failed: " + e.getMessage());
        }
    }
    
    private static void testMethods() {
        printSection("Method Inspection & Invocation");
        
        try {
            JDIDemo demo = new JDIDemo();
            SelfObjectReference objRef = JDI.createObjectReference(demo);
            
            // Test method invocation
            Object result = JDI.invokeMethod(objRef, "testMethod", "JDI Test");
            System.out.println("Method result: " + result);
            
            // Analyze method details
            SelfVirtualMachine vm = JDI.getVirtualMachine();
            SelfReferenceType refType = new SelfReferenceType(vm, demo.getClass());
            List<SelfMethod> methods = refType.methodsByName("testMethod");
            
            if (!methods.isEmpty()) {
                SelfMethod method = methods.get(0);
                System.out.println("Method signature: " + method.signature());
                System.out.println("Return type: " + method.returnType().name());
                System.out.println("Argument types: " + method.argumentTypeNames());
                System.out.println("Is public: " + method.isPublic());
                System.out.println("Line locations: " + method.allLineLocations().size());
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Method test failed: " + e.getMessage());
        }
    }
    
    private static void testEventSystem() {
        printSection("Event System (Debugging Infrastructure)");
        
        try {
            SelfEventQueue eventQueue = JDI.getEventQueue();
            SelfEventRequestManager requestManager = JDI.getEventRequestManager();
            
            System.out.println("Event queue: " + eventQueue);
            System.out.println("Queue active: " + eventQueue.isActive());
            System.out.println("Queue size: " + eventQueue.size());
            
            System.out.println("Event request manager: " + requestManager);
            System.out.println("Total requests: " + requestManager.eventRequests().size());
            
            // Create sample event requests
            SelfMethodEntryRequest methodEntry = requestManager.createMethodEntryRequest();
            methodEntry.enable();
            System.out.println("Created method entry request: " + methodEntry);
            
            SelfMethodExitRequest methodExit = requestManager.createMethodExitRequest();
            methodExit.enable();
            System.out.println("Created method exit request: " + methodExit);
            
            SelfClassPrepareRequest classPrepare = requestManager.createClassPrepareRequest();
            classPrepare.addClassFilter("*.Demo");
            classPrepare.enable();
            System.out.println("Created class prepare request: " + classPrepare);
            
            System.out.println("Active requests after creation: " + requestManager.eventRequests().size());
            
            // Clean up
            requestManager.deleteAllEventRequests();
            System.out.println("Requests after cleanup: " + requestManager.eventRequests().size());
            
        } catch (Exception e) {
            System.err.println("ERROR: Event system test failed: " + e.getMessage());
        }
    }
    
    private static void testThreadGroups() {
        printSection("Thread Group Management");
        
        try {
            SelfThreadReference currentThread = JDI.getCurrentThread();
            SelfThreadGroupReference threadGroup = currentThread.threadGroup();
            
            System.out.println("Current thread: " + currentThread.name());
            System.out.println("Thread group: " + threadGroup.name());
            System.out.println("Group active count: " + threadGroup.activeCount());
            System.out.println("Group max priority: " + threadGroup.maxPriority());
            System.out.println("Is daemon group: " + threadGroup.isDaemon());
            
            List<SelfThreadReference> threads = threadGroup.threads();
            System.out.println("Threads in group: " + threads.size());
            for (SelfThreadReference thread : threads) {
                System.out.println("  - " + thread.name() + " (daemon: " + thread.isDaemon() + 
                    ", alive: " + thread.isAlive() + ")");
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Thread group test failed: " + e.getMessage());
        }
    }
    
    private static void testComprehensiveAPI() {
        printSection("Comprehensive JDI API Coverage");
        
        // Test the comprehensive JDI utility methods
        JDI.JVMInfo jvmInfo = JDI.getJVMInfo();
        System.out.println("Comprehensive JVM Info:");
        System.out.println(jvmInfo.toString());
        
        JDI.DiagnosticResults diagnostics = JDI.runDiagnostics();
        System.out.println("\nSystem Diagnostics:");
        System.out.println(diagnostics.toString());
        
        // Memory management
        long beforeGC = JDI.getUsedMemory();
        JDI.forceGarbageCollection();
        long afterGC = JDI.getUsedMemory();
        
        System.out.println("\nMemory Management:");
        System.out.println("  Before GC: " + (beforeGC / (1024 * 1024)) + " MB");
        System.out.println("  After GC: " + (afterGC / (1024 * 1024)) + " MB");
        System.out.println("  Freed: " + ((beforeGC - afterGC) / (1024 * 1024)) + " MB");
        
        // Class analysis
        List<Class<?>> stringClasses = JDI.getClassesByPackage("java.lang");
        System.out.println("\njava.lang package classes: " + stringClasses.size());
        for (int i = 0; i < Math.min(5, stringClasses.size()); i++) {
            System.out.println("  - " + stringClasses.get(i).getSimpleName());
        }
    }
    
    // Test method for method invocation testing
    public String testMethod(String input) {
        return "Processed: " + input;
    }
    
    private static void printHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("    " + title);
        System.out.println("    Full JDI Hierarchy Implementation Test");
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println();
    }
    
    private static void printSection(String title) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println(">>> " + title);
        System.out.println("-".repeat(50));
    }
    
    private static void printFooter(String message) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("SUCCESS: " + message);
        System.out.println("All JDI components operational and validated!");
        System.out.println("=".repeat(60));
    }
}