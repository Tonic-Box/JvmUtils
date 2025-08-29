package com.jvmu.jvmti;

import com.jvmu.module.ModuleBootstrap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

/**
 * VM Object Access - Direct access to JVM runtime objects and structures
 * 
 * This class provides JVMTI-like functionality by directly accessing JVM internal
 * structures through privileged access. Unlike traditional JVMTI which requires
 * native agents, this works purely from Java using internal APIs.
 */
public class VMObjectAccess {
    
    private static Object internalUnsafe;
    private static MethodHandles.Lookup trustedLookup;
    private static boolean initialized = false;
    
    // VM structure access handles
    private static MethodHandle getObjectSizeHandle;
    private static MethodHandle getAllThreadsHandle;
    private static MethodHandle getCurrentThreadHandle;
    private static MethodHandle walkObjectsHandle;
    
    // Thread access methods
    private static Method threadFromJNIMethod;
    private static Method currentThreadMethod;
    
    // Object inspection methods
    private static final Map<Class<?>, Long> classSizeCache = new ConcurrentHashMap<>();
    
    static {
        try {
            initialize();
        } catch (Exception e) {
            System.err.println("VMObjectAccess initialization failed: " + e.getMessage());
        }
    }
    
    private static void initialize() throws Exception {
        internalUnsafe = ModuleBootstrap.getInternalUnsafe();
        trustedLookup = ModuleBootstrap.getTrustedLookup();
        
        if (internalUnsafe == null || trustedLookup == null) {
            System.err.println("VMObjectAccess requires privileged access via ModuleBootstrap");
            return;
        }
        
        setupUnsafeAccess();
        setupThreadAccess();
        setupObjectAccess();
        
        initialized = true;
        System.out.println("VMObjectAccess initialized - Direct JVM access enabled");
    }
    
    private static void setupUnsafeAccess() throws Exception {
        Class<?> unsafeClass = internalUnsafe.getClass();
        
        // Get method for object size calculation
        Method objectFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
        getObjectSizeHandle = trustedLookup.unreflect(objectFieldOffsetMethod);
    }
    
    private static void setupThreadAccess() throws Exception {
        try {
            // Access to JavaThread.current() equivalent
            Class<?> threadClass = Class.forName("java.lang.Thread");
            currentThreadMethod = threadClass.getDeclaredMethod("currentThread");
            
            // Try to access internal thread methods
            Class<?> javaThreadClass = null;
            try {
                // This may not exist in all JDK builds, but worth trying
                javaThreadClass = Class.forName("sun.misc.JavaThread");
            } catch (ClassNotFoundException e) {
                // Try HotSpot internal class
                try {
                    javaThreadClass = Class.forName("jdk.internal.misc.JavaThread");
                } catch (ClassNotFoundException e2) {
                    // Use Thread class itself as fallback
                    javaThreadClass = threadClass;
                }
            }
            
        } catch (Exception e) {
            System.err.println("Thread access setup partial: " + e.getMessage());
        }
    }
    
    private static void setupObjectAccess() throws Exception {
        Class<?> unsafeClass = internalUnsafe.getClass();
        
        // Setup methods for walking heap objects
        try {
            Method getObjectMethod = unsafeClass.getDeclaredMethod("getObject", Object.class, long.class);
            Method putObjectMethod = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
            
            // These will help us navigate object structures
        } catch (Exception e) {
            System.err.println("Object access setup partial: " + e.getMessage());
        }
    }
    
    /**
     * Get all live Java threads in the VM
     * This provides JVMTI GetAllThreads equivalent functionality
     */
    public static Thread[] getAllThreads() {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        try {
            // Access the thread group hierarchy to get all threads
            ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
            ThreadGroup parent;
            while ((parent = rootGroup.getParent()) != null) {
                rootGroup = parent;
            }
            
            // Estimate thread count and get all threads
            int threadCount = rootGroup.activeCount();
            Thread[] threads = new Thread[threadCount * 2]; // Buffer for growth
            int actualCount = rootGroup.enumerate(threads, true);
            
            // Resize to actual count
            Thread[] result = new Thread[actualCount];
            System.arraycopy(threads, 0, result, 0, actualCount);
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get all threads: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the current Java thread
     * This provides JVMTI GetCurrentThread equivalent functionality
     */
    public static Thread getCurrentThread() {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        return Thread.currentThread();
    }
    
    /**
     * Get thread information including native thread details
     */
    public static ThreadInfo getThreadInfo(Thread thread) {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        try {
            ThreadInfo info = new ThreadInfo();
            info.thread = thread;
            info.name = thread.getName();
            info.id = thread.getId();
            info.state = thread.getState();
            info.priority = thread.getPriority();
            info.daemon = thread.isDaemon();
            info.alive = thread.isAlive();
            
            // Try to get internal thread details using unsafe access
            try {
                Class<?> threadClass = thread.getClass();
                Field nativeThreadField = null;
                
                // Look for native thread ID fields
                Field[] fields = threadClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().contains("tid") || field.getName().contains("nativeId")) {
                        field.setAccessible(true);
                        Object value = field.get(thread);
                        if (value instanceof Long) {
                            info.nativeThreadId = (Long) value;
                            break;
                        }
                    }
                }
                
                // Get stack trace for current execution context
                StackTraceElement[] stack = thread.getStackTrace();
                info.stackDepth = stack.length;
                info.currentFrame = stack.length > 0 ? stack[0] : null;
                
            } catch (Exception e) {
                // Native details not accessible, continue with basic info
            }
            
            return info;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get thread info: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get object size in bytes
     * This provides JVMTI GetObjectSize equivalent functionality
     */
    public static long getObjectSize(Object obj) {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        if (obj == null) {
            return 0;
        }
        
        try {
            Class<?> clazz = obj.getClass();
            
            // Check cache first
            Long cachedSize = classSizeCache.get(clazz);
            if (cachedSize != null && !clazz.isArray()) {
                return cachedSize;
            }
            
            long size = calculateObjectSize(obj, clazz);
            
            // Cache non-array object sizes
            if (!clazz.isArray()) {
                classSizeCache.put(clazz, size);
            }
            
            return size;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object size: " + e.getMessage(), e);
        }
    }
    
    private static long calculateObjectSize(Object obj, Class<?> clazz) throws Exception {
        Class<?> unsafeClass = internalUnsafe.getClass();
        Method objectFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
        
        long maxOffset = 0;
        int maxFieldSize = 0;
        
        // Walk through all fields to find the maximum offset
        Class<?> currentClass = clazz;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            for (Field field : fields) {
                if (!java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                    long offset = (Long) objectFieldOffsetMethod.invoke(internalUnsafe, field);
                    int fieldSize = getFieldSize(field.getType());
                    
                    if (offset > maxOffset) {
                        maxOffset = offset;
                        maxFieldSize = fieldSize;
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
        
        // Add header size (typically 8 or 12 bytes) + max field offset + field size
        long headerSize = getObjectHeaderSize();
        long totalSize = headerSize + maxOffset + maxFieldSize;
        
        // Handle arrays specially
        if (clazz.isArray()) {
            Class<?> componentType = clazz.getComponentType();
            int length = java.lang.reflect.Array.getLength(obj);
            int componentSize = getFieldSize(componentType);
            totalSize = headerSize + (length * componentSize);
        }
        
        // Align to 8 bytes (typical JVM alignment)
        return (totalSize + 7) & ~7;
    }
    
    private static long getObjectHeaderSize() {
        // Most JVMs use 8 or 12 bytes for object header
        // This is architecture and JVM dependent
        return System.getProperty("java.vm.name").contains("64") ? 16 : 12;
    }
    
    private static int getFieldSize(Class<?> type) {
        if (type == boolean.class || type == byte.class) return 1;
        if (type == char.class || type == short.class) return 2;
        if (type == int.class || type == float.class) return 4;
        if (type == long.class || type == double.class) return 8;
        return 4; // Reference size (32-bit) or 8 (64-bit with compressed OOPs disabled)
    }
    
    /**
     * Get all objects of a specific class
     * This provides basic heap walking functionality similar to JVMTI
     */
    public static List<Object> getObjectsOfClass(Class<?> targetClass) {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        List<Object> result = new ArrayList<>();
        
        try {
            // We can't directly walk the heap from Java without native code
            // But we can use WeakReference tracking and finalization hooks
            // This is a simplified implementation
            
            System.gc(); // Encourage garbage collection to clean up unreachable objects
            
            // Note: This is a limited implementation. True heap walking
            // requires native JVMTI agent or HotSpot-specific internal APIs
            
            return result;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get objects of class: " + e.getMessage(), e);
        }
    }
    
    /**
     * Force garbage collection
     * This provides JVMTI ForceGarbageCollection equivalent functionality
     */
    public static void forceGarbageCollection() {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        // Multiple collection calls to be thorough
        System.gc();
        System.runFinalization();
        System.gc();
    }
    
    /**
     * Get JVM memory information
     */
    public static MemoryInfo getMemoryInfo() {
        if (!initialized) {
            throw new IllegalStateException("VMObjectAccess not initialized");
        }
        
        Runtime runtime = Runtime.getRuntime();
        MemoryInfo info = new MemoryInfo();
        
        info.totalMemory = runtime.totalMemory();
        info.freeMemory = runtime.freeMemory();
        info.maxMemory = runtime.maxMemory();
        info.usedMemory = info.totalMemory - info.freeMemory;
        
        return info;
    }
    
    /**
     * Check if VMObjectAccess is available and initialized
     */
    public static boolean isAvailable() {
        return initialized;
    }
    
    /**
     * Get initialization status
     */
    public static String getStatus() {
        return "VMObjectAccess: " + (initialized ? "Initialized ✓" : "Failed ✗");
    }
    
    /**
     * Thread information structure
     */
    public static class ThreadInfo {
        public Thread thread;
        public String name;
        public long id;
        public Thread.State state;
        public int priority;
        public boolean daemon;
        public boolean alive;
        public Long nativeThreadId;
        public int stackDepth;
        public StackTraceElement currentFrame;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Thread[name=").append(name);
            sb.append(", id=").append(id);
            sb.append(", state=").append(state);
            sb.append(", priority=").append(priority);
            sb.append(", daemon=").append(daemon);
            sb.append(", alive=").append(alive);
            if (nativeThreadId != null) {
                sb.append(", nativeId=").append(nativeThreadId);
            }
            sb.append(", stackDepth=").append(stackDepth);
            sb.append("]");
            return sb.toString();
        }
    }
    
    /**
     * Memory information structure
     */
    public static class MemoryInfo {
        public long totalMemory;
        public long freeMemory;
        public long maxMemory;
        public long usedMemory;
        
        @Override
        public String toString() {
            return String.format("Memory[total=%d, free=%d, max=%d, used=%d]",
                totalMemory, freeMemory, maxMemory, usedMemory);
        }
    }
}