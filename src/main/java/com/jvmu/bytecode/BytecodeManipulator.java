package com.jvmu.bytecode;

import com.jvmu.module.ModuleBootstrap;

import java.lang.reflect.*;

/**
 * Simplified bytecode manipulation API - focuses on analysis and basic operations
 * Complex method manipulation removed to keep only functional parts
 */
public class BytecodeManipulator {

    private static final Object unsafe = ModuleBootstrap.getUnsafe();
    private static final Object internalUnsafe = ModuleBootstrap.getInternalUnsafe();

    /**
     * Extract bytecode from an existing loaded class
     */
    public static byte[] extractBytecode(Class<?> clazz) {
        try {
            // Try to get bytecode via ClassLoader resource
            String classPath = clazz.getName().replace('.', '/') + ".class";
            ClassLoader loader = clazz.getClassLoader();
            if (loader == null) loader = ClassLoader.getSystemClassLoader();

            var resource = loader.getResourceAsStream(classPath);
            if (resource != null) {
                return resource.readAllBytes();
            }

            // Return empty for unsupported cases
            return new byte[0];

        } catch (Exception e) {
            throw new RuntimeException("Failed to extract bytecode for: " + clazz.getName(), e);
        }
    }

    /**
     * Replace entire class bytecode by creating new class with modified bytecode
     */
    public static Class<?> replaceClass(Class<?> originalClass, byte[] newBytecode) {
        try {
            String className = originalClass.getName() + "_Modified_" + System.currentTimeMillis();

            // Use Unsafe.defineClass to load modified bytecode
            Object unsafeInstance = ModuleBootstrap.getUnsafe();
            Method defineClass = unsafeInstance.getClass().getDeclaredMethod("defineClass",
                    String.class, byte[].class, int.class, int.class,
                    ClassLoader.class, java.security.ProtectionDomain.class);
            defineClass.setAccessible(true);

            Class<?> modifiedClass = (Class<?>) defineClass.invoke(unsafeInstance,
                    className, newBytecode, 0, newBytecode.length,
                    originalClass.getClassLoader(), null);

            return modifiedClass;

        } catch (Exception e) {
            throw new RuntimeException("Failed to replace class bytecode", e);
        }
    }

    /**
     * Utility: Print detailed method information
     */
    public static void analyzeMethod(Method method) {
        System.out.println("=== METHOD ANALYSIS ===");
        System.out.println("Name: " + method.getName());
        System.out.println("Declaring Class: " + method.getDeclaringClass().getName());
        System.out.println("Return Type: " + method.getReturnType().getName());
        System.out.println("Parameter Count: " + method.getParameterCount());
        System.out.println("Modifiers: " + Modifier.toString(method.getModifiers()));

        // Show parameter types
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length > 0) {
            System.out.print("Parameters: ");
            for (int i = 0; i < paramTypes.length; i++) {
                System.out.print(paramTypes[i].getSimpleName());
                if (i < paramTypes.length - 1) System.out.print(", ");
            }
            System.out.println();
        }

        try {
            Field methodAccessorField = Method.class.getDeclaredField("methodAccessor");
            methodAccessorField.setAccessible(true);
            Object accessor = methodAccessorField.get(method);
            System.out.println("Method Accessor: " + (accessor != null ? accessor.getClass().getName() : "null"));
        } catch (Exception e) {
            System.out.println("Method Accessor: Unable to access");
        }
    }

    /**
     * Check if class bytecode can be extracted
     */
    public static boolean canExtractBytecode(Class<?> clazz) {
        try {
            byte[] bytecode = extractBytecode(clazz);
            return bytecode.length > 0;
        } catch (Exception e) {
            return false;
        }
    }
}