package com.jvmu.jdi.types;

import com.jvmu.jdi.vm.SelfObjectReference;
import com.jvmu.jdi.vm.SelfVirtualMachine;
import com.jvmu.jdi.vm.SelfClassLoaderReference;
import com.jvmu.jdi.vm.SelfModuleReference;
import com.jvmu.jdi.vm.SelfClassObjectReference;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Self Reference Type - JDI-like ReferenceType for class/interface inspection
 * 
 * This class wraps a Java Class object to provide JDI ReferenceType-like
 * functionality for inspecting classes, interfaces, and their members.
 */
public class SelfReferenceType {
    
    private final SelfVirtualMachine vm;
    private final Class<?> clazz;
    private final String signature;
    private final String genericSignature;
    
    // Cached data
    private List<SelfField> fields;
    private List<SelfMethod> methods;
    private Map<String, SelfField> fieldCache;
    private Map<String, List<SelfMethod>> methodCache;
    
    public SelfReferenceType(SelfVirtualMachine vm, Class<?> clazz) {
        this.vm = vm;
        this.clazz = clazz;
        this.signature = getClassSignature(clazz);
        this.genericSignature = getGenericClassSignature(clazz);
        this.fieldCache = new HashMap<>();
        this.methodCache = new HashMap<>();
    }
    
    /**
     * Get the class name
     */
    public String name() {
        return clazz.getName();
    }
    
    /**
     * Get the generic signature
     */
    public String genericSignature() {
        return genericSignature;
    }
    
    /**
     * Get the signature
     */
    public String signature() {
        return signature;
    }
    
    /**
     * Get the class loader reference
     */
    public SelfClassLoaderReference classLoader() {
        ClassLoader loader = clazz.getClassLoader();
        return loader != null ? new SelfClassLoaderReference(vm, loader) : null;
    }
    
    /**
     * Get the module reference (Java 9+)
     */
    public SelfModuleReference module() {
        try {
            // Use reflection to access getModule() if available (Java 9+)
            Method getModuleMethod = Class.class.getDeclaredMethod("getModule");
            Object module = getModuleMethod.invoke(clazz);
            return new SelfModuleReference(vm, module);
        } catch (Exception e) {
            // Java 8 or method not available
            return null;
        }
    }
    
    /**
     * Check if this is an interface
     */
    public boolean isInterface() {
        return clazz.isInterface();
    }
    
    /**
     * Check if this is an abstract class
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(clazz.getModifiers());
    }
    
    /**
     * Check if this is a final class
     */
    public boolean isFinal() {
        return Modifier.isFinal(clazz.getModifiers());
    }
    
    /**
     * Check if this is a static class
     */
    public boolean isStatic() {
        return Modifier.isStatic(clazz.getModifiers());
    }
    
    /**
     * Check if this is prepared (always true for loaded classes)
     */
    public boolean isPrepared() {
        return true;
    }
    
    /**
     * Check if this is verified (always true for loaded classes)
     */
    public boolean isVerified() {
        return true;
    }
    
    /**
     * Check if this is initialized
     */
    public boolean isInitialized() {
        // We can't easily determine initialization status, assume true for loaded classes
        return true;
    }
    
    /**
     * Get all visible fields
     */
    public List<SelfField> allFields() {
        if (fields == null) {
            fields = new ArrayList<>();
            Class<?> currentClass = clazz;
            
            while (currentClass != null) {
                Field[] declaredFields = currentClass.getDeclaredFields();
                for (Field field : declaredFields) {
                    fields.add(new SelfField(vm, this, field));
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        
        return new ArrayList<>(fields);
    }
    
    /**
     * Get visible fields (public fields from this class and supertypes)
     */
    public List<SelfField> visibleFields() {
        List<SelfField> visibleFields = new ArrayList<>();
        for (SelfField field : allFields()) {
            if (field.isPublic()) {
                visibleFields.add(field);
            }
        }
        return visibleFields;
    }
    
    /**
     * Get fields by name
     */
    public List<SelfField> fieldsByName(String name) {
        List<SelfField> result = new ArrayList<>();
        for (SelfField field : allFields()) {
            if (name.equals(field.name())) {
                result.add(field);
            }
        }
        return result;
    }
    
    /**
     * Get all methods
     */
    public List<SelfMethod> allMethods() {
        if (methods == null) {
            methods = new ArrayList<>();
            Class<?> currentClass = clazz;
            
            while (currentClass != null) {
                Method[] declaredMethods = currentClass.getDeclaredMethods();
                for (Method method : declaredMethods) {
                    methods.add(new SelfMethod(vm, this, method));
                }
                currentClass = currentClass.getSuperclass();
            }
        }
        
        return new ArrayList<>(methods);
    }
    
    /**
     * Get visible methods (public methods from this class and supertypes)
     */
    public List<SelfMethod> visibleMethods() {
        List<SelfMethod> visibleMethods = new ArrayList<>();
        for (SelfMethod method : allMethods()) {
            if (method.isPublic()) {
                visibleMethods.add(method);
            }
        }
        return visibleMethods;
    }
    
    /**
     * Get methods by name
     */
    public List<SelfMethod> methodsByName(String name) {
        return methodCache.computeIfAbsent(name, n -> {
            List<SelfMethod> result = new ArrayList<>();
            for (SelfMethod method : allMethods()) {
                if (name.equals(method.name())) {
                    result.add(method);
                }
            }
            return result;
        });
    }
    
    /**
     * Get methods by name and signature
     */
    public List<SelfMethod> methodsByName(String name, String signature) {
        List<SelfMethod> candidates = methodsByName(name);
        List<SelfMethod> result = new ArrayList<>();
        
        for (SelfMethod method : candidates) {
            if (signature.equals(method.signature())) {
                result.add(method);
            }
        }
        
        return result;
    }
    
    /**
     * Get all nested types
     */
    public List<SelfReferenceType> nestedTypes() {
        List<SelfReferenceType> nestedTypes = new ArrayList<>();
        Class<?>[] nestedClasses = clazz.getDeclaredClasses();
        
        for (Class<?> nestedClass : nestedClasses) {
            nestedTypes.add(new SelfReferenceType(vm, nestedClass));
        }
        
        return nestedTypes;
    }
    
    /**
     * Get the source name (filename)
     */
    public String sourceName() {
        String name = clazz.getSimpleName();
        // Handle anonymous classes
        if (name.isEmpty()) {
            name = clazz.getName();
            int lastDot = name.lastIndexOf('.');
            if (lastDot >= 0) {
                name = name.substring(lastDot + 1);
            }
        }
        return name + ".java";
    }
    
    /**
     * Get source paths
     */
    public List<String> sourcePaths(String stratum) {
        List<String> paths = new ArrayList<>();
        // Basic implementation - would need debug info for complete paths
        String packageName = clazz.getPackage() != null ? clazz.getPackage().getName() : "";
        String path = packageName.replace('.', '/') + "/" + sourceName();
        paths.add(path);
        return paths;
    }
    
    /**
     * Get available strata
     */
    public List<String> availableStrata() {
        List<String> strata = new ArrayList<>();
        strata.add("Java"); // Default stratum
        return strata;
    }
    
    /**
     * Get default stratum
     */
    public String defaultStratum() {
        return "Java";
    }
    
    /**
     * Get instances of this class (limited implementation)
     */
    public List<SelfObjectReference> instances(long maxInstances) {
        // This would require heap walking - return empty list for now
        // Could potentially use WeakReference tracking or other mechanisms
        return new ArrayList<>();
    }
    
    /**
     * Get class object reference
     */
    public SelfClassObjectReference classObject() {
        return new SelfClassObjectReference(vm, clazz);
    }
    
    /**
     * Get all line locations
     */
    public List<SelfLocation> allLineLocations() {
        List<SelfLocation> locations = new ArrayList<>();
        // Would need debug information for accurate line locations
        // For now, return basic method locations
        for (SelfMethod method : allMethods()) {
            locations.addAll(method.allLineLocations());
        }
        return locations;
    }
    
    /**
     * Get line locations for specific stratum
     */
    public List<SelfLocation> allLineLocations(String stratum, String sourceName) {
        // Simplified implementation
        return allLineLocations();
    }
    
    /**
     * Get locations of line
     */
    public List<SelfLocation> locationsOfLine(int lineNumber) {
        List<SelfLocation> locations = new ArrayList<>();
        // Would need debug information for accurate line mapping
        return locations;
    }
    
    /**
     * Get major version
     */
    public int majorVersion() {
        // Would need access to class file format info
        // Return Java version-based estimate
        String javaVersion = System.getProperty("java.specification.version");
        try {
            double version = Double.parseDouble(javaVersion);
            if (version >= 11) return 55;
            if (version >= 9) return 53;
            if (version >= 8) return 52;
            if (version >= 7) return 51;
            return 50; // Java 6
        } catch (Exception e) {
            return 55; // Default to Java 11
        }
    }
    
    /**
     * Get minor version
     */
    public int minorVersion() {
        return 0; // Typically 0 for Oracle/OpenJDK
    }
    
    /**
     * Get constant pool
     */
    public byte[] constantPool() {
        // Would need access to class file bytecode
        return new byte[0];
    }
    
    /**
     * Get constant pool count
     */
    public int constantPoolCount() {
        return 0; // Would need actual constant pool parsing
    }
    
    /**
     * Get the underlying Class object
     */
    public Class<?> getJavaClass() {
        return clazz;
    }
    
    /**
     * Get the virtual machine this type belongs to
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    /**
     * Generate class signature
     */
    private String getClassSignature(Class<?> clazz) {
        if (clazz.isPrimitive()) {
            if (clazz == boolean.class) return "Z";
            if (clazz == byte.class) return "B";
            if (clazz == char.class) return "C";
            if (clazz == short.class) return "S";
            if (clazz == int.class) return "I";
            if (clazz == long.class) return "J";
            if (clazz == float.class) return "F";
            if (clazz == double.class) return "D";
            if (clazz == void.class) return "V";
        }
        
        if (clazz.isArray()) {
            return "[" + getClassSignature(clazz.getComponentType());
        }
        
        return "L" + clazz.getName().replace('.', '/') + ";";
    }
    
    /**
     * Generate generic class signature
     */
    private String getGenericClassSignature(Class<?> clazz) {
        // Simplified - would need full generic type analysis
        return getClassSignature(clazz);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfReferenceType)) return false;
        SelfReferenceType other = (SelfReferenceType) obj;
        return clazz.equals(other.clazz);
    }
    
    @Override
    public int hashCode() {
        return clazz.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("SelfReferenceType[%s]", clazz.getName());
    }
}