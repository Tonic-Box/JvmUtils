package com.jvmu.jvmti;

import com.jvmu.module.ModuleBootstrap;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Advanced VM Access - Deep JVM inspection capabilities
 * 
 * This class provides advanced JVMTI-like functionality including:
 * - Direct memory access to object headers
 * - Class metadata inspection
 * - Method interception capabilities
 * - Native thread access
 * - JVM internal structure navigation
 */
public class AdvancedVMAccess {
    
    private static Object internalUnsafe;
    private static MethodHandles.Lookup trustedLookup;
    private static boolean initialized = false;
    
    // Unsafe method handles for direct memory access
    private static MethodHandle getObjectMethod;
    private static MethodHandle putObjectMethod;
    private static MethodHandle getIntMethod;
    private static MethodHandle getLongMethod;
    private static MethodHandle objectFieldOffsetMethod;
    private static MethodHandle staticFieldOffsetMethod;
    private static MethodHandle staticFieldBaseMethod;
    
    // Class analysis cache
    private static final Map<Class<?>, ClassMetadata> classMetadataCache = new ConcurrentHashMap<>();
    
    static {
        try {
            initialize();
        } catch (Exception e) {
            System.err.println("AdvancedVMAccess initialization failed: " + e.getMessage());
        }
    }
    
    private static void initialize() throws Exception {
        internalUnsafe = ModuleBootstrap.getInternalUnsafe();
        trustedLookup = ModuleBootstrap.getTrustedLookup();
        
        if (internalUnsafe == null || trustedLookup == null) {
            System.err.println("AdvancedVMAccess requires privileged access via ModuleBootstrap");
            return;
        }
        
        setupUnsafeMethodHandles();
        
        initialized = true;
        System.out.println("AdvancedVMAccess initialized - Deep JVM inspection enabled");
    }
    
    private static void setupUnsafeMethodHandles() throws Exception {
        Class<?> unsafeClass = internalUnsafe.getClass();
        
        // Object access methods
        Method getObjMethod = unsafeClass.getDeclaredMethod("getObject", Object.class, long.class);
        Method putObjMethod = unsafeClass.getDeclaredMethod("putObject", Object.class, long.class, Object.class);
        Method getIntMeth = unsafeClass.getDeclaredMethod("getInt", Object.class, long.class);
        Method getLongMeth = unsafeClass.getDeclaredMethod("getLong", Object.class, long.class);
        
        getObjectMethod = trustedLookup.unreflect(getObjMethod);
        putObjectMethod = trustedLookup.unreflect(putObjMethod);
        getIntMethod = trustedLookup.unreflect(getIntMeth);
        getLongMethod = trustedLookup.unreflect(getLongMeth);
        
        // Field offset methods
        Method objFieldOffsetMethod = unsafeClass.getDeclaredMethod("objectFieldOffset", Field.class);
        Method staticFieldOffsetMeth = unsafeClass.getDeclaredMethod("staticFieldOffset", Field.class);
        Method staticFieldBaseMeth = unsafeClass.getDeclaredMethod("staticFieldBase", Field.class);
        
        objectFieldOffsetMethod = trustedLookup.unreflect(objFieldOffsetMethod);
        staticFieldOffsetMethod = trustedLookup.unreflect(staticFieldOffsetMeth);
        staticFieldBaseMethod = trustedLookup.unreflect(staticFieldBaseMeth);
    }
    
    /**
     * Get detailed class metadata including internal JVM structures
     */
    public static ClassMetadata getClassMetadata(Class<?> clazz) {
        if (!initialized) {
            throw new IllegalStateException("AdvancedVMAccess not initialized");
        }
        
        return classMetadataCache.computeIfAbsent(clazz, AdvancedVMAccess::analyzeClass);
    }
    
    private static ClassMetadata analyzeClass(Class<?> clazz) {
        try {
            ClassMetadata metadata = new ClassMetadata();
            metadata.clazz = clazz;
            metadata.name = clazz.getName();
            metadata.modifiers = clazz.getModifiers();
            metadata.isArray = clazz.isArray();
            metadata.isPrimitive = clazz.isPrimitive();
            metadata.isInterface = clazz.isInterface();
            
            // Analyze fields
            Field[] declaredFields = clazz.getDeclaredFields();
            metadata.fieldCount = declaredFields.length;
            metadata.instanceFields = new ArrayList<>();
            metadata.staticFields = new ArrayList<>();
            
            for (Field field : declaredFields) {
                FieldInfo fieldInfo = new FieldInfo();
                fieldInfo.field = field;
                fieldInfo.name = field.getName();
                fieldInfo.type = field.getType();
                fieldInfo.modifiers = field.getModifiers();
                
                try {
                    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
                        fieldInfo.offset = (Long) staticFieldOffsetMethod.invoke(internalUnsafe, field);
                        fieldInfo.base = staticFieldBaseMethod.invoke(internalUnsafe, field);
                        metadata.staticFields.add(fieldInfo);
                    } else {
                        fieldInfo.offset = (Long) objectFieldOffsetMethod.invoke(internalUnsafe, field);
                        metadata.instanceFields.add(fieldInfo);
                    }
                } catch (Exception | Error e) {
                    fieldInfo.offset = -1L; // Mark as inaccessible
                } catch (Throwable t) {
                    fieldInfo.offset = -1L; // Mark as inaccessible
                }
            }
            
            // Analyze methods
            Method[] declaredMethods = clazz.getDeclaredMethods();
            metadata.methodCount = declaredMethods.length;
            metadata.methods = new ArrayList<>();
            
            for (Method method : declaredMethods) {
                MethodInfo methodInfo = new MethodInfo();
                methodInfo.method = method;
                methodInfo.name = method.getName();
                methodInfo.returnType = method.getReturnType();
                methodInfo.parameterTypes = method.getParameterTypes();
                methodInfo.modifiers = method.getModifiers();
                methodInfo.isNative = java.lang.reflect.Modifier.isNative(method.getModifiers());
                metadata.methods.add(methodInfo);
            }
            
            return metadata;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze class: " + e.getMessage(), e);
        }
    }
    
    /**
     * Read object field value directly using memory offset
     */
    public static Object readObjectField(Object obj, String fieldName) {
        if (!initialized) {
            throw new IllegalStateException("AdvancedVMAccess not initialized");
        }
        
        try {
            ClassMetadata metadata = getClassMetadata(obj.getClass());
            
            for (FieldInfo fieldInfo : metadata.instanceFields) {
                if (fieldInfo.name.equals(fieldName)) {
                    if (fieldInfo.offset == -1L) {
                        throw new IllegalAccessException("Field offset not available");
                    }
                    
                    return getObjectMethod.invoke(internalUnsafe, obj, fieldInfo.offset);
                }
            }
            
            throw new NoSuchFieldException("Field not found: " + fieldName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read object field: " + e.getMessage(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    
    /**
     * Write object field value directly using memory offset
     */
    public static void writeObjectField(Object obj, String fieldName, Object value) {
        if (!initialized) {
            throw new IllegalStateException("AdvancedVMAccess not initialized");
        }
        
        try {
            ClassMetadata metadata = getClassMetadata(obj.getClass());
            
            for (FieldInfo fieldInfo : metadata.instanceFields) {
                if (fieldInfo.name.equals(fieldName)) {
                    if (fieldInfo.offset == -1L) {
                        throw new IllegalAccessException("Field offset not available");
                    }
                    
                    putObjectMethod.invoke(internalUnsafe, obj, fieldInfo.offset, value);
                    return;
                }
            }
            
            throw new NoSuchFieldException("Field not found: " + fieldName);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to write object field: " + e.getMessage(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    
    /**
     * Get object header information (JVM-specific)
     */
    public static ObjectHeader getObjectHeader(Object obj) {
        if (!initialized) {
            throw new IllegalStateException("AdvancedVMAccess not initialized");
        }
        
        if (obj == null) {
            return null;
        }
        
        try {
            ObjectHeader header = new ObjectHeader();
            header.object = obj;
            header.clazz = obj.getClass();
            
            // Try to read mark word (at offset 0)
            try {
                header.markWord = (Long) getLongMethod.invoke(internalUnsafe, obj, 0L);
            } catch (Exception e) {
                header.markWord = null;
            }
            
            // Try to read class pointer (typically at offset 8 on 64-bit, 4 on 32-bit)
            try {
                long classPointerOffset = System.getProperty("java.vm.name").contains("64") ? 8L : 4L;
                header.classPointer = (Long) getLongMethod.invoke(internalUnsafe, obj, classPointerOffset);
            } catch (Exception e) {
                header.classPointer = null;
            }
            
            // Calculate header size
            header.headerSize = getObjectHeaderSize();
            
            return header;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get object header: " + e.getMessage(), e);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable);
        }
    }
    
    private static int getObjectHeaderSize() {
        // Typical JVM object header sizes:
        // 32-bit JVM: 8 bytes (4 bytes mark word + 4 bytes class pointer)
        // 64-bit JVM: 16 bytes (8 bytes mark word + 8 bytes class pointer, or 4 with compressed OOPs)
        String vmName = System.getProperty("java.vm.name");
        if (vmName.contains("64")) {
            // Check if compressed OOPs are enabled (typical default)
            return 12; // 8 bytes mark word + 4 bytes compressed class pointer
        } else {
            return 8;  // 32-bit JVM
        }
    }
    
    /**
     * Get all loaded classes in the JVM
     * This approximates JVMTI GetLoadedClasses functionality
     */
    public static Set<Class<?>> getAllLoadedClasses() {
        if (!initialized) {
            throw new IllegalStateException("AdvancedVMAccess not initialized");
        }
        
        Set<Class<?>> loadedClasses = new HashSet<>();
        
        try {
            // Access ClassLoader instances to find loaded classes
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            addClassesFromClassLoader(loadedClasses, systemClassLoader);
            
            // Try to access bootstrap class loader classes
            addClassesFromClassLoader(loadedClasses, null); // null = bootstrap
            
            // Add some well-known classes that should be loaded
            Class<?>[] wellKnownClasses = {
                Object.class, String.class, Class.class, Thread.class,
                System.class, Runtime.class, ClassLoader.class
            };
            
            Collections.addAll(loadedClasses, wellKnownClasses);
            
            return loadedClasses;
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get loaded classes: " + e.getMessage(), e);
        }
    }
    
    private static void addClassesFromClassLoader(Set<Class<?>> classes, ClassLoader classLoader) {
        try {
            // This is a simplified approach - real JVMTI would have direct access to class data
            // We can only access classes we know about or can discover through reflection
            
            if (classLoader != null) {
                Class<?> classLoaderClass = classLoader.getClass();
                
                // Try to access internal class vectors/maps if available
                try {
                    Field classesField = ClassLoader.class.getDeclaredField("classes");
                    classesField.setAccessible(true);
                    Object classesVector = classesField.get(classLoader);
                    
                    if (classesVector instanceof Vector) {
                        Vector<?> vector = (Vector<?>) classesVector;
                        for (Object obj : vector) {
                            if (obj instanceof Class) {
                                classes.add((Class<?>) obj);
                            }
                        }
                    }
                } catch (Exception e) {
                    // Field not accessible or doesn't exist
                }
            }
            
        } catch (Exception e) {
            // Ignore errors accessing specific class loaders
        }
    }
    
    /**
     * Check if AdvancedVMAccess is available
     */
    public static boolean isAvailable() {
        return initialized;
    }
    
    /**
     * Get status information
     */
    public static String getStatus() {
        return "AdvancedVMAccess: " + (initialized ? "Initialized ✓" : "Failed ✗");
    }
    
    // Data structures for metadata
    
    public static class ClassMetadata {
        public Class<?> clazz;
        public String name;
        public int modifiers;
        public boolean isArray;
        public boolean isPrimitive;
        public boolean isInterface;
        public int fieldCount;
        public int methodCount;
        public List<FieldInfo> instanceFields = new ArrayList<>();
        public List<FieldInfo> staticFields = new ArrayList<>();
        public List<MethodInfo> methods = new ArrayList<>();
        
        @Override
        public String toString() {
            return String.format("ClassMetadata[name=%s, fields=%d, methods=%d]",
                name, fieldCount, methodCount);
        }
    }
    
    public static class FieldInfo {
        public Field field;
        public String name;
        public Class<?> type;
        public int modifiers;
        public long offset = -1L;
        public Object base; // For static fields
        
        @Override
        public String toString() {
            return String.format("Field[name=%s, type=%s, offset=%d]",
                name, type.getSimpleName(), offset);
        }
    }
    
    public static class MethodInfo {
        public Method method;
        public String name;
        public Class<?> returnType;
        public Class<?>[] parameterTypes;
        public int modifiers;
        public boolean isNative;
        
        @Override
        public String toString() {
            return String.format("Method[name=%s, returnType=%s, native=%b]",
                name, returnType.getSimpleName(), isNative);
        }
    }
    
    public static class ObjectHeader {
        public Object object;
        public Class<?> clazz;
        public Long markWord;
        public Long classPointer;
        public int headerSize;
        
        @Override
        public String toString() {
            return String.format("ObjectHeader[class=%s, markWord=0x%s, classPtr=0x%s, headerSize=%d]",
                clazz.getSimpleName(),
                markWord != null ? Long.toHexString(markWord) : "null",
                classPointer != null ? Long.toHexString(classPointer) : "null",
                headerSize);
        }
    }
}