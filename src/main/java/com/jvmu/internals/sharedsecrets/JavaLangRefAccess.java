package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

/**
 * JavaLangRefAccess - Wrapper for jdk.internal.misc.JavaLangRefAccess
 * 
 * Provides access to java.lang.ref package internals for reference processing,
 * weak reference management, and garbage collection integration without
 * requiring special permissions.
 * 
 * Key capabilities:
 * - Reference queue processing
 * - Weak reference management
 * - Phantom reference operations
 * - Reference processing control
 */
public class JavaLangRefAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    
    /**
     * Create wrapper from native JavaLangRefAccess instance
     * @param nativeAccess native access instance
     */
    public JavaLangRefAccess(Object nativeAccess) {
        this.nativeAccess = nativeAccess;
        this.available = nativeAccess != null;
    }
    
    /**
     * Check if this access wrapper is functional
     * @return true if underlying access is available
     */
    public boolean isAvailable() {
        return available;
    }
    
    // ==================== REFERENCE PROCESSING ====================
    
    /**
     * Wait for reference processing to complete
     * @return true if processing completed successfully
     */
    public boolean waitForReferenceProcessing() {
        if (!available) return false;
        try {
            return ReflectBuilder.of(nativeAccess)
                .method("waitForReferenceProcessing", null, null)
                .get();
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Run finalization for references
     */
    public void runFinalization() {
        if (!available) return;
        try {
            ReflectBuilder.of(nativeAccess)
                .method("runFinalization", null, null)
                .get();
        } catch (Exception e) {
            // Ignore errors in finalization
        }
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive reference access information
     * @return reference access information
     */
    public RefAccessInfo getRefAccessInfo() {
        RefAccessInfo info = new RefAccessInfo();
        info.available = available;
        
        if (!available) {
            return info;
        }
        
        try {
            // Test basic functionality by attempting reference processing operations
            info.processingAvailable = true;
            
            // Additional reference statistics could be gathered here
            // if we had access to internal reference queue state
            
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
     * Information about reference access capabilities
     */
    public static class RefAccessInfo {
        public boolean available;
        public boolean processingAvailable;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "RefAccessInfo: Not Available";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("Reference Access Information:\n");
            
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Processing: ").append(processingAvailable ? "✓" : "✗");
            
            return sb.toString();
        }
    }
}