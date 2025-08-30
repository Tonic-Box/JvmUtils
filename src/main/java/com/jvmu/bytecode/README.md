# Bytecode Manipulation API

Runtime bytecode generation, manipulation, and class transformation capabilities.

## Overview

The Bytecode API provides comprehensive facilities for runtime bytecode manipulation, class generation, and dynamic code transformation. Built on the privileged access framework, this package enables powerful bytecode operations that go beyond standard Java capabilities.

## Core Components

### BytecodeAPI
Main interface for bytecode operations:
```java
// Generate new classes at runtime
Class<?> newClass = BytecodeAPI.generateClass(
    "com.example.DynamicClass",
    classBuilder -> {
        classBuilder.addField("value", int.class);
        classBuilder.addMethod("getValue", int.class, methodBuilder -> {
            methodBuilder.returnField("value");
        });
    }
);

// Transform existing classes
BytecodeAPI.transformClass(ExistingClass.class, transformer -> {
    transformer.addLogging();
    transformer.addPerformanceMonitoring();
    transformer.modifyMethod("compute", methodTransformer -> {
        methodTransformer.addTimingCode();
    });
});
```

### BytecodeManipulator
Low-level bytecode manipulation utilities:
```java
// Direct bytecode modification
byte[] originalBytecode = getClassBytecode(MyClass.class);
byte[] modifiedBytecode = BytecodeManipulator.modifyBytecode(
    originalBytecode,
    manipulator -> {
        manipulator.insertBefore("methodName", "System.out.println(\"Before\");");
        manipulator.insertAfter("methodName", "System.out.println(\"After\");");
        manipulator.replaceMethod("oldMethod", newMethodBytecode);
    }
);

// Apply modifications
BytecodeManipulator.redefineClass(MyClass.class, modifiedBytecode);
```

### ArbitraryBytecodeGenerator
Advanced bytecode generation for complex scenarios:
```java
// Generate complex classes with arbitrary bytecode
Class<?> proxyClass = ArbitraryBytecodeGenerator.generateProxy(
    targetInterface,
    handler,
    options -> {
        options.addInterceptors(interceptors);
        options.enablePerformanceOptimizations();
        options.addDebugInformation();
    }
);

// Generate specialized implementations
Class<?> optimizedClass = ArbitraryBytecodeGenerator.generateOptimized(
    baseClass,
    optimizer -> {
        optimizer.inlineFrequentMethods();
        optimizer.eliminateDeadCode();
        optimizer.optimizeFieldAccess();
    }
);
```

## Key Features

### Runtime Class Generation
Create new classes dynamically with full control:
```java
public class DynamicClassExample {
    public static Class<?> createCalculator() {
        return BytecodeAPI.generateClass("Calculator", builder -> {
            // Add fields
            builder.addField("result", double.class, Modifier.PRIVATE);
            
            // Add constructor
            builder.addConstructor(constructor -> {
                constructor.addParameter("initialValue", double.class);
                constructor.setBody("this.result = initialValue;");
            });
            
            // Add methods
            builder.addMethod("add", double.class, method -> {
                method.addParameter("value", double.class);
                method.setReturnType(double.class);
                method.setBody("this.result += value; return this.result;");
            });
            
            builder.addMethod("getResult", double.class, method -> {
                method.setBody("return this.result;");
            });
        });
    }
    
    public static void useCalculator() throws Exception {
        Class<?> calcClass = createCalculator();
        Object calculator = calcClass
            .getConstructor(double.class)
            .newInstance(10.0);
        
        Method add = calcClass.getMethod("add", double.class);
        Method getResult = calcClass.getMethod("getResult");
        
        add.invoke(calculator, 5.0);
        double result = (Double) getResult.invoke(calculator);
        System.out.println("Result: " + result); // 15.0
    }
}
```

### Method Transformation
Modify existing methods without changing source code:
```java
public class MethodTransformation {
    public static void addLoggingToClass(Class<?> targetClass) {
        BytecodeAPI.transformClass(targetClass, transformer -> {
            // Add logging to all public methods
            transformer.forEachPublicMethod(method -> {
                transformer.insertBefore(method.getName(), 
                    "System.out.println(\"Entering: " + method.getName() + "\");");
                transformer.insertAfter(method.getName(), 
                    "System.out.println(\"Exiting: " + method.getName() + "\");");
            });
        });
    }
    
    public static void addPerformanceMonitoring(Class<?> targetClass) {
        BytecodeAPI.transformClass(targetClass, transformer -> {
            transformer.forEachMethod(method -> {
                if (method.isAnnotatedWith("@Performance")) {
                    transformer.wrapMethod(method.getName(), 
                        "long startTime = System.nanoTime();",
                        "long endTime = System.nanoTime(); " +
                        "System.out.println(\"" + method.getName() + 
                        " took: \" + (endTime - startTime) + \"ns\");");
                }
            });
        });
    }
}
```

### Proxy Generation
Create dynamic proxies with advanced capabilities:
```java
public class AdvancedProxyExample {
    public static <T> T createInstrumentedProxy(T target, Class<T> interfaceClass) {
        Class<?> proxyClass = ArbitraryBytecodeGenerator.generateProxy(
            interfaceClass,
            new InstrumentationHandler(target),
            options -> {
                options.enableMethodInterception();
                options.addPerformanceMetrics();
                options.enableExceptionHandling();
            }
        );
        
        try {
            return interfaceClass.cast(
                proxyClass.getConstructor(Object.class).newInstance(target)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to create proxy", e);
        }
    }
    
    private static class InstrumentationHandler {
        private final Object target;
        private final Map<String, Long> callCounts = new ConcurrentHashMap<>();
        
        public InstrumentationHandler(Object target) {
            this.target = target;
        }
        
        public Object handleMethodCall(String methodName, Object[] args) throws Exception {
            // Count method calls
            callCounts.merge(methodName, 1L, Long::sum);
            
            // Time method execution
            long start = System.nanoTime();
            try {
                Method method = target.getClass().getMethod(methodName, 
                    Arrays.stream(args).map(Object::getClass).toArray(Class[]::new));
                return method.invoke(target, args);
            } finally {
                long duration = System.nanoTime() - start;
                System.out.println(methodName + " executed in " + duration + "ns");
            }
        }
        
        public Map<String, Long> getCallCounts() {
            return new HashMap<>(callCounts);
        }
    }
}
```

### Bytecode Optimization
Optimize existing classes for better performance:
```java
public class BytecodeOptimization {
    public static void optimizeForPerformance(Class<?> targetClass) {
        BytecodeManipulator.optimizeClass(targetClass, optimizer -> {
            // Inline small methods
            optimizer.inlineMethodsSmaller(50); // Inline methods < 50 bytecodes
            
            // Eliminate dead code
            optimizer.eliminateUnreachableCode();
            
            // Optimize field access
            optimizer.replaceFieldAccessWithDirectAccess();
            
            // Remove unnecessary checks
            optimizer.eliminateRedundantNullChecks();
            optimizer.eliminateRedundantBoundsChecks();
            
            // Optimize loops
            optimizer.unrollSmallLoops(10); // Unroll loops with < 10 iterations
        });
    }
    
    public static void addDebugInstrumentation(Class<?> targetClass) {
        BytecodeAPI.transformClass(targetClass, transformer -> {
            // Add debug information
            transformer.addLineNumbers();
            transformer.addLocalVariableInfo();
            
            // Add debugging hooks
            transformer.forEachMethod(method -> {
                if (method.hasAnnotation("@Debug")) {
                    transformer.insertDebugPrint(method.getName(), 
                        "Method parameters", "args");
                    transformer.insertDebugPrint(method.getName(), 
                        "Method result", "result");
                }
            });
        });
    }
}
```

## Advanced Features

### Custom Class Loaders
Integration with custom class loading strategies:
```java
public class DynamicClassLoader extends ClassLoader {
    public Class<?> defineClassFromBytecode(String name, byte[] bytecode) {
        return defineClass(name, bytecode, 0, bytecode.length);
    }
    
    public Class<?> generateAndDefineClass(String name, ClassGenerator generator) {
        byte[] bytecode = BytecodeAPI.generateBytecode(name, generator);
        return defineClassFromBytecode(name, bytecode);
    }
}
```

### Hot Code Replacement
Replace methods in running applications:
```java
public class HotReplacement {
    public static void replaceMethod(Class<?> targetClass, String methodName, 
                                   String newMethodBody) {
        byte[] newBytecode = BytecodeManipulator.replaceMethodBody(
            targetClass, methodName, newMethodBody);
        
        // Use JVMTI or internal APIs for hot replacement
        BytecodeAPI.hotReplaceClass(targetClass, newBytecode);
    }
    
    public static void addNewMethod(Class<?> targetClass, String methodName,
                                   String methodSignature, String methodBody) {
        byte[] modifiedBytecode = BytecodeManipulator.addMethod(
            targetClass, methodName, methodSignature, methodBody);
        
        BytecodeAPI.hotReplaceClass(targetClass, modifiedBytecode);
    }
}
```

### Annotation Processing
Dynamic annotation manipulation:
```java
public class AnnotationManipulation {
    public static void addRuntimeAnnotation(Class<?> targetClass, 
                                           Class<? extends Annotation> annotationType,
                                           Map<String, Object> annotationValues) {
        BytecodeAPI.transformClass(targetClass, transformer -> {
            transformer.addClassAnnotation(annotationType, annotationValues);
        });
    }
    
    public static void modifyMethodAnnotations(Class<?> targetClass, String methodName) {
        BytecodeAPI.transformClass(targetClass, transformer -> {
            transformer.modifyMethodAnnotation(methodName, "@Deprecated", 
                Map.of("since", "2.0", "forRemoval", true));
        });
    }
}
```

## Integration with JVM Internals

### Unsafe Integration
Leverage internal Unsafe for advanced operations:
```java
public class UnsafeBytecodeOperations {
    public static void modifyClassConstantPool(Class<?> clazz, int cpIndex, Object newValue) {
        // Access constant pool via Unsafe
        Object constantPool = InternalUnsafe.getConstantPool(clazz);
        
        // Modify constant pool entry
        BytecodeManipulator.modifyConstantPoolEntry(constantPool, cpIndex, newValue);
    }
    
    public static void directMethodModification(Method method, byte[] newBytecode) {
        // Direct method bytecode replacement via Unsafe
        long methodAddress = InternalUnsafe.getMethodAddress(method);
        BytecodeManipulator.replaceMethodBytecode(methodAddress, newBytecode);
    }
}
```

### JIT Compiler Integration
Coordinate with JIT compilation:
```java
public class JITIntegration {
    public static void optimizeAfterTransformation(Class<?> transformedClass) {
        // Force recompilation of transformed methods
        for (Method method : transformedClass.getDeclaredMethods()) {
            JITCompilerAccess.clearMethodData(method);
            JITCompilerAccess.compileMethod(method, 
                JITCompilerAccess.COMP_LEVEL_FULL_OPTIMIZATION);
        }
    }
    
    public static void preventInlining(Method method) {
        // Mark method to prevent inlining during transformation
        JITCompilerAccess.setDontInline(method, true);
        
        // Apply transformation
        BytecodeAPI.transformMethod(method, transformer -> {
            transformer.addDebugInstrumentation();
        });
    }
}
```

## Performance Considerations

### Bytecode Generation Performance
- **Simple classes**: ~1-10ms generation time
- **Complex classes**: ~10-100ms generation time
- **Method transformation**: ~0.1-1ms per method
- **Hot replacement**: ~1-10ms depending on class size

### Runtime Performance Impact
- **Generated code performance**: Near-native Java performance
- **Transformation overhead**: Minimal if properly optimized
- **Proxy call overhead**: ~10-50ns per intercepted call
- **Memory usage**: ~1-5KB per generated class

### Optimization Strategies
```java
// Cache generated classes
private static final Map<String, Class<?>> classCache = new ConcurrentHashMap<>();

public static Class<?> getCachedOrGenerate(String className, ClassGenerator generator) {
    return classCache.computeIfAbsent(className, 
        name -> BytecodeAPI.generateClass(name, generator));
}

// Use method handles for better performance
private static final Map<Method, MethodHandle> handleCache = new ConcurrentHashMap<>();

public static Object invokeFast(Object target, Method method, Object... args) throws Throwable {
    MethodHandle handle = handleCache.computeIfAbsent(method, m -> {
        try {
            return ModuleBootstrap.getTrustedLookup().unreflect(m);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    });
    
    return handle.invokeWithArguments(ObjectArrays.concat(target, args));
}
```

## Security and Safety

### Validation and Safety Checks
```java
public class BytecodeSafety {
    public static boolean validateBytecode(byte[] bytecode) {
        // Validate bytecode structure
        // Check for malicious patterns
        // Verify stack map frames
        // Validate constant pool references
        return BytecodeAPI.isValidBytecode(bytecode);
    }
    
    public static byte[] sanitizeBytecode(byte[] bytecode) {
        return BytecodeManipulator.sanitize(bytecode, sanitizer -> {
            sanitizer.removeSystemCalls();
            sanitizer.limitStackDepth(1000);
            sanitizer.validateReferences();
        });
    }
}
```

### Privilege Management
```java
// Require specific permissions for bytecode operations
public class PrivilegeCheck {
    public static void requireBytecodePermission() {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("modifyBytecode"));
        }
    }
}
```

## Limitations and Considerations

### JVM Version Compatibility
- Bytecode version compatibility across JDK releases
- New bytecode instructions in newer JVM versions
- Verification algorithm changes

### Security Restrictions
- Security manager policies may block operations
- Some environments disable bytecode modification
- Code signing may prevent runtime changes

### Performance Impact
- Class loading overhead for generated classes
- JIT compilation delays for new bytecode
- Memory usage for additional class metadata

## Best Practices

### Code Generation
- Validate generated bytecode before loading
- Use meaningful class and method names
- Include proper debugging information
- Cache generated classes when possible

### Method Transformation
- Test transformations thoroughly
- Handle edge cases and exception paths
- Preserve original semantics when possible
- Document transformation logic

### Performance
- Profile before and after transformations
- Use method handles for frequent calls
- Consider JIT compiler implications
- Monitor memory usage of generated classes

## Future Enhancements

Potential improvements:
1. **Ahead-of-Time Generation** - Generate optimized bytecode at build time
2. **Template System** - Template-based code generation
3. **Visual Debugger** - GUI for bytecode manipulation
4. **Performance Profiler** - Built-in performance analysis
5. **Multi-Language Support** - Generate bytecode from other JVM languages

## Related Components

This API integrates with other JvmUtils components:
- **ModuleBootstrap** - Provides privileged access for bytecode operations
- **InternalUnsafe** - Used for low-level bytecode manipulation
- **JITCompilerAccess** - Coordinates with JIT compilation
- **ReflectBuilder** - Used for accessing generated class members