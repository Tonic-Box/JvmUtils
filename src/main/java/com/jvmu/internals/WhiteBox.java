package com.jvmu.internals;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.util.ReflectBuilder;
import sun.misc.Unsafe;

import java.lang.management.MemoryUsage;
import java.lang.reflect.Executable;
import java.util.Objects;

/**
 * WhiteBox - Wrapper for sun.hotspot.WhiteBox using privileged access
 * 
 * Provides access to HotSpot WhiteBox testing APIs without requiring -XX:+WhiteBoxAPI
 * or special JVM flags. Uses privileged access via ModuleBootstrap to bypass security
 * restrictions and access internal JVM testing functionality.
 * 
 * Key capabilities:
 * - Memory and object inspection
 * - JIT compilation control
 * - Garbage collection control
 * - JVM flag manipulation
 * - Class and module system access
 * - Performance profiling and monitoring
 * 
 * This wrapper eliminates the need for:
 * - -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI
 * - Special classpath configuration
 * - Native library dependencies
 */
public class WhiteBox {
    
    private static final Object whiteBoxInstance;
    private static final Class<?> whiteBoxClass;
    private static final boolean available;
    
    static {
        Object instance = null;
        Class<?> clazz = null;
        boolean isAvailable = false;
        
        try {
            // Load WhiteBox class using privileged access
            clazz = Class.forName("sun.hotspot.WhiteBox");
            
            // Get WhiteBox instance bypassing security check
            instance = ReflectBuilder.of(clazz)
                .staticMethod("getWhiteBox", null, null)
                .get();
            
            if (instance != null) {
                isAvailable = true;
            }
        } catch (Exception e) {
            // WhiteBox not available - silent fallback
        }
        
        whiteBoxInstance = instance;
        whiteBoxClass = clazz;
        available = isAvailable;
    }
    
    /**
     * Check if WhiteBox functionality is available
     * @return true if WhiteBox APIs can be used
     */
    public static boolean isAvailable() {
        return available && ModuleBootstrap.getInternalUnsafe() != null;
    }
    
    /**
     * Get status information about WhiteBox availability
     * @return status string
     */
    public static String getStatus() {
        if (available) {
            return "WhiteBox: Available ✓ (Bypassed -XX:+WhiteBoxAPI requirement)";
        } else {
            return "WhiteBox: Not Available ✗ (HotSpot WhiteBox class not found)";
        }
    }
    
    // ==================== MEMORY OPERATIONS ====================
    
    /**
     * Get the native memory address of an object
     * @param obj the object
     * @return native memory address
     */
    public static long getObjectAddress(Object obj) {
        Objects.requireNonNull(obj);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getObjectAddress", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Get the size of an object in bytes
     * @param obj the object
     * @return object size in bytes
     */
    public static long getObjectSize(Object obj) {
        Objects.requireNonNull(obj);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getObjectSize", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Check if object is in old generation
     * @param obj the object to check
     * @return true if object is in old generation
     */
    public static boolean isObjectInOldGen(Object obj) {
        Objects.requireNonNull(obj);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isObjectInOldGen", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Get heap OOP size (compressed OOPs)
     * @return heap OOP size in bytes
     */
    public static int getHeapOopSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getHeapOopSize", null, null)
            .get();
    }
    
    /**
     * Get VM page size
     * @return VM page size in bytes
     */
    public static int getVMPageSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getVMPageSize", null, null)
            .get();
    }
    
    /**
     * Get VM allocation granularity
     * @return allocation granularity in bytes
     */
    public static long getVMAllocationGranularity() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getVMAllocationGranularity", null, null)
            .get();
    }
    
    /**
     * Get VM large page size
     * @return large page size in bytes
     */
    public static long getVMLargePageSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getVMLargePageSize", null, null)
            .get();
    }
    
    /**
     * Get heap space alignment
     * @return heap space alignment in bytes
     */
    public static long getHeapSpaceAlignment() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getHeapSpaceAlignment", null, null)
            .get();
    }
    
    /**
     * Get heap alignment
     * @return heap alignment in bytes
     */
    public static long getHeapAlignment() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getHeapAlignment", null, null)
            .get();
    }
    
    // ==================== RUNTIME OPERATIONS ====================
    
    /**
     * Check if a class is alive (loaded and not unloaded)
     * @param className class name in internal format (/ separated)
     * @return true if class is alive
     */
    public static boolean isClassAlive(String className) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isClassAlive", new Class<?>[]{String.class}, new Object[]{className})
            .get();
    }
    
    /**
     * Get symbol reference count
     * @param name symbol name
     * @return reference count
     */
    public static int getSymbolRefcount(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getSymbolRefcount", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Check if object monitor is inflated
     * @param obj object to check
     * @return true if monitor is inflated
     */
    public static boolean isMonitorInflated(Object obj) {
        Objects.requireNonNull(obj);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isMonitorInflated", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Force a safepoint
     */
    public static void forceSafepoint() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("forceSafepoint", null, null)
            .get();
    }
    
    // ==================== CONSTANT POOL OPERATIONS ====================
    
    /**
     * Get constant pool address for a class
     * @param clazz the class
     * @return constant pool address
     */
    public static long getConstantPool(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getConstantPool", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get constant pool cache index tag
     * @return cache index tag
     */
    public static int getConstantPoolCacheIndexTag() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getConstantPoolCacheIndexTag", null, null)
            .get();
    }
    
    /**
     * Get constant pool cache length
     * @param clazz the class
     * @return cache length
     */
    public static int getConstantPoolCacheLength(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getConstantPoolCacheLength", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Remap instruction operand from CP cache
     * @param clazz the class
     * @param index the index
     * @return remapped index
     */
    public static int remapInstructionOperandFromCPCache(Class<?> clazz, int index) {
        Objects.requireNonNull(clazz);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("remapInstructionOperandFromCPCache", new Class<?>[]{Class.class, int.class}, new Object[]{clazz, index})
            .get();
    }
    
    /**
     * Encode constant pool indy index
     * @param index the index
     * @return encoded index
     */
    public static int encodeConstantPoolIndyIndex(int index) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("encodeConstantPoolIndyIndex", new Class<?>[]{int.class}, new Object[]{index})
            .get();
    }
    
    // ==================== JVMTI OPERATIONS ====================
    
    /**
     * Add to bootstrap class loader search
     * @param segment path segment to add
     */
    public static void addToBootstrapClassLoaderSearch(String segment) {
        Objects.requireNonNull(segment);
        ReflectBuilder.of(whiteBoxInstance)
            .method("addToBootstrapClassLoaderSearch", new Class<?>[]{String.class}, new Object[]{segment})
            .get();
    }
    
    /**
     * Add to system class loader search
     * @param segment path segment to add
     */
    public static void addToSystemClassLoaderSearch(String segment) {
        Objects.requireNonNull(segment);
        ReflectBuilder.of(whiteBoxInstance)
            .method("addToSystemClassLoaderSearch", new Class<?>[]{String.class}, new Object[]{segment})
            .get();
    }
    
    // ==================== GARBAGE COLLECTION ====================
    
    /**
     * Check if G1 is in concurrent mark
     * @return true if in concurrent mark phase
     */
    public static boolean g1InConcurrentMark() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1InConcurrentMark", null, null)
            .get();
    }
    
    /**
     * Check if object is humongous in G1
     * @param obj object to check
     * @return true if object is humongous
     */
    public static boolean g1IsHumongous(Object obj) {
        Objects.requireNonNull(obj);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1IsHumongous", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Check if address belongs to humongous region in G1
     * @param address memory address
     * @return true if belongs to humongous region
     */
    public static boolean g1BelongsToHumongousRegion(long address) {
        if (address == 0) {
            throw new IllegalArgumentException("address cannot be null");
        }
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1BelongsToHumongousRegion", new Class<?>[]{long.class}, new Object[]{address})
            .get();
    }
    
    /**
     * Check if address belongs to free region in G1
     * @param address memory address
     * @return true if belongs to free region
     */
    public static boolean g1BelongsToFreeRegion(long address) {
        if (address == 0) {
            throw new IllegalArgumentException("address cannot be null");
        }
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1BelongsToFreeRegion", new Class<?>[]{long.class}, new Object[]{address})
            .get();
    }
    
    /**
     * Get maximum number of G1 regions
     * @return maximum G1 regions
     */
    public static long g1NumMaxRegions() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1NumMaxRegions", null, null)
            .get();
    }
    
    /**
     * Get number of free G1 regions
     * @return number of free G1 regions
     */
    public static long g1NumFreeRegions() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1NumFreeRegions", null, null)
            .get();
    }
    
    /**
     * Get G1 region size
     * @return G1 region size in bytes
     */
    public static int g1RegionSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1RegionSize", null, null)
            .get();
    }
    
    /**
     * Get G1 auxiliary memory usage
     * @return G1 auxiliary memory usage
     */
    public static MemoryUsage g1AuxiliaryMemoryUsage() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1AuxiliaryMemoryUsage", null, null)
            .get();
    }
    
    /**
     * Force young GC
     */
    public static void youngGC() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("youngGC", null, null)
            .get();
    }
    
    /**
     * Force full GC
     */
    public static void fullGC() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("fullGC", null, null)
            .get();
    }
    
    /**
     * Check if concurrent GC phase control is supported
     * @return true if supported
     */
    public static boolean supportsConcurrentGCPhaseControl() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("supportsConcurrentGCPhaseControl", null, null)
            .get();
    }
    
    /**
     * Get concurrent GC phases
     * @return array of phase names
     */
    public static String[] getConcurrentGCPhases() {

        return ReflectBuilder.of(whiteBoxInstance)
            .method("getConcurrentGCPhases", null, null)
            .get();
    }
    
    /**
     * Request concurrent GC phase
     * @param phase phase name
     */
    public static void requestConcurrentGCPhase(String phase) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("requestConcurrentGCPhase", new Class<?>[]{String.class}, new Object[]{phase})
            .get();
    }
    
    /**
     * Start G1 concurrent mark cycle
     * @return true if cycle started
     */
    public static boolean g1StartConcMarkCycle() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("g1StartConcMarkCycle", null, null)
            .get();
    }
    
    // ==================== JIT COMPILATION ====================
    
    /**
     * Check if method is compiled
     * @param method the method
     * @return true if compiled
     */
    public static boolean isMethodCompiled(Executable method) {
        return isMethodCompiled(method, false);
    }
    
    /**
     * Check if method is compiled
     * @param method the method
     * @param isOsr true to check OSR compilation
     * @return true if compiled
     */
    public static boolean isMethodCompiled(Executable method, boolean isOsr) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isMethodCompiled", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, isOsr})
            .get();
    }
    
    /**
     * Check if method is compilable
     * @param method the method
     * @return true if compilable
     */
    public static boolean isMethodCompilable(Executable method) {
        return isMethodCompilable(method, -2);
    }
    
    /**
     * Check if method is compilable at specific level
     * @param method the method
     * @param compLevel compilation level
     * @return true if compilable
     */
    public static boolean isMethodCompilable(Executable method, int compLevel) {
        return isMethodCompilable(method, compLevel, false);
    }
    
    /**
     * Check if method is compilable
     * @param method the method
     * @param compLevel compilation level
     * @param isOsr true to check OSR compilation
     * @return true if compilable
     */
    public static boolean isMethodCompilable(Executable method, int compLevel, boolean isOsr) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isMethodCompilable", new Class<?>[]{Executable.class, int.class, boolean.class}, new Object[]{method, compLevel, isOsr})
            .get();
    }
    
    /**
     * Check if method is queued for compilation
     * @param method the method
     * @return true if queued
     */
    public static boolean isMethodQueuedForCompilation(Executable method) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isMethodQueuedForCompilation", new Class<?>[]{Executable.class}, new Object[]{method})
            .get();
    }
    
    /**
     * Check if intrinsic is available for method
     * @param method the method
     * @param compLevel compilation level
     * @return true if intrinsic is available
     */
    public static boolean isIntrinsicAvailable(Executable method, int compLevel) {
        return isIntrinsicAvailable(method, null, compLevel);
    }
    
    /**
     * Check if intrinsic is available for method
     * @param method the method
     * @param compilationContext compilation context
     * @param compLevel compilation level
     * @return true if intrinsic is available
     */
    public static boolean isIntrinsicAvailable(Executable method, Executable compilationContext, int compLevel) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isIntrinsicAvailable", new Class<?>[]{Executable.class, Executable.class, int.class}, new Object[]{method, compilationContext, compLevel})
            .get();
    }
    
    /**
     * Deoptimize method
     * @param method the method
     * @return deoptimization result
     */
    public static int deoptimizeMethod(Executable method) {
        return deoptimizeMethod(method, false);
    }
    
    /**
     * Deoptimize method
     * @param method the method
     * @param isOsr true for OSR deoptimization
     * @return deoptimization result
     */
    public static int deoptimizeMethod(Executable method, boolean isOsr) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("deoptimizeMethod", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, isOsr})
            .get();
    }
    
    /**
     * Make method not compilable
     * @param method the method
     */
    public static void makeMethodNotCompilable(Executable method) {
        makeMethodNotCompilable(method, -2);
    }
    
    /**
     * Make method not compilable at specific level
     * @param method the method
     * @param compLevel compilation level
     */
    public static void makeMethodNotCompilable(Executable method, int compLevel) {
        makeMethodNotCompilable(method, compLevel, false);
    }
    
    /**
     * Make method not compilable
     * @param method the method
     * @param compLevel compilation level
     * @param isOsr true for OSR compilation
     */
    public static void makeMethodNotCompilable(Executable method, int compLevel, boolean isOsr) {
        Objects.requireNonNull(method);
        ReflectBuilder.of(whiteBoxInstance)
            .method("makeMethodNotCompilable", new Class<?>[]{Executable.class, int.class, boolean.class}, new Object[]{method, compLevel, isOsr})
            .get();
    }
    
    /**
     * Get method compilation level
     * @param method the method
     * @return compilation level
     */
    public static int getMethodCompilationLevel(Executable method) {
        return getMethodCompilationLevel(method, false);
    }
    
    /**
     * Get method compilation level
     * @param method the method
     * @param isOsr true to check OSR compilation
     * @return compilation level
     */
    public static int getMethodCompilationLevel(Executable method, boolean isOsr) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getMethodCompilationLevel", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, isOsr})
            .get();
    }
    
    /**
     * Set dont inline method
     * @param method the method
     * @param value true to prevent inlining
     * @return previous value
     */
    public static boolean testSetDontInlineMethod(Executable method, boolean value) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("testSetDontInlineMethod", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, value})
            .get();
    }
    
    /**
     * Set force inline method
     * @param method the method
     * @param value true to force inlining
     * @return previous value
     */
    public static boolean testSetForceInlineMethod(Executable method, boolean value) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("testSetForceInlineMethod", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, value})
            .get();
    }
    
    /**
     * Enqueue method for compilation
     * @param method the method
     * @param compLevel compilation level
     * @return true if enqueued
     */
    public static boolean enqueueMethodForCompilation(Executable method, int compLevel) {
        return enqueueMethodForCompilation(method, compLevel, -1);
    }
    
    /**
     * Enqueue method for compilation
     * @param method the method
     * @param compLevel compilation level
     * @param entryBci entry BCI for OSR
     * @return true if enqueued
     */
    public static boolean enqueueMethodForCompilation(Executable method, int compLevel, int entryBci) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("enqueueMethodForCompilation", new Class<?>[]{Executable.class, int.class, int.class}, new Object[]{method, compLevel, entryBci})
            .get();
    }
    
    /**
     * Enqueue initializer for compilation
     * @param clazz the class
     * @param compLevel compilation level
     * @return true if enqueued
     */
    public static boolean enqueueInitializerForCompilation(Class<?> clazz, int compLevel) {
        Objects.requireNonNull(clazz);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("enqueueInitializerForCompilation", new Class<?>[]{Class.class, int.class}, new Object[]{clazz, compLevel})
            .get();
    }
    
    /**
     * Clear method state
     * @param method the method
     */
    public static void clearMethodState(Executable method) {
        Objects.requireNonNull(method);
        ReflectBuilder.of(whiteBoxInstance)
            .method("clearMethodState", new Class<?>[]{Executable.class}, new Object[]{method})
            .get();
    }
    
    /**
     * Mark method as profiled
     * @param method the method
     */
    public static void markMethodProfiled(Executable method) {
        Objects.requireNonNull(method);
        ReflectBuilder.of(whiteBoxInstance)
            .method("markMethodProfiled", new Class<?>[]{Executable.class}, new Object[]{method})
            .get();
    }
    
    /**
     * Lock compilation
     */
    public static void lockCompilation() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("lockCompilation", null, null)
            .get();
    }
    
    /**
     * Unlock compilation
     */
    public static void unlockCompilation() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("unlockCompilation", null, null)
            .get();
    }
    
    /**
     * Get compilation queue size
     * @return queue size
     */
    public static int getCompileQueuesSize() {
        return getCompileQueueSize(-2);
    }
    
    /**
     * Get compilation queue size for specific level
     * @param compLevel compilation level
     * @return queue size
     */
    public static int getCompileQueueSize(int compLevel) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getCompileQueueSize", new Class<?>[]{int.class}, new Object[]{compLevel})
            .get();
    }
    
    /**
     * Get method entry BCI
     * @param method the method
     * @return entry BCI
     */
    public static int getMethodEntryBci(Executable method) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getMethodEntryBci", new Class<?>[]{Executable.class}, new Object[]{method})
            .get();
    }
    
    /**
     * Get NMethod information
     * @param method the method
     * @param isOsr true for OSR compilation
     * @return NMethod info array
     */
    public static Object[] getNMethod(Executable method, boolean isOsr) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getNMethod", new Class<?>[]{Executable.class, boolean.class}, new Object[]{method, isOsr})
            .get();
    }
    
    /**
     * Allocate code blob
     * @param size blob size
     * @param type blob type
     * @return code blob address
     */
    public static long allocateCodeBlob(int size, int type) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("allocateCodeBlob", new Class<?>[]{int.class, int.class}, new Object[]{size, type})
            .get();
    }
    
    /**
     * Free code blob
     * @param address blob address
     */
    public static void freeCodeBlob(long address) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("freeCodeBlob", new Class<?>[]{long.class}, new Object[]{address})
            .get();
    }
    
    /**
     * Force NMethod sweep
     */
    public static void forceNMethodSweep() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("forceNMethodSweep", null, null)
            .get();
    }
    
    /**
     * Get code heap entries
     * @param type heap type
     * @return code heap entries
     */
    public static Object[] getCodeHeapEntries(int type) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getCodeHeapEntries", new Class<?>[]{int.class}, new Object[]{type})
            .get();
    }
    
    /**
     * Get compilation activity mode
     * @return activity mode
     */
    public static int getCompilationActivityMode() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getCompilationActivityMode", null, null)
            .get();
    }
    
    /**
     * Get method data
     * @param method the method
     * @return method data address
     */
    public static long getMethodData(Executable method) {
        Objects.requireNonNull(method);
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getMethodData", new Class<?>[]{Executable.class}, new Object[]{method})
            .get();
    }
    
    /**
     * Get code blob information
     * @param address blob address
     * @return code blob info
     */
    public static Object[] getCodeBlob(long address) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getCodeBlob", new Class<?>[]{long.class}, new Object[]{address})
            .get();
    }
    
    /**
     * Clear inline caches
     */
    public static void clearInlineCaches() {
        clearInlineCaches(false);
    }
    
    /**
     * Clear inline caches
     * @param preserveStaticStubs true to preserve static stubs
     */
    public static void clearInlineCaches(boolean preserveStaticStubs) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("clearInlineCaches", new Class<?>[]{boolean.class}, new Object[]{preserveStaticStubs})
            .get();
    }
    
    /**
     * Deoptimize all methods
     */
    public static void deoptimizeAll() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("deoptimizeAll", null, null)
            .get();
    }
    
    /**
     * Deoptimize frames
     * @param makeNotEntrant true to make methods not entrant
     * @return number of deoptimized frames
     */
    public static int deoptimizeFrames(boolean makeNotEntrant) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("deoptimizeFrames", new Class<?>[]{boolean.class}, new Object[]{makeNotEntrant})
            .get();
    }
    
    // ==================== VM FLAGS ====================
    
    /**
     * Check if VM flag is constant
     * @param name flag name
     * @return true if constant
     */
    public static boolean isConstantVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isConstantVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Check if VM flag is locked
     * @param name flag name
     * @return true if locked
     */
    public static boolean isLockedVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isLockedVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Set boolean VM flag
     * @param name flag name
     * @param value flag value
     */
    public static void setBooleanVMFlag(String name, boolean value) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("setBooleanVMFlag", new Class<?>[]{String.class, boolean.class}, new Object[]{name, value})
            .get();
    }
    
    /**
     * Set int VM flag
     * @param name flag name
     * @param value flag value
     */
    public static void setIntVMFlag(String name, long value) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("setIntVMFlag", new Class<?>[]{String.class, long.class}, new Object[]{name, value})
            .get();
    }
    
    /**
     * Set string VM flag
     * @param name flag name
     * @param value flag value
     */
    public static void setStringVMFlag(String name, String value) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("setStringVMFlag", new Class<?>[]{String.class, String.class}, new Object[]{name, value})
            .get();
    }
    
    /**
     * Get boolean VM flag
     * @param name flag name
     * @return flag value
     */
    public static Boolean getBooleanVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getBooleanVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Get int VM flag
     * @param name flag name
     * @return flag value
     */
    public static Long getIntVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getIntVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Get string VM flag
     * @param name flag name
     * @return flag value
     */
    public static String getStringVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getStringVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    /**
     * Get any VM flag value
     * @param name flag name
     * @return flag value (auto-detected type)
     */
    public static Object getVMFlag(String name) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getVMFlag", new Class<?>[]{String.class}, new Object[]{name})
            .get();
    }
    
    // ==================== METASPACE ====================
    
    /**
     * Allocate metaspace
     * @param classLoader class loader
     * @param size allocation size
     * @return metaspace address
     */
    public static long allocateMetaspace(ClassLoader classLoader, long size) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("allocateMetaspace", new Class<?>[]{ClassLoader.class, long.class}, new Object[]{classLoader, size})
            .get();
    }
    
    /**
     * Free metaspace
     * @param classLoader class loader
     * @param address metaspace address
     * @param size allocation size
     */
    public static void freeMetaspace(ClassLoader classLoader, long address, long size) {
        ReflectBuilder.of(whiteBoxInstance)
            .method("freeMetaspace", new Class<?>[]{ClassLoader.class, long.class, long.class}, new Object[]{classLoader, address, size})
            .get();
    }
    
    /**
     * Increase metaspace capacity until GC
     * @param increment capacity increment
     * @return new capacity
     */
    public static long incMetaspaceCapacityUntilGC(long increment) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("incMetaspaceCapacityUntilGC", new Class<?>[]{long.class}, new Object[]{increment})
            .get();
    }
    
    /**
     * Get metaspace capacity until GC
     * @return capacity
     */
    public static long metaspaceCapacityUntilGC() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("metaspaceCapacityUntilGC", null, null)
            .get();
    }
    
    /**
     * Check if metaspace should concurrent collect
     * @return true if should collect
     */
    public static boolean metaspaceShouldConcurrentCollect() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("metaspaceShouldConcurrentCollect", null, null)
            .get();
    }
    
    /**
     * Get metaspace reserve alignment
     * @return alignment value
     */
    public static long metaspaceReserveAlignment() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("metaspaceReserveAlignment", null, null)
            .get();
    }
    
    /**
     * Clean metaspaces
     */
    public static void cleanMetaspaces() {
        ReflectBuilder.of(whiteBoxInstance)
            .method("cleanMetaspaces", null, null)
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Check if string is in string table
     * @param str string to check
     * @return true if interned
     */
    public static boolean isInStringTable(String str) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isInStringTable", new Class<?>[]{String.class}, new Object[]{str})
            .get();
    }
    
    /**
     * Get CPU features
     * @return CPU features string
     */
    public static String getCPUFeatures() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getCPUFeatures", null, null)
            .get();
    }
    
    /**
     * Get thread stack size
     * @return stack size
     */
    public static long getThreadStackSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getThreadStackSize", null, null)
            .get();
    }
    
    /**
     * Get thread remaining stack size
     * @return remaining stack size
     */
    public static long getThreadRemainingStackSize() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getThreadRemainingStackSize", null, null)
            .get();
    }
    
    /**
     * Check if object is shared
     * @param obj object to check
     * @return true if shared
     */
    public static boolean isShared(Object obj) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isShared", new Class<?>[]{Object.class}, new Object[]{obj})
            .get();
    }
    
    /**
     * Check if class is shared
     * @param clazz class to check
     * @return true if shared
     */
    public static boolean isSharedClass(Class<?> clazz) {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isSharedClass", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Check if CDS is included
     * @return true if CDS is included
     */
    public static boolean isCDSIncluded() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isCDSIncluded", null, null)
            .get();
    }
    
    /**
     * Check if JFR is included
     * @return true if JFR is included
     */
    public static boolean isJFRIncluded() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("isJFRIncluded", null, null)
            .get();
    }
    
    /**
     * Get number of AOT libraries loaded
     * @return AOT library count
     */
    public static int aotLibrariesCount() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("aotLibrariesCount", null, null)
            .get();
    }
    
    /**
     * Get libc name
     * @return libc name
     */
    public static String getLibcName() {
        return ReflectBuilder.of(whiteBoxInstance)
            .method("getLibcName", null, null)
            .get();
    }
}