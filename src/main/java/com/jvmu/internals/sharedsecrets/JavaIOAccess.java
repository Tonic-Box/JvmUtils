package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

import java.io.Console;
import java.nio.charset.Charset;

/**
 * JavaIOAccess - Wrapper for jdk.internal.misc.JavaIOAccess
 * 
 * Provides access to java.io package internals for console operations
 * and charset management without requiring special permissions.
 * 
 * Key capabilities:
 * - Console access and management
 * - Charset configuration access
 * - I/O system internals
 */
public class JavaIOAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    
    /**
     * Create wrapper from native JavaIOAccess instance
     * @param nativeAccess native access instance
     */
    public JavaIOAccess(Object nativeAccess) {
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
    
    // ==================== CONSOLE ACCESS ====================
    
    /**
     * Get the system console
     * @return Console instance or null if not available
     */
    public Console console() {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("console", null, null)
            .get();
    }
    
    // ==================== CHARSET ACCESS ====================
    
    /**
     * Get the default charset for the system
     * @return system default charset
     */
    public Charset charset() {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("charset", null, null)
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive I/O access information
     * @return I/O access information
     */
    public IOAccessInfo getIOAccessInfo() {
        IOAccessInfo info = new IOAccessInfo();
        info.available = available;
        
        if (!available) {
            return info;
        }
        
        try {
            Console console = console();
            info.consoleAvailable = console != null;
            if (console != null) {
                info.consoleName = console.getClass().getSimpleName();
            }
            
            Charset charset = charset();
            info.charsetAvailable = charset != null;
            if (charset != null) {
                info.charsetName = charset.name();
                info.charsetDisplayName = charset.displayName();
                info.charsetCanEncode = charset.canEncode();
            }
            
        } catch (Exception e) {
            info.error = e.getMessage();
        }
        
        return info;
    }
    
    /**
     * Check if console is available
     * @return true if system console is available
     */
    public boolean isConsoleAvailable() {
        return console() != null;
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
     * Information about I/O access capabilities
     */
    public static class IOAccessInfo {
        public boolean available;
        public boolean consoleAvailable;
        public String consoleName;
        public boolean charsetAvailable;
        public String charsetName;
        public String charsetDisplayName;
        public boolean charsetCanEncode;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "IOAccessInfo: Not Available";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("I/O Access Information:\n");
            
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Available: ").append(available).append("\n");
            sb.append("  Console: ").append(consoleAvailable ? "✓" : "✗");
            if (consoleAvailable && consoleName != null) {
                sb.append(" (").append(consoleName).append(")");
            }
            sb.append("\n");
            
            sb.append("  Charset: ").append(charsetAvailable ? "✓" : "✗");
            if (charsetAvailable) {
                sb.append(" (").append(charsetName != null ? charsetName : "Unknown").append(")");
                if (charsetDisplayName != null && !charsetDisplayName.equals(charsetName)) {
                    sb.append(" - ").append(charsetDisplayName);
                }
                sb.append(", Can Encode: ").append(charsetCanEncode);
            }
            
            return sb.toString();
        }
    }
}