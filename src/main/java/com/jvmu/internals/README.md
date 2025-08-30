# JVM Internals Access API

Low-level access to JVM internal APIs and structures using privileged access.

## Overview

The JVM Internals package provides comprehensive wrapper classes for accessing internal JVM APIs that are normally restricted by the module system. Built on the privileged access framework from ModuleBootstrap, these wrappers expose powerful JVM capabilities:

- **Internal Unsafe Operations** - Memory manipulation and object layout access
- **SharedSecrets Access** - JDK-internal cross-package communication mechanisms  
- **WhiteBox API** - HotSpot testing and introspection capabilities
- **VM Lifecycle Control** - JVM initialization and system property management
- **Thread Tracking** - Advanced thread monitoring and debugging

## Core Components

### InternalUnsafe
Wrapper for `jdk.internal.misc.Unsafe` providing:
- Native memory allocation and manipulation
- Object field offset calculation
- Array base offsets and scale factors
- Atomic operations and memory barriers
- Direct memory access operations

```java
// Check availability
if (InternalUnsafe.isAvailable()) {
    // Allocate native memory
    long address = InternalUnsafe.allocateMemory(1024);
    
    // Write data
    InternalUnsafe.putLong(address, 0x1234567890ABCDEFL);
    
    // Read data back
    long value = InternalUnsafe.getLong(address);
    
    // Free memory
    InternalUnsafe.freeMemory(address);
}
```

### WhiteBox
Wrapper for `sun.hotspot.WhiteBox` providing JVM introspection:
- Garbage collection control and monitoring
- JIT compilation analysis and control
- Memory area inspection and manipulation
- Class loading and unloading operations
- VM flag access and modification

```java
// Force garbage collection
WhiteBox.fullGC();

// Check if method is compiled
Method method = MyClass.class.getDeclaredMethod("hotMethod");
boolean compiled = WhiteBox.isMethodCompiled(method, false);

// Get heap memory usage
long heapUsed = WhiteBox.getHeapUsed();
```

### SharedSecrets
Main access point for JDK internal communication interfaces:
- Returns typed wrapper objects instead of raw Objects
- Provides access to all major internal access interfaces
- Enables cross-package access within the JDK

```java
// Get Java NIO access
JavaNioAccess nioAccess = SharedSecrets.getJavaNioAccess();
if (nioAccess != null && nioAccess.isAvailable()) {
    // Access DirectBuffer internals
}

// Get Java Lang access
JavaLangAccess langAccess = SharedSecrets.getJavaLangAccess();
if (langAccess != null && langAccess.isAvailable()) {
    // Access java.lang internals
}
```

### VM
Wrapper for `jdk.internal.misc.VM` providing VM lifecycle control:
- JVM initialization level monitoring
- System property access
- Runtime information queries

```java
// Check VM initialization level
int level = VM.initLevel();

// Get runtime information
String info = VM.getRuntimeArguments();
```

### ThreadTracker
Advanced thread monitoring and tracking capabilities:
- Thread lifecycle monitoring
- Performance analysis
- Resource usage tracking

## Shared Secrets Sub-Package

The `sharedsecrets` sub-package contains wrapper classes for all major JDK internal access interfaces:

- **JavaLangAccess** - java.lang package internals
- **JavaNioAccess** - NIO and direct buffer operations  
- **JavaIOAccess** - I/O operations and console access
- **JavaUtilJarAccess** - JAR file operations
- **JavaSecurityAccess** - Security operations
- **GenericAccess** - Dynamic wrapper for any access interface

Each wrapper follows a consistent pattern:
- Instance-based design with constructor taking native access object
- `isAvailable()` method for capability checking
- Comprehensive method coverage with proper error handling
- Integration with the broader JvmUtils ecosystem

## Usage Requirements

### Prerequisites
- JDK 11 or higher
- ModuleBootstrap must be properly initialized
- Privileged access to internal JVM APIs

### Initialization
```java
// Ensure ModuleBootstrap is initialized
if (ModuleBootstrap.isInitialized()) {
    // All internals APIs should be available
    System.out.println("Unsafe: " + InternalUnsafe.isAvailable());
    System.out.println("WhiteBox: " + WhiteBox.isAvailable());
    System.out.println("SharedSecrets: " + SharedSecrets.isAvailable());
}
```

## Safety Considerations

### Memory Safety
- Direct memory operations bypass Java's memory safety
- Always free allocated native memory
- Validate memory addresses before access
- Use bounds checking for array operations

### JVM Stability
- WhiteBox operations can affect JVM stability
- Test thoroughly before production use
- Some operations may cause JVM crashes if misused

### Security Implications
- Provides access to privileged JVM internals
- Should only be used in trusted code
- Not suitable for sandboxed environments
- Consider security manager restrictions

## Performance Characteristics

### Access Overhead
- Wrapper classes add minimal overhead (~1-5 ns per call)
- Reflection-based fallbacks are slower (~100-1000x)
- Direct access preferred when available

### Memory Usage
- Wrapper objects have minimal memory footprint
- Native memory operations don't affect GC
- SharedSecrets instances are lightweight

## Integration Examples

### High-Performance Memory Operations
```java
public class FastMemoryOperations {
    public static void fastArrayCopy(Object[] src, Object[] dest, int length) {
        if (InternalUnsafe.isAvailable()) {
            long srcOffset = InternalUnsafe.arrayBaseOffset(Object[].class);
            long destOffset = InternalUnsafe.arrayBaseOffset(Object[].class);
            long scale = InternalUnsafe.arrayIndexScale(Object[].class);
            
            InternalUnsafe.copyMemory(src, srcOffset, dest, destOffset, length * scale);
        } else {
            System.arraycopy(src, 0, dest, 0, length);
        }
    }
}
```

### JVM Monitoring and Analysis
```java
public class JVMMonitor {
    public static void printJVMStatus() {
        if (WhiteBox.isAvailable()) {
            System.out.println("Heap used: " + WhiteBox.getHeapUsed());
            System.out.println("GC count: " + WhiteBox.getGCCount());
            System.out.println("VM version: " + VM.vmVersion());
        }
    }
    
    public static void analyzeMethod(Method method) {
        if (WhiteBox.isAvailable()) {
            boolean compiled = WhiteBox.isMethodCompiled(method, false);
            int level = WhiteBox.getMethodCompilationLevel(method, false);
            System.out.println("Method " + method.getName() + 
                " compiled: " + compiled + ", level: " + level);
        }
    }
}
```

## Limitations

### Platform Dependencies
- Some operations are platform-specific (Windows/Linux/macOS)
- Memory layouts may vary between architectures
- JVM implementation differences (HotSpot vs others)

### Version Compatibility
- Internal APIs may change between JDK versions
- Some features require specific JVM flags
- WhiteBox API requires diagnostic VM options

### Security Restrictions
- May not work with security managers
- Restricted in some deployment environments
- Requires elevated privileges in some contexts

## Related APIs

This package forms the foundation for other JvmUtils components:

- **DirectMemoryManager** - Uses InternalUnsafe for memory operations
- **JITCompilerAccess** - Uses WhiteBox for compilation control
- **ModuleBootstrap** - Provides the privileged access foundation
- **BytecodeAPI** - Uses internal access for runtime manipulation

## Future Enhancements

Potential improvements:
1. **Additional Internal APIs** - More JDK internal wrapper classes
2. **Platform-Specific Optimizations** - OS and architecture specific features
3. **Enhanced Safety Checks** - Better validation and error handling
4. **Performance Monitoring** - Built-in performance metrics
5. **Alternative JVM Support** - Support for GraalVM, OpenJ9, etc.