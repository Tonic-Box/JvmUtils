package com.jvmu.jdi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * Self Class Object Reference - JDI-like ClassObjectReference for Class object inspection
 */
public class SelfClassObjectReference extends SelfObjectReference {
    
    private final Class<?> reflectedType;
    
    public SelfClassObjectReference(SelfVirtualMachine vm, Class<?> reflectedType) {
        super(vm, reflectedType, vm.getOrCreateObjectId(reflectedType));
        this.reflectedType = reflectedType;
    }
    
    /**
     * Get the type that this Class object reflects
     */
    public SelfReferenceType reflectedType() {
        return new SelfReferenceType(virtualMachine(), reflectedType);
    }
    
    /**
     * Get new instance of the reflected type
     */
    public SelfObjectReference newInstance() {
        try {
            Object instance = reflectedType.getDeclaredConstructor().newInstance();
            return virtualMachine().createObjectReference(instance);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get new instance with constructor arguments
     */
    public SelfObjectReference newInstance(Object... args) {
        try {
            // Find matching constructor
            Class<?>[] argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            
            java.lang.reflect.Constructor<?> constructor = reflectedType.getDeclaredConstructor(argTypes);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(args);
            return virtualMachine().createObjectReference(instance);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to create new instance with args: " + e.getMessage(), e);
        }
    }
    
    /**
     * Invoke static method on the reflected type
     */
    public Object invokeMethod(String methodName, Object... args) {
        try {
            // Find matching static method
            Class<?>[] argTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                argTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            
            Method method = findMethod(methodName, argTypes);
            if (method == null) {
                throw new NoSuchMethodException("No matching method found: " + methodName);
            }
            
            method.setAccessible(true);
            return method.invoke(null, args);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke static method: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get static field value
     */
    public Object getStaticFieldValue(String fieldName) {
        try {
            Field field = reflectedType.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get static field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set static field value
     */
    public void setStaticFieldValue(String fieldName, Object value) {
        try {
            Field field = reflectedType.getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set static field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get all static methods
     */
    public List<SelfMethod> staticMethods() {
        List<SelfMethod> staticMethods = new ArrayList<>();
        SelfReferenceType refType = reflectedType();
        
        for (SelfMethod method : refType.allMethods()) {
            if (method.isStatic()) {
                staticMethods.add(method);
            }
        }
        
        return staticMethods;
    }
    
    /**
     * Get all static fields
     */
    public List<SelfField> staticFields() {
        List<SelfField> staticFields = new ArrayList<>();
        SelfReferenceType refType = reflectedType();
        
        for (SelfField field : refType.allFields()) {
            if (field.isStatic()) {
                staticFields.add(field);
            }
        }
        
        return staticFields;
    }
    
    /**
     * Get class simple name
     */
    public String getSimpleName() {
        return reflectedType.getSimpleName();
    }
    
    /**
     * Get class canonical name
     */
    public String getCanonicalName() {
        return reflectedType.getCanonicalName();
    }
    
    /**
     * Get package name
     */
    public String getPackageName() {
        Package pkg = reflectedType.getPackage();
        return pkg != null ? pkg.getName() : "";
    }
    
    /**
     * Get superclass
     */
    public SelfClassObjectReference getSuperclass() {
        Class<?> superClass = reflectedType.getSuperclass();
        return superClass != null ? new SelfClassObjectReference(virtualMachine(), superClass) : null;
    }
    
    /**
     * Get interfaces
     */
    public List<SelfClassObjectReference> getInterfaces() {
        List<SelfClassObjectReference> interfaces = new ArrayList<>();
        Class<?>[] interfaceClasses = reflectedType.getInterfaces();
        
        for (Class<?> interfaceClass : interfaceClasses) {
            interfaces.add(new SelfClassObjectReference(virtualMachine(), interfaceClass));
        }
        
        return interfaces;
    }
    
    /**
     * Check if this class is assignable from another
     */
    public boolean isAssignableFrom(SelfClassObjectReference other) {
        return reflectedType.isAssignableFrom(other.reflectedType);
    }
    
    /**
     * Check if instance of this class
     */
    public boolean isInstance(SelfObjectReference obj) {
        return reflectedType.isInstance(obj.getJavaObject());
    }
    
    /**
     * Get the underlying Class object
     */
    public Class<?> getReflectedType() {
        return reflectedType;
    }
    
    /**
     * Find method by name and argument types
     */
    private Method findMethod(String methodName, Class<?>[] argTypes) {
        Method[] methods = reflectedType.getDeclaredMethods();
        
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == argTypes.length) {
                    boolean matches = true;
                    for (int i = 0; i < paramTypes.length; i++) {
                        if (!paramTypes[i].isAssignableFrom(argTypes[i])) {
                            matches = false;
                            break;
                        }
                    }
                    if (matches) {
                        return method;
                    }
                }
            }
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("SelfClassObjectReference[class=%s]", reflectedType.getName());
    }
}