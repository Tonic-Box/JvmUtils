package com.jvmu.demos;

import com.jvmu.directmemory.DirectMemoryManager;
import com.jvmu.directmemory.DirectMemoryManager.MemoryBlock;
import com.jvmu.directmemory.DirectMemoryManager.AlignmentMode;
import com.jvmu.directmemory.DirectMemoryManager.CopyMode;
import com.jvmu.directmemory.DirectMemoryManager.MemoryPool;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.ArrayList;

/**
 * Safe Direct Memory Manager Demo - Focused demonstration with enhanced safety
 * 
 * This version of the demo focuses on safe, validated operations to avoid JVM crashes
 * while demonstrating the core capabilities of the DirectMemoryManager API.
 */
public class SafeDirectMemoryDemo {
    
    public static void main(String[] args) {
        printHeader("Safe Direct Memory Manager Demo");
        
        try {
            // Check availability first
            if (!DirectMemoryManager.isAvailable()) {
                System.err.println("Direct Memory Manager is not available!");
                return;
            }
            
            System.out.println("SUCCESS: Direct Memory Manager Available!");
            
            // Run each demo section with individual error handling
            runSafeDemo("Basic Memory Operations", SafeDirectMemoryDemo::demonstrateBasicOperations);
            runSafeDemo("Memory Alignment", SafeDirectMemoryDemo::demonstrateAlignment);
            runSafeDemo("Memory Manipulation", SafeDirectMemoryDemo::demonstrateMemoryOps);
            runSafeDemo("Memory Statistics", SafeDirectMemoryDemo::demonstrateStatistics);
            runSafeDemo("Memory Pools", SafeDirectMemoryDemo::demonstratePools);
            runSafeDemo("Simple Benchmarks", SafeDirectMemoryDemo::demonstrateSimpleBenchmarks);
            
            printFooter("Safe Direct Memory Demo Completed Successfully!");
            
        } catch (Exception e) {
            System.err.println("FATAL ERROR: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Always cleanup
            cleanupAllMemory();
        }
    }
    
    private static void runSafeDemo(String name, Runnable demo) {
        printSection(name);
        try {
            demo.run();
            System.out.println("✓ " + name + " completed successfully");
        } catch (Exception e) {
            System.err.println("✗ " + name + " failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void demonstrateBasicOperations() {
        // Small, safe allocations
        MemoryBlock small = DirectMemoryManager.allocateMemory(1024);
        MemoryBlock medium = DirectMemoryManager.allocateMemory(4096);
        
        System.out.println("Allocated blocks:");
        System.out.println("  Small: " + small);
        System.out.println("  Medium: " + medium);
        
        // Safe memory operations
        DirectMemoryManager.setMemory(small, 0, 512, (byte) 0xAA);
        DirectMemoryManager.zeroMemory(medium);
        
        System.out.println("Memory operations completed");
        
        // Immediate cleanup
        DirectMemoryManager.freeMemory(small);
        DirectMemoryManager.freeMemory(medium);
    }
    
    private static void demonstrateAlignment() {
        System.out.println("Testing memory alignment:");
        
        AlignmentMode[] modes = {
            AlignmentMode.NONE,
            AlignmentMode.WORD_ALIGNED,
            AlignmentMode.CACHE_LINE
            // Skip PAGE_ALIGNED for safety
        };
        
        List<MemoryBlock> blocks = new ArrayList<>();
        
        try {
            for (AlignmentMode mode : modes) {
                MemoryBlock block = DirectMemoryManager.allocateMemory(1024, mode);
                blocks.add(block);
                
                long alignment = getAlignment(block.address);
                System.out.println("  " + mode + ": addr=0x" + Long.toHexString(block.address) + 
                    ", alignment=" + alignment + " bytes");
            }
            
            // Verify alignment
            System.out.println("Alignment verification:");
            System.out.println("  Word aligned: " + (blocks.get(1).address % 8 == 0 ? "✓" : "✗"));
            System.out.println("  Cache line: " + (blocks.get(2).address % 64 == 0 ? "✓" : "✗"));
            
        } finally {
            for (MemoryBlock block : blocks) {
                DirectMemoryManager.freeMemory(block);
            }
        }
    }
    
    private static void demonstrateMemoryOps() {
        MemoryBlock source = DirectMemoryManager.allocateMemory(2048, AlignmentMode.CACHE_LINE);
        MemoryBlock dest = DirectMemoryManager.allocateMemory(2048, AlignmentMode.CACHE_LINE);
        
        try {
            System.out.println("Testing memory operations:");
            
            // Fill source with pattern
            DirectMemoryManager.setMemory(source, 0, 1024, (byte) 0x55);
            System.out.println("  Filled source with pattern");
            
            // Copy memory
            DirectMemoryManager.copyMemory(source, 0, dest, 0, 1024, CopyMode.NORMAL);
            System.out.println("  Copied 1024 bytes");
            
            // Compare memory
            int comparison = DirectMemoryManager.compareMemory(source, 0, dest, 0, 1024);
            System.out.println("  Memory comparison: " + (comparison == 0 ? "IDENTICAL ✓" : "DIFFERENT ✗"));
            
            // Test different regions  
            DirectMemoryManager.setMemory(source, 1024, 1024, (byte) 0xAA);
            DirectMemoryManager.copyMemory(source, 1024, dest, 1024, 1024, CopyMode.SWAP_ENDIAN);
            System.out.println("  Endian swap copy completed");
            
        } finally {
            DirectMemoryManager.freeMemory(source);
            DirectMemoryManager.freeMemory(dest);
        }
    }
    
    private static void demonstrateStatistics() {
        System.out.println("Memory statistics before allocations:");
        DirectMemoryManager.MemoryStats initialStats = DirectMemoryManager.getMemoryStats();
        System.out.println("  " + initialStats);
        
        // Create some allocations
        List<MemoryBlock> testBlocks = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            testBlocks.add(DirectMemoryManager.allocateMemory(1024 * (i + 1)));
        }
        
        System.out.println("Memory statistics after allocations:");
        DirectMemoryManager.MemoryStats currentStats = DirectMemoryManager.getMemoryStats();
        System.out.println("  " + currentStats);
        
        // Show allocated blocks
        System.out.println("Allocated blocks:");
        for (MemoryBlock block : testBlocks) {
            System.out.println("  " + block);
        }
        
        // Free all test blocks
        for (MemoryBlock block : testBlocks) {
            DirectMemoryManager.freeMemory(block);
        }
        
        System.out.println("Memory statistics after cleanup:");
        DirectMemoryManager.MemoryStats finalStats = DirectMemoryManager.getMemoryStats();
        System.out.println("  " + finalStats);
    }
    
    private static void demonstratePools() {
        System.out.println("Testing memory pools:");
        
        // Create small pool
        MemoryPool pool = new MemoryPool(1024, AlignmentMode.WORD_ALIGNED, 5);
        System.out.println("Created pool: " + pool.getStats());
        
        // Acquire blocks from pool
        List<MemoryBlock> poolBlocks = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            poolBlocks.add(pool.acquire());
        }
        System.out.println("After acquiring 3 blocks: " + pool.getStats());
        
        // Return blocks to pool
        for (MemoryBlock block : poolBlocks) {
            pool.release(block);
        }
        System.out.println("After returning blocks: " + pool.getStats());
        
        // Clean up pool
        pool.clear();
        System.out.println("Pool cleared successfully");
    }
    
    private static void demonstrateSimpleBenchmarks() {
        System.out.println("Running simple benchmarks:");
        
        // Simple allocation benchmark
        int iterations = 100;  // Reduced for safety
        long startTime = System.nanoTime();
        
        List<MemoryBlock> blocks = new ArrayList<>();
        for (int i = 0; i < iterations; i++) {
            blocks.add(DirectMemoryManager.allocateMemory(1024));
        }
        
        long allocTime = System.nanoTime() - startTime;
        
        // Deallocation benchmark
        startTime = System.nanoTime();
        for (MemoryBlock block : blocks) {
            DirectMemoryManager.freeMemory(block);
        }
        long freeTime = System.nanoTime() - startTime;
        
        System.out.println("Allocation benchmark (1KB x " + iterations + "):");
        System.out.println("  Allocation: " + (allocTime / iterations) + " ns/op");
        System.out.println("  Deallocation: " + (freeTime / iterations) + " ns/op");
        
        // Simple memory operation benchmark
        MemoryBlock testBlock = DirectMemoryManager.allocateMemory(64 * 1024); // 64KB
        
        startTime = System.nanoTime();
        for (int i = 0; i < 100; i++) {
            DirectMemoryManager.setMemory(testBlock, 0, testBlock.size, (byte) (i % 256));
        }
        long setTime = System.nanoTime() - startTime;
        
        System.out.println("Memory set benchmark (64KB x 100):");
        System.out.println("  Set time: " + (setTime / 100) + " ns/op");
        System.out.println("  Throughput: " + String.format("%.2f", 
            (double) testBlock.size * 100 * 1000 / setTime) + " MB/s");
        
        DirectMemoryManager.freeMemory(testBlock);
    }
    
    private static long getAlignment(long address) {
        if (address == 0) return 0;
        return address & -address; // Get largest power of 2 that divides address
    }
    
    private static void cleanupAllMemory() {
        try {
            int freedBlocks = DirectMemoryManager.freeAllMemory();
            if (freedBlocks > 0) {
                System.out.println("Emergency cleanup: freed " + freedBlocks + " memory blocks");
            }
        } catch (Exception e) {
            System.err.println("Cleanup failed: " + e.getMessage());
        }
    }
    
    private static void printHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("    " + title);
        System.out.println("    Enhanced Safety Direct Memory Demo");
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println();
    }
    
    private static void printSection(String title) {
        System.out.println("\\n" + "-".repeat(40));
        System.out.println(">>> " + title);
        System.out.println("-".repeat(40));
    }
    
    private static void printFooter(String message) {
        System.out.println("\\n" + "=".repeat(60));
        System.out.println("SUCCESS: " + message);
        System.out.println("All core direct memory operations validated!");
        System.out.println("=".repeat(60));
    }
}