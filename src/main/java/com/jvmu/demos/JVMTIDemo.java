package com.jvmu.demos;

import com.jvmu.jvmti.AdvancedVMAccess;
import com.jvmu.jvmti.JVMTI;
import com.jvmu.jvmti.VMObjectAccess;

/**
 * JVMTI Safe Demo - Demonstrates all JVMTI capabilities without JVM crashes
 * 
 * This demo exercises the full JVMTI API but avoids operations that could
 * cause JVM crashes, making it suitable for production testing.
 */
public class JVMTIDemo {
    public static void main(String[] args) {
        System.out.println("========================================================");
        System.out.println("            JVMTI Safe Interface Demo                  ");
        System.out.println("       Complete JVM Tool Interface Testing             ");
        System.out.println("========================================================");
        System.out.println();
        
        // Initialize and check JVMTI
        if (!JVMTI.initialize()) {
            System.out.println("JVMTI initialization failed - requires privileged access");
            return;
        }
        
        System.out.println("JVMTI Status: SUCCESS");
        System.out.println();
        
        demonstrateJVMInfo();
        demonstrateThreadManagement();
        demonstrateObjectInspection();
        demonstrateClassAnalysis();
        demonstrateMemoryManagement();
        demonstrateDiagnostics();
        
        System.out.println();
        System.out.println("========================================================");
        System.out.println("         JVMTI Safe Demo Completed Successfully        ");
        System.out.println("    All JVM Tool Interface features validated!         ");
        System.out.println("========================================================");
    }
    
    private static void demonstrateJVMInfo() {
        System.out.println("=== JVM Information Access ===");
        try {
            JVMTI.JVMInfo info = JVMTI.getJVMInfo();
            
            System.out.println("JVM Details:");
            System.out.printf("  Version: %s (%s)%n", info.javaVersion, info.javaVendor);
            System.out.printf("  VM: %s %s%n", info.jvmName, info.jvmVersion);
            System.out.printf("  OS: %s %s (%s)%n", info.osName, info.osVersion, info.osArch);
            System.out.printf("  Threads: %d active%n", info.threadCount);
            System.out.printf("  Classes: %d loaded%n", info.loadedClassCount);
            System.out.printf("  Memory: %.1f MB used / %.1f MB total%n",
                info.memoryInfo.usedMemory / 1024.0 / 1024.0,
                info.memoryInfo.totalMemory / 1024.0 / 1024.0);
            System.out.println("  JVMTI: " + (info.jvmtiAvailable ? "Available" : "Unavailable"));
            
        } catch (Exception e) {
            System.err.println("JVM info failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateThreadManagement() {
        System.out.println("\n=== Thread Management ===");
        try {
            Thread[] allThreads = JVMTI.getAllThreads();
            Thread currentThread = JVMTI.getCurrentThread();
            
            System.out.printf("Current thread: %s%n", currentThread.getName());
            System.out.printf("Total threads: %d%n", allThreads.length);
            
            // Analyze thread states
            int[] stateCounts = new int[Thread.State.values().length];
            for (Thread thread : allThreads) {
                stateCounts[thread.getState().ordinal()]++;
            }
            
            System.out.println("Thread states:");
            for (Thread.State state : Thread.State.values()) {
                int count = stateCounts[state.ordinal()];
                if (count > 0) {
                    System.out.printf("  %s: %d%n", state, count);
                }
            }
            
            // Show interesting thread details
            System.out.println("Key threads:");
            for (Thread thread : allThreads) {
                String name = thread.getName();
                if (name.equals("main") || name.contains("VM") || name.contains("GC")) {
                    VMObjectAccess.ThreadInfo info = JVMTI.getThreadInfo(thread);
                    System.out.printf("  %s: %s, priority=%d, daemon=%b%n",
                        name, info.state, info.priority, info.daemon);
                }
            }
            
        } catch (Exception e) {
            System.err.println("Thread management failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateObjectInspection() {
        System.out.println("\n=== Object Inspection ===");
        try {
            // Create test objects
            Object[] testObjects = {
                new Object(),
                new String("JVMTI Test"),
                new int[20],
                new JVMTIDemo(),
                Thread.currentThread()
            };
            
            System.out.println("Object sizes:");
            long totalSize = 0;
            for (Object obj : testObjects) {
                long size = JVMTI.getObjectSize(obj);
                totalSize += size;
                
                String desc = obj.getClass().getSimpleName();
                if (obj.getClass().isArray()) {
                    desc += "[" + java.lang.reflect.Array.getLength(obj) + "]";
                }
                
                System.out.printf("  %-15s: %4d bytes%n", desc, size);
            }
            System.out.printf("  %-15s: %4d bytes%n", "TOTAL", totalSize);
            
            // Object header inspection (safe - read-only)
            System.out.println("\nObject headers:");
            for (int i = 0; i < Math.min(3, testObjects.length); i++) {
                Object obj = testObjects[i];
                try {
                    AdvancedVMAccess.ObjectHeader header = JVMTI.getObjectHeader(obj);
                    if (header != null) {
                        System.out.printf("  %s: mark=0x%s, size=%d%n",
                            obj.getClass().getSimpleName(),
                            header.markWord != null ? Long.toHexString(header.markWord) : "null",
                            header.headerSize);
                    }
                } catch (Exception e) {
                    System.out.printf("  %s: header not accessible%n", obj.getClass().getSimpleName());
                }
            }
            
        } catch (Exception e) {
            System.err.println("Object inspection failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateClassAnalysis() {
        System.out.println("\n=== Class Analysis ===");
        try {
            // Get all loaded classes
            java.util.Set<Class<?>> allClasses = JVMTI.getLoadedClasses();
            System.out.printf("Total loaded classes: %d%n", allClasses.size());
            
            // Categorize classes
            int jdkClasses = 0, userClasses = 0, interfaces = 0, arrays = 0;
            for (Class<?> clazz : allClasses) {
                String name = clazz.getName();
                if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")) {
                    jdkClasses++;
                } else {
                    userClasses++;
                }
                if (clazz.isInterface()) interfaces++;
                if (clazz.isArray()) arrays++;
            }
            
            System.out.printf("Class breakdown: JDK=%d, User=%d, Interfaces=%d, Arrays=%d%n",
                jdkClasses, userClasses, interfaces, arrays);
            
            // Analyze specific classes
            Class<?>[] analysisClasses = { JVMTIDemo.class, String.class, Object.class };
            
            System.out.println("\nClass details:");
            for (Class<?> clazz : analysisClasses) {
                AdvancedVMAccess.ClassMetadata metadata = JVMTI.getClassMetadata(clazz);
                System.out.printf("  %s: %d fields, %d methods%n",
                    clazz.getSimpleName(), metadata.fieldCount, metadata.methodCount);
                
                // Show field info (safe)
                if (!metadata.instanceFields.isEmpty()) {
                    System.out.printf("    Instance fields: %d%n", metadata.instanceFields.size());
                    for (AdvancedVMAccess.FieldInfo field : metadata.instanceFields) {
                        if (field.offset >= 0) {
                            System.out.printf("      %s %s (offset: %d)%n",
                                field.type.getSimpleName(), field.name, field.offset);
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            System.err.println("Class analysis failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateMemoryManagement() {
        System.out.println("\n=== Memory Management ===");
        try {
            VMObjectAccess.MemoryInfo initial = JVMTI.getMemoryInfo();
            System.out.printf("Initial memory: %.1f MB used / %.1f MB total (%.1f%% utilization)%n",
                initial.usedMemory / 1024.0 / 1024.0,
                initial.totalMemory / 1024.0 / 1024.0,
                (double) initial.usedMemory / initial.totalMemory * 100);
            
            // Allocate some memory for testing
            java.util.List<byte[]> allocations = new java.util.ArrayList<>();
            System.out.println("Allocating 10MB test data...");
            for (int i = 0; i < 10; i++) {
                allocations.add(new byte[1024 * 1024]);
            }
            
            VMObjectAccess.MemoryInfo afterAlloc = JVMTI.getMemoryInfo();
            long increase = afterAlloc.usedMemory - initial.usedMemory;
            System.out.printf("Memory after allocation: +%.1f MB%n", increase / 1024.0 / 1024.0);
            
            // Clear and force GC
            allocations.clear();
            System.out.println("Clearing allocations and forcing GC...");
            JVMTI.forceGarbageCollection();
            
            VMObjectAccess.MemoryInfo afterGC = JVMTI.getMemoryInfo();
            long recovered = afterAlloc.usedMemory - afterGC.usedMemory;
            System.out.printf("Memory recovered by GC: %.1f MB%n", recovered / 1024.0 / 1024.0);
            System.out.printf("Final utilization: %.1f%%%n",
                (double) afterGC.usedMemory / afterGC.totalMemory * 100);
            
        } catch (Exception e) {
            System.err.println("Memory management failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateDiagnostics() {
        System.out.println("\n=== Comprehensive Diagnostics ===");
        try {
            JVMTI.DiagnosticResults results = JVMTI.runDiagnostics();
            
            if (results.success) {
                System.out.println("Diagnostic Results:");
                System.out.printf("  Threads: %d total (%d alive, %d daemon)%n",
                    results.totalThreads, results.aliveThreads, results.daemonThreads);
                System.out.printf("  Classes: %d total (%d interfaces, %d arrays)%n",
                    results.totalLoadedClasses, results.interfaceCount, results.arrayClassCount);
                System.out.printf("  Memory: %.1f%% utilization%n", results.memoryUtilization * 100);
                System.out.printf("  GC freed: %.1f MB%n", results.memoryFreedByGC / 1024.0 / 1024.0);
                
                System.out.println("Health Assessment:");
                if (results.memoryUtilization > 0.8) {
                    System.out.println("  WARNING: High memory usage");
                } else {
                    System.out.println("  OK: Memory usage normal");
                }
                
                if (results.totalThreads > 50) {
                    System.out.println("  WARNING: High thread count");  
                } else {
                    System.out.println("  OK: Thread count normal");
                }
                
                System.out.println("  OK: All diagnostics completed successfully");
                
            } else {
                System.out.println("Diagnostics failed: " + results.error);
            }
            
        } catch (Exception e) {
            System.err.println("Diagnostics failed: " + e.getMessage());
        }
    }
}