package com.jvmu.internals;

import com.jvmu.module.ModuleBootstrap;
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
 * JavaLangAccess - Wrapper for jdk.internal.misc.JavaLangAccess using privileged access
 * 
 * Provides access to java.lang package internals without requiring special permissions
 * or module access restrictions. JavaLangAccess is a "shared secret" interface that
 * allows access to package-private methods and functionality within java.lang.
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
 * 
 * This wrapper bypasses the normal module system restrictions and provides
 * direct access to java.lang implementation internals.
 */
public class JavaLangAccess {
    
    private static final Object javaLangAccess;
    private static final Class<?> javaLangAccessClass;
    private static final boolean available;
    
    static {
        Object access = null;
        Class<?> clazz = null;
        boolean isAvailable = false;
        
        try {
            // Get JavaLangAccess instance via SharedSecrets
            Object sharedSecrets = SharedSecrets.getJavaLangAccess();
            if (sharedSecrets != null) {
                access = sharedSecrets;
                clazz = Class.forName("jdk.internal.misc.JavaLangAccess");
                isAvailable = true;
            }
        } catch (Exception e) {
            // JavaLangAccess not available - silent fallback
        }
        
        javaLangAccess = access;
        javaLangAccessClass = clazz;
        available = isAvailable;
    }
    
    /**
     * Check if JavaLangAccess functionality is available
     * @return true if JavaLangAccess APIs can be used
     */
    public static boolean isAvailable() {
        return available && ModuleBootstrap.getInternalUnsafe() != null;
    }
    
    /**
     * Get status information about JavaLangAccess availability
     * @return status string
     */
    public static String getStatus() {
        if (available) {
            return "JavaLangAccess: Available ✓ (Bypassed java.lang internal access restrictions)";
        } else {
            return "JavaLangAccess: Not Available ✗ (JavaLangAccess instance not found)";
        }
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
    public static List<Method> getDeclaredPublicMethods(Class<?> clazz, String name, Class<?>... parameterTypes) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getDeclaredPublicMethods", new Class<?>[]{Class.class, String.class, Class[].class}, new Object[]{clazz, name, parameterTypes})
            .get();
    }
    
    // ==================== CONSTANT POOL ACCESS ====================
    
    /**
     * Return the constant pool for a class
     * @param clazz the class
     * @return constant pool instance
     */
    public static Object getConstantPool(Class<?> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static boolean casAnnotationType(Class<?> clazz, Object oldType, Object newType) {
        if (!isAvailable()) return false;
        try {
            Class<?> annotationTypeClass = Class.forName("sun.reflect.annotation.AnnotationType");
            return ReflectBuilder.of(javaLangAccess)
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
    public static Object getAnnotationType(Class<?> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getAnnotationType", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the declared annotations for a given class, indexed by their types
     * @param clazz the class
     * @return map of annotation type to annotation instance
     */
    public static Map<Class<? extends Annotation>, Annotation> getDeclaredAnnotationMap(Class<?> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getDeclaredAnnotationMap", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Class' annotations
     * @param clazz the class
     * @return raw class annotations bytes
     */
    public static byte[] getRawClassAnnotations(Class<?> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getRawClassAnnotations", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Class' type annotations
     * @param clazz the class
     * @return raw class type annotations bytes
     */
    public static byte[] getRawClassTypeAnnotations(Class<?> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getRawClassTypeAnnotations", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    /**
     * Get the array of bytes that is the class-file representation of this Executable's type annotations
     * @param executable the executable
     * @return raw executable type annotations bytes
     */
    public static byte[] getRawExecutableTypeAnnotations(Executable executable) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getRawExecutableTypeAnnotations", new Class<?>[]{Executable.class}, new Object[]{executable})
            .get();
    }
    
    // ==================== ENUM ACCESS ====================
    
    /**
     * Returns the elements of an enum class or null if the Class object does not represent an enum type
     * @param clazz enum class
     * @return enum constants array (shared, not cloned)
     */
    public static <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> clazz) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getEnumConstantsShared", new Class<?>[]{Class.class}, new Object[]{clazz})
            .get();
    }
    
    // ==================== THREAD MANAGEMENT ====================
    
    /**
     * Set current thread's blocker field
     * @param b interruptible blocker
     */
    public static void blockedOn(Object b) {
        if (!isAvailable()) return;
        try {
            Class<?> interruptibleClass = Class.forName("sun.nio.ch.Interruptible");
            ReflectBuilder.of(javaLangAccess)
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
    public static Thread newThreadWithAcc(Runnable target, AccessControlContext acc) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static void registerShutdownHook(int slot, boolean registerShutdownInProgress, Runnable hook) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("registerShutdownHook", new Class<?>[]{int.class, boolean.class, Runnable.class}, new Object[]{slot, registerShutdownInProgress, hook})
            .get();
    }
    
    // ==================== FINALIZATION ====================
    
    /**
     * Invokes the finalize method of the given object
     * @param obj object to finalize
     * @throws Throwable if finalization throws
     */
    public static void invokeFinalize(Object obj) throws Throwable {
        if (!isAvailable()) return;
        try {
            ReflectBuilder.of(javaLangAccess)
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
    public static ConcurrentHashMap<?, ?> createOrGetClassLoaderValueMap(ClassLoader cl) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static Class<?> defineClass(ClassLoader cl, String name, byte[] b, ProtectionDomain pd, String source) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("defineClass", new Class<?>[]{ClassLoader.class, String.class, byte[].class, ProtectionDomain.class, String.class}, new Object[]{cl, name, b, pd, source})
            .get();
    }
    
    /**
     * Returns a class loaded by the bootstrap class loader
     * @param cl class loader
     * @param name class name
     * @return bootstrap class or null
     */
    public static Class<?> findBootstrapClassOrNull(ClassLoader cl, String name) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static Package definePackage(ClassLoader cl, String name, Module module) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("definePackage", new Class<?>[]{ClassLoader.class, String.class, Module.class}, new Object[]{cl, name, module})
            .get();
    }
    
    /**
     * Get loader name ID for display purposes
     * @param loader class loader
     * @return loader name ID string
     */
    public static String getLoaderNameID(ClassLoader loader) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getLoaderNameID", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    // ==================== MODULE SYSTEM ====================
    
    /**
     * Record the non-exported packages of the modules in the given layer
     * @param layer module layer
     */
    public static void addNonExportedPackages(ModuleLayer layer) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addNonExportedPackages", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Invalidate package access cache
     */
    public static void invalidatePackageAccessCache() {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
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
    public static Module defineModule(ClassLoader loader, ModuleDescriptor descriptor, URI uri) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("defineModule", new Class<?>[]{ClassLoader.class, ModuleDescriptor.class, URI.class}, new Object[]{loader, descriptor, uri})
            .get();
    }
    
    /**
     * Defines the unnamed module for the given class loader
     * @param loader class loader
     * @return unnamed module
     */
    public static Module defineUnnamedModule(ClassLoader loader) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("defineUnnamedModule", new Class<?>[]{ClassLoader.class}, new Object[]{loader})
            .get();
    }
    
    /**
     * Updates the readability so that module m1 reads m2
     * @param m1 source module
     * @param m2 target module
     */
    public static void addReads(Module m1, Module m2) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addReads", new Class<?>[]{Module.class, Module.class}, new Object[]{m1, m2})
            .get();
    }
    
    /**
     * Updates module m to read all unnamed modules
     * @param m module
     */
    public static void addReadsAllUnnamed(Module m) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addReadsAllUnnamed", new Class<?>[]{Module.class}, new Object[]{m})
            .get();
    }
    
    /**
     * Updates module m1 to export a package to module m2
     * @param m1 source module
     * @param pkg package name
     * @param m2 target module
     */
    public static void addExports(Module m1, String pkg, Module m2) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addExports", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{m1, pkg, m2})
            .get();
    }
    
    /**
     * Updates a module m to export a package to all unnamed modules
     * @param m module
     * @param pkg package name
     */
    public static void addExportsToAllUnnamed(Module m, String pkg) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addExportsToAllUnnamed", new Class<?>[]{Module.class, String.class}, new Object[]{m, pkg})
            .get();
    }
    
    /**
     * Updates module m1 to open a package to module m2
     * @param m1 source module
     * @param pkg package name
     * @param m2 target module
     */
    public static void addOpens(Module m1, String pkg, Module m2) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addOpens", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{m1, pkg, m2})
            .get();
    }
    
    /**
     * Updates module m to open a package to all unnamed modules
     * @param m module
     * @param pkg package name
     */
    public static void addOpensToAllUnnamed(Module m, String pkg) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addOpensToAllUnnamed", new Class<?>[]{Module.class, String.class}, new Object[]{m, pkg})
            .get();
    }
    
    /**
     * Updates module m to open all packages returned by the given iterator
     * @param m module
     * @param packages package iterator
     */
    public static void addOpensToAllUnnamed(Module m, Iterator<String> packages) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
            .method("addOpensToAllUnnamed", new Class<?>[]{Module.class, Iterator.class}, new Object[]{m, packages})
            .get();
    }
    
    /**
     * Updates module m to use a service
     * @param m module
     * @param service service class
     */
    public static void addUses(Module m, Class<?> service) {
        if (!isAvailable()) return;
        ReflectBuilder.of(javaLangAccess)
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
    public static boolean isReflectivelyExported(Module module, String pn, Module other) {
        if (!isAvailable()) return false;
        return ReflectBuilder.of(javaLangAccess)
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
    public static boolean isReflectivelyOpened(Module module, String pn, Module other) {
        if (!isAvailable()) return false;
        return ReflectBuilder.of(javaLangAccess)
            .method("isReflectivelyOpened", new Class<?>[]{Module.class, String.class, Module.class}, new Object[]{module, pn, other})
            .get();
    }
    
    /**
     * Returns the ServicesCatalog for the given Layer
     * @param layer module layer
     * @return services catalog
     */
    public static Object getServicesCatalog(ModuleLayer layer) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getServicesCatalog", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Returns an ordered stream of layers
     * @param layer starting layer
     * @return stream of layers
     */
    public static Stream<ModuleLayer> layers(ModuleLayer layer) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("layers", new Class<?>[]{ModuleLayer.class}, new Object[]{layer})
            .get();
    }
    
    /**
     * Returns a stream of the layers that have modules defined to the given class loader
     * @param loader class loader
     * @return stream of layers
     */
    public static Stream<ModuleLayer> layers(ClassLoader loader) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static String fastUUID(long lsb, long msb) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
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
    public static String newStringNoRepl(byte[] bytes, Charset cs) throws CharacterCodingException {
        if (!isAvailable()) return null;
        try {
            return ReflectBuilder.of(javaLangAccess)
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
    public static byte[] getBytesNoRepl(String s, Charset cs) throws CharacterCodingException {
        if (!isAvailable()) return null;
        try {
            return ReflectBuilder.of(javaLangAccess)
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
    public static String newStringUTF8NoRepl(byte[] bytes, int off, int len) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("newStringUTF8NoRepl", new Class<?>[]{byte[].class, int.class, int.class}, new Object[]{bytes, off, len})
            .get();
    }
    
    /**
     * Encode the given string into a sequence of bytes using utf8
     * @param s string to encode
     * @return encoded UTF-8 bytes
     */
    public static byte[] getBytesUTF8NoRepl(String s) {
        if (!isAvailable()) return null;
        return ReflectBuilder.of(javaLangAccess)
            .method("getBytesUTF8NoRepl", new Class<?>[]{String.class}, new Object[]{s})
            .get();
    }
}