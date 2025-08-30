package com.jvmu.util;

import org.objectweb.asm.*;
import static org.objectweb.asm.Opcodes.*;

/**
 * ASMTestClassGenerator - Generates real bytecode using ASM for class redefinition testing
 * 
 * Creates both original and modified versions of test classes to demonstrate
 * actual working class redefinition with behavior changes.
 */
public class ASMTestClassGenerator {
    
    /**
     * Generate the original version of a test class
     */
    public static byte[] generateOriginalTestClass(String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // Define class
        cw.visit(V11, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), null, 
                "java/lang/Object", null);
        
        // Generate default constructor
        generateConstructor(cw);
        
        // Generate original calculate method: return a + b * 2
        generateOriginalCalculateMethod(cw);
        
        // Generate original processString method: return "original: " + input.toUpperCase()
        generateOriginalProcessStringMethod(cw);
        
        // Generate original getValue method: return 42
        generateOriginalGetValueMethod(cw);
        
        // Generate original isActive method: return true
        generateOriginalIsActiveMethod(cw);
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    /**
     * Generate the modified version of a test class with different behavior
     */
    public static byte[] generateModifiedTestClass(String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // Define class
        cw.visit(V11, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), null, 
                "java/lang/Object", null);
        
        // Generate default constructor
        generateConstructor(cw);
        
        // Generate modified calculate method: return a * b + 10
        generateModifiedCalculateMethod(cw);
        
        // Generate modified processString method: return "MODIFIED: " + input.toLowerCase()
        generateModifiedProcessStringMethod(cw);
        
        // Generate modified getValue method: return 99
        generateModifiedGetValueMethod(cw);
        
        // Generate modified isActive method: return false
        generateModifiedIsActiveMethod(cw);
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    /**
     * Generate enhanced version with additional functionality
     */
    public static byte[] generateEnhancedTestClass(String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        
        // Define class
        cw.visit(V11, ACC_PUBLIC + ACC_SUPER, className.replace('.', '/'), null, 
                "java/lang/Object", null);
        
        // Generate default constructor
        generateConstructor(cw);
        
        // Generate enhanced calculate method: return (a + b) * (a - b) + 100
        generateEnhancedCalculateMethod(cw);
        
        // Generate enhanced processString method: return "ENHANCED[" + input.length() + "]: " + input
        generateEnhancedProcessStringMethod(cw);
        
        // Generate enhanced getValue method: return System.currentTimeMillis() % 1000
        generateEnhancedGetValueMethod(cw);
        
        // Generate enhanced isActive method: return input parameter based
        generateEnhancedIsActiveMethod(cw);
        
        // Add new method: getSignature
        generateSignatureMethod(cw);
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    // Original methods
    private static void generateOriginalCalculateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "calculate", "(II)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1); // load a
        mv.visitVarInsn(ILOAD, 2); // load b
        mv.visitInsn(ICONST_2);    // load 2
        mv.visitInsn(IMUL);        // b * 2
        mv.visitInsn(IADD);        // a + (b * 2)
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateOriginalProcessStringMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "processString", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("original: ");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toUpperCase", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateOriginalGetValueMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getValue", "()I", null, null);
        mv.visitCode();
        mv.visitIntInsn(BIPUSH, 42);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateOriginalIsActiveMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "isActive", "()Z", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_1); // true
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    // Modified methods (different behavior)
    private static void generateModifiedCalculateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "calculate", "(II)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1); // load a
        mv.visitVarInsn(ILOAD, 2); // load b
        mv.visitInsn(IMUL);        // a * b
        mv.visitIntInsn(BIPUSH, 10); // load 10
        mv.visitInsn(IADD);        // (a * b) + 10
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateModifiedProcessStringMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "processString", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("MODIFIED: ");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toLowerCase", "()Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateModifiedGetValueMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getValue", "()I", null, null);
        mv.visitCode();
        mv.visitIntInsn(BIPUSH, 99);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateModifiedIsActiveMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "isActive", "()Z", null, null);
        mv.visitCode();
        mv.visitInsn(ICONST_0); // false
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    // Enhanced methods (more complex behavior)
    private static void generateEnhancedCalculateMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "calculate", "(II)I", null, null);
        mv.visitCode();
        mv.visitVarInsn(ILOAD, 1); // load a
        mv.visitVarInsn(ILOAD, 2); // load b
        mv.visitInsn(IADD);        // a + b
        mv.visitVarInsn(ILOAD, 1); // load a
        mv.visitVarInsn(ILOAD, 2); // load b
        mv.visitInsn(ISUB);        // a - b
        mv.visitInsn(IMUL);        // (a + b) * (a - b)
        mv.visitIntInsn(BIPUSH, 100); // load 100
        mv.visitInsn(IADD);        // result + 100
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateEnhancedProcessStringMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "processString", "(Ljava/lang/String;)Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("ENHANCED[");
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "length", "()I", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(I)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitLdcInsn("]: ");
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateEnhancedGetValueMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getValue", "()I", null, null);
        mv.visitCode();
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitLdcInsn(1000L);
        mv.visitInsn(LREM);
        mv.visitInsn(L2I);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateEnhancedIsActiveMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "isActive", "()Z", null, null);
        mv.visitCode();
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitLdcInsn(2L);
        mv.visitInsn(LREM);
        mv.visitLdcInsn(0L);
        mv.visitInsn(LCMP);
        Label labelFalse = new Label();
        mv.visitJumpInsn(IFNE, labelFalse);
        mv.visitInsn(ICONST_1); // true
        mv.visitInsn(IRETURN);
        mv.visitLabel(labelFalse);
        mv.visitInsn(ICONST_0); // false
        mv.visitInsn(IRETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    private static void generateSignatureMethod(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getSignature", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn("ENHANCED_VERSION_");
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "currentTimeMillis", "()J", false);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/String", "valueOf", "(J)Ljava/lang/String;", false);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "concat", "(Ljava/lang/String;)Ljava/lang/String;", false);
        mv.visitInsn(ARETURN);
        mv.visitMaxs(0, 0);
        mv.visitEnd();
    }
    
    /**
     * Generate bytecode info for logging
     */
    public static String getBytecodeInfo(byte[] bytecode) {
        return "ASM-generated bytecode: " + bytecode.length + " bytes, Java 11 class file";
    }
}