package com.jvmu.internals;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.util.ReflectBuilder;

import java.lang.module.ModuleDescriptor;
import java.util.Map;

/**
 * VM - Wrapper for jdk.internal.misc.VM using privileged access
 * 
 * Provides access to JVM internal state and lifecycle management without requiring
 * special permissions or module access restrictions. Uses privileged access via
 * ModuleBootstrap to bypass security restrictions and access internal JVM functionality.
 * 
 * Key capabilities:
 * - JVM initialization level tracking
 * - System property management  
 * - Direct memory configuration
 * - Thread state utilities
 * - Module system integration
 * - UID/GID access
 * - Runtime argument inspection
 * - Finalization tracking
 * 
 * This wrapper provides functionality normally restricted to internal JDK code.
 */
public class VM {
    
    private static final Class<?> vmClass;
    private static final boolean available;
    
    // JVM initialization levels from jdk.internal.misc.VM
    public static final int JAVA_LANG_SYSTEM_INITED = 1;
    public static final int MODULE_SYSTEM_INITED = 2;
    public static final int SYSTEM_LOADER_INITIALIZING = 3;
    public static final int SYSTEM_BOOTED = 4;
    public static final int SYSTEM_SHUTDOWN = 5;
    
    // JVMTI thread state constants
    public static final int JVMTI_THREAD_STATE_ALIVE = 0x0001;
    public static final int JVMTI_THREAD_STATE_TERMINATED = 0x0002;
    public static final int JVMTI_THREAD_STATE_RUNNABLE = 0x0004;
    public static final int JVMTI_THREAD_STATE_BLOCKED_ON_MONITOR_ENTER = 0x0400;
    public static final int JVMTI_THREAD_STATE_WAITING_INDEFINITELY = 0x0010;
    public static final int JVMTI_THREAD_STATE_WAITING_WITH_TIMEOUT = 0x0020;
    
    static {
        Class<?> clazz = null;
        boolean isAvailable = false;
        
        try {
            // Load VM class using privileged access
            clazz = Class.forName("jdk.internal.misc.VM");
            if (clazz != null) {
                isAvailable = true;
            }
        } catch (Exception e) {
            // VM not available - silent fallback
        }
        
        vmClass = clazz;
        available = isAvailable;
    }
    
    /**
     * Check if VM functionality is available
     * @return true if VM APIs can be used
     */
    public static boolean isAvailable() {
        return available && ModuleBootstrap.getInternalUnsafe() != null;
    }
    
    /**
     * Get status information about VM availability
     * @return status string
     */
    public static String getStatus() {
        if (available) {
            return "VM: Available ✓ (Bypassed jdk.internal.misc access restrictions)";
        } else {
            return "VM: Not Available ✗ (jdk.internal.misc.VM class not found)";
        }
    }
    
    // ==================== INITIALIZATION LEVEL MANAGEMENT ====================
    
    /**
     * Returns the current init level
     * @return current initialization level
     */
    public static int initLevel() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("initLevel", null, null)
            .get();
    }
    
    /**
     * Sets the init level
     * @param value new initialization level
     */
    public static void initLevel(int value) {
        ReflectBuilder.of(vmClass)
            .staticMethod("initLevel", new Class<?>[]{int.class}, new Object[]{value})
            .get();
    }
    
    /**
     * Waits for the init level to reach the given value
     * @param value target initialization level
     * @throws InterruptedException if interrupted while waiting
     */
    public static void awaitInitLevel(int value) throws InterruptedException {
        try {
            ReflectBuilder.of(vmClass)
                .staticMethod("awaitInitLevel", new Class<?>[]{int.class}, new Object[]{value})
                .get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof InterruptedException) {
                throw (InterruptedException) e.getCause();
            }
            throw e;
        }
    }
    
    /**
     * Returns true if the module system has been initialized
     * @return true if module system is initialized
     */
    public static boolean isModuleSystemInited() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isModuleSystemInited", null, null)
            .get();
    }
    
    /**
     * Returns true if the VM is fully initialized
     * @return true if VM is booted
     */
    public static boolean isBooted() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isBooted", null, null)
            .get();
    }
    
    /**
     * Set shutdown state
     */
    public static void shutdown() {
        ReflectBuilder.of(vmClass)
            .staticMethod("shutdown", null, null)
            .get();
    }
    
    /**
     * Returns true if the VM has been shutdown
     * @return true if VM is shutdown
     */
    public static boolean isShutdown() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isShutdown", null, null)
            .get();
    }
    
    // ==================== DIRECT MEMORY MANAGEMENT ====================
    
    /**
     * Returns the maximum amount of allocatable direct buffer memory
     * @return maximum direct memory in bytes
     */
    public static long maxDirectMemory() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("maxDirectMemory", null, null)
            .get();
    }
    
    /**
     * Returns true if direct buffers should be page aligned
     * @return true if direct memory is page aligned
     */
    public static boolean isDirectMemoryPageAligned() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isDirectMemoryPageAligned", null, null)
            .get();
    }
    
    // ==================== CLASS LOADER UTILITIES ====================
    
    /**
     * Returns true if the given class loader is the bootstrap class loader
     * or the platform class loader
     * @param loader class loader to check
     * @return true if system domain loader
     */
    public static boolean isSystemDomainLoader(ClassLoader loader) {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isSystemDomainLoader", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    /**
     * Returns the first user-defined class loader up the execution stack,
     * or the platform class loader if only code from the platform or
     * bootstrap class loader is on the stack
     * @return latest user defined class loader
     */
    public static ClassLoader latestUserDefinedLoader() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("latestUserDefinedLoader", null, null)
            .get();
    }
    
    // ==================== SYSTEM PROPERTIES ====================
    
    /**
     * Returns the system property of the specified key saved at system initialization time
     * @param key property key
     * @return saved property value
     */
    public static String getSavedProperty(String key) {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getSavedProperty", new Class<?>[]{String.class}, new Object[]{key})
            .get();
    }
    
    /**
     * Gets an unmodifiable view of the system properties saved at system initialization time
     * @return saved properties map
     */
    public static Map<String, String> getSavedProperties() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getSavedProperties", null, null)
            .get();
    }
    
    // ==================== OS ENVIRONMENT ====================
    
    /**
     * Initialize any miscellaneous operating system settings
     */
    public static void initializeOSEnvironment() {
        ReflectBuilder.of(vmClass)
            .staticMethod("initializeOSEnvironment", null, null)
            .get();
    }
    
    /**
     * Returns true if we are in a set UID program
     * @return true if running with set UID
     */
    public static boolean isSetUID() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("isSetUID", null, null)
            .get();
    }
    
    /**
     * Returns the real user ID of the calling process
     * @return user ID or -1 if not available
     */
    public static long getuid() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getuid", null, null)
            .get();
    }
    
    /**
     * Returns the effective user ID of the calling process
     * @return effective user ID or -1 if not available
     */
    public static long geteuid() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("geteuid", null, null)
            .get();
    }
    
    /**
     * Returns the real group ID of the calling process
     * @return group ID or -1 if not available
     */
    public static long getgid() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getgid", null, null)
            .get();
    }
    
    /**
     * Returns the effective group ID of the calling process
     * @return effective group ID or -1 if not available
     */
    public static long getegid() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getegid", null, null)
            .get();
    }
    
    // ==================== FINALIZATION TRACKING ====================
    
    /**
     * Gets the number of objects pending for finalization
     * @return number of objects pending finalization
     */
    public static int getFinalRefCount() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getFinalRefCount", null, null)
            .get();
    }
    
    /**
     * Gets the peak number of objects pending for finalization
     * @return peak number of objects pending finalization
     */
    public static int getPeakFinalRefCount() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getPeakFinalRefCount", null, null)
            .get();
    }
    
    /**
     * Add n to the objects pending for finalization count
     * @param n value to add to finalization count
     */
    public static void addFinalRefCount(int n) {
        ReflectBuilder.of(vmClass)
            .staticMethod("addFinalRefCount", new Class<?>[]{int.class}, new Object[]{n})
            .get();
    }
    
    // ==================== THREAD UTILITIES ====================
    
    /**
     * Returns Thread.State for the given threadStatus
     * @param threadStatus JVMTI thread status
     * @return Thread.State for the status
     */
    public static Thread.State toThreadState(int threadStatus) {
        return ReflectBuilder.of(vmClass)
            .staticMethod("toThreadState", new Class<?>[]{int.class}, new Object[]{threadStatus})
            .get();
    }
    
    // ==================== TIMING UTILITIES ====================
    
    /**
     * Get a nanosecond time stamp adjustment in the form of a single long
     * @param offsetInSeconds offset in seconds from which the nanosecond time stamp should be computed
     * @return nanosecond time stamp adjustment or -1 if offset is too far off
     */
    public static long getNanoTimeAdjustment(long offsetInSeconds) {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getNanoTimeAdjustment", new Class<?>[]{long.class}, new Object[]{offsetInSeconds})
            .get();
    }
    
    // ==================== RUNTIME ARGUMENTS ====================
    
    /**
     * Returns the VM arguments for this runtime environment
     * @return array of VM runtime arguments
     */
    public static String[] getRuntimeArguments() {
        return ReflectBuilder.of(vmClass)
            .staticMethod("getRuntimeArguments", null, null)
            .get();
    }
    
    // ==================== ARCHIVE SUPPORT ====================
    
    /**
     * Initialize archived static fields in the given Class using archived
     * values from CDS dump time
     * @param clazz class to initialize from archive
     */
    public static void initializeFromArchive(Class<?> clazz) {
        ReflectBuilder.of(vmClass)
            .staticMethod("initializeFromArchive", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive VM status and information
     * @return detailed VM status information
     */
    public static VMInfo getVMInfo() {
        VMInfo info = new VMInfo();
        
        try {
            info.available = isAvailable();
            info.initLevel = initLevel();
            info.isModuleSystemInited = isModuleSystemInited();
            info.isBooted = isBooted();
            info.isShutdown = isShutdown();
            info.maxDirectMemory = maxDirectMemory();
            info.isDirectMemoryPageAligned = isDirectMemoryPageAligned();
            info.finalRefCount = getFinalRefCount();
            info.peakFinalRefCount = getPeakFinalRefCount();
            info.isSetUID = isSetUID();
            
            try {
                info.uid = getuid();
                info.euid = geteuid();
                info.gid = getgid();
                info.egid = getegid();
            } catch (Exception e) {
                // UID/GID not available on this platform
            }
            
            try {
                info.runtimeArguments = getRuntimeArguments();
            } catch (Exception e) {
                // Runtime arguments not available
            }
            
            info.savedProperties = getSavedProperties();
            
        } catch (Exception e) {
            info.error = e.getMessage();
        }
        
        return info;
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Comprehensive VM information structure
     */
    public static class VMInfo {
        public boolean available;
        public int initLevel = -1;
        public boolean isModuleSystemInited;
        public boolean isBooted;
        public boolean isShutdown;
        public long maxDirectMemory = -1;
        public boolean isDirectMemoryPageAligned;
        public int finalRefCount = -1;
        public int peakFinalRefCount = -1;
        public boolean isSetUID;
        public long uid = -1;
        public long euid = -1;
        public long gid = -1;
        public long egid = -1;
        public String[] runtimeArguments;
        public Map<String, String> savedProperties;
        public String error;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("VM Information:\n");
            sb.append("  Available: ").append(available).append("\n");
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            sb.append("  Init Level: ").append(initLevel).append(" (").append(getInitLevelName(initLevel)).append(")\n");
            sb.append("  Module System: ").append(isModuleSystemInited ? "Initialized" : "Not Initialized").append("\n");
            sb.append("  VM State: ").append(isBooted ? "Booted" : "Not Booted");
            if (isShutdown) sb.append(" (Shutdown)");
            sb.append("\n");
            sb.append("  Direct Memory: ").append(maxDirectMemory / (1024 * 1024)).append(" MB");
            sb.append(isDirectMemoryPageAligned ? " (Page Aligned)" : "").append("\n");
            sb.append("  Finalization: ").append(finalRefCount).append(" pending (peak: ").append(peakFinalRefCount).append(")\n");
            sb.append("  Security: ").append(isSetUID ? "SetUID" : "Normal");
            if (uid != -1) {
                sb.append(", UID=").append(uid).append("/").append(euid);
                sb.append(", GID=").append(gid).append("/").append(egid);
            }
            sb.append("\n");
            if (runtimeArguments != null) {
                sb.append("  Runtime Args: ").append(runtimeArguments.length).append(" arguments\n");
            }
            if (savedProperties != null) {
                sb.append("  Saved Properties: ").append(savedProperties.size()).append(" properties");
            }
            return sb.toString();
        }
        
        private String getInitLevelName(int level) {
            switch (level) {
                case JAVA_LANG_SYSTEM_INITED: return "System Initialized";
                case MODULE_SYSTEM_INITED: return "Module System Initialized";
                case SYSTEM_LOADER_INITIALIZING: return "System Loader Initializing";
                case SYSTEM_BOOTED: return "System Booted";
                case SYSTEM_SHUTDOWN: return "System Shutdown";
                default: return "Unknown";
            }
        }
    }
}