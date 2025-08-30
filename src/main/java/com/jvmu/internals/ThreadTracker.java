package com.jvmu.internals;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.util.ReflectBuilder;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ThreadTracker - Wrapper for jdk.internal.misc.ThreadTracker using privileged access
 * 
 * Provides access to JDK internal thread tracking functionality without requiring special
 * permissions or module access restrictions. ThreadTracker helps detect reentrancy
 * without using ThreadLocal variables, which is useful for performance-critical code.
 * 
 * Key capabilities:
 * - Reentrancy detection without ThreadLocal overhead
 * - Thread tracking with begin/end semantics
 * - Concurrent thread set management
 * - Memory-efficient thread references
 * - Safe thread identity comparison
 * 
 * This wrapper bypasses the normal module system restrictions and provides
 * direct access to internal thread tracking utilities.
 */
public class ThreadTracker {
    
    private static final Class<?> threadTrackerClass;
    private static final boolean available;
    
    static {
        Class<?> clazz = null;
        boolean isAvailable = false;
        
        try {
            // Load ThreadTracker class using privileged access
            clazz = Class.forName("jdk.internal.misc.ThreadTracker");
            if (clazz != null) {
                isAvailable = true;
            }
        } catch (Exception e) {
            // ThreadTracker not available - silent fallback
        }
        
        threadTrackerClass = clazz;
        available = isAvailable;
    }
    
    /**
     * Check if ThreadTracker functionality is available
     * @return true if ThreadTracker APIs can be used
     */
    public static boolean isAvailable() {
        return available && ModuleBootstrap.getInternalUnsafe() != null;
    }
    
    /**
     * Get status information about ThreadTracker availability
     * @return status string
     */
    public static String getStatus() {
        if (available) {
            return "ThreadTracker: Available ✓ (Bypassed jdk.internal.misc access restrictions)";
        } else {
            return "ThreadTracker: Not Available ✗ (jdk.internal.misc.ThreadTracker class not found)";
        }
    }
    
    // ==================== THREAD TRACKER WRAPPER ====================
    
    /**
     * ThreadTracker instance wrapper that provides the same functionality
     * as jdk.internal.misc.ThreadTracker but with privileged access
     */
    public static class Tracker {
        private final Object nativeTracker;
        
        /**
         * Create a new ThreadTracker instance
         */
        public Tracker() {
            if (!isAvailable()) {
                throw new UnsupportedOperationException("ThreadTracker not available");
            }
            
            try {
                this.nativeTracker = ReflectBuilder.newInstance(
                    threadTrackerClass.getName(),
                    null,
                    null
                ).get();
            } catch (Exception e) {
                throw new RuntimeException("Failed to create ThreadTracker instance", e);
            }
        }
        
        /**
         * Adds the current thread to thread set if not already in the set.
         * Returns a key to remove the thread or null if already in the set.
         * @return key to remove thread, or null if already tracked
         */
        public Object tryBegin() {
            return ReflectBuilder.of(nativeTracker)
                .method("tryBegin", null, null)
                .get();
        }
        
        /**
         * Adds the current thread to thread set if not already in the set.
         * Returns a key to remove the thread.
         * @return key to remove thread
         */
        public Object begin() {
            return ReflectBuilder.of(nativeTracker)
                .method("begin", null, null)
                .get();
        }
        
        /**
         * Removes the thread identified by the key from the thread set
         * @param key key returned from begin() or tryBegin()
         */
        public void end(Object key) {
            ReflectBuilder.of(nativeTracker)
                .method("end", new Class<?>[]{Object.class}, new Object[]{key})
                .get();
        }
        
        /**
         * Returns true if the given thread is tracked
         * @param thread thread to check
         * @return true if thread is being tracked
         */
        public boolean contains(Thread thread) {
            return ReflectBuilder.of(nativeTracker)
                .method("contains", new Class<?>[]{Thread.class}, new Object[]{thread})
                .get();
        }
        
        /**
         * Get information about this tracker
         * @return tracker information
         */
        public TrackerInfo getTrackerInfo() {
            TrackerInfo info = new TrackerInfo();
            info.available = true;
            info.currentThreadTracked = contains(Thread.currentThread());
            
            // Try to get internal thread set size via reflection
            try {
                Object threadsSet = ReflectBuilder.of(nativeTracker)
                    .field("threads")
                    .get();
                
                if (threadsSet instanceof Set) {
                    info.trackedThreadCount = ((Set<?>) threadsSet).size();
                }
            } catch (Exception e) {
                // Unable to get internal information
            }
            
            return info;
        }
    }
    
    // ==================== STATIC UTILITIES ====================
    
    /**
     * Create a new ThreadTracker instance
     * @return new tracker instance
     */
    public static Tracker create() {
        return new Tracker();
    }
    
    /**
     * Helper method to track a code block execution
     * @param tracker tracker instance
     * @param runnable code to execute
     * @throws IllegalStateException if thread is already being tracked
     */
    public static void track(Tracker tracker, Runnable runnable) {
        Object key = tracker.begin();
        try {
            runnable.run();
        } finally {
            tracker.end(key);
        }
    }
    
    /**
     * Helper method to conditionally track a code block execution
     * @param tracker tracker instance
     * @param runnable code to execute
     * @return true if tracking was applied, false if thread was already tracked
     */
    public static boolean tryTrack(Tracker tracker, Runnable runnable) {
        Object key = tracker.tryBegin();
        if (key == null) {
            // Thread already tracked
            runnable.run();
            return false;
        }
        
        try {
            runnable.run();
            return true;
        } finally {
            tracker.end(key);
        }
    }
    
    /**
     * Create a ThreadTracker that can detect reentrancy for a specific operation
     * @return reentrancy detector
     */
    public static ReentrancyDetector createReentrancyDetector() {
        return new ReentrancyDetector();
    }
    
    // ==================== REENTRANCY DETECTOR ====================
    
    /**
     * Specialized utility for detecting reentrancy in operations
     */
    public static class ReentrancyDetector {
        private final Tracker tracker;
        
        private ReentrancyDetector() {
            this.tracker = new Tracker();
        }
        
        /**
         * Execute code with reentrancy detection
         * @param operation code to execute
         * @throws IllegalStateException if reentrancy is detected
         */
        public void execute(Runnable operation) {
            Object key = tracker.tryBegin();
            if (key == null) {
                throw new IllegalStateException("Reentrancy detected in thread: " + Thread.currentThread().getName());
            }
            
            try {
                operation.run();
            } finally {
                tracker.end(key);
            }
        }
        
        /**
         * Execute code with conditional reentrancy handling
         * @param operation code to execute
         * @param onReentrancy action to take if reentrancy is detected
         */
        public void executeWithFallback(Runnable operation, Runnable onReentrancy) {
            Object key = tracker.tryBegin();
            if (key == null) {
                // Reentrancy detected
                onReentrancy.run();
                return;
            }
            
            try {
                operation.run();
            } finally {
                tracker.end(key);
            }
        }
        
        /**
         * Check if current thread would cause reentrancy
         * @return true if current thread is already being tracked
         */
        public boolean wouldCauseReentrancy() {
            return tracker.contains(Thread.currentThread());
        }
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Information about a ThreadTracker instance
     */
    public static class TrackerInfo {
        public boolean available;
        public boolean currentThreadTracked;
        public int trackedThreadCount = -1;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("ThreadTracker Information:\n");
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Current Thread Tracked: ").append(currentThreadTracked).append("\n");
            if (trackedThreadCount >= 0) {
                sb.append("  Total Tracked Threads: ").append(trackedThreadCount);
            } else {
                sb.append("  Total Tracked Threads: Unknown");
            }
            return sb.toString();
        }
    }
    
    /**
     * Exception thrown when reentrancy is detected
     */
    public static class ReentrancyException extends RuntimeException {
        public ReentrancyException(String message) {
            super(message);
        }
        
        public ReentrancyException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}