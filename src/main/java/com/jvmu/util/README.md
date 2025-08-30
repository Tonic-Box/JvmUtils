# Utility Classes

Core utility classes providing reflection, classpath management, and code generation capabilities.

## Overview

The `util` package contains foundational utility classes used throughout JvmUtils for reflection operations, class loading, and dynamic code generation. These utilities leverage the privileged access provided by ModuleBootstrap to offer enhanced capabilities beyond standard Java APIs.

## Core Components

### ReflectBuilder
A fluent builder for complex reflection operations with privileged access:

```java
// Access internal Unsafe
Object result = ReflectBuilder.unsafe()
    .method("allocateMemory", long.class, 1024L)
    .get();

// Chain method calls and field access
String result = ReflectBuilder.of(someObject)
    .field("internalField")
    .method("toString")
    .get();

// Load and access internal classes
Class<?> internalClass = ReflectBuilder.lookupClass("jdk.internal.misc.VM");
```

**Key Features:**
- Fluent API for complex reflection chains
- Privileged access to internal classes and methods
- Automatic error handling and fallbacks
- Support for static and instance operations
- Type-safe method and field access

### ReflectUtil
Utility methods for common reflection operations:

```java
// Enhanced field access
Field field = ReflectUtil.getField(MyClass.class, "privateField");
Object value = ReflectUtil.getFieldValue(instance, field);
ReflectUtil.setFieldValue(instance, field, newValue);

// Method discovery and invocation
Method method = ReflectUtil.findMethod(MyClass.class, "methodName", paramTypes);
Object result = ReflectUtil.invokeMethod(instance, method, args);

// Class analysis
boolean hasField = ReflectUtil.hasField(MyClass.class, "fieldName");
boolean hasMethod = ReflectUtil.hasMethod(MyClass.class, "methodName", paramTypes);
```

**Key Features:**
- Simplified reflection operations
- Automatic accessibility handling
- Error resilient field and method access
- Class introspection utilities
- Type conversion helpers

### ClassPath
Classpath management and analysis utilities:

```java
// Get current classpath
List<String> classPath = ClassPath.getClassPath();

// Find classes in packages
Set<Class<?>> classes = ClassPath.findClasses("com.example.package");

// Resource discovery
URL resource = ClassPath.findResource("config/settings.properties");
List<URL> resources = ClassPath.findResources("META-INF/services");
```

**Key Features:**
- Dynamic classpath analysis
- Package scanning and class discovery  
- Resource location and enumeration
- Classpath modification capabilities
- JAR and directory handling

### ASMTestClassGenerator
Dynamic class generation using ASM for testing and runtime scenarios:

```java
// Generate test classes at runtime
Class<?> testClass = ASMTestClassGenerator.generateTestClass(
    "TestClass", 
    interfacesToImplement,
    fieldsToAdd,
    methodsToAdd
);

// Create proxy classes
Class<?> proxyClass = ASMTestClassGenerator.createProxy(
    targetInterface,
    customBehaviors
);
```

**Key Features:**
- Runtime class generation using ASM
- Test class creation for dynamic scenarios
- Interface implementation generation
- Custom method and field injection
- Bytecode manipulation capabilities

## Reflection Sub-Package

The `reflection` sub-package contains the core reflection framework classes:

### Element (Base Class)
Abstract base for all reflection operations:
- Common functionality for all reflection elements
- Error handling and validation
- Chain management for fluent operations

### FieldElement
Specialized handling for field access:
```java
FieldElement fieldElement = new FieldElement(targetObject, fieldName);
Object value = fieldElement.get();
fieldElement.set(newValue);
```

### MethodElement
Specialized handling for method invocation:
```java
MethodElement methodElement = new MethodElement(targetObject, methodName, paramTypes);
Object result = methodElement.invoke(arguments);
```

## Usage Examples

### Complex Reflection Chains
```java
public class ReflectionExample {
    public static void complexAccess() {
        // Access internal JVM data through multiple levels
        String vmInfo = ReflectBuilder.ofClass("jdk.internal.misc.VM")
            .staticMethod("getSavedProperties")
            .method("getProperty", String.class, "java.vm.name")
            .get();
        
        System.out.println("VM Name: " + vmInfo);
    }
    
    public static void unsafeOperations() {
        // Direct Unsafe access
        long address = ReflectBuilder.unsafe()
            .method("allocateMemory", long.class, 1024L)
            .get();
        
        // Use allocated memory
        ReflectBuilder.unsafe()
            .method("putLong", long.class, byte.class, address, 0x1234567890ABCDEFL)
            .execute();
        
        // Read back
        Long value = ReflectBuilder.unsafe()
            .method("getLong", long.class, address)
            .get();
        
        // Free memory
        ReflectBuilder.unsafe()
            .method("freeMemory", long.class, address)
            .execute();
    }
}
```

### Dynamic Class Loading and Analysis
```java
public class ClassAnalysisExample {
    public static void analyzePackage(String packageName) {
        // Find all classes in package
        Set<Class<?>> classes = ClassPath.findClasses(packageName);
        
        for (Class<?> clazz : classes) {
            System.out.println("Class: " + clazz.getName());
            
            // Analyze fields
            if (ReflectUtil.hasField(clazz, "serialVersionUID")) {
                Field field = ReflectUtil.getField(clazz, "serialVersionUID");
                Object value = ReflectUtil.getFieldValue(null, field);
                System.out.println("  Serial UID: " + value);
            }
            
            // Analyze methods
            Method[] methods = clazz.getDeclaredMethods();
            System.out.println("  Methods: " + methods.length);
        }
    }
}
```

### Runtime Class Generation
```java
public class DynamicClassExample {
    public static Class<?> createTestInterface() {
        // Generate a test class implementing multiple interfaces
        return ASMTestClassGenerator.generateTestClass(
            "DynamicTestClass",
            new Class<?>[] { Runnable.class, Comparable.class },
            new String[] { "testField" },
            new String[] { "testMethod" }
        );
    }
    
    public static void useGeneratedClass() throws Exception {
        Class<?> dynamicClass = createTestInterface();
        Object instance = dynamicClass.getDeclaredConstructor().newInstance();
        
        // Use the generated class
        if (instance instanceof Runnable) {
            ((Runnable) instance).run();
        }
    }
}
```

## Integration with ModuleBootstrap

All utility classes leverage ModuleBootstrap's privileged access:

```java
// ReflectBuilder automatically uses privileged access
public static ReflectBuilder ofClass(String classFqdn) {
    Class<?> clazz = ModuleBootstrap.class
        .getClassLoader()
        .loadClass(classFqdn);
    return new ReflectBuilder(clazz);
}

// Direct Unsafe access
public static ReflectBuilder unsafe() {
    return new ReflectBuilder(ModuleBootstrap.getInternalUnsafe());
}
```

## Error Handling and Safety

### Exception Management
```java
// Graceful handling of reflection failures
try {
    Object result = ReflectBuilder.of(target)
        .field("mayNotExist")
        .get();
} catch (RuntimeException e) {
    // Handle reflection failure
    System.err.println("Reflection failed: " + e.getMessage());
}
```

### Validation and Safety Checks
```java
// Always validate before access
if (ReflectUtil.hasMethod(clazz, "methodName", paramTypes)) {
    Method method = ReflectUtil.findMethod(clazz, "methodName", paramTypes);
    Object result = ReflectUtil.invokeMethod(instance, method, args);
}
```

### Memory Safety
```java
// For Unsafe operations, always validate addresses
long address = ReflectBuilder.unsafe()
    .method("allocateMemory", long.class, size)
    .get();

if (address != 0) {
    try {
        // Use memory...
    } finally {
        // Always free memory
        ReflectBuilder.unsafe()
            .method("freeMemory", long.class, address)
            .execute();
    }
}
```

## Performance Considerations

### Reflection Optimization
- ReflectBuilder caches method handles when possible
- Field access is optimized for repeated operations
- Method lookup uses efficient caching strategies

### Class Loading Performance
- ClassPath utilities minimize class loading overhead
- Package scanning is optimized for large classpaths
- Resource discovery uses efficient lookup algorithms

### Code Generation Performance
- ASM generation is optimized for minimal overhead
- Generated classes use efficient bytecode patterns
- Runtime generation is cached when appropriate

## Limitations

### Security Restrictions
- Requires ModuleBootstrap initialization
- May not work in restricted security environments
- Some operations require specific JVM permissions

### JVM Compatibility
- Optimized for HotSpot JVM
- Some features may not work on alternative JVMs
- Version-specific internal API dependencies

### Performance Trade-offs
- Reflection operations have inherent overhead
- Dynamic class generation has initialization cost
- Complex reflection chains can impact performance

## Best Practices

### Reflection Usage
- Cache ReflectBuilder instances when possible
- Validate class and member existence before access
- Use appropriate error handling for reflection failures
- Prefer direct access when reflection isn't necessary

### Class Generation
- Generate classes once and reuse instances
- Validate generated bytecode in development
- Consider memory usage of generated classes
- Use appropriate class loaders for isolation

### Resource Management
- Always free allocated native memory
- Close resources properly in error conditions
- Monitor memory usage in long-running applications

## Related Components

These utilities support other JvmUtils packages:
- **ModuleBootstrap** - Provides privileged access foundation
- **InternalUnsafe** - Uses ReflectBuilder for method access
- **SharedSecrets** - Uses reflection utilities for wrapper creation
- **DirectMemoryManager** - Uses utilities for memory operations
- **JITCompilerAccess** - Uses reflection for compiler control