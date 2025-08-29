package com.jvmu.jdi;

import java.util.List;
import java.util.ArrayList;

/**
 * Self ClassLoader Reference - JDI-like ClassLoaderReference for class loader inspection
 */
public class SelfClassLoaderReference extends SelfObjectReference {
    
    private final ClassLoader classLoader;
    
    public SelfClassLoaderReference(SelfVirtualMachine vm, ClassLoader classLoader) {
        super(vm, classLoader, vm.getOrCreateObjectId(classLoader));
        this.classLoader = classLoader;
    }
    
    /**
     * Get all classes defined by this class loader
     */
    public List<SelfReferenceType> definedClasses() {
        List<SelfReferenceType> classes = new ArrayList<>();
        
        // Get all loaded classes and filter by class loader
        List<Class<?>> allClasses = virtualMachine().getAllLoadedClasses();
        for (Class<?> clazz : allClasses) {
            if (clazz.getClassLoader() == classLoader) {
                classes.add(new SelfReferenceType(virtualMachine(), clazz));
            }
        }
        
        return classes;
    }
    
    /**
     * Get all classes visible to this class loader
     */
    public List<SelfReferenceType> visibleClasses() {
        List<SelfReferenceType> classes = new ArrayList<>();
        
        // Include all defined classes
        classes.addAll(definedClasses());
        
        // Include classes from parent class loaders
        ClassLoader parent = classLoader != null ? classLoader.getParent() : null;
        while (parent != null) {
            SelfClassLoaderReference parentRef = new SelfClassLoaderReference(virtualMachine(), parent);
            classes.addAll(parentRef.definedClasses());
            parent = parent.getParent();
        }
        
        // Include bootstrap classes (null class loader)
        if (classLoader != null) {
            List<Class<?>> allClasses = virtualMachine().getAllLoadedClasses();
            for (Class<?> clazz : allClasses) {
                if (clazz.getClassLoader() == null) {
                    classes.add(new SelfReferenceType(virtualMachine(), clazz));
                }
            }
        }
        
        return classes;
    }
    
    /**
     * Get classes by name pattern
     */
    public List<SelfReferenceType> classesByName(String className) {
        List<SelfReferenceType> result = new ArrayList<>();
        
        for (SelfReferenceType refType : visibleClasses()) {
            if (refType.name().equals(className) || 
                refType.name().contains(className) ||
                className.contains("*") && matchesPattern(refType.name(), className)) {
                result.add(refType);
            }
        }
        
        return result;
    }
    
    /**
     * Load class by name
     */
    public SelfReferenceType loadClass(String className) {
        try {
            Class<?> clazz;
            if (classLoader != null) {
                clazz = classLoader.loadClass(className);
            } else {
                // Bootstrap class loader
                clazz = Class.forName(className, true, null);
            }
            return new SelfReferenceType(virtualMachine(), clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
    
    /**
     * Get parent class loader
     */
    public SelfClassLoaderReference parent() {
        if (classLoader == null) {
            return null; // Bootstrap class loader has no parent
        }
        
        ClassLoader parent = classLoader.getParent();
        return parent != null ? new SelfClassLoaderReference(virtualMachine(), parent) : null;
    }
    
    /**
     * Check if this is the bootstrap class loader
     */
    public boolean isBootstrapClassLoader() {
        return classLoader == null;
    }
    
    /**
     * Check if this is a system class loader
     */
    public boolean isSystemClassLoader() {
        return classLoader == ClassLoader.getSystemClassLoader();
    }
    
    /**
     * Get class loader name (Java 9+)
     */
    public String name() {
        if (classLoader == null) {
            return "bootstrap";
        }
        
        try {
            // Try to get name using reflection (Java 9+)
            java.lang.reflect.Method getNameMethod = ClassLoader.class.getDeclaredMethod("getName");
            String name = (String) getNameMethod.invoke(classLoader);
            return name != null ? name : classLoader.getClass().getSimpleName();
        } catch (Exception e) {
            // Fall back to class name
            return classLoader.getClass().getSimpleName();
        }
    }
    
    /**
     * Get the underlying ClassLoader
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }
    
    /**
     * Simple pattern matching for class names
     */
    private boolean matchesPattern(String className, String pattern) {
        if (!pattern.contains("*")) {
            return className.equals(pattern);
        }
        
        // Simple wildcard matching
        String regex = pattern.replace(".", "\\.").replace("*", ".*");
        return className.matches(regex);
    }
    
    @Override
    public String toString() {
        return String.format("SelfClassLoaderReference[name=%s, classes=%d]",
            name(), definedClasses().size());
    }
}