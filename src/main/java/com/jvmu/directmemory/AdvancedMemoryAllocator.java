package com.jvmu.directmemory;

import com.jvmu.directmemory.DirectMemoryManager.MemoryBlock;
import com.jvmu.directmemory.DirectMemoryManager.AlignmentMode;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Advanced Memory Allocator - Sophisticated allocation strategies for direct memory
 * 
 * This allocator provides multiple allocation strategies optimized for different use cases:
 * - Buddy System: Efficient for varied sizes with low fragmentation
 * - Slab Allocator: Optimized for fixed-size objects
 * - Arena Allocator: Fast sequential allocation with bulk deallocation
 * - Ring Buffer: Circular allocation for streaming data
 * - Object Pool: Recycling allocator for frequently used objects
 * 
 * Features:
 * - Multiple allocation algorithms
 * - Automatic fragmentation management
 * - Memory usage analytics
 * - Thread-safe operations
 * - Custom alignment support
 */
public class AdvancedMemoryAllocator {
    
    public enum AllocatorType {
        BUDDY_SYSTEM,     // Binary buddy allocator
        SLAB_ALLOCATOR,   // Fixed-size slab allocation
        ARENA_ALLOCATOR,  // Sequential arena allocation
        RING_BUFFER,      // Circular buffer allocation
        OBJECT_POOL,      // Object recycling pool
        HYBRID           // Combines multiple strategies
    }
    
    private static final Map<String, AdvancedMemoryAllocator> instances = new ConcurrentHashMap<>();
    
    private final String name;
    private final AllocatorType type;
    private final long totalSize;
    private final AlignmentMode alignment;
    private final ReentrantLock allocationLock;
    
    // Strategy-specific implementations
    private BuddyAllocator buddyAllocator;
    private SlabAllocator slabAllocator;
    private ArenaAllocator arenaAllocator;
    private RingBufferAllocator ringAllocator;
    private ObjectPoolAllocator poolAllocator;
    private HybridAllocator hybridAllocator;
    
    private AdvancedMemoryAllocator(String name, AllocatorType type, long totalSize, AlignmentMode alignment) {
        this.name = name;
        this.type = type;
        this.totalSize = totalSize;
        this.alignment = alignment;
        this.allocationLock = new ReentrantLock();
        
        initializeAllocator();
    }
    
    /**
     * Create or get named allocator instance
     */
    public static AdvancedMemoryAllocator getInstance(String name, AllocatorType type, 
                                                     long totalSize, AlignmentMode alignment) {
        return instances.computeIfAbsent(name, 
            k -> new AdvancedMemoryAllocator(name, type, totalSize, alignment));
    }
    
    /**
     * Create allocator with default alignment
     */
    public static AdvancedMemoryAllocator getInstance(String name, AllocatorType type, long totalSize) {
        return getInstance(name, type, totalSize, AlignmentMode.WORD_ALIGNED);
    }
    
    private void initializeAllocator() {
        switch (type) {
            case BUDDY_SYSTEM:
                buddyAllocator = new BuddyAllocator(totalSize, alignment);
                break;
            case SLAB_ALLOCATOR:
                slabAllocator = new SlabAllocator(totalSize, alignment);
                break;
            case ARENA_ALLOCATOR:
                arenaAllocator = new ArenaAllocator(totalSize, alignment);
                break;
            case RING_BUFFER:
                ringAllocator = new RingBufferAllocator(totalSize, alignment);
                break;
            case OBJECT_POOL:
                poolAllocator = new ObjectPoolAllocator(totalSize, alignment);
                break;
            case HYBRID:
                hybridAllocator = new HybridAllocator(totalSize, alignment);
                break;
        }
    }
    
    /**
     * Allocate memory using the configured strategy
     */
    public AllocatedMemory allocate(long size) {
        allocationLock.lock();
        try {
            switch (type) {
                case BUDDY_SYSTEM:
                    return buddyAllocator.allocate(size);
                case SLAB_ALLOCATOR:
                    return slabAllocator.allocate(size);
                case ARENA_ALLOCATOR:
                    return arenaAllocator.allocate(size);
                case RING_BUFFER:
                    return ringAllocator.allocate(size);
                case OBJECT_POOL:
                    return poolAllocator.allocate(size);
                case HYBRID:
                    return hybridAllocator.allocate(size);
                default:
                    throw new UnsupportedOperationException("Unknown allocator type: " + type);
            }
        } finally {
            allocationLock.unlock();
        }
    }
    
    /**
     * Free memory using the configured strategy
     */
    public void free(AllocatedMemory memory) {
        if (memory == null) return;
        
        allocationLock.lock();
        try {
            switch (type) {
                case BUDDY_SYSTEM:
                    buddyAllocator.free(memory);
                    break;
                case SLAB_ALLOCATOR:
                    slabAllocator.free(memory);
                    break;
                case ARENA_ALLOCATOR:
                    arenaAllocator.free(memory);
                    break;
                case RING_BUFFER:
                    ringAllocator.free(memory);
                    break;
                case OBJECT_POOL:
                    poolAllocator.free(memory);
                    break;
                case HYBRID:
                    hybridAllocator.free(memory);
                    break;
            }
        } finally {
            allocationLock.unlock();
        }
    }
    
    /**
     * Get allocator statistics
     */
    public AllocatorStats getStats() {
        switch (type) {
            case BUDDY_SYSTEM:
                return buddyAllocator.getStats();
            case SLAB_ALLOCATOR:
                return slabAllocator.getStats();
            case ARENA_ALLOCATOR:
                return arenaAllocator.getStats();
            case RING_BUFFER:
                return ringAllocator.getStats();
            case OBJECT_POOL:
                return poolAllocator.getStats();
            case HYBRID:
                return hybridAllocator.getStats();
            default:
                return new AllocatorStats();
        }
    }
    
    /**
     * Reset allocator and free all memory
     */
    public void reset() {
        allocationLock.lock();
        try {
            switch (type) {
                case BUDDY_SYSTEM:
                    buddyAllocator.reset();
                    break;
                case SLAB_ALLOCATOR:
                    slabAllocator.reset();
                    break;
                case ARENA_ALLOCATOR:
                    arenaAllocator.reset();
                    break;
                case RING_BUFFER:
                    ringAllocator.reset();
                    break;
                case OBJECT_POOL:
                    poolAllocator.reset();
                    break;
                case HYBRID:
                    hybridAllocator.reset();
                    break;
            }
        } finally {
            allocationLock.unlock();
        }
    }
    
    // ==================== BUDDY SYSTEM ALLOCATOR ====================
    
    private static class BuddyAllocator {
        private final MemoryBlock baseBlock;
        private final int maxOrder;
        private final Map<Integer, List<Long>> freeBlocks;
        private final Map<Long, Integer> allocatedBlocks;
        
        public BuddyAllocator(long totalSize, AlignmentMode alignment) {
            // Round to power of 2 for buddy system
            long alignedSize = Long.highestOneBit(totalSize - 1) << 1;
            this.baseBlock = DirectMemoryManager.allocateMemory(alignedSize, alignment);
            this.maxOrder = Long.numberOfTrailingZeros(alignedSize);
            this.freeBlocks = new HashMap<>();
            this.allocatedBlocks = new HashMap<>();
            
            // Initialize with single large free block
            freeBlocks.computeIfAbsent(maxOrder, k -> new ArrayList<>()).add(0L);
        }
        
        public AllocatedMemory allocate(long size) {
            int order = findOrder(size);
            if (order > maxOrder) {
                throw new OutOfMemoryError("Allocation size too large: " + size);
            }
            
            // Find available block
            Long offset = findFreeBlock(order);
            if (offset == null) {
                throw new OutOfMemoryError("No suitable block available");
            }
            
            allocatedBlocks.put(offset, order);
            return new AllocatedMemory(baseBlock, offset, 1L << order, size);
        }
        
        public void free(AllocatedMemory memory) {
            Long offset = memory.offset;
            Integer order = allocatedBlocks.remove(offset);
            if (order == null) {
                throw new IllegalArgumentException("Memory not allocated by this buddy allocator");
            }
            
            // Coalesce with buddy blocks
            coalesce(offset, order);
        }
        
        private Long findFreeBlock(int order) {
            // Try to find exact size
            List<Long> blocks = freeBlocks.get(order);
            if (blocks != null && !blocks.isEmpty()) {
                return blocks.remove(blocks.size() - 1);
            }
            
            // Split larger block
            for (int largerOrder = order + 1; largerOrder <= maxOrder; largerOrder++) {
                List<Long> largerBlocks = freeBlocks.get(largerOrder);
                if (largerBlocks != null && !largerBlocks.isEmpty()) {
                    Long largerOffset = largerBlocks.remove(largerBlocks.size() - 1);
                    
                    // Split the block
                    for (int splitOrder = largerOrder - 1; splitOrder >= order; splitOrder--) {
                        long blockSize = 1L << splitOrder;
                        freeBlocks.computeIfAbsent(splitOrder, k -> new ArrayList<>()).add(largerOffset + blockSize);
                    }
                    
                    return largerOffset;
                }
            }
            
            return null;
        }
        
        private void coalesce(long offset, int order) {
            long blockSize = 1L << order;
            long buddyOffset = offset ^ blockSize;
            
            List<Long> sameOrderBlocks = freeBlocks.get(order);
            if (sameOrderBlocks != null && sameOrderBlocks.contains(buddyOffset)) {
                // Buddy is free, coalesce
                sameOrderBlocks.remove(buddyOffset);
                if (sameOrderBlocks.isEmpty()) {
                    freeBlocks.remove(order);
                }
                
                long parentOffset = Math.min(offset, buddyOffset);
                coalesce(parentOffset, order + 1);
            } else {
                // Buddy not free, add to free list
                freeBlocks.computeIfAbsent(order, k -> new ArrayList<>()).add(offset);
            }
        }
        
        private int findOrder(long size) {
            return 64 - Long.numberOfLeadingZeros(size - 1);
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Buddy System";
            stats.totalSize = baseBlock.size;
            stats.allocatedBlocks = allocatedBlocks.size();
            stats.freeBlocks = freeBlocks.values().stream().mapToInt(List::size).sum();
            
            long allocatedSize = allocatedBlocks.values().stream().mapToLong(order -> 1L << order).sum();
            stats.allocatedSize = allocatedSize;
            stats.freeSize = stats.totalSize - allocatedSize;
            
            return stats;
        }
        
        public void reset() {
            for (Long offset : new ArrayList<>(allocatedBlocks.keySet())) {
                coalesce(offset, allocatedBlocks.remove(offset));
            }
        }
    }
    
    // ==================== SLAB ALLOCATOR ====================
    
    private static class SlabAllocator {
        private final Map<Long, Slab> slabs;
        private final AlignmentMode alignment;
        private final long totalSize;
        
        public SlabAllocator(long totalSize, AlignmentMode alignment) {
            this.totalSize = totalSize;
            this.alignment = alignment;
            this.slabs = new HashMap<>();
        }
        
        public AllocatedMemory allocate(long size) {
            Slab slab = slabs.computeIfAbsent(size, this::createSlab);
            return slab.allocate();
        }
        
        public void free(AllocatedMemory memory) {
            long size = memory.requestedSize;
            Slab slab = slabs.get(size);
            if (slab != null) {
                slab.free(memory);
            }
        }
        
        private Slab createSlab(long objectSize) {
            long slabSize = Math.min(totalSize / 10, 1024 * 1024); // 1MB max per slab
            return new Slab(objectSize, slabSize, alignment);
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Slab Allocator";
            stats.totalSize = totalSize;
            
            for (Slab slab : slabs.values()) {
                stats.allocatedSize += slab.getAllocatedSize();
                stats.freeSize += slab.getFreeSize();
                stats.allocatedBlocks += slab.getAllocatedCount();
                stats.freeBlocks += slab.getFreeCount();
            }
            
            return stats;
        }
        
        public void reset() {
            for (Slab slab : slabs.values()) {
                slab.reset();
            }
            slabs.clear();
        }
        
        private static class Slab {
            private final long objectSize;
            private final MemoryBlock baseBlock;
            private final BitSet allocated;
            private final int maxObjects;
            
            public Slab(long objectSize, long slabSize, AlignmentMode alignment) {
                this.objectSize = objectSize;
                this.baseBlock = DirectMemoryManager.allocateMemory(slabSize, alignment);
                this.maxObjects = (int) (slabSize / objectSize);
                this.allocated = new BitSet(maxObjects);
            }
            
            public AllocatedMemory allocate() {
                int index = allocated.nextClearBit(0);
                if (index >= maxObjects) {
                    throw new OutOfMemoryError("Slab full for object size " + objectSize);
                }
                
                allocated.set(index);
                long offset = index * objectSize;
                return new AllocatedMemory(baseBlock, offset, objectSize, objectSize);
            }
            
            public void free(AllocatedMemory memory) {
                int index = (int) (memory.offset / objectSize);
                allocated.clear(index);
            }
            
            public long getAllocatedSize() { return allocated.cardinality() * objectSize; }
            public long getFreeSize() { return (maxObjects - allocated.cardinality()) * objectSize; }
            public int getAllocatedCount() { return allocated.cardinality(); }
            public int getFreeCount() { return maxObjects - allocated.cardinality(); }
            
            public void reset() {
                allocated.clear();
            }
        }
    }
    
    // ==================== ARENA ALLOCATOR ====================
    
    private static class ArenaAllocator {
        private final List<MemoryBlock> arenas;
        private final long arenaSize;
        private final AlignmentMode alignment;
        private int currentArena = 0;
        private long currentOffset = 0;
        
        public ArenaAllocator(long totalSize, AlignmentMode alignment) {
            this.arenaSize = Math.min(totalSize, 64 * 1024 * 1024); // 64MB max per arena
            this.alignment = alignment;
            this.arenas = new ArrayList<>();
            
            // Allocate first arena
            arenas.add(DirectMemoryManager.allocateMemory(arenaSize, alignment));
        }
        
        public AllocatedMemory allocate(long size) {
            // Simple bump allocation
            if (currentOffset + size > arenaSize) {
                // Need new arena
                arenas.add(DirectMemoryManager.allocateMemory(arenaSize, alignment));
                currentArena++;
                currentOffset = 0;
            }
            
            MemoryBlock arena = arenas.get(currentArena);
            long offset = currentOffset;
            currentOffset += size;
            
            return new AllocatedMemory(arena, offset, size, size);
        }
        
        public void free(AllocatedMemory memory) {
            // Arena allocators typically don't support individual free
            // Memory is freed when arena is reset
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Arena Allocator";
            stats.totalSize = arenas.size() * arenaSize;
            stats.allocatedSize = (currentArena * arenaSize) + currentOffset;
            stats.freeSize = stats.totalSize - stats.allocatedSize;
            stats.allocatedBlocks = 0; // Can't track individual allocations
            stats.freeBlocks = 0;
            
            return stats;
        }
        
        public void reset() {
            // Keep first arena, free others
            while (arenas.size() > 1) {
                DirectMemoryManager.freeMemory(arenas.remove(arenas.size() - 1));
            }
            currentArena = 0;
            currentOffset = 0;
        }
    }
    
    // ==================== RING BUFFER ALLOCATOR ====================
    
    private static class RingBufferAllocator {
        private final MemoryBlock buffer;
        private final long bufferSize;
        private long readPointer = 0;
        private long writePointer = 0;
        private boolean bufferFull = false;
        
        public RingBufferAllocator(long totalSize, AlignmentMode alignment) {
            this.buffer = DirectMemoryManager.allocateMemory(totalSize, alignment);
            this.bufferSize = totalSize;
        }
        
        public AllocatedMemory allocate(long size) {
            if (size > bufferSize) {
                throw new IllegalArgumentException("Allocation size exceeds buffer size");
            }
            
            // Simple implementation - just advance write pointer
            if (bufferFull || (writePointer + size) > bufferSize) {
                throw new OutOfMemoryError("Ring buffer full");
            }
            
            long offset = writePointer;
            writePointer += size;
            
            if (writePointer >= bufferSize) {
                writePointer = 0;
                bufferFull = (readPointer == 0);
            }
            
            return new AllocatedMemory(buffer, offset, size, size);
        }
        
        public void free(AllocatedMemory memory) {
            // Ring buffers typically use mark-and-sweep or other mechanisms
            // This is a simplified implementation
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Ring Buffer";
            stats.totalSize = bufferSize;
            stats.allocatedSize = bufferFull ? bufferSize : 
                (writePointer >= readPointer ? writePointer - readPointer : 
                 bufferSize - readPointer + writePointer);
            stats.freeSize = bufferSize - stats.allocatedSize;
            
            return stats;
        }
        
        public void reset() {
            readPointer = 0;
            writePointer = 0;
            bufferFull = false;
        }
    }
    
    // ==================== OBJECT POOL ALLOCATOR ====================
    
    private static class ObjectPoolAllocator {
        private final Map<Long, Queue<AllocatedMemory>> pools;
        private final long totalSize;
        private final AlignmentMode alignment;
        private long usedSize = 0;
        
        public ObjectPoolAllocator(long totalSize, AlignmentMode alignment) {
            this.totalSize = totalSize;
            this.alignment = alignment;
            this.pools = new HashMap<>();
        }
        
        public AllocatedMemory allocate(long size) {
            Queue<AllocatedMemory> pool = pools.get(size);
            if (pool != null && !pool.isEmpty()) {
                return pool.poll();
            }
            
            // Create new allocation
            if (usedSize + size > totalSize) {
                throw new OutOfMemoryError("Pool allocator full");
            }
            
            MemoryBlock block = DirectMemoryManager.allocateMemory(size, alignment);
            usedSize += size;
            
            return new AllocatedMemory(block, 0, size, size);
        }
        
        public void free(AllocatedMemory memory) {
            // Return to appropriate pool
            long size = memory.requestedSize;
            pools.computeIfAbsent(size, k -> new LinkedList<>()).offer(memory);
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Object Pool";
            stats.totalSize = totalSize;
            stats.allocatedSize = usedSize;
            stats.freeSize = totalSize - usedSize;
            
            for (Queue<AllocatedMemory> pool : pools.values()) {
                stats.freeBlocks += pool.size();
            }
            
            return stats;
        }
        
        public void reset() {
            // Free all pooled memory
            for (Queue<AllocatedMemory> pool : pools.values()) {
                for (AllocatedMemory memory : pool) {
                    DirectMemoryManager.freeMemory(memory.block);
                }
                pool.clear();
            }
            pools.clear();
            usedSize = 0;
        }
    }
    
    // ==================== HYBRID ALLOCATOR ====================
    
    private static class HybridAllocator {
        private final SlabAllocator smallAllocator;
        private final BuddyAllocator mediumAllocator;
        private final ArenaAllocator largeAllocator;
        
        private static final long SMALL_THRESHOLD = 1024;      // 1KB
        private static final long MEDIUM_THRESHOLD = 64 * 1024; // 64KB
        
        public HybridAllocator(long totalSize, AlignmentMode alignment) {
            long smallSize = totalSize / 4;
            long mediumSize = totalSize / 2;
            long largeSize = totalSize / 4;
            
            this.smallAllocator = new SlabAllocator(smallSize, alignment);
            this.mediumAllocator = new BuddyAllocator(mediumSize, alignment);
            this.largeAllocator = new ArenaAllocator(largeSize, alignment);
        }
        
        public AllocatedMemory allocate(long size) {
            if (size <= SMALL_THRESHOLD) {
                return smallAllocator.allocate(size);
            } else if (size <= MEDIUM_THRESHOLD) {
                return mediumAllocator.allocate(size);
            } else {
                return largeAllocator.allocate(size);
            }
        }
        
        public void free(AllocatedMemory memory) {
            long size = memory.requestedSize;
            if (size <= SMALL_THRESHOLD) {
                smallAllocator.free(memory);
            } else if (size <= MEDIUM_THRESHOLD) {
                mediumAllocator.free(memory);
            } else {
                largeAllocator.free(memory);
            }
        }
        
        public AllocatorStats getStats() {
            AllocatorStats stats = new AllocatorStats();
            stats.allocatorType = "Hybrid Allocator";
            
            AllocatorStats smallStats = smallAllocator.getStats();
            AllocatorStats mediumStats = mediumAllocator.getStats();
            AllocatorStats largeStats = largeAllocator.getStats();
            
            stats.totalSize = smallStats.totalSize + mediumStats.totalSize + largeStats.totalSize;
            stats.allocatedSize = smallStats.allocatedSize + mediumStats.allocatedSize + largeStats.allocatedSize;
            stats.freeSize = smallStats.freeSize + mediumStats.freeSize + largeStats.freeSize;
            stats.allocatedBlocks = smallStats.allocatedBlocks + mediumStats.allocatedBlocks + largeStats.allocatedBlocks;
            stats.freeBlocks = smallStats.freeBlocks + mediumStats.freeBlocks + largeStats.freeBlocks;
            
            return stats;
        }
        
        public void reset() {
            smallAllocator.reset();
            mediumAllocator.reset();
            largeAllocator.reset();
        }
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Represents memory allocated by advanced allocators
     */
    public static class AllocatedMemory {
        public final MemoryBlock block;
        public final long offset;
        public final long allocatedSize;
        public final long requestedSize;
        public final long address;
        
        public AllocatedMemory(MemoryBlock block, long offset, long allocatedSize, long requestedSize) {
            this.block = block;
            this.offset = offset;
            this.allocatedSize = allocatedSize;
            this.requestedSize = requestedSize;
            this.address = block.address + offset;
        }
        
        @Override
        public String toString() {
            return String.format("AllocatedMemory[addr=0x%x, allocated=%d, requested=%d]", 
                address, allocatedSize, requestedSize);
        }
    }
    
    /**
     * Statistics for advanced allocators
     */
    public static class AllocatorStats {
        public String allocatorType = "Unknown";
        public long totalSize = 0;
        public long allocatedSize = 0;
        public long freeSize = 0;
        public int allocatedBlocks = 0;
        public int freeBlocks = 0;
        
        public double utilizationPercent() {
            return totalSize > 0 ? (double) allocatedSize / totalSize * 100.0 : 0.0;
        }
        
        public double fragmentationPercent() {
            if (totalSize == 0) return 0.0;
            long wastedSpace = totalSize - allocatedSize - freeSize;
            return (double) wastedSpace / totalSize * 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("%s[total=%d MB, allocated=%d MB (%.1f%%), free=%d MB, blocks=%d/%d]",
                allocatorType, totalSize / (1024 * 1024), allocatedSize / (1024 * 1024), 
                utilizationPercent(), freeSize / (1024 * 1024), allocatedBlocks, freeBlocks);
        }
    }
}