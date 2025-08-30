package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * JavaNioAccess - Wrapper for jdk.internal.misc.JavaNioAccess
 * 
 * Provides access to java.nio package internals for direct buffer management,
 * memory operations, and buffer pool monitoring without requiring special permissions.
 * 
 * Key capabilities:
 * - Direct buffer creation from memory addresses
 * - Buffer pool monitoring and statistics
 * - Buffer truncation and manipulation
 * - Low-level memory buffer operations
 */
public class JavaNioAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    
    /**
     * Create wrapper from native JavaNioAccess instance
     * @param nativeAccess native access instance
     */
    public JavaNioAccess(Object nativeAccess) {
        this.nativeAccess = nativeAccess;
        this.available = nativeAccess != null;
    }
    
    /**
     * Check if this access wrapper is functional
     * @return true if underlying access is available
     */
    public boolean isAvailable() {
        return available;
    }
    
    // ==================== BUFFER POOL ACCESS ====================
    
    /**
     * Get the direct buffer pool for monitoring
     * @return buffer pool wrapper
     */
    public BufferPool getDirectBufferPool() {
        if (!available) return null;
        Object pool = ReflectBuilder.of(nativeAccess)
            .method("getDirectBufferPool", null, null)
            .get();
        return pool != null ? new BufferPool(pool) : null;
    }
    
    // ==================== DIRECT BUFFER OPERATIONS ====================
    
    /**
     * Constructs a direct ByteBuffer referring to the block of memory starting
     * at the given memory address and extending cap bytes
     * @param addr memory address
     * @param cap capacity in bytes
     * @param ob arbitrary object attached to the buffer
     * @return direct ByteBuffer wrapping the memory
     */
    public ByteBuffer newDirectByteBuffer(long addr, int cap, Object ob) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("newDirectByteBuffer", new Class<?>[]{long.class, int.class, Object.class}, new Object[]{addr, cap, ob})
            .get();
    }
    
    /**
     * Truncates a buffer by changing its capacity to 0
     * @param buf buffer to truncate
     */
    public void truncate(Buffer buf) {
        if (!available || buf == null) return;
        ReflectBuilder.of(nativeAccess)
            .method("truncate", new Class<?>[]{Buffer.class}, new Object[]{buf})
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive NIO access information
     * @return NIO access information
     */
    public NioAccessInfo getNioAccessInfo() {
        NioAccessInfo info = new NioAccessInfo();
        info.available = available;
        
        if (!available) {
            return info;
        }
        
        try {
            BufferPool pool = getDirectBufferPool();
            if (pool != null) {
                info.bufferPoolAvailable = true;
                info.bufferPoolName = pool.getName();
                info.bufferCount = pool.getCount();
                info.totalCapacity = pool.getTotalCapacity();
                info.memoryUsed = pool.getMemoryUsed();
            }
        } catch (Exception e) {
            info.error = e.getMessage();
        }
        
        return info;
    }
    
    /**
     * Create a direct ByteBuffer from memory address (simplified)
     * @param addr memory address
     * @param cap capacity
     * @return direct ByteBuffer
     */
    public ByteBuffer newDirectByteBuffer(long addr, int cap) {
        return newDirectByteBuffer(addr, cap, null);
    }
    
    /**
     * Get the native access object
     * @return native access instance
     */
    public Object getNativeAccess() {
        return nativeAccess;
    }
    
    // ==================== BUFFER POOL WRAPPER ====================
    
    /**
     * Wrapper for BufferPool interface providing access to buffer pool information
     */
    public static class BufferPool {
        private final Object nativePool;
        private final boolean available;
        
        BufferPool(Object nativePool) {
            this.nativePool = nativePool;
            this.available = nativePool != null;
        }
        
        /**
         * Get the name of this buffer pool
         * @return pool name
         */
        public String getName() {
            if (!available) return "Unavailable";
            return ReflectBuilder.of(nativePool)
                .method("getName", null, null)
                .get();
        }
        
        /**
         * Get the number of buffers in this pool
         * @return buffer count
         */
        public long getCount() {
            if (!available) return -1;
            return ReflectBuilder.of(nativePool)
                .method("getCount", null, null)
                .get();
        }
        
        /**
         * Get the total capacity of all buffers in this pool
         * @return total capacity in bytes
         */
        public long getTotalCapacity() {
            if (!available) return -1;
            return ReflectBuilder.of(nativePool)
                .method("getTotalCapacity", null, null)
                .get();
        }
        
        /**
         * Get the amount of memory used by this pool
         * @return memory used in bytes
         */
        public long getMemoryUsed() {
            if (!available) return -1;
            return ReflectBuilder.of(nativePool)
                .method("getMemoryUsed", null, null)
                .get();
        }
        
        /**
         * Check if this buffer pool is available
         * @return true if pool operations are functional
         */
        public boolean isAvailable() {
            return available;
        }
        
        /**
         * Get buffer pool statistics
         * @return buffer pool information
         */
        public BufferPoolInfo getInfo() {
            BufferPoolInfo info = new BufferPoolInfo();
            info.available = available;
            
            if (available) {
                try {
                    info.name = getName();
                    info.count = getCount();
                    info.totalCapacity = getTotalCapacity();
                    info.memoryUsed = getMemoryUsed();
                } catch (Exception e) {
                    info.error = e.getMessage();
                }
            }
            
            return info;
        }
        
        @Override
        public String toString() {
            if (!available) return "BufferPool[Unavailable]";
            try {
                return String.format("BufferPool[name=%s, count=%d, capacity=%d bytes, used=%d bytes]",
                    getName(), getCount(), getTotalCapacity(), getMemoryUsed());
            } catch (Exception e) {
                return "BufferPool[Error: " + e.getMessage() + "]";
            }
        }
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Information about NIO access capabilities
     */
    public static class NioAccessInfo {
        public boolean available;
        public boolean bufferPoolAvailable;
        public String bufferPoolName;
        public long bufferCount = -1;
        public long totalCapacity = -1;
        public long memoryUsed = -1;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "NioAccessInfo: Not Available";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("NIO Access Information:\n");
            
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Buffer Pool: ").append(bufferPoolAvailable ? "✓" : "✗").append("\n");
            
            if (bufferPoolAvailable) {
                sb.append("    Name: ").append(bufferPoolName != null ? bufferPoolName : "Unknown").append("\n");
                sb.append("    Buffer Count: ").append(bufferCount >= 0 ? bufferCount : "Unknown").append("\n");
                sb.append("    Total Capacity: ");
                if (totalCapacity >= 0) {
                    sb.append(formatBytes(totalCapacity));
                } else {
                    sb.append("Unknown");
                }
                sb.append("\n");
                sb.append("    Memory Used: ");
                if (memoryUsed >= 0) {
                    sb.append(formatBytes(memoryUsed));
                } else {
                    sb.append("Unknown");
                }
            }
            
            return sb.toString();
        }
        
        private String formatBytes(long bytes) {
            if (bytes < 1024) return bytes + " B";
            if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
            if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
            return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
    
    /**
     * Information about a buffer pool
     */
    public static class BufferPoolInfo {
        public boolean available;
        public String name;
        public long count = -1;
        public long totalCapacity = -1;
        public long memoryUsed = -1;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "BufferPoolInfo: Not Available";
            }
            
            if (error != null) {
                return "BufferPoolInfo: Error - " + error;
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Buffer Pool Information:\n");
            sb.append("  Name: ").append(name != null ? name : "Unknown").append("\n");
            sb.append("  Count: ").append(count >= 0 ? count : "Unknown").append("\n");
            sb.append("  Total Capacity: ").append(totalCapacity >= 0 ? totalCapacity + " bytes" : "Unknown").append("\n");
            sb.append("  Memory Used: ").append(memoryUsed >= 0 ? memoryUsed + " bytes" : "Unknown");
            
            return sb.toString();
        }
    }
}