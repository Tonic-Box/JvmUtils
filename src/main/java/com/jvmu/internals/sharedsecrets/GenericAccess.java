package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * GenericAccess - Generic wrapper for SharedSecrets access interfaces
 * 
 * Provides a generic wrapper for access interfaces that don't have specific
 * implementations yet. This allows dynamic method invocation on any access
 * interface through reflection.
 * 
 * Key capabilities:
 * - Dynamic method invocation
 * - Generic parameter handling
 * - Method discovery and caching
 * - Error handling and fallbacks
 */
public class GenericAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    private final String interfaceName;
    private final Map<String, Method> methodCache = new HashMap<>();
    
    /**
     * Create wrapper from native access instance
     * @param nativeAccess native access instance
     * @param interfaceName name of the access interface
     */
    public GenericAccess(Object nativeAccess, String interfaceName) {
        this.nativeAccess = nativeAccess;
        this.interfaceName = interfaceName;
        this.available = nativeAccess != null;
        
        if (available) {
            cacheInterfaceMethods();
        }
    }
    
    /**
     * Cache all methods from the interface for faster access
     */
    private void cacheInterfaceMethods() {
        if (!available) return;
        
        try {
            Class<?> clazz = nativeAccess.getClass();
            Method[] methods = clazz.getMethods();
            
            for (Method method : methods) {
                methodCache.put(method.getName(), method);
            }
        } catch (Exception e) {
            // Failed to cache methods - fallback to dynamic lookup
        }
    }
    
    /**
     * Check if this access wrapper is functional
     * @return true if underlying access is available
     */
    public boolean isAvailable() {
        return available;
    }
    
    /**
     * Get the interface name
     * @return name of the wrapped interface
     */
    public String getInterfaceName() {
        return interfaceName;
    }
    
    // ==================== DYNAMIC METHOD INVOCATION ====================
    
    /**
     * Invoke a method on the wrapped access interface
     * @param methodName method name
     * @param parameterTypes parameter types
     * @param args arguments
     * @param <T> return type
     * @return method result
     */
    @SuppressWarnings("unchecked")
    public <T> T invoke(String methodName, Class<?>[] parameterTypes, Object[] args) {
        if (!available) return null;
        
        try {
            return (T) ReflectBuilder.of(nativeAccess)
                .method(methodName, parameterTypes, args)
                .get();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Invoke a method with no parameters
     * @param methodName method name
     * @param <T> return type
     * @return method result
     */
    public <T> T invoke(String methodName) {
        return invoke(methodName, (Class<?>[]) null, (Object[]) null);
    }
    
    /**
     * Invoke a method with single parameter
     * @param methodName method name
     * @param parameterType parameter type
     * @param arg argument
     * @param <T> return type
     * @return method result
     */
    public <T> T invoke(String methodName, Class<?> parameterType, Object arg) {
        return invoke(methodName, new Class<?>[]{parameterType}, new Object[]{arg});
    }
    
    /**
     * Check if a method exists on the interface
     * @param methodName method name
     * @return true if method exists
     */
    public boolean hasMethod(String methodName) {
        if (!available) return false;
        
        if (methodCache.containsKey(methodName)) {
            return true;
        }
        
        try {
            Method[] methods = nativeAccess.getClass().getMethods();
            for (Method method : methods) {
                if (method.getName().equals(methodName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            // Ignore
        }
        
        return false;
    }
    
    /**
     * Get all available method names
     * @return array of method names
     */
    public String[] getMethodNames() {
        if (!available) return new String[0];
        
        try {
            Method[] methods = nativeAccess.getClass().getMethods();
            String[] names = new String[methods.length];
            for (int i = 0; i < methods.length; i++) {
                names[i] = methods[i].getName();
            }
            return names;
        } catch (Exception e) {
            return new String[0];
        }
    }
    
    /**
     * Get comprehensive generic access information
     * @return generic access information
     */
    public GenericAccessInfo getAccessInfo() {
        GenericAccessInfo info = new GenericAccessInfo();
        info.available = available;
        info.interfaceName = interfaceName;
        
        if (!available) {
            return info;
        }
        
        try {
            info.className = nativeAccess.getClass().getName();
            info.methodNames = getMethodNames();
            info.methodCount = info.methodNames.length;
            info.cachedMethods = methodCache.size();
            
        } catch (Exception e) {
            info.error = e.getMessage();
        }
        
        return info;
    }
    
    /**
     * Get the native access object
     * @return native access instance
     */
    public Object getNativeAccess() {
        return nativeAccess;
    }
    
    // ==================== DATA STRUCTURES ====================
    
    /**
     * Information about generic access capabilities
     */
    public static class GenericAccessInfo {
        public boolean available;
        public String interfaceName;
        public String className;
        public String[] methodNames;
        public int methodCount = -1;
        public int cachedMethods = -1;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "GenericAccessInfo: Not Available (" + interfaceName + ")";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Generic Access Information:\n");
            sb.append("  Interface: ").append(interfaceName != null ? interfaceName : "Unknown").append("\n");
            
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Class: ").append(className != null ? className : "Unknown").append("\n");
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Methods: ").append(methodCount >= 0 ? methodCount : "Unknown");
            if (cachedMethods >= 0) {
                sb.append(" (").append(cachedMethods).append(" cached)");
            }
            sb.append("\n");
            
            if (methodNames != null && methodNames.length > 0) {
                sb.append("  Method Names: ");
                for (int i = 0; i < Math.min(methodNames.length, 5); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(methodNames[i]);
                }
                if (methodNames.length > 5) {
                    sb.append("... (").append(methodNames.length - 5).append(" more)");
                }
            }
            
            return sb.toString();
        }
    }
}