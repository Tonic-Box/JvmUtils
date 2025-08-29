package com.jvmu.jvmti;

import java.util.List;
import java.util.Set;

/**
 * JVMTI - JVM Tool Interface Implementation for Java
 * 
 * This class provides a comprehensive JVMTI-like API that works purely from Java
 * using privileged internal access. It combines functionality from VMObjectAccess
 * and AdvancedVMAccess to provide a complete debugging and profiling interface.
 * 
 * Key Features:
 * - Thread inspection and management
 * - Object and memory analysis
 * - Class metadata and field access
 * - Garbage collection control
 * - JVM internal structure access
 * 
 * This implementation provides capabilities similar to native JVMTI agents
 * but works entirely within the Java runtime using internal APIs.
 */
public class JVMTI {
    
    /**
     * Initialize the JVMTI interface
     * @return true if initialization successful, false otherwise
     */
    public static boolean initialize() {
        boolean vmObjectAccessOk = VMObjectAccess.isAvailable();
        boolean advancedVMAccessOk = AdvancedVMAccess.isAvailable();
        
        if (!vmObjectAccessOk) {
            System.err.println("JVMTI: VMObjectAccess initialization failed");
        }
        
        if (!advancedVMAccessOk) {
            System.err.println("JVMTI: AdvancedVMAccess initialization failed");
        }
        
        return vmObjectAccessOk && advancedVMAccessOk;
    }
    
    /**
     * Check if JVMTI interface is available
     * @return true if all required components are available
     */
    public static boolean isAvailable() {
        return VMObjectAccess.isAvailable() && AdvancedVMAccess.isAvailable();
    }
    
    /**
     * Get JVMTI status information
     * @return status string describing availability and version
     */
    public static String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("JVMTI Interface Status:\n");
        sb.append("  ").append(VMObjectAccess.getStatus()).append("\n");
        sb.append("  ").append(AdvancedVMAccess.getStatus()).append("\n");
        sb.append("  Overall: ").append(isAvailable() ? "Available ✓" : "Unavailable ✗");
        return sb.toString();
    }
    
    // ==================== THREAD MANAGEMENT ====================
    
    /**
     * Get all threads in the JVM
     * Equivalent to JVMTI GetAllThreads
     * @return array of all Java threads
     */
    public static Thread[] getAllThreads() {
        return VMObjectAccess.getAllThreads();
    }
    
    /**
     * Get the current thread
     * Equivalent to JVMTI GetCurrentThread
     * @return current thread
     */
    public static Thread getCurrentThread() {
        return VMObjectAccess.getCurrentThread();
    }
    
    /**
     * Get detailed information about a thread
     * Equivalent to JVMTI GetThreadInfo
     * @param thread the thread to inspect
     * @return thread information structure
     */
    public static VMObjectAccess.ThreadInfo getThreadInfo(Thread thread) {
        return VMObjectAccess.getThreadInfo(thread);
    }
    
    // ==================== OBJECT INSPECTION ====================
    
    /**
     * Get the size of an object in bytes
     * Equivalent to JVMTI GetObjectSize
     * @param obj the object to measure
     * @return size in bytes
     */
    public static long getObjectSize(Object obj) {
        return VMObjectAccess.getObjectSize(obj);
    }
    
    /**
     * Get object header information
     * Provides access to JVM internal object header data
     * @param obj the object to inspect
     * @return object header information
     */
    public static AdvancedVMAccess.ObjectHeader getObjectHeader(Object obj) {
        return AdvancedVMAccess.getObjectHeader(obj);
    }
    
    /**
     * Find all objects of a specific class
     * Equivalent to JVMTI IterateOverInstancesOfClass
     * @param clazz the class to search for
     * @return list of objects (limited implementation)
     */
    public static List<Object> getObjectsOfClass(Class<?> clazz) {
        return VMObjectAccess.getObjectsOfClass(clazz);
    }
    
    // ==================== CLASS INSPECTION ====================
    
    /**
     * Get all loaded classes in the JVM
     * Equivalent to JVMTI GetLoadedClasses
     * @return set of all loaded classes
     */
    public static Set<Class<?>> getLoadedClasses() {
        return AdvancedVMAccess.getAllLoadedClasses();
    }
    
    /**
     * Get detailed metadata about a class
     * Provides comprehensive class analysis including field offsets
     * @param clazz the class to analyze
     * @return class metadata structure
     */
    public static AdvancedVMAccess.ClassMetadata getClassMetadata(Class<?> clazz) {
        return AdvancedVMAccess.getClassMetadata(clazz);
    }
    
    // ==================== FIELD ACCESS ====================
    
    /**
     * Read an object field directly using memory access
     * Bypasses normal field access restrictions and visibility
     * @param obj the object containing the field
     * @param fieldName name of the field to read
     * @return field value
     */
    public static Object readObjectField(Object obj, String fieldName) {
        return AdvancedVMAccess.readObjectField(obj, fieldName);
    }
    
    /**
     * Write an object field directly using memory access
     * Bypasses normal field access restrictions and visibility
     * @param obj the object containing the field
     * @param fieldName name of the field to write
     * @param value new value for the field
     */
    public static void writeObjectField(Object obj, String fieldName, Object value) {
        AdvancedVMAccess.writeObjectField(obj, fieldName, value);
    }
    
    // ==================== MEMORY MANAGEMENT ====================
    
    /**
     * Force garbage collection
     * Equivalent to JVMTI ForceGarbageCollection
     */
    public static void forceGarbageCollection() {
        VMObjectAccess.forceGarbageCollection();
    }
    
    /**
     * Get JVM memory information
     * Provides heap and memory usage statistics
     * @return memory information structure
     */
    public static VMObjectAccess.MemoryInfo getMemoryInfo() {
        return VMObjectAccess.getMemoryInfo();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive JVM information
     * @return detailed JVM state information
     */
    public static JVMInfo getJVMInfo() {
        JVMInfo info = new JVMInfo();
        
        // Basic JVM properties
        info.javaVersion = System.getProperty("java.version");
        info.javaVendor = System.getProperty("java.vendor");
        info.jvmName = System.getProperty("java.vm.name");
        info.jvmVersion = System.getProperty("java.vm.version");
        info.jvmVendor = System.getProperty("java.vm.vendor");
        info.osName = System.getProperty("os.name");
        info.osArch = System.getProperty("os.arch");
        info.osVersion = System.getProperty("os.version");
        
        // Thread information
        Thread[] threads = getAllThreads();
        info.threadCount = threads.length;
        info.currentThread = getCurrentThread().getName();
        
        // Memory information
        info.memoryInfo = getMemoryInfo();
        
        // Class information
        Set<Class<?>> classes = getLoadedClasses();
        info.loadedClassCount = classes.size();
        
        // JVMTI availability
        info.jvmtiAvailable = isAvailable();
        
        return info;
    }
    
    /**
     * Run comprehensive JVM diagnostics
     * Performs various checks and measurements on JVM state
     * @return diagnostic results
     */
    public static DiagnosticResults runDiagnostics() {
        DiagnosticResults results = new DiagnosticResults();
        
        try {
            // Memory diagnostics
            VMObjectAccess.MemoryInfo beforeGC = getMemoryInfo();
            forceGarbageCollection();
            VMObjectAccess.MemoryInfo afterGC = getMemoryInfo();
            
            results.memoryFreedByGC = beforeGC.usedMemory - afterGC.usedMemory;
            results.memoryUtilization = (double) afterGC.usedMemory / afterGC.totalMemory;
            
            // Thread diagnostics
            Thread[] allThreads = getAllThreads();
            results.totalThreads = allThreads.length;
            results.daemonThreads = 0;
            results.aliveThreads = 0;
            
            for (Thread thread : allThreads) {
                if (thread.isDaemon()) results.daemonThreads++;
                if (thread.isAlive()) results.aliveThreads++;
            }
            
            // Class diagnostics
            Set<Class<?>> loadedClasses = getLoadedClasses();
            results.totalLoadedClasses = loadedClasses.size();
            results.interfaceCount = 0;
            results.arrayClassCount = 0;
            results.primitiveCount = 0;
            
            for (Class<?> clazz : loadedClasses) {
                if (clazz.isInterface()) results.interfaceCount++;
                if (clazz.isArray()) results.arrayClassCount++;
                if (clazz.isPrimitive()) results.primitiveCount++;
            }
            
            // Object size testing
            results.testObjectSizes = new long[] {
                getObjectSize(new Object()),
                getObjectSize(new String("test")),
                getObjectSize(new int[10]),
                getObjectSize(new Thread())
            };
            
            results.success = true;
            
        } catch (Exception e) {
            results.success = false;
            results.error = e.getMessage();
        }
        
        return results;
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Comprehensive JVM information structure
     */
    public static class JVMInfo {
        public String javaVersion;
        public String javaVendor;
        public String jvmName;
        public String jvmVersion;
        public String jvmVendor;
        public String osName;
        public String osArch;
        public String osVersion;
        public int threadCount;
        public String currentThread;
        public VMObjectAccess.MemoryInfo memoryInfo;
        public int loadedClassCount;
        public boolean jvmtiAvailable;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("JVM Information:\n");
            sb.append("  Java: ").append(javaVersion).append(" (").append(javaVendor).append(")\n");
            sb.append("  JVM: ").append(jvmName).append(" ").append(jvmVersion).append("\n");
            sb.append("  OS: ").append(osName).append(" ").append(osVersion).append(" (").append(osArch).append(")\n");
            sb.append("  Threads: ").append(threadCount).append(" (current: ").append(currentThread).append(")\n");
            sb.append("  Loaded Classes: ").append(loadedClassCount).append("\n");
            sb.append("  Memory: ").append(memoryInfo).append("\n");
            sb.append("  JVMTI Available: ").append(jvmtiAvailable);
            return sb.toString();
        }
    }
    
    /**
     * Diagnostic results structure
     */
    public static class DiagnosticResults {
        public boolean success;
        public String error;
        public long memoryFreedByGC;
        public double memoryUtilization;
        public int totalThreads;
        public int daemonThreads;
        public int aliveThreads;
        public int totalLoadedClasses;
        public int interfaceCount;
        public int arrayClassCount;
        public int primitiveCount;
        public long[] testObjectSizes;
        
        @Override
        public String toString() {
            if (!success) {
                return "Diagnostics failed: " + error;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("JVM Diagnostic Results:\n");
            sb.append("  Memory freed by GC: ").append(memoryFreedByGC).append(" bytes\n");
            sb.append("  Memory utilization: ").append(String.format("%.1f%%", memoryUtilization * 100)).append("\n");
            sb.append("  Threads: ").append(totalThreads).append(" (").append(aliveThreads).append(" alive, ").append(daemonThreads).append(" daemon)\n");
            sb.append("  Classes: ").append(totalLoadedClasses).append(" (").append(interfaceCount).append(" interfaces, ").append(arrayClassCount).append(" arrays)\n");
            sb.append("  Test object sizes: [");
            for (int i = 0; i < testObjectSizes.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(testObjectSizes[i]);
            }
            sb.append("] bytes");
            return sb.toString();
        }
    }
}