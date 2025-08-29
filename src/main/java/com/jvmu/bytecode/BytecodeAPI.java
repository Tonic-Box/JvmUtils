package com.jvmu.bytecode;

import com.jvmu.module.ModuleBootstrap;

import java.lang.reflect.*;
import java.util.*;

/**
 * Static API for Bytecode Analysis and Class Inspection
 *
 * Provides high-level interface for:
 * - Analyzing existing class structure and bytecode
 * - Extracting bytecode from loaded classes  
 * - Method and field inspection
 * - Access to JDK 11 internal reflection infrastructure
 *
 * Note: Complex bytecode generation requires proper ClassFileAssembler implementation
 */
public class BytecodeAPI {
    
    private static volatile boolean initialized = false;
    
    static {
        initialize();
    }
    
    private static void initialize() {
        if (!initialized) {
            if (ModuleBootstrap.getUnsafe() == null) {
                throw new RuntimeException("BytecodeAPI requires Unsafe access");
            }
            initialized = true;
        }
    }
    
    // =========================================================================
    // CLASS ANALYSIS API
    // =========================================================================
    
    /**
     * Analyze bytecode structure of a class
     */
    public static void analyzeClass(Class<?> clazz) {
        System.out.println("=== CLASS ANALYSIS: " + clazz.getName() + " ===");
        System.out.println("Superclass: " + (clazz.getSuperclass() != null ? clazz.getSuperclass().getName() : "none"));
        System.out.println("Interfaces: " + Arrays.toString(clazz.getInterfaces()));
        System.out.println("Methods: " + clazz.getDeclaredMethods().length);
        System.out.println("Fields: " + clazz.getDeclaredFields().length);
        System.out.println("Constructors: " + clazz.getDeclaredConstructors().length);
        System.out.println("Modifiers: " + Modifier.toString(clazz.getModifiers()));
        
        // Analyze methods
        for (Method method : clazz.getDeclaredMethods()) {
            System.out.println("  Method: " + method.getName() + " " + method.getParameterTypes().length + " params");
        }
        
        // Show bytecode size if available
        try {
            byte[] bytecode = BytecodeManipulator.extractBytecode(clazz);
            if (bytecode.length > 0) {
                System.out.println("Bytecode Size: " + bytecode.length + " bytes");
            }
        } catch (Exception e) {
            System.out.println("Bytecode: Unable to extract");
        }
    }
    
    /**
     * Get detailed information about a class
     */
    public static ClassInfo getClassInfo(Class<?> clazz) {
        return new ClassInfo(clazz);
    }
    
    /**
     * Check if bytecode generation infrastructure is available
     */
    public static boolean isBytecodeGenerationAvailable() {
        try {
            // Check if JDK 11 internal classes are available
            Class.forName("jdk.internal.reflect.MagicAccessorImpl");
            Class.forName("jdk.internal.reflect.ClassDefiner");
            Class.forName("jdk.internal.reflect.ClassFileAssembler");
            return ModuleBootstrap.getUnsafe() != null;
        } catch (Exception e) {
            return false;
        }
    }
    
    // =========================================================================
    // BYTECODE GENERATION API
    // =========================================================================
    
    /**
     * Generate a simple class that returns a string constant
     */
    public static Class<?> generateSimpleClass(String className, String returnValue) {
        return ArbitraryBytecodeGenerator.generateClass(className, assembler -> {
            // Generate class header
            assembler.emitMagicAndVersion();
            
            // Constant pool setup
            assembler.emitShort((short) 5); // CP size
            
            // CP[1] = class name UTF8
            assembler.emitConstantPoolUTF8(className.replace('.', '/'));
            // CP[2] = this class
            assembler.emitConstantPoolClass((short) 1);
            // CP[3] = java/lang/Object UTF8
            assembler.emitConstantPoolUTF8("java/lang/Object");
            // CP[4] = Object class
            assembler.emitConstantPoolClass((short) 3);
            
            // Class info
            assembler.emitShort((short) 0x0021); // ACC_PUBLIC | ACC_SUPER
            assembler.emitShort((short) 2); // this class
            assembler.emitShort((short) 4); // super class (Object)
            
            // Interfaces count = 0
            assembler.emitShort((short) 0);
            
            // Fields count = 0
            assembler.emitShort((short) 0);
            
            // Methods count = 0 (minimal class)
            assembler.emitShort((short) 0);
            
            // Attributes count = 0
            assembler.emitShort((short) 0);
        });
    }
    
    /**
     * Generate a class with a method that returns a string
     */
    public static Class<?> generateStringClass(String className, String methodName, String returnValue) {
        return ArbitraryBytecodeGenerator.generateClass(className, assembler -> {
            // Generate class header
            assembler.emitMagicAndVersion();
            
            // Constant pool setup
            assembler.emitShort((short) 9); // CP size (8 entries + 1 for 0-based)
            
            // CP[1] = class name UTF8
            assembler.emitConstantPoolUTF8(className.replace('.', '/'));
            // CP[2] = this class
            assembler.emitConstantPoolClass((short) 1);
            // CP[3] = java/lang/Object UTF8
            assembler.emitConstantPoolUTF8("java/lang/Object");
            // CP[4] = Object class
            assembler.emitConstantPoolClass((short) 3);
            // CP[5] = method name UTF8
            assembler.emitConstantPoolUTF8(methodName);
            // CP[6] = method signature UTF8
            assembler.emitConstantPoolUTF8("()Ljava/lang/String;");
            // CP[7] = return value UTF8
            assembler.emitConstantPoolUTF8(returnValue);
            // CP[8] = string constant
            assembler.emitConstantPoolString((short) 7);
            
            // Class info (abstract class since methods have no implementation)
            assembler.emitShort((short) 0x0421); // ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT
            assembler.emitShort((short) 2); // this class
            assembler.emitShort((short) 4); // super class (Object)
            
            // Interfaces count = 0
            assembler.emitShort((short) 0);
            
            // Fields count = 0
            assembler.emitShort((short) 0);
            
            // Methods count = 1
            assembler.emitShort((short) 1);
            
            // Method: public abstract String methodName() (non-static since static can't be abstract)
            assembler.emitShort((short) 0x0401); // ACC_PUBLIC | ACC_ABSTRACT
            assembler.emitShort((short) 5); // name index (method name)
            assembler.emitShort((short) 6); // descriptor index (signature)
            assembler.emitShort((short) 0); // attributes count = 0 (no Code attribute - abstract method)
            
            // Class attributes count = 0
            assembler.emitShort((short) 0);
        });
    }
    
    /**
     * Generate a proxy class that implements an interface
     */
    public static Class<?> generateProxyClass(String className, String interfaceClassName, String methodName) {
        return ArbitraryBytecodeGenerator.generateClass(className, assembler -> {
            // Generate class header
            assembler.emitMagicAndVersion();
            
            // Constant pool setup
            assembler.emitShort((short) 10); // CP size
            
            // CP[1] = class name UTF8
            assembler.emitConstantPoolUTF8(className.replace('.', '/'));
            // CP[2] = this class
            assembler.emitConstantPoolClass((short) 1);
            // CP[3] = java/lang/Object UTF8
            assembler.emitConstantPoolUTF8("java/lang/Object");
            // CP[4] = Object class
            assembler.emitConstantPoolClass((short) 3);
            // CP[5] = interface name UTF8
            assembler.emitConstantPoolUTF8(interfaceClassName.replace('.', '/'));
            // CP[6] = interface class
            assembler.emitConstantPoolClass((short) 5);
            // CP[7] = "<init>" UTF8
            assembler.emitConstantPoolUTF8("<init>");
            // CP[8] = "()V" UTF8
            assembler.emitConstantPoolUTF8("()V");
            // CP[9] = method name UTF8
            assembler.emitConstantPoolUTF8(methodName);
            
            // Class info (abstract class)
            assembler.emitShort((short) 0x0421); // ACC_PUBLIC | ACC_SUPER | ACC_ABSTRACT
            assembler.emitShort((short) 2); // this class
            assembler.emitShort((short) 4); // super class (Object)
            
            // Interfaces count = 1
            assembler.emitShort((short) 1);
            assembler.emitShort((short) 6); // interface index
            
            // Fields count = 0
            assembler.emitShort((short) 0);
            
            // Methods count = 1 (just interface method, no constructor)
            assembler.emitShort((short) 1);
            
            // Method: Abstract interface method
            assembler.emitShort((short) 0x0401); // ACC_PUBLIC | ACC_ABSTRACT
            assembler.emitShort((short) 9); // name index (method name)
            assembler.emitShort((short) 8); // descriptor index ("()V")
            assembler.emitShort((short) 0); // attributes count = 0 (no Code attribute - abstract method)
            
            // Class attributes count = 0
            assembler.emitShort((short) 0);
        });
    }
    
    // =========================================================================
    // CLASS INFORMATION WRAPPER
    // =========================================================================
    
    public static class ClassInfo {
        private final Class<?> clazz;
        private final byte[] bytecode;
        
        public ClassInfo(Class<?> clazz) {
            this.clazz = clazz;
            this.bytecode = BytecodeManipulator.extractBytecode(clazz);
        }
        
        public String getName() { return clazz.getName(); }
        public String getSimpleName() { return clazz.getSimpleName(); }
        public Class<?> getSuperclass() { return clazz.getSuperclass(); }
        public Class<?>[] getInterfaces() { return clazz.getInterfaces(); }
        public Method[] getMethods() { return clazz.getDeclaredMethods(); }
        public Field[] getFields() { return clazz.getDeclaredFields(); }
        public Constructor<?>[] getConstructors() { return clazz.getDeclaredConstructors(); }
        public int getModifiers() { return clazz.getModifiers(); }
        public byte[] getBytecode() { return bytecode.clone(); }
        public int getBytecodeSize() { return bytecode.length; }
        
        public boolean isValid() {
            return bytecode.length > 4 && 
                   ((bytecode[0] & 0xFF) << 24 | (bytecode[1] & 0xFF) << 16 | 
                    (bytecode[2] & 0xFF) << 8 | (bytecode[3] & 0xFF)) == 0xCAFEBABE;
        }
        
        public String getMagicNumber() {
            if (bytecode.length < 4) return "Invalid";
            int magic = ((bytecode[0] & 0xFF) << 24) | ((bytecode[1] & 0xFF) << 16) | 
                       ((bytecode[2] & 0xFF) << 8) | (bytecode[3] & 0xFF);
            return "0x" + Integer.toHexString(magic);
        }
        
        @Override
        public String toString() {
            return "ClassInfo{name='" + getName() + "', bytecode=" + getBytecodeSize() + " bytes}";
        }
    }
}