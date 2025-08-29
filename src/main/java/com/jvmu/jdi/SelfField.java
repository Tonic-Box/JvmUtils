package com.jvmu.jdi;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Self Field - JDI-like Field wrapper for field inspection and manipulation
 */
public class SelfField {
    
    private final SelfVirtualMachine vm;
    private final SelfReferenceType declaringType;
    private final Field field;
    private final String signature;
    private final String genericSignature;
    
    public SelfField(SelfVirtualMachine vm, SelfReferenceType declaringType, Field field) {
        this.vm = vm;
        this.declaringType = declaringType;
        this.field = field;
        this.signature = getFieldSignature(field.getType());
        this.genericSignature = getGenericFieldSignature(field);
    }
    
    /**
     * Get field name
     */
    public String name() {
        return field.getName();
    }
    
    /**
     * Get field signature
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
     * Get field type
     */
    public SelfReferenceType type() {
        return new SelfReferenceType(vm, field.getType());
    }
    
    /**
     * Get declaring type
     */
    public SelfReferenceType declaringType() {
        return declaringType;
    }
    
    /**
     * Check if field is transient
     */
    public boolean isTransient() {
        return Modifier.isTransient(field.getModifiers());
    }
    
    /**
     * Check if field is volatile
     */
    public boolean isVolatile() {
        return Modifier.isVolatile(field.getModifiers());
    }
    
    /**
     * Check if field is enum constant
     */
    public boolean isEnumConstant() {
        return field.isEnumConstant();
    }
    
    /**
     * Check if field is synthetic
     */
    public boolean isSynthetic() {
        return field.isSynthetic();
    }
    
    /**
     * Check if field is public
     */
    public boolean isPublic() {
        return Modifier.isPublic(field.getModifiers());
    }
    
    /**
     * Check if field is private
     */
    public boolean isPrivate() {
        return Modifier.isPrivate(field.getModifiers());
    }
    
    /**
     * Check if field is protected
     */
    public boolean isProtected() {
        return Modifier.isProtected(field.getModifiers());
    }
    
    /**
     * Check if field is package private
     */
    public boolean isPackagePrivate() {
        return !isPublic() && !isPrivate() && !isProtected();
    }
    
    /**
     * Check if field is static
     */
    public boolean isStatic() {
        return Modifier.isStatic(field.getModifiers());
    }
    
    /**
     * Check if field is final
     */
    public boolean isFinal() {
        return Modifier.isFinal(field.getModifiers());
    }
    
    /**
     * Get field modifiers
     */
    public int modifiers() {
        return field.getModifiers();
    }
    
    /**
     * Get field value from object
     */
    public Object getValue(SelfObjectReference object) {
        try {
            field.setAccessible(true);
            return field.get(object.getJavaObject());
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set field value in object
     */
    public void setValue(SelfObjectReference object, Object value) {
        try {
            field.setAccessible(true);
            field.set(object.getJavaObject(), value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get static field value
     */
    public Object getValue() {
        if (!isStatic()) {
            throw new IllegalArgumentException("Field is not static");
        }
        try {
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get static field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set static field value
     */
    public void setValue(Object value) {
        if (!isStatic()) {
            throw new IllegalArgumentException("Field is not static");
        }
        try {
            field.setAccessible(true);
            field.set(null, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set static field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get the underlying Field object
     */
    public Field getField() {
        return field;
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    /**
     * Generate field signature
     */
    private String getFieldSignature(Class<?> type) {
        if (type.isPrimitive()) {
            if (type == boolean.class) return "Z";
            if (type == byte.class) return "B";
            if (type == char.class) return "C";
            if (type == short.class) return "S";
            if (type == int.class) return "I";
            if (type == long.class) return "J";
            if (type == float.class) return "F";
            if (type == double.class) return "D";
        }
        
        if (type.isArray()) {
            return "[" + getFieldSignature(type.getComponentType());
        }
        
        return "L" + type.getName().replace('.', '/') + ";";
    }
    
    /**
     * Generate generic field signature
     */
    private String getGenericFieldSignature(Field field) {
        // Simplified - would need full generic type analysis
        return getFieldSignature(field.getType());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfField)) return false;
        SelfField other = (SelfField) obj;
        return field.equals(other.field) && declaringType.equals(other.declaringType);
    }
    
    @Override
    public int hashCode() {
        return field.hashCode() ^ declaringType.hashCode();
    }
    
    @Override
    public String toString() {
        return String.format("SelfField[%s %s.%s]", 
            signature, declaringType.name(), name());
    }
}