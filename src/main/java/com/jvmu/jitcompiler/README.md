# JIT Compiler Access API

Advanced JIT compilation control for Java applications using privileged JVM access.

## Overview

The JIT Compiler Access API provides fine-grained control over HotSpot's Just-In-Time compilation system. Built on the privileged access framework established by ModuleBootstrap, this API allows developers to:

- Control compilation levels for individual methods
- Monitor and analyze compilation statistics  
- Apply compilation strategies at runtime
- Force recompilation and deoptimization
- Access internal compiler performance data

## Architecture

### Core Components

1. **JITCompilerAccess** - Low-level API for direct compiler control
2. **CompilerController** - High-level management and strategy application
3. **JITCompilerDemo** - Comprehensive demonstration of capabilities

### Compilation Levels (HotSpot)

Based on analysis of HotSpot source code (`src/hotspot/share/compiler/`):

- **Level 0**: Interpreter only - No compilation, full profiling
- **Level 1**: C1 Simple - Fast compilation, no profiling  
- **Level 2**: C1 Limited Profile - C1 with invocation/backedge counters
- **Level 3**: C1 Full Profile - C1 with complete MDO profiling
- **Level 4**: C2/JVMCI Full Optimization - Maximum optimization with profile data

### Access Methods

The API attempts multiple access methods in order of capability:

1. **WhiteBox API** - Full HotSpot testing interface (requires `-XX:+WhiteBoxAPI`)
2. **CompilerOracle** - Direct compiler command interface
3. **Management Interface** - Standard JMX compilation statistics
4. **Tracking Mode** - Local monitoring and strategy application

## Key Features

### Direct Compilation Control

```java
// Force C2 compilation
JITCompilerAccess.compileMethod(method, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);

// Check compilation level
int level = JITCompilerAccess.getCompilationLevel(method);

// Prevent inlining
JITCompilerAccess.setDontInline(method, true);

// Force recompilation
JITCompilerAccess.clearMethodData(method);
```

### Compilation Strategies

```java
CompilerController controller = CompilerController.getInstance();

// Apply different strategies
controller.applyStrategy(method, CompilationStrategy.AGGRESSIVE);
controller.applyStrategy(method, CompilationStrategy.PROFILE_GUIDED);
controller.applyStrategy(method, CompilationStrategy.C1_ONLY);

// Pattern-based application
controller.applyStrategyByPattern("com.example.*", "hot*", CompilationStrategy.AGGRESSIVE);
```

### Performance Analysis

```java
// Analyze individual method performance
PerformanceAnalysis analysis = controller.analyzeMethodPerformance(method);

// Get comprehensive statistics
CompilerStatistics stats = JITCompilerAccess.getCompilerStatistics();

// Generate detailed reports
String report = controller.generateCompilationReport();
```

### Startup Optimization

```java
// Optimize for startup performance
Method[] essentialMethods = { ... };
OptimizationReport report = controller.optimizeForStartup(essentialMethods);
```

## Implementation Details

### Privileged Access Integration

The API leverages the ModuleBootstrap framework to obtain:

- **Internal Unsafe** - Low-level JVM operations
- **Trusted Lookup** - Unrestricted method handle access
- **Module System Bypass** - Access to internal compiler classes

### HotSpot Integration

Key HotSpot classes analyzed and integrated:

- `CompileBroker` - Main compilation coordination
- `CompilerOracle` - Compilation command processing  
- `CompilationPolicy` - Tiered compilation decisions
- `CompileQueue` - Compilation task management
- `CompilerCounters` - Performance statistics

### Fallback Mechanisms

When privileged access is unavailable:

1. **Management Interface** - Standard JMX compilation data
2. **Reflection Access** - Basic method information
3. **Tracking Mode** - Local state management and strategy application
4. **Graceful Degradation** - Core functionality with reduced capabilities

## Usage Examples

### Basic Compilation Control

```java
public class CompilationExample {
    public static void main(String[] args) throws Exception {
        Method hotMethod = CompilationExample.class.getDeclaredMethod("computeHeavy", int.class);
        
        // Check if compilation access is available
        if (JITCompilerAccess.isAvailable()) {
            // Force immediate C2 compilation
            JITCompilerAccess.compileMethod(hotMethod, JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
            
            // Monitor compilation level
            int level = JITCompilerAccess.getCompilationLevel(hotMethod);
            System.out.println("Compilation level: " + JITCompilerAccess.getLevelName(level));
        }
        
        // Execute method
        computeHeavy(10000);
    }
    
    public static long computeHeavy(int iterations) {
        long sum = 0;
        for (int i = 0; i < iterations; i++) {
            sum += Math.sqrt(i * i + 1) * Math.sin(i);
        }
        return sum;
    }
}
```

### Advanced Strategy Application

```java
public class StrategyExample {
    public static void main(String[] args) {
        CompilerController controller = CompilerController.getInstance();
        
        // Apply aggressive compilation to hot methods
        controller.applyStrategyByPattern("com.myapp.core.*", "*", 
            CompilerController.CompilationStrategy.AGGRESSIVE);
        
        // Use conservative compilation for initialization
        controller.applyStrategyByPattern("com.myapp.init.*", "*", 
            CompilerController.CompilationStrategy.CONSERVATIVE);
        
        // Force profiling for analytics methods
        controller.applyStrategyByPattern("com.myapp.analytics.*", "*", 
            CompilerController.CompilationStrategy.PROFILE_GUIDED);
        
        // Generate compilation report
        System.out.println(controller.generateCompilationReport());
    }
}
```

### Performance Monitoring

```java
public class MonitoringExample {
    public static void monitorMethod(Method method) {
        CompilerController controller = CompilerController.getInstance();
        
        // Initial analysis
        PerformanceAnalysis initial = controller.analyzeMethodPerformance(method);
        System.out.println("Initial state: " + initial);
        
        // Run method multiple times
        for (int i = 0; i < 1000; i++) {
            // Execute method...
        }
        
        // Post-execution analysis
        PerformanceAnalysis final = controller.analyzeMethodPerformance(method);
        System.out.println("Final state: " + final);
        
        // Print recommendations
        for (String recommendation : final.recommendations) {
            System.out.println("Recommendation: " + recommendation);
        }
    }
}
```

## Configuration and Setup

### Standard Setup

```java
// Basic initialization (automatic via static block)
if (JITCompilerAccess.isAvailable()) {
    System.out.println("JIT Compiler Access ready");
    System.out.println(JITCompilerAccess.getCompilationStatus());
}
```

### WhiteBox API Setup

For full functionality, enable WhiteBox API:

```bash
java -XX:+UnlockDiagnosticVMOptions -XX:+WhiteBoxAPI -Xbootclasspath/a:whitebox.jar MyApp
```

### JVM Flags for Enhanced Control

```bash
# Enable diagnostic options
-XX:+UnlockDiagnosticVMOptions

# Control compilation levels
-XX:TieredStopAtLevel=4

# Compilation thresholds
-XX:Tier3InvocationThreshold=200
-XX:Tier4InvocationThreshold=5000

# Compiler threads
-XX:CICompilerCount=4

# Debugging output
-XX:+PrintCompilation
-XX:+PrintInlining
```

## Performance Considerations

### Compilation Overhead

- Forced compilation adds immediate CPU overhead
- Benefits realized only if methods are actually hot
- Consider profiling before forcing compilation

### Memory Usage

- Compiled code consumes more memory than bytecode
- Multiple compilation levels can increase memory pressure
- Monitor code cache usage with `-XX:+PrintCodeCache`

### Strategy Selection

- **Aggressive**: Best for known hot methods
- **Conservative**: Let HotSpot's heuristics decide
- **Profile-Guided**: Optimal for complex control flow
- **C1-Only**: Good for startup-critical code

## Limitations and Considerations

### JVM Version Compatibility

- Primarily tested on HotSpot JVM (OpenJDK/Oracle JDK)
- JDK 11+ required for full functionality
- Some features may not work on alternative JVMs

### Security Restrictions

- Requires privileged access via ModuleBootstrap
- May not work in restricted security environments
- Consider security implications of JVM internal access

### Performance Impact

- Compilation control adds runtime overhead
- Use judiciously in production environments
- Profile before and after optimization

## Future Enhancements

Potential areas for enhancement:

1. **Code Cache Management** - Direct code cache allocation control
2. **OSR Compilation** - On-Stack Replacement optimization
3. **Deoptimization Events** - Detailed deoptimization analysis
4. **Vectorization Control** - SIMD optimization management
5. **Escape Analysis** - Object allocation optimization
6. **Inline Caching** - Call site optimization control

## Related APIs

This API complements other JVM utilities:

- **JDI (Java Debug Interface)** - Runtime debugging and inspection
- **JVMTI** - JVM Tool Interface for profiling
- **ModuleBootstrap** - Privileged access framework
- **BytecodeAPI** - Runtime bytecode manipulation

## References

Based on analysis of OpenJDK HotSpot source code:

- `src/hotspot/share/compiler/` - Compilation infrastructure
- `src/hotspot/share/opto/` - C2 optimizing compiler
- `src/hotspot/share/c1/` - C1 compiler implementation
- `src/hotspot/share/runtime/tieredThresholdPolicy.*` - Compilation policy
- JEP 295: Ahead-of-Time Compilation
- JEP 243: Java-Level JVM Compiler Interface