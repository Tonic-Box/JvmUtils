package com.jvmu.jdi;

import java.util.List;

/**
 * JDI - Java Debug Interface Implementation for Self-Debugging
 * 
 * This class provides a comprehensive JDI-like API for debugging and inspecting
 * the current JVM process. Unlike traditional JDI which debugs external processes
 * via JDWP, this implementation provides debugging capabilities within the same JVM
 * using privileged internal access methods.
 * 
 * Key Features:
 * - Virtual machine inspection and control
 * - Thread management and inspection
 * - Object reference and field manipulation
 * - Stack frame analysis
 * - Class and method inspection
 * - Memory management and monitoring
 * - Process information access
 * 
 * This implementation provides the core JDI functionality without requiring
 * external process connection or JDWP protocol.
 */
public class JDI {
    
    private static SelfVirtualMachine vm = null;
    
    /**
     * Initialize the JDI interface
     * @return true if initialization successful, false otherwise
     */
    public static boolean initialize() {
        try {
            if (!SelfVirtualMachine.isAvailable()) {
                System.err.println("JDI: SelfVirtualMachine not available - requires privileged access");
                return false;
            }
            
            vm = SelfVirtualMachine.getInstance();
            return true;
            
        } catch (Exception e) {
            System.err.println("JDI initialization failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Check if JDI interface is available
     * @return true if JDI is available and initialized
     */
    public static boolean isAvailable() {
        return vm != null && SelfVirtualMachine.isAvailable();
    }
    
    /**
     * Get JDI status information
     * @return status string describing availability and version
     */
    public static String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("JDI Interface Status:\n");
        sb.append("  Self Virtual Machine: ").append(vm != null ? "Available ✓" : "Unavailable ✗").append("\n");
        sb.append("  Process ID: ").append(vm != null ? vm.getProcessId() : "Unknown").append("\n");
        sb.append("  JVM Name: ").append(vm != null ? vm.name() : "Unknown").append("\n");
        sb.append("  Java Version: ").append(System.getProperty("java.version")).append("\n");
        sb.append("  Overall: ").append(isAvailable() ? "Available ✓" : "Unavailable ✗");
        return sb.toString();
    }
    
    // ==================== VIRTUAL MACHINE MANAGEMENT ====================
    
    /**
     * Get the self virtual machine instance
     * @return SelfVirtualMachine instance for current JVM
     */
    public static SelfVirtualMachine getVirtualMachine() {
        if (!isAvailable()) {
            throw new IllegalStateException("JDI not initialized or unavailable");
        }
        return vm;
    }
    
    /**
     * Get virtual machine name
     * @return JVM name and version
     */
    public static String getVMName() {
        return getVirtualMachine().name();
    }
    
    /**
     * Get virtual machine version
     * @return Java version string
     */
    public static String getVMVersion() {
        return getVirtualMachine().version();
    }
    
    /**
     * Get process ID of current JVM
     * @return process ID
     */
    public static long getProcessId() {
        return getVirtualMachine().getProcessId();
    }
    
    /**
     * Get virtual machine capabilities
     * @return VMCapabilities structure
     */
    public static SelfVirtualMachine.VMCapabilities getCapabilities() {
        return getVirtualMachine().getCapabilities();
    }
    
    /**
     * Get event queue
     * @return event queue for debugging events
     */
    public static SelfEventQueue getEventQueue() {
        return getVirtualMachine().eventQueue();
    }
    
    /**
     * Get event request manager
     * @return event request manager for creating event requests
     */
    public static SelfEventRequestManager getEventRequestManager() {
        return getVirtualMachine().eventRequestManager();
    }
    
    // ==================== THREAD MANAGEMENT ====================
    
    /**
     * Get all threads in the JVM
     * @return list of all thread references
     */
    public static List<SelfThreadReference> getAllThreads() {
        return getVirtualMachine().getAllThreads();
    }
    
    /**
     * Get thread reference by thread ID
     * @param threadId the thread ID
     * @return thread reference or null if not found
     */
    public static SelfThreadReference getThreadById(long threadId) {
        return getVirtualMachine().getThreadReference(threadId);
    }
    
    /**
     * Get current thread reference
     * @return thread reference for current thread
     */
    public static SelfThreadReference getCurrentThread() {
        return getVirtualMachine().getCurrentThread();
    }
    
    /**
     * Get thread by name
     * @param threadName name of thread to find
     * @return first thread with matching name, or null
     */
    public static SelfThreadReference getThreadByName(String threadName) {
        for (SelfThreadReference thread : getAllThreads()) {
            if (threadName.equals(thread.name())) {
                return thread;
            }
        }
        return null;
    }
    
    /**
     * Get threads by state
     * @param state thread state to match
     * @return list of threads in specified state
     */
    public static List<SelfThreadReference> getThreadsByState(String state) {
        List<SelfThreadReference> result = new java.util.ArrayList<>();
        for (SelfThreadReference thread : getAllThreads()) {
            if (state.equals(thread.status())) {
                result.add(thread);
            }
        }
        return result;
    }
    
    // ==================== OBJECT MANAGEMENT ====================
    
    /**
     * Create object reference for any object
     * @param obj the object to wrap
     * @return object reference for the object
     */
    public static SelfObjectReference createObjectReference(Object obj) {
        return getVirtualMachine().createObjectReference(obj);
    }
    
    /**
     * Get field value from object
     * @param objRef object reference
     * @param fieldName name of field to get
     * @return field value
     */
    public static Object getFieldValue(SelfObjectReference objRef, String fieldName) {
        return objRef.getValue(fieldName);
    }
    
    /**
     * Set field value in object
     * @param objRef object reference  
     * @param fieldName name of field to set
     * @param value new value for field
     */
    public static void setFieldValue(SelfObjectReference objRef, String fieldName, Object value) {
        objRef.setValue(fieldName, value);
    }
    
    /**
     * Invoke method on object
     * @param objRef object reference
     * @param methodName name of method to invoke
     * @param args method arguments
     * @return method return value
     */
    public static Object invokeMethod(SelfObjectReference objRef, String methodName, Object... args) {
        return objRef.invokeMethod(methodName, args);
    }
    
    /**
     * Get object size in bytes
     * @param objRef object reference
     * @return object size in bytes
     */
    public static long getObjectSize(SelfObjectReference objRef) {
        return objRef.getObjectSize();
    }
    
    // ==================== CLASS INSPECTION ====================
    
    /**
     * Get all loaded classes
     * @return list of all loaded classes
     */
    public static List<Class<?>> getAllLoadedClasses() {
        return getVirtualMachine().getAllLoadedClasses();
    }
    
    /**
     * Get class by name
     * @param className fully qualified class name
     * @return Class object or null if not found
     */
    public static Class<?> getClassByName(String className) {
        try {
            return getVirtualMachine().getClassByName(className);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Get classes by package name
     * @param packageName package name to match
     * @return list of classes in specified package
     */
    public static List<Class<?>> getClassesByPackage(String packageName) {
        List<Class<?>> result = new java.util.ArrayList<>();
        for (Class<?> clazz : getAllLoadedClasses()) {
            Package pkg = clazz.getPackage();
            if (pkg != null && packageName.equals(pkg.getName())) {
                result.add(clazz);
            }
        }
        return result;
    }
    
    // ==================== STACK INSPECTION ====================
    
    /**
     * Get stack frames for current thread
     * @return list of stack frames
     */
    public static List<SelfStackFrame> getCurrentStackFrames() {
        return getCurrentThread().getStackFrames();
    }
    
    /**
     * Get stack frames for specific thread
     * @param threadRef thread reference
     * @return list of stack frames for the thread
     */
    public static List<SelfStackFrame> getStackFrames(SelfThreadReference threadRef) {
        return threadRef.getStackFrames();
    }
    
    /**
     * Get current frame for thread
     * @param threadRef thread reference
     * @return top stack frame or null
     */
    public static SelfStackFrame getCurrentFrame(SelfThreadReference threadRef) {
        return threadRef.getCurrentFrame();
    }
    
    // ==================== MEMORY MANAGEMENT ====================
    
    /**
     * Force garbage collection
     */
    public static void forceGarbageCollection() {
        getVirtualMachine().gc();
    }
    
    /**
     * Get memory information
     * @return memory information structure
     */
    public static SelfVirtualMachine.MemoryInfo getMemoryInfo() {
        return getVirtualMachine().getMemoryInfo();
    }
    
    /**
     * Get available memory in bytes
     * @return available memory
     */
    public static long getAvailableMemory() {
        SelfVirtualMachine.MemoryInfo info = getMemoryInfo();
        return info.freeMemory;
    }
    
    /**
     * Get used memory in bytes
     * @return used memory
     */
    public static long getUsedMemory() {
        SelfVirtualMachine.MemoryInfo info = getMemoryInfo();
        return info.usedMemory;
    }
    
    /**
     * Get total memory in bytes
     * @return total memory
     */
    public static long getTotalMemory() {
        SelfVirtualMachine.MemoryInfo info = getMemoryInfo();
        return info.totalMemory;
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive JVM information
     * @return detailed JVM state information
     */
    public static JVMInfo getJVMInfo() {
        JVMInfo info = new JVMInfo();
        
        // Basic JVM properties
        info.processId = getProcessId();
        info.vmName = getVMName();
        info.vmVersion = getVMVersion();
        info.javaVersion = System.getProperty("java.version");
        info.javaVendor = System.getProperty("java.vendor");
        info.osName = System.getProperty("os.name");
        info.osArch = System.getProperty("os.arch");
        info.osVersion = System.getProperty("os.version");
        
        // Thread information
        List<SelfThreadReference> allThreads = getAllThreads();
        info.threadCount = allThreads.size();
        info.currentThread = getCurrentThread().name();
        
        // Memory information
        info.memoryInfo = getMemoryInfo();
        
        // Class information
        List<Class<?>> allClasses = getAllLoadedClasses();
        info.loadedClassCount = allClasses.size();
        
        // Capabilities
        info.capabilities = getCapabilities();
        info.jdiAvailable = isAvailable();
        
        return info;
    }
    
    /**
     * Run comprehensive JVM diagnostics
     * @return diagnostic results
     */
    public static DiagnosticResults runDiagnostics() {
        DiagnosticResults results = new DiagnosticResults();
        
        try {
            // Memory diagnostics
            SelfVirtualMachine.MemoryInfo beforeGC = getMemoryInfo();
            forceGarbageCollection();
            SelfVirtualMachine.MemoryInfo afterGC = getMemoryInfo();
            
            results.memoryFreedByGC = beforeGC.usedMemory - afterGC.usedMemory;
            results.memoryUtilization = (double) afterGC.usedMemory / afterGC.totalMemory;
            
            // Thread diagnostics
            List<SelfThreadReference> allThreads = getAllThreads();
            results.totalThreads = allThreads.size();
            results.daemonThreads = 0;
            results.aliveThreads = 0;
            
            for (SelfThreadReference thread : allThreads) {
                if (thread.isDaemon()) results.daemonThreads++;
                if (thread.isAlive()) results.aliveThreads++;
            }
            
            // Class diagnostics  
            List<Class<?>> allClasses = getAllLoadedClasses();
            results.totalLoadedClasses = allClasses.size();
            results.interfaceCount = 0;
            results.arrayClassCount = 0;
            
            for (Class<?> clazz : allClasses) {
                if (clazz.isInterface()) results.interfaceCount++;
                if (clazz.isArray()) results.arrayClassCount++;
            }
            
            // Process diagnostics
            results.processId = getProcessId();
            results.vmName = getVMName();
            results.capabilities = getCapabilities();
            
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
        public long processId;
        public String vmName;
        public String vmVersion;
        public String javaVersion;
        public String javaVendor;
        public String osName;
        public String osArch;
        public String osVersion;
        public int threadCount;
        public String currentThread;
        public SelfVirtualMachine.MemoryInfo memoryInfo;
        public int loadedClassCount;
        public SelfVirtualMachine.VMCapabilities capabilities;
        public boolean jdiAvailable;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("JVM Information:\n");
            sb.append("  Process ID: ").append(processId).append("\n");
            sb.append("  VM: ").append(vmName).append("\n");
            sb.append("  Java: ").append(javaVersion).append(" (").append(javaVendor).append(")\n");
            sb.append("  OS: ").append(osName).append(" ").append(osVersion).append(" (").append(osArch).append(")\n");
            sb.append("  Threads: ").append(threadCount).append(" (current: ").append(currentThread).append(")\n");
            sb.append("  Classes: ").append(loadedClassCount).append(" loaded\n");
            sb.append("  Memory: ").append(memoryInfo).append("\n");
            sb.append("  Capabilities: ").append(capabilities).append("\n");
            sb.append("  JDI Available: ").append(jdiAvailable);
            return sb.toString();
        }
    }
    
    /**
     * Diagnostic results structure
     */
    public static class DiagnosticResults {
        public boolean success;
        public String error;
        public long processId;
        public String vmName;
        public long memoryFreedByGC;
        public double memoryUtilization;
        public int totalThreads;
        public int daemonThreads;
        public int aliveThreads;
        public int totalLoadedClasses;
        public int interfaceCount;
        public int arrayClassCount;
        public SelfVirtualMachine.VMCapabilities capabilities;
        
        @Override
        public String toString() {
            if (!success) {
                return "JDI Diagnostics failed: " + error;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("JDI Diagnostic Results:\n");
            sb.append("  Process: ").append(processId).append(" (").append(vmName).append(")\n");
            sb.append("  Memory: ").append(String.format("%.1f%% utilization", memoryUtilization * 100));
            sb.append(", GC freed ").append(memoryFreedByGC / (1024 * 1024)).append(" MB\n");
            sb.append("  Threads: ").append(totalThreads).append(" (").append(aliveThreads);
            sb.append(" alive, ").append(daemonThreads).append(" daemon)\n");
            sb.append("  Classes: ").append(totalLoadedClasses).append(" (").append(interfaceCount);
            sb.append(" interfaces, ").append(arrayClassCount).append(" arrays)\n");
            sb.append("  Capabilities: ").append(capabilities);
            return sb.toString();
        }
    }
}