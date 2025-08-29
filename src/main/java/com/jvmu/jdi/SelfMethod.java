package com.jvmu.jdi;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.ArrayList;

/**
 * Self Method - JDI-like Method wrapper for method inspection and invocation
 */
public class SelfMethod {
    
    private final SelfVirtualMachine vm;
    private final SelfReferenceType declaringType;
    private final Method method;
    private final String signature;
    private final String genericSignature;
    
    public SelfMethod(SelfVirtualMachine vm, SelfReferenceType declaringType, Method method) {
        this.vm = vm;
        this.declaringType = declaringType;
        this.method = method;
        this.signature = getMethodSignature(method);
        this.genericSignature = getGenericMethodSignature(method);
    }
    
    /**
     * Get method name
     */
    public String name() {
        return method.getName();
    }
    
    /**
     * Get method signature
     */
    public String signature() {
        return signature;
    }
    
    /**
     * Get generic signature
     */
    public String genericSignature() {
        return genericSignature;
    }
    
    /**
     * Get return type
     */
    public SelfReferenceType returnType() {
        return new SelfReferenceType(vm, method.getReturnType());
    }
    
    /**
     * Get declaring type
     */
    public SelfReferenceType declaringType() {
        return declaringType;
    }
    
    /**
     * Get argument types
     */
    public List<SelfReferenceType> argumentTypes() {
        List<SelfReferenceType> types = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            types.add(new SelfReferenceType(vm, paramType));
        }
        return types;
    }
    
    /**
     * Get argument type names
     */
    public List<String> argumentTypeNames() {
        List<String> names = new ArrayList<>();
        Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            names.add(paramType.getName());
        }
        return names;
    }
    
    /**
     * Get method parameters (Java 8+)
     */
    public List<String> argumentNames() {
        List<String> names = new ArrayList<>();
        Parameter[] parameters = method.getParameters();
        for (Parameter parameter : parameters) {
            names.add(parameter.getName());
        }
        return names;
    }
    
    /**
     * Check if method is constructor
     */
    public boolean isConstructor() {
        return "<init>".equals(method.getName());
    }
    
    /**
     * Check if method is static initializer
     */
    public boolean isStaticInitializer() {
        return "<clinit>".equals(method.getName());
    }
    
    /**
     * Check if method is obsolete
     */
    public boolean isObsolete() {
        // Can't determine from Method object
        return false;
    }
    
    /**
     * Check if method is synthetic
     */
    public boolean isSynthetic() {
        return method.isSynthetic();
    }
    
    /**
     * Check if method is bridge
     */
    public boolean isBridge() {
        return method.isBridge();
    }
    
    /**
     * Check if method is var args
     */
    public boolean isVarArgs() {
        return method.isVarArgs();
    }
    
    /**
     * Check if method is native
     */
    public boolean isNative() {
        return Modifier.isNative(method.getModifiers());
    }
    
    /**
     * Check if method is abstract
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(method.getModifiers());
    }
    
    /**
     * Check if method is synchronized
     */
    public boolean isSynchronized() {
        return Modifier.isSynchronized(method.getModifiers());
    }
    
    /**
     * Check if method is public
     */
    public boolean isPublic() {
        return Modifier.isPublic(method.getModifiers());
    }
    
    /**
     * Check if method is private
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(method.getModifiers());
    }
    
    /**
     * Check if method is protected
     */
    public boolean isProtected() {
        return Modifier.isProtected(method.getModifiers());
    }
    
    /**
     * Check if method is package private
     */
    public boolean isPackagePrivate() {
        return !isPublic() && !isPrivate() && !isProtected();
    }
    
    /**
     * Check if method is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(method.getModifiers());
    }
    
    /**
     * Check if method is final
     */
    public boolean isFinal() {
        return Modifier.isFinal(method.getModifiers());
    }
    
    /**
     * Get method modifiers
     */
    public int modifiers() {
        return method.getModifiers();
    }
    
    /**
     * Get all line locations for this method
     */
    public List<SelfLocation> allLineLocations() {
        List<SelfLocation> locations = new ArrayList<>();
        // Would need debug information for accurate line locations
        // For now, create a basic location for method entry
        locations.add(new SelfLocation(vm, this, 1));
        return locations;
    }
    
    /**
     * Get line locations by line number
     */
    public List<SelfLocation> locationsOfLine(int lineNumber) {
        List<SelfLocation> locations = new ArrayList<>();
        // Would need debug information for accurate line mapping
        if (lineNumber >= 1) {
            locations.add(new SelfLocation(vm, this, lineNumber));
        }
        return locations;
    }
    
    /**
     * Get bytecode information
     */
    public byte[] bytecodes() {
        // Would need access to method bytecode
        return new byte[0];
    }
    
    /**
     * Invoke method on object
     */
    public Object invoke(SelfObjectReference object, Object... args) {
        try {
            method.setAccessible(true);
            return method.invoke(object.getJavaObject(), args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + e.getMessage(), e);
        }
    }
    
    /**
     * Invoke static method
     */
    public Object invoke(Object... args) {
        if (!isStatic()) {
            throw new IllegalArgumentException("Method is not static");
        }
        try {
            method.setAccessible(true);
            return method.invoke(null, args);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke static method: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the underlying Method object
     */
    public Method getMethod() {
        return method;
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    /**
     * Generate method signature
     */
    private String getMethodSignature(Method method) {
        StringBuilder sb = new StringBuilder();
        sb.append("(");
        
        Class<?>[] paramTypes = method.getParameterTypes();
        for (Class<?> paramType : paramTypes) {
            sb.append(getTypeSignature(paramType));
        }
        
        sb.append(")");
        sb.append(getTypeSignature(method.getReturnType()));
        
        return sb.toString();
    }
    
    /**
     * Generate generic method signature
     */
    private String getGenericMethodSignature(Method method) {
        // Simplified - would need full generic type analysis
        return getMethodSignature(method);
    }
    
    /**
     * Get type signature
     */
    private String getTypeSignature(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return "Z";
            if (type == byte.class) return "B";
            if (type == char.class) return "C";
            if (type == short.class) return "S";
            if (type == int.class) return "I";
            if (type == long.class) return "J";
            if (type == float.class) return "F";
            if (type == double.class) return "D";
            if (type == void.class) return "V";
        }
        
        if (type.isArray()) {
            return "[" + getTypeSignature(type.getComponentType());
        }
        
        return "L" + type.getName().replace('.', '/') + ";";
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfMethod)) return false;
        SelfMethod other = (SelfMethod) obj;
        return method.equals(other.method) && declaringType.equals(other.declaringType);
    }
    
    @Override
    public int hashCode() {
        return method.hashCode() ^ declaringType.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("SelfMethod[%s %s.%s%s]",
            getTypeSignature(method.getReturnType()), 
            declaringType.name(), 
            name(), 
            signature);
    }
}