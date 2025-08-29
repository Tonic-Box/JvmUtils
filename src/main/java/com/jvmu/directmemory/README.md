# DirectMemoryManager API

Advanced off-heap memory management for Java applications using privileged JVM access.

## Overview

The DirectMemoryManager API provides comprehensive control over native memory allocation and manipulation, built on top of the privileged access framework. This implementation analyzes and leverages internal JDK structures to provide:

- **Native Memory Operations**: Direct allocation, reallocation, and deallocation using internal Unsafe APIs
- **Advanced Memory Alignment**: Support for word, cache-line, and page alignment
- **Memory Manipulation**: High-performance copy, fill, and comparison operations  
- **Sophisticated Allocators**: Multiple allocation strategies including buddy system, slab allocation, arena allocation
- **Memory Pool Management**: Efficient pooling for frequently allocated/freed objects
- **Leak Detection**: Comprehensive tracking and leak detection capabilities
- **DirectByteBuffer Integration**: Seamless integration with NIO direct buffers

## Architecture

### Core Components

1. **DirectMemoryManager** - Main API for direct memory operations
2. **AdvancedMemoryAllocator** - Multiple allocation strategies
3. **DirectMemoryDemo** - Comprehensive demonstration and benchmarks

### JDK Integration Analysis

Based on deep analysis of JDK 11 source code:

#### Internal Unsafe (jdk.internal.misc.Unsafe)
```java
// Core memory allocation methods analyzed:
- allocateMemory(long bytes) -> long address
- freeMemory(long address) -> void  
- reallocateMemory(long address, long bytes) -> long newAddress
- setMemory(long address, long bytes, byte value) -> void
- copyMemory(long srcAddress, long destAddress, long bytes) -> void
- copySwapMemory(...) -> void // Endian conversion
```

#### Native Memory Segments (jdk.internal.foreign.NativeMemorySegmentImpl)
```java
// Memory segment structure analyzed:
- long min (base address)
- long length (segment size)  
- boolean readOnly (access permissions)
- MemorySessionImpl scope (lifecycle management)
```

#### DirectBuffer Interface (sun.nio.ch.DirectBuffer)
```java  
// Direct buffer integration points:
- long address() // Get native memory address
- Object attachment() // Get associated object
- Cleaner cleaner() // Get cleanup mechanism
```

### Memory Management Strategies

The API provides multiple allocation strategies optimized for different use cases:

#### 1. Buddy System Allocator
- **Use Case**: General-purpose allocation with low fragmentation
- **Algorithm**: Binary splitting/coalescing of power-of-2 sized blocks
- **Pros**: Low fragmentation, fast coalescing
- **Cons**: Internal fragmentation for non-power-of-2 sizes

#### 2. Slab Allocator  
- **Use Case**: Fixed-size object allocation
- **Algorithm**: Pre-allocated slabs of fixed-size objects
- **Pros**: O(1) allocation/deallocation, no fragmentation
- **Cons**: Memory waste if object sizes vary

#### 3. Arena Allocator
- **Use Case**: Sequential allocation with bulk deallocation
- **Algorithm**: Bump pointer allocation in large arenas
- **Pros**: Extremely fast allocation, good cache locality
- **Cons**: No individual deallocation, memory held until arena reset

#### 4. Ring Buffer Allocator
- **Use Case**: Streaming/circular data processing
- **Algorithm**: Circular buffer with head/tail pointers
- **Pros**: Constant memory usage, good for streaming
- **Cons**: Complex lifetime management

#### 5. Object Pool Allocator
- **Use Case**: Frequently allocated/freed objects
- **Algorithm**: Pre-allocated object recycling
- **Pros**: Eliminates allocation overhead, reduces GC pressure
- **Cons**: Memory held even when not in use

#### 6. Hybrid Allocator
- **Use Case**: Mixed workloads
- **Algorithm**: Size-based strategy selection
- **Pros**: Optimal strategy for each allocation size
- **Cons**: Increased complexity

## Key Features

### Memory Allocation with Alignment

```java
// Basic allocation
MemoryBlock block = DirectMemoryManager.allocateMemory(1024 * 1024); // 1MB

// Aligned allocation
MemoryBlock aligned = DirectMemoryManager.allocateMemory(4096, AlignmentMode.PAGE_ALIGNED);

// Reallocation
MemoryBlock expanded = DirectMemoryManager.reallocateMemory(block, 2 * 1024 * 1024);
```

### Memory Operations

```java
// Fill memory
DirectMemoryManager.setMemory(block, 0, block.size, (byte) 0xFF);
DirectMemoryManager.zeroMemory(block); // Shortcut for zero-fill

// Copy memory  
DirectMemoryManager.copyMemory(source, 0, dest, 0, 1024, CopyMode.NORMAL);
DirectMemoryManager.copyMemory(source, 0, dest, 0, 1024, CopyMode.SWAP_ENDIAN);

// Compare memory
int result = DirectMemoryManager.compareMemory(block1, 0, block2, 0, 1024);
```

### Advanced Allocators

```java
// Create specialized allocators
AdvancedMemoryAllocator buddyAlloc = AdvancedMemoryAllocator.getInstance(
    "buddy", AllocatorType.BUDDY_SYSTEM, 64 * 1024 * 1024);

AdvancedMemoryAllocator slabAlloc = AdvancedMemoryAllocator.getInstance(
    "slab", AllocatorType.SLAB_ALLOCATOR, 16 * 1024 * 1024);

// Use allocators
AllocatedMemory mem1 = buddyAlloc.allocate(4096);
AllocatedMemory mem2 = slabAlloc.allocate(128);

// Free memory
buddyAlloc.free(mem1);
slabAlloc.free(mem2);
```

### Memory Pools

```java
// Create memory pool
MemoryPool pool = new MemoryPool(1024, AlignmentMode.CACHE_LINE, 50);

// Use pool
MemoryBlock block = pool.acquire();
// ... use block ...
pool.release(block); // Returns to pool for reuse

// Monitor pool
PoolStats stats = pool.getStats();
System.out.println("Pool: " + stats);
```

### Memory Statistics and Leak Detection

```java
// Get memory statistics
MemoryStats stats = DirectMemoryManager.getMemoryStats();
System.out.println("Memory usage: " + stats);

// Find potential leaks
List<MemoryBlock> leaks = DirectMemoryManager.findPotentialLeaks(60000); // 1 minute
for (MemoryBlock leak : leaks) {
    System.out.println("Potential leak: " + leak);
}

// Get all allocated blocks
List<MemoryBlock> allBlocks = DirectMemoryManager.getAllocatedBlocks();
```

### DirectByteBuffer Integration

```java
// Create DirectByteBuffer from allocated memory
MemoryBlock block = DirectMemoryManager.allocateMemory(8192);
ByteBuffer buffer = DirectMemoryManager.createDirectByteBuffer(block);

// Extract address from existing DirectByteBuffer
ByteBuffer directBuffer = ByteBuffer.allocateDirect(1024);
long address = DirectMemoryManager.getDirectBufferAddress(directBuffer);
```

## Performance Characteristics

### Allocation Performance
Based on benchmarks from the demo:

- **Small allocations (64B-1KB)**: ~100-500 ns/allocation
- **Medium allocations (1-64KB)**: ~500-2000 ns/allocation  
- **Large allocations (1MB+)**: ~2-10 μs/allocation

### Memory Operation Performance
- **Memory copy**: 10-20 GB/s (CPU dependent)
- **Memory fill**: 15-30 GB/s (CPU dependent)
- **Memory compare**: 5-15 GB/s (CPU dependent)

### Allocator Strategy Performance
- **Arena Allocator**: Fastest allocation (~50 ns/op)
- **Buddy System**: Good balance (~200 ns/op)
- **Slab Allocator**: Fast for fixed sizes (~100 ns/op)
- **Object Pool**: Fastest when hitting pool (~10 ns/op)

## Memory Alignment

### Alignment Modes
- **NONE**: No specific alignment requirement
- **WORD_ALIGNED**: 8-byte alignment (64-bit word)  
- **CACHE_LINE**: 64-byte alignment (CPU cache line)
- **PAGE_ALIGNED**: 4096-byte alignment (memory page)

### Benefits of Alignment
- **Word Alignment**: Required for atomic operations
- **Cache Line Alignment**: Reduces cache line splits, improves performance
- **Page Alignment**: Required for memory mapping, reduces TLB pressure

## Safety and Best Practices

### Memory Safety
```java
// Always check validity before access
if (block.isValid()) {
    // Safe to use block
    DirectMemoryManager.setMemory(block, 0, 1024, (byte) 0);
}

// Bounds checking is automatic
try {
    DirectMemoryManager.setMemory(block, 0, block.size + 1, (byte) 0); // Throws exception
} catch (IllegalArgumentException e) {
    System.err.println("Access beyond bounds: " + e.getMessage());
}
```

### Resource Management
```java
// Always free allocated memory
MemoryBlock block = DirectMemoryManager.allocateMemory(1024);
try {
    // Use block...
} finally {
    DirectMemoryManager.freeMemory(block);
}

// Or use emergency cleanup (use sparingly)
int freedBlocks = DirectMemoryManager.freeAllMemory();
```

### Error Handling
```java
// Handle allocation failures
try {
    MemoryBlock huge = DirectMemoryManager.allocateMemory(Long.MAX_VALUE);
} catch (OutOfMemoryError e) {
    System.err.println("Allocation failed: " + e.getMessage());
}

// Handle access to freed memory
MemoryBlock block = DirectMemoryManager.allocateMemory(1024);
DirectMemoryManager.freeMemory(block);
try {
    DirectMemoryManager.setMemory(block, 0, 1024, (byte) 0); // Throws exception
} catch (IllegalArgumentException e) {
    System.err.println("Access to freed memory: " + e.getMessage());
}
```

## Configuration and Tuning

### JVM Flags for Optimal Performance
```bash
# Increase direct memory limit
-XX:MaxDirectMemorySize=8g

# Enable large pages (if using page alignment)  
-XX:+UseLargePages

# Reduce GC pressure
-XX:+UseG1GC -XX:MaxGCPauseMillis=50

# Enable compressed OOPs (if using < 32GB heap)
-XX:+UseCompressedOops
```

### Memory Pool Sizing
```java
// Size pools based on allocation patterns
MemoryPool smallPool = new MemoryPool(64, AlignmentMode.WORD_ALIGNED, 1000);   // Many small objects
MemoryPool largePool = new MemoryPool(64*1024, AlignmentMode.CACHE_LINE, 10); // Few large objects
```

### Allocator Selection Guide
- **High allocation rate, mixed sizes**: Hybrid Allocator
- **Fixed-size objects**: Slab Allocator  
- **Temporary allocations**: Arena Allocator
- **Long-lived objects**: Buddy System
- **Streaming data**: Ring Buffer
- **Frequent alloc/free cycles**: Object Pool

## Limitations and Considerations

### Platform Dependencies
- **Memory addresses**: 64-bit on 64-bit JVMs, 32-bit on 32-bit JVMs
- **Alignment requirements**: CPU-specific (x86-64 vs ARM vs others)
- **Memory limits**: OS and JVM imposed limits

### JVM Compatibility
- **Requires JDK 11+** for full functionality
- **HotSpot JVM optimized** (OpenJDK/Oracle JDK)
- **Alternative JVMs** may have reduced functionality

### Security Considerations
- **Privileged access required** via ModuleBootstrap
- **Direct memory access** bypasses Java memory safety
- **Use in trusted code only** - not suitable for sandboxed environments

### Performance Trade-offs
- **Memory overhead**: Tracking structures add ~64 bytes per allocation
- **Synchronization cost**: Thread-safe operations have locking overhead
- **Leak detection cost**: Statistics tracking adds minor per-allocation cost

## Integration Examples

### High-Performance Buffer Management
```java
public class HighPerformanceBufferManager {
    private final AdvancedMemoryAllocator allocator;
    private final MemoryPool bufferPool;
    
    public HighPerformanceBufferManager() {
        // Use hybrid allocator for mixed workload
        allocator = AdvancedMemoryAllocator.getInstance("hpbm", 
            AllocatorType.HYBRID, 256 * 1024 * 1024);
        
        // Pool for common buffer sizes
        bufferPool = new MemoryPool(8192, AlignmentMode.CACHE_LINE, 100);
    }
    
    public ByteBuffer acquireBuffer(int size) {
        MemoryBlock block;
        if (size == 8192) {
            block = bufferPool.acquire();
        } else {
            block = DirectMemoryManager.allocateMemory(size, AlignmentMode.CACHE_LINE);
        }
        
        return DirectMemoryManager.createDirectByteBuffer(block);
    }
    
    public void releaseBuffer(ByteBuffer buffer, int size) {
        if (size == 8192) {
            // Return to pool logic would need buffer -> MemoryBlock mapping
        } else {
            // Free individual allocation logic
        }
    }
}
```

### Zero-Copy Data Processing
```java
public class ZeroCopyProcessor {
    private final MemoryBlock processingBuffer;
    
    public ZeroCopyProcessor(long bufferSize) {
        // Allocate large aligned buffer for zero-copy operations
        processingBuffer = DirectMemoryManager.allocateMemory(
            bufferSize, AlignmentMode.PAGE_ALIGNED);
    }
    
    public void processData(MemoryBlock inputData) {
        // Direct memory operations without copying to heap
        DirectMemoryManager.copyMemory(inputData, 0, 
            processingBuffer, 0, inputData.size, CopyMode.NORMAL);
        
        // Process data in-place...
        
        // Zero the buffer for security
        DirectMemoryManager.zeroMemory(processingBuffer);
    }
    
    public void cleanup() {
        DirectMemoryManager.freeMemory(processingBuffer);
    }
}
```

## Future Enhancements

Potential areas for enhancement:

1. **Memory Mapping Integration** - Support for memory-mapped files
2. **NUMA Awareness** - Allocate memory on specific NUMA nodes
3. **Compression Support** - Transparent compression for large allocations
4. **Memory Encryption** - Hardware-accelerated memory encryption
5. **Cross-Process Sharing** - Shared memory segments between JVM processes
6. **Advanced Leak Detection** - Stack trace capture and analysis
7. **Memory Profiling Integration** - JFR event integration
8. **Alternative JVM Support** - Support for GraalVM, Eclipse OpenJ9

## References

This implementation is based on analysis of OpenJDK source code:

- `jdk.internal.misc.Unsafe` - Core unsafe memory operations
- `jdk.internal.foreign.NativeMemorySegmentImpl` - Native memory segments  
- `sun.nio.ch.DirectBuffer` - Direct buffer interface
- `java.nio.MappedByteBuffer` - Memory-mapped buffer implementation
- HotSpot memory management internals
- Panama Foreign Memory Access API (JEP 393, 419)

## Related APIs

This API complements other JvmUtils components:

- **ModuleBootstrap** - Provides the privileged access foundation
- **JITCompilerAccess** - Controls compilation of memory-intensive code
- **JDI (Java Debug Interface)** - Runtime debugging and inspection  
- **BytecodeAPI** - Runtime bytecode manipulation for memory operations