# MethodInterceptor API

Advanced runtime method hooking without bytecode modification for Java applications using privileged JVM access.

## Overview

The MethodInterceptor API provides comprehensive runtime method interception capabilities built on top of the privileged access framework. This implementation analyzes and leverages internal JDK 11 structures to provide method hooking that works at the JVM level, intercepting both direct calls and reflective calls.

## Key Features

- **Runtime Method Hooking**: Intercept method calls without modifying bytecode
- **Direct Call Interception**: Hook direct method invocations (invoke_virtual, invoke_static)
- **Reflection Call Interception**: Hook Method.invoke() calls  
- **MethodHandle Interception**: Intercept MethodHandle-based calls
- **Multi-Level Hooking**: Different levels of interception depth
- **Exception Handling**: Intercept and modify exception behavior
- **Performance Monitoring**: Built-in call statistics and performance measurement
- **Memory Safe**: Proper cleanup and restoration of original method behavior

## Architecture

### Core Components

1. **MethodInterceptor** - High-level API for method interception with reflection focus
2. **RuntimeMethodHooker** - Advanced API for deep method hooking with multiple levels
3. **MethodInterceptorDemo** - Comprehensive demonstration and testing

### JDK 11 Integration Analysis

Based on deep analysis of JDK 11 HotSpot source code:

#### Internal Method Structure (oops/method.hpp)
```cpp
class Method : public Metadata {
  // Entry points for method calls
  address _i2i_entry;                    // Interpreter-to-interpreter entry
  volatile address _from_compiled_entry;  // Entry from compiled code
  volatile address _from_interpreted_entry; // Entry from interpreted code
  CompiledMethod* volatile _code;         // Native compiled method pointer
};
```

#### Java Method Class Structure
```java
public final class Method extends Executable {
  // Internal method accessor for invocation
  private MethodAccessor methodAccessor;
  
  // Root method for method copying
  private Method root;
  
  // Access override flag
  private boolean override;
}
```

### Interception Strategies

The API provides multiple interception strategies with different capabilities:

#### 1. Reflection Interception (MethodInterceptor)
- **Target**: Method.invoke() calls
- **Mechanism**: Replace MethodAccessor with intercepting wrapper
- **Scope**: Reflection-based calls only
- **Performance**: Low overhead for direct calls

#### 2. Method Accessor Hooking (RuntimeMethodHooker.REFLECTION_ONLY)
- **Target**: MethodAccessor interface
- **Mechanism**: Replace internal method accessor
- **Scope**: Reflection calls and some MethodHandle calls
- **Performance**: Minimal overhead

#### 3. Full Interception (RuntimeMethodHooker.FULL_INTERCEPTION)
- **Target**: Multiple call paths
- **Mechanism**: Hook method resolution and accessors
- **Scope**: Most method calls including direct calls
- **Performance**: Moderate overhead

#### 4. Native Entry Point Hooking (RuntimeMethodHooker.NATIVE_ENTRY_POINT)
- **Target**: Native method entry points
- **Mechanism**: Patch method entry addresses with trampolines
- **Scope**: All method calls including JIT compiled calls
- **Performance**: Comprehensive but higher overhead

## Usage Guide

### Basic Method Interception

```java
// Create target method
Method method = MyClass.class.getDeclaredMethod("myMethod", int.class);

// Create interceptor handler
MethodInterceptor.InterceptionHandler handler = new MethodInterceptor.InterceptionHandler() {
    @Override
    public MethodInterceptor.InterceptionResult beforeInvocation(Method method, Object instance, Object[] args) {
        System.out.println("Before: " + method.getName() + " with args: " + Arrays.toString(args));
        return MethodInterceptor.InterceptionResult.PROCEED;
    }
    
    @Override
    public Object afterInvocation(Method method, Object instance, Object[] args, Object result) {
        System.out.println("After: " + method.getName() + " returned: " + result);
        return result; // Can modify the return value
    }
    
    @Override
    public MethodInterceptor.InterceptionResult onException(Method method, Object instance, Object[] args, Throwable exception) {
        System.out.println("Exception in: " + method.getName() + " - " + exception.getMessage());
        return MethodInterceptor.InterceptionResult.PROCEED;
    }
};

// Install interception
MethodInterceptor.InterceptorHandle handle = MethodInterceptor.interceptMethod(method, handler);

// Method calls will now be intercepted
method.invoke(instance, 42); // Triggers interception

// Remove interception
handle.remove();
```

### Advanced Runtime Hooking

```java
// Create runtime interceptor
RuntimeMethodHooker.MethodInterceptor interceptor = new RuntimeMethodHooker.MethodInterceptor() {
    @Override
    public RuntimeMethodHooker.InterceptionAction beforeMethod(Method method, Object instance, Object[] args) {
        // Pre-processing
        return RuntimeMethodHooker.InterceptionAction.PROCEED;
    }
    
    @Override
    public Object afterMethod(Method method, Object instance, Object[] args, Object result) {
        // Post-processing
        return result;
    }
    
    @Override
    public RuntimeMethodHooker.InterceptionAction onException(Method method, Object instance, Object[] args, Throwable exception) {
        // Exception handling
        return RuntimeMethodHooker.InterceptionAction.PROCEED;
    }
};

// Install hook with specific level
RuntimeMethodHooker.RuntimeHook hook = RuntimeMethodHooker.installHook(
    method, interceptor, RuntimeMethodHooker.HookLevel.FULL_INTERCEPTION);

// Both direct and reflection calls are intercepted
instance.myMethod(42);        // Direct call - intercepted
method.invoke(instance, 42);  // Reflection call - intercepted

// Remove hook
hook.remove();
```

### Hook Level Comparison

| Hook Level | Direct Calls | Reflection Calls | MethodHandle Calls | Performance Impact |
|------------|-------------|------------------|--------------------|--------------------|
| REFLECTION_ONLY | ❌ | ✅ | Partial | Minimal |
| METHOD_ACCESSOR | ❌ | ✅ | ✅ | Low |
| FULL_INTERCEPTION | ✅ | ✅ | ✅ | Moderate |
| NATIVE_ENTRY_POINT | ✅ | ✅ | ✅ | Higher |

### Exception Interception

```java
RuntimeMethodHooker.MethodInterceptor exceptionHandler = new RuntimeMethodHooker.MethodInterceptor() {
    @Override
    public RuntimeMethodHooker.InterceptionAction onException(Method method, Object instance, Object[] args, Throwable exception) {
        if (exception instanceof RuntimeException) {
            // Suppress runtime exceptions and return default value
            return RuntimeMethodHooker.InterceptionAction.SKIP_WITH_RESULT;
        }
        return RuntimeMethodHooker.InterceptionAction.PROCEED;
    }
    
    // ... other methods
};
```

### Performance Monitoring

```java
// Get interception statistics
MethodInterceptor.InterceptionStats stats = MethodInterceptor.getInterceptionStats();
System.out.println("Total interceptions: " + stats.totalInterceptions);
System.out.println("Total invocations: " + stats.totalInvocations);

// Get hook statistics  
RuntimeMethodHooker.HookStats hookStats = RuntimeMethodHooker.getHookStats();
System.out.println("Native entry point hooks: " + hookStats.nativeEntryPointHooks);
System.out.println("Total interceptions: " + hookStats.totalInterceptions);
```

## Performance Characteristics

### Interception Overhead

Based on benchmarks from the demo:

- **Reflection-only hooks**: 5-15% overhead on Method.invoke()
- **Method accessor hooks**: 10-25% overhead on reflection calls
- **Full interception hooks**: 15-50% overhead depending on call type
- **Native entry point hooks**: 25-75% overhead but comprehensive coverage

### Memory Impact

- **Per-hook overhead**: ~200-500 bytes per intercepted method
- **Registry overhead**: Concurrent hash maps for hook tracking
- **Trampoline memory**: Additional native memory for entry point hooks

### Optimization Recommendations

1. **Use appropriate hook level**: Don't use NATIVE_ENTRY_POINT if REFLECTION_ONLY suffices
2. **Minimize interceptor work**: Keep before/after processing lightweight
3. **Batch hook operations**: Install/remove hooks in groups when possible
4. **Monitor performance impact**: Use built-in statistics to track overhead

## Advanced Features

### Method Result Modification

```java
@Override
public Object afterInvocation(Method method, Object instance, Object[] args, Object result) {
    if (method.getName().equals("computeValue") && result instanceof Integer) {
        // Add security audit trail
        auditLog.record(method, args, result);
        
        // Apply business rule modification
        return applyBusinessRules((Integer) result);
    }
    return result;
}
```

### Conditional Interception

```java
@Override
public InterceptionResult beforeInvocation(Method method, Object instance, Object[] args) {
    // Only intercept calls from specific classes
    String callerClass = Thread.currentThread().getStackTrace()[3].getClassName();
    if (callerClass.startsWith("com.untrusted")) {
        return InterceptionResult.PROCEED;
    }
    return InterceptionResult.SKIP_EXECUTION; // Skip for trusted callers
}
```

### Dynamic Hook Management

```java
// Get all currently hooked methods
List<Method> hookedMethods = RuntimeMethodHooker.getHookedMethods();

// Check if specific method is hooked
boolean isHooked = RuntimeMethodHooker.isMethodHooked(someMethod);

// Remove all hooks (cleanup)
RuntimeMethodHooker.removeAllHooks();
```

## Security Considerations

### Privileged Access Requirements
- **ModuleBootstrap access**: Requires privileged internal JVM access
- **Reflection permissions**: Needs deep reflection access to Method internals
- **Memory manipulation**: Uses Unsafe for low-level operations

### Safety Measures
- **Bounds checking**: All memory operations are bounds-checked
- **State validation**: Hook state is validated before operations
- **Cleanup on failure**: Failed installations are automatically cleaned up
- **Thread safety**: All operations are thread-safe

### Usage Restrictions
- **Trusted code only**: Should only be used in trusted environments
- **No sandboxing**: Cannot be used in sandboxed environments
- **JVM specific**: Implementation tied to HotSpot JVM internals

## Troubleshooting

### Common Issues

**"MethodInterceptor not available"**
- Ensure ModuleBootstrap is properly initialized
- Check that privileged access is available
- Verify JDK version compatibility (requires JDK 11+)

**"Method already intercepted"**
- Each method can only have one active interception
- Remove existing interception before installing new one
- Use hook management APIs to check current state

**"Failed to install method hook"**
- Method may be final or native (limited hookability)
- JIT compilation may interfere with some hook types
- Check method accessibility and visibility

### Performance Issues

**High overhead with NATIVE_ENTRY_POINT hooks**
- Consider using lower hook levels for non-critical methods
- Profile the application to identify bottlenecks
- Use conditional interception to reduce scope

**Memory leaks with hooks**
- Always clean up hooks when done
- Use try-finally or try-with-resources patterns
- Monitor hook statistics for unexpected growth

## Limitations

### JVM Compatibility
- **JDK Version**: Requires JDK 11+ (tested on OpenJDK 11)
- **JVM Type**: Optimized for HotSpot JVM
- **Architecture**: x86-64 focus (limited testing on other architectures)

### Method Types
- **Native methods**: Limited interception capabilities
- **JVM intrinsics**: Cannot intercept intrinsic methods
- **Generated methods**: Synthetic methods may have limited support
- **Lambda methods**: Dynamic lambda methods have restricted hooking

### Call Types
- **Inlined calls**: JIT-inlined calls may bypass some hook levels
- **Optimized calls**: Highly optimized code paths may avoid interception
- **Unsafe calls**: Direct Unsafe-based method calls cannot be intercepted

## Future Enhancements

Potential areas for enhancement:

1. **JIT Integration** - Deeper integration with JIT compiler for inlined call interception
2. **Lambda Support** - Enhanced support for dynamic method handles and lambdas
3. **Performance Optimization** - Reduced overhead through assembly-level optimizations
4. **Cross-JVM Support** - Support for alternative JVM implementations
5. **Debugging Integration** - Integration with debugging and profiling tools
6. **Aspect-Oriented Programming** - Higher-level AOP constructs and annotations
7. **Distributed Tracing** - Built-in support for distributed tracing frameworks

## Implementation Notes

This implementation leverages the privileged access framework established by ModuleBootstrap to gain deep access to JVM internals. The approach is inspired by analysis of:

- HotSpot Method class structure (oops/method.hpp, oops/method.cpp)
- Java Method class implementation
- MethodAccessor interface and implementations
- MethodHandle internals and invocation mechanisms
- JIT compilation and method entry point management

The code is designed to be educational and demonstrates advanced JVM internals manipulation while maintaining safety and providing comprehensive functionality.