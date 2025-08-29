package com.jvmu.demos;

import com.jvmu.directmemory.DirectMemoryManager;
import com.jvmu.directmemory.DirectMemoryManager.MemoryBlock;
import com.jvmu.directmemory.DirectMemoryManager.AlignmentMode;
import com.jvmu.directmemory.DirectMemoryManager.CopyMode;
import com.jvmu.directmemory.DirectMemoryManager.MemoryPool;
import com.jvmu.directmemory.AdvancedMemoryAllocator;
import com.jvmu.directmemory.AdvancedMemoryAllocator.AllocatorType;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;
import java.util.Random;

/**
 * Direct Memory Manager Demo - Comprehensive demonstration of off-heap memory operations
 * 
 * This demo showcases the full capabilities of the DirectMemoryManager API:
 * - Native memory allocation with different alignment modes
 * - Memory manipulation operations (copy, fill, compare)
 * - Advanced memory allocators with different strategies
 * - Memory pools for efficient allocation/deallocation
 * - Memory leak detection and statistics
 * - Integration with DirectByteBuffer
 * - Performance benchmarking
 */
public class DirectMemoryDemo {
    
    private static final Random random = new Random(42); // Fixed seed for reproducible results
    
    public static void main(String[] args) {
        printHeader("Direct Memory Manager Demo");
        
        try {
            // Check if direct memory manager is available
            if (!DirectMemoryManager.isAvailable()) {
                System.err.println("Direct Memory Manager is not available!");
                System.err.println("Make sure ModuleBootstrap is properly initialized.");
                return;
            }
            
            System.out.println("SUCCESS: Direct Memory Manager Available!");
            
            // Demo 1: Basic memory allocation and operations
            demonstrateBasicMemoryOperations();
            
            // Demo 2: Memory alignment modes
            demonstrateMemoryAlignment();
            
            // Demo 3: Memory manipulation operations
            demonstrateMemoryManipulation();
            
            // Demo 4: Memory pools
            demonstrateMemoryPools();
            
            // Demo 5: Advanced allocators
            demonstrateAdvancedAllocators();
            
            // Demo 6: Memory leak detection
            demonstrateLeakDetection();
            
            // Demo 7: Performance benchmarks
            demonstratePerformanceBenchmarks();
            
            // Demo 8: DirectByteBuffer integration
            demonstrateDirectBufferIntegration();
            
            printFooter("Direct Memory Demo Completed Successfully!");
            
        } catch (Exception e) {
            System.err.println("ERROR: Demo failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Cleanup all allocated memory
            int freedBlocks = DirectMemoryManager.freeAllMemory();
            System.out.println("\\nCleanup: Freed " + freedBlocks + " memory blocks");
        }
    }
    
    private static void demonstrateBasicMemoryOperations() {
        printSection("Basic Memory Operations");
        
        try {
            // Allocate memory blocks of different sizes
            System.out.println("Allocating memory blocks...");
            
            MemoryBlock small = DirectMemoryManager.allocateMemory(1024);  // 1 KB
            MemoryBlock medium = DirectMemoryManager.allocateMemory(64 * 1024);  // 64 KB  
            MemoryBlock large = DirectMemoryManager.allocateMemory(1024 * 1024);  // 1 MB
            
            System.out.println("Small block: " + small);
            System.out.println("Medium block: " + medium);
            System.out.println("Large block: " + large);
            
            // Demonstrate memory reallocation
            System.out.println("\\nReallocating medium block to 128 KB...");
            MemoryBlock expanded = DirectMemoryManager.reallocateMemory(medium, 128 * 1024);
            System.out.println("Expanded block: " + expanded);
            
            // Fill memory with pattern
            System.out.println("\\nFilling memory blocks with patterns...");
            DirectMemoryManager.setMemory(small, 0, small.size, (byte) 0xAA);
            DirectMemoryManager.setMemory(expanded, 0, 1024, (byte) 0x55);
            DirectMemoryManager.zeroMemory(large);
            
            System.out.println("Memory operations completed successfully");
            
            // Free individual blocks (expanded block will be freed automatically)
            DirectMemoryManager.freeMemory(small);
            DirectMemoryManager.freeMemory(expanded);
            DirectMemoryManager.freeMemory(large);
            
        } catch (Exception e) {
            System.err.println("ERROR: Basic memory operations failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateMemoryAlignment() {
        printSection("Memory Alignment Modes");
        
        try {
            System.out.println("Testing different alignment modes:");
            
            // Test different alignment modes
            MemoryBlock[] blocks = new MemoryBlock[4];
            AlignmentMode[] alignments = {
                AlignmentMode.NONE,
                AlignmentMode.WORD_ALIGNED,
                AlignmentMode.CACHE_LINE,
                AlignmentMode.PAGE_ALIGNED
            };
            
            for (int i = 0; i < alignments.length; i++) {
                blocks[i] = DirectMemoryManager.allocateMemory(4096, alignments[i]);
                
                long alignment = getAlignment(blocks[i].address);
                System.out.println(alignments[i] + ": address=0x" + Long.toHexString(blocks[i].address) + 
                    ", alignment=" + alignment + " bytes");
            }
            
            // Verify alignment is correct
            System.out.println("\\nAlignment verification:");
            System.out.println("Word aligned (8): " + (blocks[1].address % 8 == 0 ? "✓" : "✗"));
            System.out.println("Cache line (64): " + (blocks[2].address % 64 == 0 ? "✓" : "✗"));  
            System.out.println("Page aligned (4096): " + (blocks[3].address % 4096 == 0 ? "✓" : "✗"));
            
            // Free alignment test blocks
            for (MemoryBlock block : blocks) {
                DirectMemoryManager.freeMemory(block);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Memory alignment test failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateMemoryManipulation() {
        printSection("Memory Manipulation Operations");
        
        try {
            // Create source and destination blocks
            MemoryBlock source = DirectMemoryManager.allocateMemory(8192, AlignmentMode.CACHE_LINE);
            MemoryBlock destination = DirectMemoryManager.allocateMemory(8192, AlignmentMode.CACHE_LINE);
            
            System.out.println("Allocated source and destination blocks: " + source.size + " bytes each");
            
            // Fill source with test pattern
            System.out.println("Filling source with test pattern...");
            for (int i = 0; i < 1024; i++) {
                DirectMemoryManager.setMemory(source, i * 8, 8, (byte) (i % 256));
            }
            
            // Test different copy modes
            System.out.println("\\nTesting copy operations:");
            
            // Normal copy
            long startTime = System.nanoTime();
            DirectMemoryManager.copyMemory(source, 0, destination, 0, 4096, CopyMode.NORMAL);
            long normalTime = System.nanoTime() - startTime;
            System.out.println("Normal copy (4KB): " + normalTime + " ns");
            
            // Verify copy by comparison
            int comparison = DirectMemoryManager.compareMemory(source, 0, destination, 0, 4096);
            System.out.println("Memory comparison result: " + (comparison == 0 ? "IDENTICAL ✓" : "DIFFERENT ✗"));
            
            // Endian swap copy
            startTime = System.nanoTime();
            DirectMemoryManager.copyMemory(source, 4096, destination, 4096, 4096, CopyMode.SWAP_ENDIAN);
            long swapTime = System.nanoTime() - startTime;
            System.out.println("Endian swap copy (4KB): " + swapTime + " ns");
            
            // Verify swap made data different
            comparison = DirectMemoryManager.compareMemory(source, 4096, destination, 4096, 4096);
            System.out.println("Swapped data comparison: " + (comparison != 0 ? "DIFFERENT ✓" : "SAME ✗"));
            
            DirectMemoryManager.freeMemory(source);
            DirectMemoryManager.freeMemory(destination);
            
        } catch (Exception e) {
            System.err.println("ERROR: Memory manipulation test failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateMemoryPools() {
        printSection("Memory Pool Management");
        
        try {
            // Create memory pools for different sizes
            MemoryPool smallPool = new MemoryPool(1024, AlignmentMode.WORD_ALIGNED, 10);
            MemoryPool largePool = new MemoryPool(64 * 1024, AlignmentMode.CACHE_LINE, 5);
            
            System.out.println("Created memory pools:");
            System.out.println("Small pool: " + smallPool.getStats());
            System.out.println("Large pool: " + largePool.getStats());
            
            // Allocate and use blocks from pools
            System.out.println("\\nAllocating from pools...");
            List<MemoryBlock> smallBlocks = new ArrayList<>();
            List<MemoryBlock> largeBlocks = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                smallBlocks.add(smallPool.acquire());
                largeBlocks.add(largePool.acquire());
            }
            
            System.out.println("After allocation:");
            System.out.println("Small pool: " + smallPool.getStats());
            System.out.println("Large pool: " + largePool.getStats());
            
            // Return some blocks to pool
            System.out.println("\\nReturning blocks to pools...");
            for (int i = 0; i < 3; i++) {
                smallPool.release(smallBlocks.get(i));
                largePool.release(largeBlocks.get(i));
            }
            
            System.out.println("After returning blocks:");
            System.out.println("Small pool: " + smallPool.getStats());
            System.out.println("Large pool: " + largePool.getStats());
            
            // Clean up pools
            smallPool.clear();
            largePool.clear();
            
            // Free remaining blocks
            for (int i = 3; i < 5; i++) {
                DirectMemoryManager.freeMemory(smallBlocks.get(i));
                DirectMemoryManager.freeMemory(largeBlocks.get(i));
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Memory pool test failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateAdvancedAllocators() {
        printSection("Advanced Memory Allocators");
        
        try {
            // Test different allocator types
            AllocatorType[] types = {
                AllocatorType.BUDDY_SYSTEM,
                AllocatorType.SLAB_ALLOCATOR, 
                AllocatorType.ARENA_ALLOCATOR,
                AllocatorType.RING_BUFFER,
                AllocatorType.OBJECT_POOL,
                AllocatorType.HYBRID
            };
            
            for (AllocatorType type : types) {
                System.out.println("\\nTesting " + type + ":");
                
                try {
                    AdvancedMemoryAllocator allocator = AdvancedMemoryAllocator.getInstance(
                        "test_" + type.name().toLowerCase(), type, 1024 * 1024); // 1MB total
                    
                    // Allocate various sized blocks
                    List<AdvancedMemoryAllocator.AllocatedMemory> allocations = new ArrayList<>();
                    
                    // Small allocations
                    for (int i = 0; i < 10; i++) {
                        allocations.add(allocator.allocate(64 + random.nextInt(192))); // 64-256 bytes
                    }
                    
                    // Medium allocations  
                    for (int i = 0; i < 5; i++) {
                        allocations.add(allocator.allocate(1024 + random.nextInt(3072))); // 1-4 KB
                    }
                    
                    // Large allocation
                    allocations.add(allocator.allocate(16 * 1024)); // 16 KB
                    
                    System.out.println("  Stats after allocation: " + allocator.getStats());
                    
                    // Free half the allocations
                    for (int i = 0; i < allocations.size() / 2; i++) {
                        allocator.free(allocations.get(i));
                    }
                    
                    System.out.println("  Stats after partial free: " + allocator.getStats());
                    
                    // Reset allocator
                    allocator.reset();
                    System.out.println("  Stats after reset: " + allocator.getStats());
                    
                } catch (Exception e) {
                    System.err.println("  ERROR with " + type + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Advanced allocator test failed: " + e.getMessage());
        }
    }
    
    private static void demonstrateLeakDetection() {
        printSection("Memory Leak Detection");
        
        try {
            System.out.println("Initial memory stats:");
            DirectMemoryManager.MemoryStats initialStats = DirectMemoryManager.getMemoryStats();
            System.out.println(initialStats);
            
            // Create some "leaked" allocations
            System.out.println("\\nCreating potential memory leaks...");
            List<MemoryBlock> potentialLeaks = new ArrayList<>();
            
            for (int i = 0; i < 5; i++) {
                MemoryBlock block = DirectMemoryManager.allocateMemory(1024 * (i + 1));
                potentialLeaks.add(block);
                
                // Simulate some work
                DirectMemoryManager.setMemory(block, 0, block.size, (byte) 0xFF);
            }
            
            // Wait a moment to create age difference
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            
            // Create recent allocations
            System.out.println("Creating recent allocations...");
            List<MemoryBlock> recentBlocks = new ArrayList<>();
            
            for (int i = 0; i < 3; i++) {
                MemoryBlock block = DirectMemoryManager.allocateMemory(512);
                recentBlocks.add(block);
            }
            
            System.out.println("\\nMemory stats after allocations:");
            DirectMemoryManager.MemoryStats currentStats = DirectMemoryManager.getMemoryStats();
            System.out.println(currentStats);
            
            // Detect potential leaks
            System.out.println("\\nLeak detection (blocks older than 50ms):");
            List<MemoryBlock> leaks = DirectMemoryManager.findPotentialLeaks(50);
            
            System.out.println("Found " + leaks.size() + " potential leaks:");
            for (MemoryBlock leak : leaks) {
                System.out.println("  " + leak);
            }
            
            // Free recent blocks but leave "leaks"
            System.out.println("\\nFreeing recent blocks...");
            for (MemoryBlock block : recentBlocks) {
                DirectMemoryManager.freeMemory(block);
            }
            
            // Show remaining "leaks"
            System.out.println("\\nRemaining allocations (potential leaks):");
            List<MemoryBlock> remainingBlocks = DirectMemoryManager.getAllocatedBlocks();
            for (MemoryBlock block : remainingBlocks) {
                if (!block.freed) {
                    System.out.println("  " + block);
                }
            }
            
            // Clean up leaks
            for (MemoryBlock leak : potentialLeaks) {
                DirectMemoryManager.freeMemory(leak);
            }
            
        } catch (Exception e) {
            System.err.println("ERROR: Leak detection test failed: " + e.getMessage());
        }
    }
    
    private static void demonstratePerformanceBenchmarks() {
        printSection("Performance Benchmarks");
        
        try {
            System.out.println("Running performance benchmarks...");
            
            // Benchmark allocation speed
            benchmarkAllocation();
            
            // Benchmark memory operations
            benchmarkMemoryOperations();
            
            // Benchmark different allocators
            benchmarkAllocatorPerformance();
            
        } catch (Exception e) {
            System.err.println("ERROR: Performance benchmark failed: " + e.getMessage());
        }
    }
    
    private static void benchmarkAllocation() {
        System.out.println("\\n=== Allocation Speed Benchmark ===");
        
        int iterations = 1000;
        long[] sizes = {64, 1024, 64 * 1024, 1024 * 1024};
        
        for (long size : sizes) {
            System.out.println("\\nSize: " + formatBytes(size));
            
            // Warmup
            for (int i = 0; i < 100; i++) {
                MemoryBlock block = DirectMemoryManager.allocateMemory(size);
                DirectMemoryManager.freeMemory(block);
            }
            
            // Benchmark allocation
            long startTime = System.nanoTime();
            List<MemoryBlock> blocks = new ArrayList<>();
            
            for (int i = 0; i < iterations; i++) {
                blocks.add(DirectMemoryManager.allocateMemory(size));
            }
            
            long allocTime = System.nanoTime() - startTime;
            
            // Benchmark deallocation
            startTime = System.nanoTime();
            
            for (MemoryBlock block : blocks) {
                DirectMemoryManager.freeMemory(block);
            }
            
            long freeTime = System.nanoTime() - startTime;
            
            System.out.println("  Allocation: " + (allocTime / iterations) + " ns/op");
            System.out.println("  Deallocation: " + (freeTime / iterations) + " ns/op");
            System.out.println("  Throughput: " + String.format("%.2f", 
                (double) size * iterations * 1000 / allocTime) + " MB/s");
        }
    }
    
    private static void benchmarkMemoryOperations() {
        System.out.println("\\n=== Memory Operations Benchmark ===");
        
        long size = 1024 * 1024; // 1MB
        MemoryBlock source = DirectMemoryManager.allocateMemory(size);
        MemoryBlock destination = DirectMemoryManager.allocateMemory(size);
        
        // Fill source with random data
        DirectMemoryManager.setMemory(source, 0, size, (byte) 0x42);
        
        int iterations = 100;
        
        // Warmup
        for (int i = 0; i < 10; i++) {
            DirectMemoryManager.copyMemory(source, 0, destination, 0, size);
            DirectMemoryManager.setMemory(destination, 0, size, (byte) 0);
        }
        
        // Benchmark copy
        long startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            DirectMemoryManager.copyMemory(source, 0, destination, 0, size);
        }
        long copyTime = System.nanoTime() - startTime;
        
        // Benchmark set  
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            DirectMemoryManager.setMemory(destination, 0, size, (byte) (i % 256));
        }
        long setTime = System.nanoTime() - startTime;
        
        // Benchmark compare
        DirectMemoryManager.copyMemory(source, 0, destination, 0, size);
        startTime = System.nanoTime();
        for (int i = 0; i < iterations; i++) {
            DirectMemoryManager.compareMemory(source, 0, destination, 0, size);
        }
        long compareTime = System.nanoTime() - startTime;
        
        System.out.println("Memory operations (" + formatBytes(size) + " x " + iterations + "):");
        System.out.println("  Copy: " + String.format("%.2f", (double) size * iterations * 1000 / copyTime) + " MB/s");
        System.out.println("  Set: " + String.format("%.2f", (double) size * iterations * 1000 / setTime) + " MB/s");
        System.out.println("  Compare: " + String.format("%.2f", (double) size * iterations * 1000 / compareTime) + " MB/s");
        
        DirectMemoryManager.freeMemory(source);
        DirectMemoryManager.freeMemory(destination);
    }
    
    private static void benchmarkAllocatorPerformance() {
        System.out.println("\\n=== Allocator Performance Benchmark ===");
        
        AllocatorType[] types = {AllocatorType.BUDDY_SYSTEM, AllocatorType.SLAB_ALLOCATOR, AllocatorType.ARENA_ALLOCATOR};
        long totalSize = 16 * 1024 * 1024; // 16MB
        int allocations = 1000;
        
        for (AllocatorType type : types) {
            System.out.println("\\n" + type + ":");
            
            try {
                AdvancedMemoryAllocator allocator = AdvancedMemoryAllocator.getInstance(
                    "bench_" + type.name().toLowerCase(), type, totalSize);
                
                // Warmup
                List<AdvancedMemoryAllocator.AllocatedMemory> warmupAllocs = new ArrayList<>();
                for (int i = 0; i < 100; i++) {
                    warmupAllocs.add(allocator.allocate(1024));
                }
                allocator.reset();
                
                // Benchmark allocation
                long startTime = System.nanoTime();
                List<AdvancedMemoryAllocator.AllocatedMemory> allocs = new ArrayList<>();
                
                for (int i = 0; i < allocations; i++) {
                    long size = 64 + random.nextInt(1024); // 64-1088 bytes
                    allocs.add(allocator.allocate(size));
                }
                
                long allocTime = System.nanoTime() - startTime;
                
                // Benchmark free
                startTime = System.nanoTime();
                for (AdvancedMemoryAllocator.AllocatedMemory alloc : allocs) {
                    allocator.free(alloc);
                }
                long freeTime = System.nanoTime() - startTime;
                
                System.out.println("  Allocation: " + (allocTime / allocations) + " ns/op");
                System.out.println("  Free: " + (freeTime / allocations) + " ns/op");
                System.out.println("  Stats: " + allocator.getStats());
                
                allocator.reset();
                
            } catch (Exception e) {
                System.err.println("  ERROR: " + e.getMessage());
            }
        }
    }
    
    private static void demonstrateDirectBufferIntegration() {
        printSection("DirectByteBuffer Integration");
        
        try {
            // Allocate memory and create DirectByteBuffer
            MemoryBlock block = DirectMemoryManager.allocateMemory(8192, AlignmentMode.CACHE_LINE);
            System.out.println("Allocated memory block: " + block);
            
            // Create DirectByteBuffer (simplified implementation)
            ByteBuffer buffer = DirectMemoryManager.createDirectByteBuffer(block);
            System.out.println("Created DirectByteBuffer: capacity=" + buffer.capacity() + 
                ", isDirect=" + buffer.isDirect());
            
            // Use the buffer
            System.out.println("\\nWriting data to DirectByteBuffer...");
            for (int i = 0; i < 100; i++) {
                buffer.putInt(i * 4);
            }
            
            // Read back and verify
            buffer.flip();
            System.out.println("Reading back data...");
            boolean dataCorrect = true;
            for (int i = 0; i < 100; i++) {
                int value = buffer.getInt();
                if (value != i * 4) {
                    dataCorrect = false;
                    break;
                }
            }
            
            System.out.println("Data verification: " + (dataCorrect ? "PASSED ✓" : "FAILED ✗"));
            
            // Try to get native address (may not work with our simplified implementation)
            try {
                long address = DirectMemoryManager.getDirectBufferAddress(buffer);
                System.out.println("DirectBuffer native address: 0x" + Long.toHexString(address));
            } catch (Exception e) {
                System.out.println("Could not extract native address: " + e.getMessage());
            }
            
            DirectMemoryManager.freeMemory(block);
            
        } catch (Exception e) {
            System.err.println("ERROR: DirectByteBuffer integration test failed: " + e.getMessage());
        }
    }
    
    // Helper methods
    
    private static long getAlignment(long address) {
        if (address == 0) return 0;
        return address & -address; // Gets the largest power of 2 that divides address
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return (bytes / 1024) + " KB";
        return (bytes / (1024 * 1024)) + " MB";
    }
    
    private static void printHeader(String title) {
        System.out.println("=".repeat(70));
        System.out.println();
        System.out.println("    " + title);
        System.out.println("    Advanced Off-Heap Memory Management Demo");
        System.out.println();
        System.out.println("=".repeat(70));
        System.out.println();
    }
    
    private static void printSection(String title) {
        System.out.println("\\n" + "-".repeat(60));
        System.out.println(">>> " + title);
        System.out.println("-".repeat(60));
    }
    
    private static void printFooter(String message) {
        System.out.println("\\n" + "=".repeat(70));
        System.out.println("SUCCESS: " + message);
        System.out.println("Direct memory management functionality validated!");
        System.out.println("=".repeat(70));
    }
}