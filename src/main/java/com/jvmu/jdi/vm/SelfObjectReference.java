package com.jvmu.jdi.vm;

import com.jvmu.jvmti.AdvancedVMAccess;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Self Object Reference - JDI-like ObjectReference for internal objects
 * 
 * This class wraps a Java Object to provide JDI ObjectReference-like
 * functionality for debugging and inspecting objects within the current JVM.
 */
public class SelfObjectReference {
    
    private final SelfVirtualMachine vm;
    private final Object object;
    private final int objectId;
    private final Class<?> objectClass;
    
    public SelfObjectReference(SelfVirtualMachine vm, Object object, int objectId) {
        this.vm = vm;
        this.object = object;
        this.objectId = objectId;
        this.objectClass = object.getClass();
    }
    
    /**
     * Get the wrapped object
     */
    public Object getObject() {
        return object;
    }
    
    /**
     * Get the Java object (alias for getObject for JDI compatibility)
     */
    public Object getJavaObject() {
        return object;
    }
    
    /**
     * Get unique object ID
     */
    public long uniqueID() {
        return objectId;
    }
    
    /**
     * Get the object's class
     */
    public Class<?> referenceType() {
        return objectClass;
    }
    
    /**
     * Get the object's type name
     */
    public String type() {
        return objectClass.getName();
    }
    
    /**
     * Get field value by name
     */
    public Object getValue(String fieldName) {
        try {
            Field field = findField(fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            
            field.setAccessible(true);
            return field.get(object);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get multiple field values
     */
    public Map<String, Object> getValues(List<String> fieldNames) {
        Map<String, Object> values = new HashMap<>();
        
        for (String fieldName : fieldNames) {
            try {
                values.put(fieldName, getValue(fieldName));
            } catch (Exception e) {
                values.put(fieldName, null);
            }
        }
        
        return values;
    }
    
    /**
     * Set field value by name
     */
    public void setValue(String fieldName, Object value) {
        try {
            Field field = findField(fieldName);
            if (field == null) {
                throw new NoSuchFieldException("Field not found: " + fieldName);
            }
            
            field.setAccessible(true);
            field.set(object, value);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field value: " + e.getMessage(), e);
        }
    }
    
    /**
     * Set multiple field values
     */
    public void setValues(Map<String, Object> fieldValues) {
        for (Map.Entry<String, Object> entry : fieldValues.entrySet()) {
            setValue(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Get all field information
     */
    public List<FieldInfo> getAllFields() {
        List<FieldInfo> fieldInfos = new ArrayList<>();
        
        Class<?> currentClass = objectClass;
        while (currentClass != null) {
            Field[] fields = currentClass.getDeclaredFields();
            
            for (Field field : fields) {
                if (!Modifier.isStatic(field.getModifiers())) {
                    fieldInfos.add(new FieldInfo(field, this));
                }
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return fieldInfos;
    }
    
    /**
     * Find field by name in class hierarchy
     */
    private Field findField(String fieldName) {
        Class<?> currentClass = objectClass;
        
        while (currentClass != null) {
            try {
                Field field = currentClass.getDeclaredField(fieldName);
                if (!Modifier.isStatic(field.getModifiers())) {
                    return field;
                }
            } catch (NoSuchFieldException e) {
                // Continue searching in parent class
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return null;
    }
    
    /**
     * Invoke method on this object
     */
    public Object invokeMethod(String methodName, Object... args) {
        try {
            Class<?>[] paramTypes = new Class[args.length];
            for (int i = 0; i < args.length; i++) {
                paramTypes[i] = args[i] != null ? args[i].getClass() : Object.class;
            }
            
            Method method = findMethod(methodName, paramTypes);
            if (method == null) {
                throw new NoSuchMethodException("Method not found: " + methodName);
            }
            
            method.setAccessible(true);
            return method.invoke(object, args);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke method: " + e.getMessage(), e);
        }
    }
    
    /**
     * Find method by name and parameter types
     */
    private Method findMethod(String methodName, Class<?>[] paramTypes) {
        Class<?> currentClass = objectClass;
        
        while (currentClass != null) {
            Method[] methods = currentClass.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(methodName) && 
                    !Modifier.isStatic(method.getModifiers()) &&
                    isCompatibleParameterTypes(method.getParameterTypes(), paramTypes)) {
                    return method;
                }
            }
            
            currentClass = currentClass.getSuperclass();
        }
        
        return null;
    }
    
    /**
     * Check if parameter types are compatible for method invocation
     */
    private boolean isCompatibleParameterTypes(Class<?>[] methodParams, Class<?>[] providedParams) {
        if (methodParams.length != providedParams.length) {
            return false;
        }
        
        for (int i = 0; i < methodParams.length; i++) {
            if (!methodParams[i].isAssignableFrom(providedParams[i]) &&
                !isAutoboxingCompatible(methodParams[i], providedParams[i])) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Check if types are compatible via autoboxing
     */
    private boolean isAutoboxingCompatible(Class<?> target, Class<?> source) {
        if (target.isPrimitive() && !source.isPrimitive()) {
            return getPrimitiveWrapper(target) == source;
        } else if (!target.isPrimitive() && source.isPrimitive()) {
            return target == getPrimitiveWrapper(source);
        }
        return false;
    }
    
    /**
     * Get wrapper class for primitive type
     */
    private Class<?> getPrimitiveWrapper(Class<?> primitive) {
        if (primitive == boolean.class) return Boolean.class;
        if (primitive == byte.class) return Byte.class;
        if (primitive == char.class) return Character.class;
        if (primitive == short.class) return Short.class;
        if (primitive == int.class) return Integer.class;
        if (primitive == long.class) return Long.class;
        if (primitive == float.class) return Float.class;
        if (primitive == double.class) return Double.class;
        return primitive;
    }
    
    /**
     * Get object size using advanced VM access
     */
    public long getObjectSize() {
        try {
            if (AdvancedVMAccess.isAvailable()) {
                // Use JVMTI-like sizing if available
                return com.jvmu.jvmti.JVMTI.getObjectSize(object);
            } else {
                // Fallback estimation
                return estimateObjectSize();
            }
        } catch (Exception e) {
            return estimateObjectSize();
        }
    }
    
    /**
     * Estimate object size without advanced access
     */
    private long estimateObjectSize() {
        // Basic estimation - object header + field sizes
        long size = 16; // Estimated object header size
        
        for (FieldInfo fieldInfo : getAllFields()) {
            Field field = fieldInfo.getField();
            Class<?> type = field.getType();
            
            if (type == boolean.class || type == byte.class) {
                size += 1;
            } else if (type == char.class || type == short.class) {
                size += 2;
            } else if (type == int.class || type == float.class) {
                size += 4;
            } else if (type == long.class || type == double.class) {
                size += 8;
            } else {
                size += 8; // Reference size (64-bit)
            }
        }
        
        return size;
    }
    
    /**
     * Get object hash code
     */
    public int hashCode() {
        return System.identityHashCode(object);
    }
    
    /**
     * Get the virtual machine this object belongs to
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfObjectReference)) return false;
        SelfObjectReference other = (SelfObjectReference) obj;
        return object == other.object; // Identity comparison
    }
    
    @Override
    public String toString() {
        return String.format("SelfObjectReference[id=%d, type=%s, hash=0x%x]",
            objectId, objectClass.getSimpleName(), hashCode());
    }
    
    /**
     * Field information class
     */
    public static class FieldInfo {
        private final Field field;
        private final SelfObjectReference objectRef;
        
        public FieldInfo(Field field, SelfObjectReference objectRef) {
            this.field = field;
            this.objectRef = objectRef;
        }
        
        public Field getField() {
            return field;
        }
        
        public String getName() {
            return field.getName();
        }
        
        public Class<?> getType() {
            return field.getType();
        }
        
        public String getTypeName() {
            return field.getType().getName();
        }
        
        public int getModifiers() {
            return field.getModifiers();
        }
        
        public boolean isPublic() {
            return Modifier.isPublic(field.getModifiers());
        }
        
        public boolean isPrivate() {
            return Modifier.isPrivate(field.getModifiers());
        }
        
        public boolean isFinal() {
            return Modifier.isFinal(field.getModifiers());
        }
        
        public boolean isVolatile() {
            return Modifier.isVolatile(field.getModifiers());
        }
        
        public Object getValue() {
            return objectRef.getValue(field.getName());
        }
        
        public void setValue(Object value) {
            objectRef.setValue(field.getName(), value);
        }
        
        @Override
        public String toString() {
            return String.format("FieldInfo[%s %s.%s]",
                getType().getSimpleName(), 
                field.getDeclaringClass().getSimpleName(),
                field.getName());
        }
    }
}