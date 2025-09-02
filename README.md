# JvmUtils

A comprehensive library providing privileged access to JVM internals and advanced runtime capabilities.

## Overview

JvmUtils is a powerful Java library that bypasses the Java module system (Project Jigsaw) to provide unrestricted access to JVM internal APIs. Built around the **ModuleBootstrap** centerpiece, this library enables developers to access and manipulate JVM internals that are normally restricted, opening up possibilities for advanced memory management, JIT compilation control, bytecode manipulation, and more.

### Key Capabilities

- **Privileged JVM Access** - Bypass module system restrictions to access internal APIs
- **Direct Memory Management** - Advanced off-heap memory allocation and manipulation
- **JIT Compilation Control** - Fine-grained control over HotSpot's compilation system
- **Bytecode Manipulation** - Runtime class generation and transformation
- **JVM Introspection** - Deep access to JVM internals via WhiteBox API
- **SharedSecrets Access** - Typed wrappers for JDK internal communication interfaces
- **Debug Interface** - Advanced debugging and inspection capabilities

## Architecture

### Core Foundation
```
ModuleBootstrap (Privilege Escalation)
    ├── InternalUnsafe Access
    ├── Trusted Lookup Acquisition  
    ├── Module System Bypass
    └── Security Restriction Removal
```

### API Layers
```
High-Level APIs
├── DirectMemoryManager     (Memory Management)
├── JITCompilerAccess       (Compilation Control)
├── BytecodeAPI            (Code Generation)
└── JDI Integration        (Debugging)

Mid-Level APIs  
├── WhiteBox               (JVM Testing Interface)
├── SharedSecrets          (Internal Communications)
├── ThreadTracker          (Thread Monitoring)
└── VM Control             (VM Lifecycle)

Low-Level Foundation
├── InternalUnsafe         (Memory Operations)
├── ReflectBuilder         (Enhanced Reflection)
└── Utility Classes        (Support Functions)
```

## Quick Start

### Basic Setup
```java
import com.jvmu.module.ModuleBootstrap;
import com.jvmu.directmemory.DirectMemoryManager;
import com.jvmu.internals.InternalUnsafe;

public class QuickStart {
    public static void main(String[] args) {
        // Verify privileged access is available
        if (ModuleBootstrap.isInitialized()) {
            System.out.println("JvmUtils ready!");
            
            // Example: Direct memory allocation
            var block = DirectMemoryManager.allocateMemory(1024);
            System.out.println("Allocated 1KB at: 0x" + 
                Long.toHexString(block.address));
            
            DirectMemoryManager.freeMemory(block);
        } else {
            System.err.println("Failed to initialize privileged access");
        }
    }
}
```

### Checking API Availability
```java
public class AvailabilityCheck {
    public static void checkAPIs() {
        System.out.println("=== JvmUtils API Availability ===");
        System.out.println("ModuleBootstrap: " + ModuleBootstrap.isInitialized());
        System.out.println("InternalUnsafe: " + InternalUnsafe.isAvailable());
        System.out.println("WhiteBox: " + WhiteBox.isAvailable());
        System.out.println("SharedSecrets: " + SharedSecrets.isAvailable());
        System.out.println("DirectMemory: " + DirectMemoryManager.isAvailable());
        System.out.println("JITCompiler: " + JITCompilerAccess.isAvailable());
    }
}
```

## Major API Packages

### 🔐 [Module System Bootstrap](src/main/java/com/jvmu/module/README.md)
**Package:** `com.jvmu.module`

The foundation that makes everything possible. ModuleBootstrap performs privilege escalation to bypass Java's module system restrictions.

```java
// Automatic initialization provides access to internal APIs
ModuleBootstrap.ensureInitialized();
Object internalUnsafe = ModuleBootstrap.getInternalUnsafe();
MethodHandles.Lookup trustedLookup = ModuleBootstrap.getTrustedLookup();
```

### 🧠 [JVM Internals Access](src/main/java/com/jvmu/internals/README.md)
**Package:** `com.jvmu.internals`

Direct access to JVM internal APIs with comprehensive wrapper classes:

- **InternalUnsafe** - Memory operations and object manipulation
- **WhiteBox** - HotSpot testing and introspection API
- **SharedSecrets** - JDK internal communication interfaces
- **VM** - Virtual machine lifecycle control
- **ThreadTracker** - Advanced thread monitoring

```java
// Memory operations
long address = InternalUnsafe.allocateMemory(1024);
InternalUnsafe.putLong(address, 0x1234567890ABCDEFL);
long value = InternalUnsafe.getLong(address);
InternalUnsafe.freeMemory(address);

// JVM introspection
WhiteBox.fullGC();
boolean compiled = WhiteBox.isMethodCompiled(method, false);
long heapUsed = WhiteBox.getHeapUsed();
```

### 💾 [Direct Memory Management](src/main/java/com/jvmu/directmemory/README.md)
**Package:** `com.jvmu.directmemory`

Advanced off-heap memory management with sophisticated allocation strategies:

```java
// Basic allocation
MemoryBlock block = DirectMemoryManager.allocateMemory(1024 * 1024);

// Aligned allocation  
MemoryBlock aligned = DirectMemoryManager.allocateMemory(4096, 
    AlignmentMode.PAGE_ALIGNED);

// Advanced allocators
AdvancedMemoryAllocator buddyAlloc = AdvancedMemoryAllocator.getInstance(
    "buddy", AllocatorType.BUDDY_SYSTEM, 64 * 1024 * 1024);

// Memory pools
MemoryPool pool = new MemoryPool(1024, AlignmentMode.CACHE_LINE, 50);
```

### ⚡ [JIT Compilation Control](src/main/java/com/jvmu/jitcompiler/README.md)
**Package:** `com.jvmu.jitcompiler`

Fine-grained control over HotSpot's Just-In-Time compilation system:

```java
// Force compilation
JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);

// Check compilation status
int level = JITCompilerAccess.getCompilationLevel(method);
System.out.println("Compilation level: " + JITCompilerAccess.getLevelName(level));

// Apply compilation strategies
CompilerController controller = CompilerController.getInstance();
controller.applyStrategy(method, CompilationStrategy.AGGRESSIVE);
```

### 🔧 [Bytecode Manipulation](src/main/java/com/jvmu/bytecode/README.md)
**Package:** `com.jvmu.bytecode`

Runtime bytecode generation, manipulation, and class transformation:

```java
// Generate new classes
Class<?> newClass = BytecodeAPI.generateClass("DynamicClass", builder -> {
    builder.addField("value", int.class);
    builder.addMethod("getValue", int.class, method -> {
        method.returnField("value");
    });
});

// Transform existing classes  
BytecodeAPI.transformClass(ExistingClass.class, transformer -> {
    transformer.addLogging();
    transformer.addPerformanceMonitoring();
});
```

### 🛠️ [Utility Classes](src/main/java/com/jvmu/util/README.md)
**Package:** `com.jvmu.util`

Core utilities providing enhanced reflection, classpath management, and code generation:

```java
// Fluent reflection API
Object result = ReflectBuilder.of(someObject)
    .field("internalField")
    .method("toString")
    .get();

// Enhanced field/method access
Field field = ReflectUtil.getField(MyClass.class, "privateField");
Object value = ReflectUtil.getFieldValue(instance, field);

// Dynamic class loading
Set<Class<?>> classes = ClassPath.findClasses("com.example.package");
```

## Additional APIs

### 🔍 Java Debug Interface (JDI)
**Package:** `com.jvmu.jdi`
Advanced debugging capabilities with event handling, thread management, and runtime inspection.

### 🏗️ Class Redefinition
**Package:** `com.jvmu.classredefinition` 
Hot-swapping of class definitions with multiple redefinition strategies.

### 🌐 JVMTI Integration
**Package:** `com.jvmu.jvmti`
Integration with JVM Tool Interface for profiling and monitoring.

### 🧪 Agent Support
**Package:** `com.jvmu.agent`
Java agent capabilities for instrumentation and monitoring.

### 🚫 Verification Bypass
**Package:** `com.jvmu.noverify`
Utilities for bypassing bytecode verification when needed.

## Requirements and Compatibility

### System Requirements
- **Java Version:** JDK 11
- **JVM:** HotSpot JVM (OpenJDK or Oracle JDK)
- **Operating System:** Windows, Linux, macOS
- **Architecture:** x86-64 (primary), ARM64 (limited support)

### JVM Compatibility Matrix
| JVM Implementation | Support Level | Notes |
|-------------------|---------------|-------|
| HotSpot (OpenJDK) | Full Support | All features available |
| HotSpot (Oracle)  | Full Support | All features available |
| OpenJ9 (Eclipse)  | Partial | Some internal APIs differ |
| GraalVM JIT       | Partial | Limited internal access |
| GraalVM Native    | None | Not compatible |

## Usage Examples

### Memory Management Example
```java
public class MemoryExample {
    public static void demonstrateMemoryManagement() {
        // Allocate 1MB of direct memory
        MemoryBlock block = DirectMemoryManager.allocateMemory(1024 * 1024);
        
        try {
            // Fill with pattern
            DirectMemoryManager.setMemory(block, 0, block.size, (byte) 0xAA);
            
            // Copy data
            byte[] data = "Hello, JvmUtils!".getBytes();
            DirectMemoryManager.copyMemoryFromArray(data, 0, block, 0, data.length);
            
            // Read back
            byte[] result = new byte[data.length];
            DirectMemoryManager.copyMemoryToArray(block, 0, result, 0, data.length);
            
            System.out.println("Data: " + new String(result));
            
            // Get memory statistics
            MemoryStats stats = DirectMemoryManager.getMemoryStats();
            System.out.println("Allocated blocks: " + stats.allocatedBlocks);
            System.out.println("Total allocated: " + stats.totalAllocated + " bytes");
            
        } finally {
            // Always free memory
            DirectMemoryManager.freeMemory(block);
        }
    }
}
```

### JIT Compilation Example
```java
public class JITExample {
    public static void optimizeHotMethod() {
        try {
            Method hotMethod = JITExample.class.getDeclaredMethod("computeIntensive", int.class);
            
            // Check current compilation status
            if (JITCompilerAccess.isAvailable()) {
                int level = JITCompilerAccess.getCompilationLevel(hotMethod);
                System.out.println("Current level: " + JITCompilerAccess.getLevelName(level));
                
                // Force C2 compilation
                JITCompilerAccess.compileMethod(hotMethod, 
                    JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
                
                // Verify compilation
                if (JITCompilerAccess.isCompiled(hotMethod)) {
                    System.out.println("Method successfully compiled");
                }
                
                // Get compiler statistics
                CompilerStatistics stats = JITCompilerAccess.getCompilerStatistics();
                System.out.println("Total compilations: " + stats.totalCompilations);
            }
            
            // Run the method to see performance improvement
            long start = System.nanoTime();
            long result = computeIntensive(10000);
            long duration = System.nanoTime() - start;
            
            System.out.println("Result: " + result + ", Time: " + duration + "ns");
            
        } catch (NoSuchMethodException e) {
            System.err.println("Method not found: " + e.getMessage());
        }
    }
    
    public static long computeIntensive(int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(i * i + 1) * Math.sin(i);
        }
        return sum;
    }
}
```

### Bytecode Generation Example
```java
public class BytecodeExample {
    public static void createDynamicClass() {
        // Generate a simple data class
        Class<?> personClass = BytecodeAPI.generateClass("Person", builder -> {
            // Fields
            builder.addField("name", String.class, Modifier.PRIVATE);
            builder.addField("age", int.class, Modifier.PRIVATE);
            
            // Constructor
            builder.addConstructor(constructor -> {
                constructor.addParameter("name", String.class);
                constructor.addParameter("age", int.class);
                constructor.setBody("this.name = name; this.age = age;");
            });
            
            // Getters
            builder.addMethod("getName", String.class, method -> {
                method.setBody("return this.name;");
            });
            
            builder.addMethod("getAge", int.class, method -> {
                method.setBody("return this.age;");
            });
            
            // toString method
            builder.addMethod("toString", String.class, method -> {
                method.setBody("return name + \" (\" + age + \")\";");
            });
        });
        
        try {
            // Use the generated class
            Object person = personClass
                .getConstructor(String.class, int.class)
                .newInstance("John Doe", 30);
            
            Method getName = personClass.getMethod("getName");
            Method getAge = personClass.getMethod("getAge");
            Method toString = personClass.getMethod("toString");
            
            System.out.println("Name: " + getName.invoke(person));
            System.out.println("Age: " + getAge.invoke(person));
            System.out.println("String: " + toString.invoke(person));
            
        } catch (Exception e) {
            System.err.println("Failed to use generated class: " + e.getMessage());
        }
    }
}
```

#### Partial Functionality
Some APIs may be available while others are not. Check individual API availability:
```java
public class APIStatus {
    public static void checkAll() {
        Map<String, Boolean> status = Map.of(
            "InternalUnsafe", InternalUnsafe.isAvailable(),
            "WhiteBox", WhiteBox.isAvailable(),
            "SharedSecrets", SharedSecrets.isAvailable(),
            "DirectMemory", DirectMemoryManager.isAvailable(),
            "JITCompiler", JITCompilerAccess.isAvailable(),
            "BytecodeAPI", BytecodeAPI.isAvailable()
        );
        
        status.forEach((api, available) -> 
            System.out.println(api + ": " + (available ? "✓" : "✗"))
        );
    }
}
```

## Contributing

### Development Setup
1. Clone the repository
2. Ensure JDK 11+ is installed  
3. Run tests: `./gradlew test`
4. Build: `./gradlew build`

### Code Style
- Follow existing code patterns
- Add comprehensive documentation
- Include safety checks and validation
- Write thorough tests

### Testing
- Test across multiple JDK versions (11, 17, 21)
- Test on different operating systems
- Include performance benchmarks
- Test security and error conditions

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- **OpenJDK Project** - For the internal API implementations this library accesses
- **ASM Project** - Used for bytecode manipulation capabilities
- **Java Community** - For ongoing discussions about internal API access

## Support and Community

- **Issues**: [GitHub Issues](https://github.com/yourorg/jvmutils/issues)
- **Discussions**: [GitHub Discussions](https://github.com/yourorg/jvmutils/discussions)
- **Documentation**: [Wiki](https://github.com/yourorg/jvmutils/wiki)

---

**⚠️ Important Notice:** JvmUtils provides powerful low-level access to JVM internals. Use responsibly and understand the security implications. This library is intended for advanced use cases, debugging, performance optimization, and educational purposes. Always test thoroughly before using in production environments.
