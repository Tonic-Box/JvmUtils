package com.jvmu.demos;

import com.jvmu.noverify.NoVerifyClassLoader;

import java.net.URLClassLoader;
import java.net.URL;

/**
 * Clean demonstration of the ClassUnloader API
 */
public class NoVerifyDemo {
    
    public static void main(String[] args) {
        System.out.println("=== ClassUnloader API Demo ===");
        System.out.println(NoVerifyClassLoader.getStatus());
        System.out.println();
        
        if (!NoVerifyClassLoader.isAvailable()) {
            System.out.println("API not available - requires privileged access");
            return;
        }
        
        // Generate simple valid bytecode for testing
        byte[] testBytecode = generateSimpleInterfaceBytecode("TestInterface");
        System.out.println("Generated test bytecode: " + testBytecode.length + " bytes");
        System.out.println("Valid bytecode: " + NoVerifyClassLoader.isValidBytecode(testBytecode));
        System.out.println();
        
        // Demo 1: System ClassLoader
        demoSystemClassLoader(testBytecode);
        
        // Demo 2: Custom ClassLoader
        demoCustomClassLoader(testBytecode);
        
        // Demo 3: URLClassLoader
        demoURLClassLoader(testBytecode);
        
        // Demo 4: Bootstrap ClassLoader
        demoBootstrapClassLoader(testBytecode);
        
        System.out.println("=== API Usage Examples ===");
        showUsageExamples();
    }
    
    private static void demoSystemClassLoader(byte[] bytecode) {
        System.out.println("--- System ClassLoader ---");
        try {
            Class<?> result = NoVerifyClassLoader.defineInSystemClassLoader("com.test.SystemClass", bytecode);
            System.out.println("✓ SUCCESS: " + result.getName());
            System.out.println("  ClassLoader: " + result.getClassLoader().getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demoCustomClassLoader(byte[] bytecode) {
        System.out.println("--- Custom ClassLoader ---");
        try {
            ClassLoader customLoader = new ClassLoader() {
                @Override
                public String toString() { return "DemoCustomLoader"; }
            };
            
            Class<?> result = NoVerifyClassLoader.defineClass("com.test.CustomClass", bytecode, customLoader);
            System.out.println("✓ SUCCESS: " + result.getName());
            System.out.println("  ClassLoader: " + result.getClassLoader());
            System.out.println("  Same as expected: " + (result.getClassLoader() == customLoader));
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demoURLClassLoader(byte[] bytecode) {
        System.out.println("--- URLClassLoader ---");
        try {
            URLClassLoader urlLoader = new URLClassLoader(new URL[0]);
            
            Class<?> result = NoVerifyClassLoader.defineClass("com.test.URLClass", bytecode, urlLoader);
            System.out.println("✓ SUCCESS: " + result.getName());
            System.out.println("  ClassLoader: " + result.getClassLoader().getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void demoBootstrapClassLoader(byte[] bytecode) {
        System.out.println("--- Bootstrap ClassLoader ---");
        try {
            Class<?> result = NoVerifyClassLoader.defineInBootstrapClassLoader("com.test.BootstrapClass", bytecode);
            System.out.println("✓ SUCCESS: " + result.getName());
            System.out.println("  ClassLoader: " + result.getClassLoader() + " (null = bootstrap)");
        } catch (Exception e) {
            System.out.println("✗ FAILED: " + e.getMessage());
        }
        System.out.println();
    }
    
    private static void showUsageExamples() {
        System.out.println("// Basic usage:");
        System.out.println("ClassLoader myLoader = new MyClassLoader();");
        System.out.println("byte[] bytecode = loadMyBytecode();");
        System.out.println("Class<?> myClass = ClassUnloader.defineClass(\"com.example.MyClass\", bytecode, myLoader);");
        System.out.println();
        
        System.out.println("// Convenience methods:");
        System.out.println("Class<?> systemClass = ClassUnloader.defineInSystemClassLoader(\"MySystemClass\", bytecode);");
        System.out.println("Class<?> bootstrapClass = ClassUnloader.defineInBootstrapClassLoader(\"MyBootstrapClass\", bytecode);");
        System.out.println();
        
        System.out.println("// With validation:");
        System.out.println("Class<?> validatedClass = ClassUnloader.defineClassWithValidation(\"MyClass\", bytecode, myLoader);");
    }
    
    /**
     * Generate simple interface bytecode for testing
     */
    private static byte[] generateSimpleInterfaceBytecode(String interfaceName) {
        try {
            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.DataOutputStream dos = new java.io.DataOutputStream(baos);
            
            dos.writeInt(0xCAFEBABE); // magic
            dos.writeShort(0);         // minor version
            dos.writeShort(55);        // major version (Java 11)
            
            dos.writeShort(4);         // cp_count = 4 entries
            
            // CP[1] = UTF8 interface name
            dos.writeByte(1);          // CONSTANT_Utf8
            dos.writeUTF(interfaceName);
            
            // CP[2] = Class this interface
            dos.writeByte(7);          // CONSTANT_Class
            dos.writeShort(1);         // name_index
            
            // CP[3] = UTF8 "java/lang/Object"
            dos.writeByte(1);          // CONSTANT_Utf8
            dos.writeUTF("java/lang/Object");
            
            dos.writeShort(0x0601);    // access_flags (PUBLIC | INTERFACE | ABSTRACT)
            dos.writeShort(2);         // this_class
            dos.writeShort(0);         // super_class (0 for interfaces)
            dos.writeShort(0);         // interfaces_count
            dos.writeShort(0);         // fields_count
            dos.writeShort(0);         // methods_count
            dos.writeShort(0);         // attributes_count
            
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate bytecode", e);
        }
    }
}