package com.jvmu.noverify;

import com.jvmu.module.ModuleBootstrap;
import lombok.Getter;
import sun.misc.Unsafe;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * NoVerifyClassLoader - Clean Static API for Bypassing Bytecode Verification
 * This class provides a simple, clean API to define classes in any ClassLoader
 * while bypassing JDK 11's bytecode verification process.
 * Usage:
 *   ClassLoader myLoader = new MyClassLoader();
 *   byte[] bytecode = generateOrLoadBytecode();
 *   Class<?> definedClass = NoVerifyClassLoader.defineClass("com.example.MyClass", bytecode, myLoader);
 */
public class NoVerifyClassLoader {
    
    private static final Object internalUnsafe;
    private static final Method unsafeDefineClassMethod;
    /**
     * -- GETTER --
     *  Check if the NoVerifyClassLoader API is available
     *
     * @return true if the API can be used, false if privileged access is not available
     */
    @Getter
    private static final boolean available;
    
    static {
        Object unsafe = null;
        Method defineMethod = null;
        boolean isAvailable = false;
        
        try {
            unsafe = ModuleBootstrap.getInternalUnsafe();
            if (unsafe != null) {
                defineMethod = unsafe.getClass().getMethod(
                    "defineClass", 
                    String.class, 
                    byte[].class, 
                    int.class, 
                    int.class,
                    ClassLoader.class, 
                    ProtectionDomain.class
                );
                isAvailable = true;
            }
        } catch (Exception e) {
            // Initialization failed - API will be unavailable
        }
        
        internalUnsafe = unsafe;
        unsafeDefineClassMethod = defineMethod;
        available = isAvailable;
    }
    
    /**
     * Define a class in the specified ClassLoader bypassing bytecode verification
     * 
     * @param className The fully qualified class name (e.g., "com.example.MyClass")
     * @param bytecode The class bytecode as byte array
     * @param classLoader The target ClassLoader (null for bootstrap ClassLoader)
     * @return The defined Class object
     * @throws IllegalStateException if the API is not available (requires privileged access)
     * @throws RuntimeException if class definition fails
     */
    public static Class<?> defineClass(String className, byte[] bytecode, ClassLoader classLoader) {
        return defineClass(className, bytecode, classLoader, null);
    }
    
    /**
     * Define a class in the specified ClassLoader with a ProtectionDomain, bypassing bytecode verification
     * 
     * @param className The fully qualified class name (e.g., "com.example.MyClass")
     * @param bytecode The class bytecode as byte array
     * @param classLoader The target ClassLoader (null for bootstrap ClassLoader)
     * @param protectionDomain The ProtectionDomain for the class (null for default)
     * @return The defined Class object
     * @throws IllegalStateException if the API is not available (requires privileged access)
     * @throws RuntimeException if class definition fails
     */
    public static Class<?> defineClass(String className, byte[] bytecode, ClassLoader classLoader, ProtectionDomain protectionDomain) {
        if (!available) {
            throw new IllegalStateException(
                "NoVerifyClassLoader API is not available. Requires privileged JVM access via ModuleBootstrap.");
        }
        
        if (className == null) {
            throw new IllegalArgumentException("className cannot be null");
        }
        
        if (bytecode == null) {
            throw new IllegalArgumentException("bytecode cannot be null");
        }
        
        if (bytecode.length == 0) {
            throw new IllegalArgumentException("bytecode cannot be empty");
        }
        
        try {
            return (Class<?>) unsafeDefineClassMethod.invoke(
                internalUnsafe,
                className,
                bytecode,
                0,
                bytecode.length,
                classLoader,
                protectionDomain
            );
        } catch (Exception e) {
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            throw new RuntimeException(
                "Failed to define class '" + className + "' in ClassLoader: " + classLoader, 
                cause
            );
        }
    }

    /**
     * Get the status of the NoVerifyClassLoader API
     * 
     * @return Status string describing availability and dependencies
     */
    public static String getStatus() {
        if (available) {
            return "NoVerifyClassLoader: Available ✓";
        } else {
            return "NoVerifyClassLoader: Not Available ✗ (requires privileged JVM access)";
        }
    }
    
    /**
     * Define a class in the System ClassLoader bypassing verification
     * 
     * @param className The fully qualified class name
     * @param bytecode The class bytecode
     * @return The defined Class object
     */
    public static Class<?> defineInSystemClassLoader(String className, byte[] bytecode) {
        return defineClass(className, bytecode, ClassLoader.getSystemClassLoader());
    }
    
    /**
     * Define a class in the Bootstrap ClassLoader bypassing verification
     * 
     * @param className The fully qualified class name
     * @param bytecode The class bytecode
     * @return The defined Class object
     */
    public static Class<?> defineInBootstrapClassLoader(String className, byte[] bytecode) {
        return defineClass(className, bytecode, null);
    }
    
    /**
     * Define a class in the Platform ClassLoader bypassing verification
     * 
     * @param className The fully qualified class name
     * @param bytecode The class bytecode
     * @return The defined Class object
     */
    public static Class<?> defineInPlatformClassLoader(String className, byte[] bytecode) {
        return defineClass(className, bytecode, ClassLoader.getPlatformClassLoader());
    }
    
    /**
     * Define a class in the current thread's context ClassLoader bypassing verification
     * 
     * @param className The fully qualified class name
     * @param bytecode The class bytecode
     * @return The defined Class object
     */
    public static Class<?> defineInContextClassLoader(String className, byte[] bytecode) {
        ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
        if (contextLoader == null) {
            contextLoader = ClassLoader.getSystemClassLoader();
        }
        return defineClass(className, bytecode, contextLoader);
    }
    
    /**
     * Validate bytecode basic structure (magic number check)
     * 
     * @param bytecode The bytecode to validate
     * @return true if bytecode appears to be a valid class file
     */
    public static boolean isValidBytecode(byte[] bytecode) {
        if (bytecode == null || bytecode.length < 8) {
            return false;
        }
        
        // Check magic number (0xCAFEBABE)
        int magic = ((bytecode[0] & 0xFF) << 24) | 
                   ((bytecode[1] & 0xFF) << 16) | 
                   ((bytecode[2] & 0xFF) << 8) | 
                   (bytecode[3] & 0xFF);
        
        return magic == 0xCAFEBABE;
    }
    
    /**
     * Define a class with bytecode validation bypassing verification
     * 
     * @param className The fully qualified class name
     * @param bytecode The class bytecode
     * @param classLoader The target ClassLoader
     * @return The defined Class object
     * @throws IllegalArgumentException if bytecode is invalid
     */
    public static Class<?> defineClassWithValidation(String className, byte[] bytecode, ClassLoader classLoader) {
        if (!isValidBytecode(bytecode)) {
            throw new IllegalArgumentException("Invalid bytecode: bad magic number or insufficient length");
        }
        
        return defineClass(className, bytecode, classLoader);
    }
}