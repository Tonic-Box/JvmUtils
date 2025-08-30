# SharedSecrets Access Wrappers

Instance-based wrapper classes for JDK internal SharedSecrets access interfaces.

## Overview

The SharedSecrets sub-package provides typed wrapper classes for all major JDK internal access interfaces accessed through `jdk.internal.misc.SharedSecrets`. These wrappers replace the generic `Object` returns from SharedSecrets with strongly-typed, feature-rich wrapper objects.

## Design Pattern

All wrapper classes follow a consistent design:

```java
public class AccessWrapper {
    private final Object nativeAccess;
    private final boolean available;
    
    public AccessWrapper(Object nativeAccess) {
        this.nativeAccess = nativeAccess;
        this.available = nativeAccess != null;
    }
    
    public boolean isAvailable() {
        return available;
    }
    
    // Wrapped methods using ReflectBuilder
}
```

## Available Wrappers

### JavaLangAccess
Provides access to `java.lang` package internals:

```java
JavaLangAccess langAccess = SharedSecrets.getJavaLangAccess();
if (langAccess.isAvailable()) {
    // Get constant pool
    ConstantPool cp = langAccess.getConstantPool(MyClass.class);
    
    // Access annotation data
    byte[] rawAnnotations = langAccess.getRawClassAnnotations(MyClass.class);
    
    // Define classes
    Class<?> newClass = langAccess.defineClass(classLoader, name, bytecode, protectionDomain, source);
}
```

**Key Capabilities:**
- Constant pool access
- Raw annotation data retrieval
- Class definition and loading
- Module system operations
- String encoding/decoding optimizations
- Enum constants access
- Thread and shutdown hook management

### JavaNioAccess
Manages NIO operations and direct buffer access:

```java
JavaNioAccess nioAccess = SharedSecrets.getJavaNioAccess();
if (nioAccess.isAvailable()) {
    // Direct buffer operations
    ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    long address = nioAccess.getDirectBufferAddress(buffer);
    
    // Buffer pool access
    JavaNioAccess.BufferPool pool = nioAccess.getDirectBufferPool();
    long poolSize = pool.getTotalCapacity();
}
```

**Key Capabilities:**
- DirectBuffer address extraction
- Buffer pool management and statistics
- Channel and selector operations
- Memory mapping support
- Buffer cleanup and resource management

### JavaIOAccess
Handles I/O operations and console access:

```java
JavaIOAccess ioAccess = SharedSecrets.getJavaIOAccess();
if (ioAccess.isAvailable()) {
    // Console access
    Console console = ioAccess.console();
    
    // Charset operations
    Charset charset = ioAccess.jnuEncoding();
}
```

**Key Capabilities:**
- Console operations
- Character encoding management
- I/O stream optimizations

### JavaUtilJarAccess
Manages JAR file operations:

```java
JavaUtilJarAccess jarAccess = SharedSecrets.getJavaUtilJarAccess();
if (jarAccess.isAvailable()) {
    // JAR operations
    boolean hasClassPathAttribute = jarAccess.jarFileHasClassPathAttribute(jarFile);
    
    // Code source access
    CodeSource[] codeSources = jarAccess.getCodeSources(jarFile, url);
}
```

**Key Capabilities:**
- JAR file analysis and manipulation
- Code source management
- ClassPath attribute handling
- Manifest processing

### JavaSecurityAccess
Provides security-related operations:

```java
JavaSecurityAccess securityAccess = SharedSecrets.getJavaSecurityAccess();
if (securityAccess.isAvailable()) {
    // Security operations
    Object[] contextArray = securityAccess.getContext(accessControlContext);
    
    // Protection domain operations
    ProtectionDomain[] domains = securityAccess.getProtectDomains(accessControlContext);
}
```

**Key Capabilities:**
- AccessControlContext manipulation
- ProtectionDomain operations
- Security policy access
- Permission checking optimizations

### GenericAccess
Dynamic wrapper for any access interface:

```java
// Can wrap any access interface dynamically
Object someAccess = SharedSecrets.getJavaUtilCollectionAccess();
GenericAccess generic = new GenericAccess(someAccess, "JavaUtilCollectionAccess");

if (generic.isAvailable()) {
    // Dynamic method invocation
    Object result = generic.invoke("someMethod", String.class, "parameter");
    
    // Check available methods
    String[] methods = generic.getMethodNames();
    boolean hasMethod = generic.hasMethod("specificMethod");
}
```

**Key Capabilities:**
- Dynamic method discovery and invocation
- Generic parameter handling
- Method existence checking
- Comprehensive access information
- Fallback for unimplemented specific wrappers

## Usage Examples

### Cross-Package JDK Access
```java
public class JDKInternalsExample {
    public static void analyzeClass(Class<?> clazz) {
        JavaLangAccess langAccess = SharedSecrets.getJavaLangAccess();
        if (langAccess.isAvailable()) {
            // Get raw annotation data
            byte[] annotations = langAccess.getRawClassAnnotations(clazz);
            System.out.println("Raw annotations size: " + 
                (annotations != null ? annotations.length : 0));
            
            // Access constant pool
            ConstantPool cp = langAccess.getConstantPool(clazz);
            if (cp != null) {
                System.out.println("Constant pool size: " + cp.getSize());
            }
        }
    }
    
    public static void analyzeDirectBuffer(ByteBuffer buffer) {
        JavaNioAccess nioAccess = SharedSecrets.getJavaNioAccess();
        if (nioAccess.isAvailable() && buffer.isDirect()) {
            long address = nioAccess.getDirectBufferAddress(buffer);
            System.out.println("Direct buffer address: 0x" + Long.toHexString(address));
            
            // Check buffer pool statistics
            JavaNioAccess.BufferPool pool = nioAccess.getDirectBufferPool();
            System.out.println("Pool capacity: " + pool.getTotalCapacity());
            System.out.println("Pool used: " + pool.getMemoryUsed());
        }
    }
}
```

### Custom Class Loading
```java
public class CustomClassLoader {
    public static Class<?> defineClass(ClassLoader loader, String name, byte[] bytecode) {
        JavaLangAccess langAccess = SharedSecrets.getJavaLangAccess();
        if (langAccess.isAvailable()) {
            return langAccess.defineClass(loader, name, bytecode, null, "CustomClassLoader");
        } else {
            // Fallback to standard defineClass via reflection
            // ... standard reflection approach
            return null;
        }
    }
}
```

### Security Context Analysis
```java
public class SecurityAnalyzer {
    public static void analyzeSecurityContext(AccessControlContext context) {
        JavaSecurityAccess securityAccess = SharedSecrets.getJavaSecurityAccess();
        if (securityAccess.isAvailable()) {
            ProtectionDomain[] domains = securityAccess.getProtectDomains(context);
            System.out.println("Protection domains: " + domains.length);
            
            for (ProtectionDomain domain : domains) {
                System.out.println("  Domain: " + domain.getCodeSource());
            }
        }
    }
}
```

## Integration with Main SharedSecrets Class

The main `SharedSecrets` class in the parent package returns these typed wrappers:

```java
// Instead of returning Object (generic)
public static Object getJavaNioAccess() { ... }

// Now returns typed wrapper
public static com.jvmu.internals.sharedsecrets.JavaNioAccess getJavaNioAccess() {
    Object nativeAccess = // get from JDK SharedSecrets
    return nativeAccess != null ? new JavaNioAccess(nativeAccess) : null;
}
```

## Error Handling and Safety

### Availability Checking
Always check availability before use:
```java
JavaLangAccess access = SharedSecrets.getJavaLangAccess();
if (access != null && access.isAvailable()) {
    // Safe to use
} else {
    // Fallback approach or error handling
}
```

### Exception Handling
Wrapper methods handle exceptions gracefully:
```java
// Methods return null on error rather than throwing exceptions
ConstantPool cp = langAccess.getConstantPool(clazz);
if (cp != null) {
    // Successfully obtained constant pool
}
```

### Validation
Built-in validation for common error conditions:
```java
// Automatic null checking and bounds validation
byte[] annotations = langAccess.getRawClassAnnotations(clazz);
// Returns null if clazz is null or access fails
```

## Performance Considerations

### Overhead
- Minimal wrapper overhead (~1-5 ns per call)
- ReflectBuilder caching reduces reflection overhead
- Instance-based design avoids static lookup costs

### Memory Usage
- Lightweight wrapper objects
- No caching of large data structures
- Native access objects are shared references

### Optimization Tips
- Cache wrapper instances when possible
- Check availability once per session
- Use batch operations when available

## Version Compatibility

### JDK Version Support
- Designed for JDK 11+
- Graceful degradation for missing interfaces
- Version-specific feature detection

### Internal API Changes
- Wrapper design isolates from internal API changes
- ReflectBuilder provides resilience to method signature changes
- Fallback mechanisms for removed functionality

## Limitations

### Access Restrictions
- Requires ModuleBootstrap initialization
- May not work in restricted security environments
- Some operations require specific JVM flags

### Platform Dependencies
- Some features are JVM implementation specific
- Platform-specific behavior in native operations
- Architecture dependencies for memory operations

## Future Enhancements

Potential improvements:
1. **Additional Wrappers** - More JDK internal interfaces
2. **Caching Layer** - Method handle caching for performance
3. **Event System** - Notifications for access failures
4. **Metrics Collection** - Usage statistics and performance data
5. **Alternative Access Methods** - Multiple access strategies per interface

## Related Components

These wrappers integrate with:
- **ModuleBootstrap** - Privileged access foundation
- **ReflectBuilder** - Reflection utilities
- **InternalUnsafe** - Low-level memory operations
- **DirectMemoryManager** - High-level memory management