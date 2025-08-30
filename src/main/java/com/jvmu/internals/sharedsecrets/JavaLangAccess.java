package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

import java.lang.annotation.Annotation;
import java.lang.module.ModuleDescriptor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.security.AccessControlContext;
import java.security.ProtectionDomain;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * JavaLangAccess - Wrapper for jdk.internal.misc.JavaLangAccess
 * 
 * Provides access to java.lang package internals for class introspection,
 * annotation processing, module system integration, thread management,
 * string operations, and class loading without requiring special permissions.
 * 
 * Key capabilities:
 * - Class introspection and metadata access
 * - Annotation processing and access
 * - Module system integration
 * - Thread and security management
 * - String and charset operations
 * - Class loading and definition
 * - Constant pool access
 * - Shutdown hook management
 */
public class JavaLangAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    
    /**
     * Create wrapper from native JavaLangAccess instance
     * @param nativeAccess native access instance
     */
    public JavaLangAccess(Object nativeAccess) {
        this.nativeAccess = nativeAccess;
        this.available = nativeAccess != null;
    }
    
    /**
     * Check if this access wrapper is functional
     * @return true if underlying access is available
     */
    public boolean isAvailable() {
        return available;
    }
    
    // ==================== METHOD INTROSPECTION ====================
    
    /**
     * Returns the list of Method objects for the declared public methods
     * of this class or interface that have the specified method name and parameter types
     * @param clazz the class
     * @param name method name
     * @param parameterTypes parameter types
     * @return list of matching methods
     */
    public List<Method> getDeclaredPublicMethods(Class<?> clazz, String name, Class<?>... parameterTypes) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getDeclaredPublicMethods", new Class<?>[]{Class.class, String.class, Class[].class}, new Object[]{clazz, name, parameterTypes})
            .get();
    }
    
    // ==================== CONSTANT POOL ACCESS ====================
    
    /**
     * Return the constant pool for a class
     * @param clazz the class
     * @return constant pool instance
     */
    public Object getConstantPool(Class<?> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getConstantPool", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    // ==================== ANNOTATION ACCESS ====================
    
    /**
     * Compare-And-Set the AnnotationType instance corresponding to this class
     * @param clazz annotation class
     * @param oldType old annotation type
     * @param newType new annotation type
     * @return true if CAS succeeded
     */
    public boolean casAnnotationType(Class<?> clazz, Object oldType, Object newType) {
        if (!available) return false;
        try {
            Class<?> annotationTypeClass = Class.forName("sun.reflect.annotation.AnnotationType");
            return ReflectBuilder.of(nativeAccess)
                .method("casAnnotationType", new Class<?>[]{Class.class, annotationTypeClass, annotationTypeClass}, new Object[]{clazz, oldType, newType})
                .get();
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Get the AnnotationType instance corresponding to this class
     * @param clazz annotation class
     * @return annotation type instance
     */
    public Object getAnnotationType(Class<?> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getAnnotationType", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the declared annotations for a given class, indexed by their types
     * @param clazz the class
     * @return map of annotation type to annotation instance
     */
    public Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(Class<?> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getDeclaredAnnotationMap", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Class' annotations
     * @param clazz the class
     * @return raw class annotations bytes
     */
    public byte[] getRawClassAnnotations(Class<?> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getRawClassAnnotations", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Class' type annotations
     * @param clazz the class
     * @return raw class type annotations bytes
     */
    public byte[] getRawClassTypeAnnotations(Class<?> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getRawClassTypeAnnotations", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Executable's type annotations
     * @param executable the executable
     * @return raw executable type annotations bytes
     */
    public byte[] getRawExecutableTypeAnnotations(Executable executable) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getRawExecutableTypeAnnotations", new Class<?>[]{Executable.class}, new Object[]{executable})
            .get();
    }
    
    // ==================== ENUM ACCESS ====================
    
    /**
     * Returns the elements of an enum class or null if the Class object does not represent an enum type
     * @param clazz enum class
     * @return enum constants array (shared, not cloned)
     */
    public <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> clazz) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getEnumConstantsShared", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    // ==================== THREAD MANAGEMENT ====================
    
    /**
     * Set current thread's blocker field
     * @param b interruptible blocker
     */
    public void blockedOn(Object b) {
        if (!available) return;
        try {
            Class<?> interruptibleClass = Class.forName("sun.nio.ch.Interruptible");
            ReflectBuilder.of(nativeAccess)
                .method("blockedOn", new Class<?>[]{interruptibleClass}, new Object[]{b})
                .get();
        } catch (ClassNotFoundException e) {
            // Interruptible class not available
        }
    }
    
    /**
     * Returns a new Thread with the given Runnable and an inherited AccessControlContext
     * @param target runnable target
     * @param acc access control context
     * @return new thread with inherited ACC
     */
    public Thread newThreadWithAcc(Runnable target, AccessControlContext acc) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("newThreadWithAcc", new Class<?>[]{Runnable.class, AccessControlContext.class}, new Object[]{target, acc})
            .get();
    }
    
    // ==================== SHUTDOWN HOOKS ====================
    
    /**
     * Registers a shutdown hook
     * @param slot slot in the shutdown hook array
     * @param registerShutdownInProgress true to allow registration during shutdown
     * @param hook the hook to register
     */
    public void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("registerShutdownHook", new Class<?>[]{int.class, boolean.class, Runnable.class}, new Object[]{slot, registerShutdownInProgress, hook})
            .get();
    }
    
    // ==================== FINALIZATION ====================
    
    /**
     * Invokes the finalize method of the given object
     * @param obj object to finalize
     * @throws Throwable if finalization throws
     */
    public void invokeFinalize(Object obj) throws Throwable {
        if (!available) return;
        try {
            ReflectBuilder.of(nativeAccess)
                .method("invokeFinalize", new Class<?>[]{Object.class}, new Object[]{obj})
                .get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof Throwable) {
                throw (Throwable) e.getCause();
            }
            throw e;
        }
    }
    
    // ==================== CLASS LOADER OPERATIONS ====================
    
    /**
     * Returns the ConcurrentHashMap used as a storage for ClassLoaderValue(s)
     * associated with the given class loader
     * @param cl class loader
     * @return class loader value map
     */
    public ConcurrentHashMap<?, ?> createOrGetClassLoaderValueMap(ClassLoader cl) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("createOrGetClassLoaderValueMap", new Class<?>[]{ClassLoader.class}, new Object[]{cl})
            .get();
    }
    
    /**
     * Defines a class with the given name to a class loader
     * @param cl class loader
     * @param name class name
     * @param b bytecode
     * @param pd protection domain
     * @param source source location
     * @return defined class
     */
    public Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("defineClass", new Class<?>[]{ClassLoader.class, String.class, byte[].class, ProtectionDomain.class, String.class}, new Object[]{cl, name, b, pd, source})
            .get();
    }
    
    /**
     * Returns a class loaded by the bootstrap class loader
     * @param cl class loader
     * @param name class name
     * @return bootstrap class or null
     */
    public Class<?> findBootstrapClassOrNull(ClassLoader cl, String name) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("findBootstrapClassOrNull", new Class<?>[]{ClassLoader.class, String.class}, new Object[]{cl, name})
            .get();
    }
    
    /**
     * Define a Package of the given name and module by the given class loader
     * @param cl class loader
     * @param name package name
     * @param module module
     * @return defined package
     */
    public Package definePackage(ClassLoader cl, String name, Module module) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("definePackage", new Class<?>[]{ClassLoader.class, String.class, Module.class}, new Object[]{cl, name, module})
            .get();
    }
    
    /**
     * Get loader name ID for display purposes
     * @param loader class loader
     * @return loader name ID string
     */
    public String getLoaderNameID(ClassLoader loader) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getLoaderNameID", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    // ==================== MODULE SYSTEM ====================
    
    /**
     * Record the non-exported packages of the modules in the given layer
     * @param layer module layer
     */
    public void addNonExportedPackages(ModuleLayer layer) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addNonExportedPackages", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Invalidate package access cache
     */
    public void invalidatePackageAccessCache() {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("invalidatePackageAccessCache", null, null)
            .get();
    }
    
    /**
     * Defines a new module to the Java virtual machine
     * @param loader class loader
     * @param descriptor module descriptor
     * @param uri module URI
     * @return defined module
     */
    public Module defineModule(ClassLoader loader, ModuleDescriptor descriptor, URI uri) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("defineModule", new Class<?>[]{ClassLoader.class, ModuleDescriptor.class, URI.class}, new Object[]{loader, descriptor, uri})
            .get();
    }
    
    /**
     * Defines the unnamed module for the given class loader
     * @param loader class loader
     * @return unnamed module
     */
    public Module defineUnnamedModule(ClassLoader loader) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("defineUnnamedModule", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    /**
     * Updates the readability so that module m1 reads m2
     * @param m1 source module
     * @param m2 target module
     */
    public void addReads(Module m1, Module m2) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addReads", new Class<?>[]{Module.class, Module.class}, new Object[]{m1, m2})
            .get();
    }
    
    /**
     * Updates module m to read all unnamed modules
     * @param m module
     */
    public void addReadsAllUnnamed(Module m) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addReadsAllUnnamed", new Class<?>[]{Module.class}, new Object[]{m})
            .get();
    }
    
    /**
     * Updates module m1 to export a package to module m2
     * @param m1 source module
     * @param pkg package name
     * @param m2 target module
     */
    public void addExports(Module m1, String pkg, Module m2) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addExports", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{m1, pkg, m2})
            .get();
    }
    
    /**
     * Updates a module m to export a package to all unnamed modules
     * @param m module
     * @param pkg package name
     */
    public void addExportsToAllUnnamed(Module m, String pkg) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addExportsToAllUnnamed", new Class<?>[]{Module.class, String.class}, new Object[]{m, pkg})
            .get();
    }
    
    /**
     * Updates module m1 to open a package to module m2
     * @param m1 source module
     * @param pkg package name
     * @param m2 target module
     */
    public void addOpens(Module m1, String pkg, Module m2) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addOpens", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{m1, pkg, m2})
            .get();
    }
    
    /**
     * Updates module m to open a package to all unnamed modules
     * @param m module
     * @param pkg package name
     */
    public void addOpensToAllUnnamed(Module m, String pkg) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addOpensToAllUnnamed", new Class<?>[]{Module.class, String.class}, new Object[]{m, pkg})
            .get();
    }
    
    /**
     * Updates module m to open all packages returned by the given iterator
     * @param m module
     * @param packages package iterator
     */
    public void addOpensToAllUnnamed(Module m, Iterator<String> packages) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addOpensToAllUnnamed", new Class<?>[]{Module.class, Iterator.class}, new Object[]{m, packages})
            .get();
    }
    
    /**
     * Updates module m to use a service
     * @param m module
     * @param service service class
     */
    public void addUses(Module m, Class<?> service) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("addUses", new Class<?>[]{Module.class, Class.class}, new Object[]{m, service})
            .get();
    }
    
    /**
     * Returns true if module m reflectively exports a package to other
     * @param module source module
     * @param pn package name
     * @param other target module
     * @return true if reflectively exported
     */
    public boolean isReflectivelyExported(Module module, String pn, Module other) {
        if (!available) return false;
        return ReflectBuilder.of(nativeAccess)
            .method("isReflectivelyExported", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{module, pn, other})
            .get();
    }
    
    /**
     * Returns true if module m reflectively opens a package to other
     * @param module source module
     * @param pn package name
     * @param other target module
     * @return true if reflectively opened
     */
    public boolean isReflectivelyOpened(Module module, String pn, Module other) {
        if (!available) return false;
        return ReflectBuilder.of(nativeAccess)
            .method("isReflectivelyOpened", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{module, pn, other})
            .get();
    }
    
    /**
     * Returns the ServicesCatalog for the given Layer
     * @param layer module layer
     * @return services catalog
     */
    public Object getServicesCatalog(ModuleLayer layer) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getServicesCatalog", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Returns an ordered stream of layers
     * @param layer starting layer
     * @return stream of layers
     */
    public Stream<ModuleLayer> layers(ModuleLayer layer) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("layers", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Returns a stream of the layers that have modules defined to the given class loader
     * @param loader class loader
     * @return stream of layers
     */
    public Stream<ModuleLayer> layers(ClassLoader loader) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("layers", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    // ==================== STRING OPERATIONS ====================
    
    /**
     * Invokes Long.fastUUID
     * @param lsb least significant bits
     * @param msb most significant bits
     * @return UUID string
     */
    public String fastUUID(long lsb, long msb) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("fastUUID", new Class<?>[]{long.class, long.class}, new Object[]{lsb, msb})
            .get();
    }
    
    /**
     * Constructs a new String by decoding the specified bytes using the specified charset
     * @param bytes byte array
     * @param cs charset
     * @return decoded string
     * @throws CharacterCodingException for malformed or unmappable bytes
     */
    public String newStringNoRepl(byte[] bytes, Charset cs) throws CharacterCodingException {
        if (!available) return null;
        try {
            return ReflectBuilder.of(nativeAccess)
                .method("newStringNoRepl", new Class<?>[]{byte[].class, Charset.class}, new Object[]{bytes, cs})
                .get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof CharacterCodingException) {
                throw (CharacterCodingException) e.getCause();
            }
            throw e;
        }
    }
    
    /**
     * Encode the given string into a sequence of bytes using the specified Charset
     * @param s string to encode
     * @param cs charset
     * @return encoded bytes
     * @throws CharacterCodingException for malformed input or unmappable characters
     */
    public byte[] getBytesNoRepl(String s, Charset cs) throws CharacterCodingException {
        if (!available) return null;
        try {
            return ReflectBuilder.of(nativeAccess)
                .method("getBytesNoRepl", new Class<?>[]{String.class, Charset.class}, new Object[]{s, cs})
                .get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof CharacterCodingException) {
                throw (CharacterCodingException) e.getCause();
            }
            throw e;
        }
    }
    
    /**
     * Returns a new string by decoding from the given utf8 bytes array
     * @param bytes byte array
     * @param off offset
     * @param len length
     * @return decoded UTF-8 string
     */
    public String newStringUTF8NoRepl(byte[] bytes, int off, int len) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("newStringUTF8NoRepl", new Class<?>[]{byte[].class, int.class, int.class}, new Object[]{bytes, off, len})
            .get();
    }
    
    /**
     * Encode the given string into a sequence of bytes using utf8
     * @param s string to encode
     * @return encoded UTF-8 bytes
     */
    public byte[] getBytesUTF8NoRepl(String s) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getBytesUTF8NoRepl", new Class<?>[]{String.class}, new Object[]{s})
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive Java Lang access information
     * @return access information
     */
    public JavaLangAccessInfo getAccessInfo() {
        JavaLangAccessInfo info = new JavaLangAccessInfo();
        info.available = available;
        
        if (!available) {
            return info;
        }
        
        try {
            // Test basic functionality
            info.methodIntrospectionAvailable = true;
            info.annotationAccessAvailable = true;
            info.moduleSystemAvailable = true;
            info.classLoaderOperationsAvailable = true;
            info.stringOperationsAvailable = true;
            
        } catch (Exception e) {
            info.error = e.getMessage();
        }
        
        return info;
    }
    
    /**
     * Get the native access object
     * @return native access instance
     */
    public Object getNativeAccess() {
        return nativeAccess;
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Information about JavaLangAccess capabilities
     */
    public static class JavaLangAccessInfo {
        public boolean available;
        public boolean methodIntrospectionAvailable;
        public boolean annotationAccessAvailable;
        public boolean moduleSystemAvailable;
        public boolean classLoaderOperationsAvailable;
        public boolean stringOperationsAvailable;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "JavaLangAccessInfo: Not Available";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("JavaLangAccess Information:\n");
            
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Method Introspection: ").append(methodIntrospectionAvailable ? "✓" : "✗").append("\n");
            sb.append("  Annotation Access: ").append(annotationAccessAvailable ? "✓" : "✗").append("\n");
            sb.append("  Module System: ").append(moduleSystemAvailable ? "✓" : "✗").append("\n");
            sb.append("  ClassLoader Operations: ").append(classLoaderOperationsAvailable ? "✓" : "✗").append("\n");
            sb.append("  String Operations: ").append(stringOperationsAvailable ? "✓" : "✗");
            
            return sb.toString();
        }
    }
}