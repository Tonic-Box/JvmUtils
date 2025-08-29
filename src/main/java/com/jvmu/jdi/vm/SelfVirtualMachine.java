package com.jvmu.jdi.vm;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.jdi.events.SelfEventQueue;
import com.jvmu.jdi.events.SelfEventRequestManager;
import com.jvmu.jdi.threads.SelfThreadReference;

import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

/**
 * Self Virtual Machine - JDI-like interface for the current JVM
 * This class provides a VirtualMachine-like interface for debugging and
 * inspecting the current JVM process, similar to JDI VirtualMachine but
 * without requiring external process connection.
 * Key capabilities:
 * - Thread management and inspection
 * - Object reference wrapping and access
 * - Class and method inspection
 * - Field access and modification
 * - Process information and monitoring
 */
public class SelfVirtualMachine {

    private static boolean initialized = false;
    private static SelfVirtualMachine instance = null;

    // Process information
    private final long processId;
    private final String vmName;
    private final String vmVersion;
    
    // Thread management
    private final ThreadMXBean threadMXBean;
    private final Map<Long, SelfThreadReference> threadCache;
    
    // Object reference management
    private final Map<Object, SelfObjectReference> objectCache;
    private final WeakHashMap<Object, Integer> objectIds;
    private int nextObjectId = 1;
    
    // Event management
    private final SelfEventQueue eventQueue;
    private final SelfEventRequestManager eventRequestManager;
    
    static {
        try {
            initialize();
        } catch (Exception e) {
            System.err.println("SelfVirtualMachine initialization failed: " + e.getMessage());
        }
    }
    
    private SelfVirtualMachine() {
        this.processId = getCurrentProcessId();
        this.vmName = System.getProperty("java.vm.name", "Unknown");
        this.vmVersion = System.getProperty("java.vm.version", "Unknown");
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        this.threadCache = new ConcurrentHashMap<>();
        this.objectCache = new ConcurrentHashMap<>();
        this.objectIds = new WeakHashMap<>();
        this.eventQueue = new SelfEventQueue(this);
        this.eventRequestManager = new SelfEventRequestManager(this);
    }
    
    private static void initialize() {
        Object internalUnsafe = ModuleBootstrap.getInternalUnsafe();
        MethodHandles.Lookup trustedLookup = ModuleBootstrap.getTrustedLookup();
        
        if (internalUnsafe == null || trustedLookup == null) {
            System.err.println("SelfVirtualMachine requires privileged access via ModuleBootstrap");
            return;
        }
        
        initialized = true;
    }
    
    /**
     * Get the singleton instance of SelfVirtualMachine
     */
    public static synchronized SelfVirtualMachine getInstance() {
        if (!initialized) {
            throw new IllegalStateException("SelfVirtualMachine not initialized");
        }
        
        if (instance == null) {
            instance = new SelfVirtualMachine();
        }
        
        return instance;
    }

    /**
     * Get the JVM name
     */
    public String name() {
        return vmName + " " + vmVersion;
    }
    
    /**
     * Get the JVM version
     */
    public String version() {
        return System.getProperty("java.version");
    }
    
    /**
     * Get the process ID of this JVM
     */
    public long getProcessId() {
        return processId;
    }
    
    /**
     * Get all threads in this JVM
     */
    public List<SelfThreadReference> getAllThreads() {
        List<SelfThreadReference> threads = new ArrayList<>();
        
        // Get all thread IDs
        long[] threadIds = threadMXBean.getAllThreadIds();
        
        for (long threadId : threadIds) {
            SelfThreadReference threadRef = getOrCreateThreadReference(threadId);
            if (threadRef != null) {
                threads.add(threadRef);
            }
        }
        
        return threads;
    }
    
    /**
     * Get thread reference by thread ID
     */
    public SelfThreadReference getThreadReference(long threadId) {
        return getOrCreateThreadReference(threadId);
    }
    
    /**
     * Get thread reference for current thread
     */
    public SelfThreadReference getCurrentThread() {
        Thread current = Thread.currentThread();
        return getOrCreateThreadReference(current.getId());
    }
    
    private SelfThreadReference getOrCreateThreadReference(long threadId) {
        return threadCache.computeIfAbsent(threadId, id -> {
            // Find the actual Thread object
            Thread[] allThreads = getAllJavaThreads();
            for (Thread thread : allThreads) {
                if (thread.getId() == id) {
                    return new SelfThreadReference(this, thread);
                }
            }
            return null; // Thread may have terminated
        });
    }
    
    /**
     * Get all Java Thread objects
     */
    private Thread[] getAllJavaThreads() {
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = rootGroup.getParent()) != null) {
            rootGroup = parent;
        }
        
        int threadCount = rootGroup.activeCount();
        Thread[] threads = new Thread[threadCount * 2]; // Buffer for growth
        int actualCount = rootGroup.enumerate(threads, true);
        
        return Arrays.copyOf(threads, actualCount);
    }
    
    /**
     * Create an object reference for any object
     */
    public SelfObjectReference createObjectReference(Object obj) {
        if (obj == null) {
            return null;
        }
        
        return objectCache.computeIfAbsent(obj, o -> {
            int objId = getOrCreateObjectId(o);
            return new SelfObjectReference(this, o, objId);
        });
    }
    
    /**
     * Get or create object ID (public method)
     */
    public int getOrCreateObjectId(Object obj) {
        return objectIds.computeIfAbsent(obj, o -> nextObjectId++);
    }
    
    /**
     * Get all classes loaded in this JVM
     */
    public List<Class<?>> getAllLoadedClasses() {
        Set<Class<?>> classes = new HashSet<>();
        
        // Get classes from thread group
        ThreadGroup rootGroup = Thread.currentThread().getThreadGroup();
        ThreadGroup parent;
        while ((parent = rootGroup.getParent()) != null) {
            rootGroup = parent;
        }
        
        Thread[] allThreads = getAllJavaThreads();
        for (Thread thread : allThreads) {
            classes.add(thread.getClass());
            
            // Get stack trace classes
            StackTraceElement[] stack = thread.getStackTrace();
            for (StackTraceElement element : stack) {
                try {
                    Class<?> clazz = Class.forName(element.getClassName());
                    classes.add(clazz);
                } catch (Exception e) {
                    // Class may not be accessible
                }
            }
        }
        
        // Add common system classes
        Class<?>[] systemClasses = {
            Object.class, String.class, Class.class, Thread.class,
            System.class, Runtime.class, ClassLoader.class
        };
        Collections.addAll(classes, systemClasses);
        
        return new ArrayList<>(classes);
    }
    
    /**
     * Get class by name
     */
    public Class<?> getClassByName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }
    
    /**
     * Force garbage collection
     */
    public void gc() {
        System.gc();
        System.runFinalization();
    }
    
    /**
     * Get memory information
     */
    public MemoryInfo getMemoryInfo() {
        Runtime runtime = Runtime.getRuntime();
        
        MemoryInfo info = new MemoryInfo();
        info.totalMemory = runtime.totalMemory();
        info.freeMemory = runtime.freeMemory();
        info.maxMemory = runtime.maxMemory();
        info.usedMemory = info.totalMemory - info.freeMemory;
        
        return info;
    }
    
    /**
     * Get VM capabilities
     */
    public VMCapabilities getCapabilities() {
        VMCapabilities caps = new VMCapabilities();
        caps.canGetThreadInfo = true;
        caps.canGetObjectInfo = true;
        caps.canGetClassInfo = true;
        caps.canGetFieldInfo = true;
        caps.canGetMethodInfo = true;
        caps.canForceGC = true;
        caps.canGetMemoryInfo = true;
        caps.canSuspendThreads = false; // Can't safely suspend in same VM
        caps.canSetBreakpoints = false; // Would require bytecode modification
        caps.canModifyFields = true;
        caps.canInvokeMethod = true;
        
        return caps;
    }
    
    /**
     * Get the event queue
     */
    public SelfEventQueue eventQueue() {
        return eventQueue;
    }
    
    /**
     * Get the event request manager
     */
    public SelfEventRequestManager eventRequestManager() {
        return eventRequestManager;
    }
    
    /**
     * Check if self VM is available
     */
    public static boolean isAvailable() {
        return initialized;
    }
    
    /**
     * Get current process ID
     */
    private static long getCurrentProcessId() {
        try {
            return ProcessHandle.current().pid();
        } catch (Exception e) {
            try {
                String processName = ManagementFactory.getRuntimeMXBean().getName();
                return Long.parseLong(processName.split("@")[0]);
            } catch (Exception ex) {
                return -1;
            }
        }
    }
    
    @Override
    public String toString() {
        return String.format("SelfVirtualMachine[pid=%d, name=%s, version=%s]",
            processId, vmName, version());
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
            return String.format("Memory[total=%d MB, free=%d MB, used=%d MB, max=%d MB]",
                totalMemory / (1024 * 1024), freeMemory / (1024 * 1024),
                usedMemory / (1024 * 1024), maxMemory / (1024 * 1024));
        }
    }
    
    /**
     * VM capabilities structure
     */
    public static class VMCapabilities {
        public boolean canGetThreadInfo;
        public boolean canGetObjectInfo;
        public boolean canGetClassInfo;
        public boolean canGetFieldInfo;
        public boolean canGetMethodInfo;
        public boolean canForceGC;
        public boolean canGetMemoryInfo;
        public boolean canSuspendThreads;
        public boolean canSetBreakpoints;
        public boolean canModifyFields;
        public boolean canInvokeMethod;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("VMCapabilities[");
            if (canGetThreadInfo) sb.append("threads, ");
            if (canGetObjectInfo) sb.append("objects, ");
            if (canGetClassInfo) sb.append("classes, ");
            if (canGetFieldInfo) sb.append("fields, ");
            if (canGetMethodInfo) sb.append("methods, ");
            if (canForceGC) sb.append("gc, ");
            if (canGetMemoryInfo) sb.append("memory, ");
            if (canSuspendThreads) sb.append("suspend, ");
            if (canSetBreakpoints) sb.append("breakpoints, ");
            if (canModifyFields) sb.append("modify, ");
            if (canInvokeMethod) sb.append("invoke, ");
            if (sb.length() > 15) sb.setLength(sb.length() - 2); // Remove trailing ", "
            sb.append("]");
            return sb.toString();
        }
    }
}