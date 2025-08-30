package com.jvmu.internals.sharedsecrets;

import com.jvmu.util.ReflectBuilder;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * JavaUtilJarAccess - Wrapper for jdk.internal.misc.JavaUtilJarAccess
 * 
 * Provides access to java.util.jar package internals for JAR file processing,
 * code source management, and manifest handling without requiring special permissions.
 * 
 * Key capabilities:
 * - JAR file introspection and validation
 * - Code source extraction and management
 * - Entry enumeration and filtering
 * - Manifest processing and security attributes
 * - JAR file initialization control
 */
public class JavaUtilJarAccess {
    
    private final Object nativeAccess;
    private final boolean available;
    
    /**
     * Create wrapper from native JavaUtilJarAccess instance
     * @param nativeAccess native access instance
     */
    public JavaUtilJarAccess(Object nativeAccess) {
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
    
    // ==================== JAR FILE PROPERTIES ====================
    
    /**
     * Check if JAR file has Class-Path attribute in manifest
     * @param jar JAR file to check
     * @return true if Class-Path attribute exists
     * @throws IOException if JAR file cannot be read
     */
    public boolean jarFileHasClassPathAttribute(JarFile jar) throws IOException {
        if (!available) return false;
        try {
            return ReflectBuilder.of(nativeAccess)
                .method("jarFileHasClassPathAttribute", new Class<?>[]{JarFile.class}, new Object[]{jar})
                .get();
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }
    
    /**
     * Check if JAR file initialization is currently in progress
     * @return true if initializing
     */
    public boolean isInitializing() {
        if (!available) return false;
        return ReflectBuilder.of(nativeAccess)
            .method("isInitializing", null, null)
            .get();
    }
    
    // ==================== CODE SOURCE OPERATIONS ====================
    
    /**
     * Get all code sources for a JAR file
     * @param jar JAR file
     * @param url base URL
     * @return array of code sources
     */
    public CodeSource[] getCodeSources(JarFile jar, URL url) {
        if (!available) return new CodeSource[0];
        return ReflectBuilder.of(nativeAccess)
            .method("getCodeSources", new Class<?>[]{JarFile.class, URL.class}, new Object[]{jar, url})
            .get();
    }
    
    /**
     * Get code source for a specific entry in JAR file
     * @param jar JAR file
     * @param url base URL
     * @param name entry name
     * @return code source for the entry
     */
    public CodeSource getCodeSource(JarFile jar, URL url, String name) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getCodeSource", new Class<?>[]{JarFile.class, URL.class, String.class}, new Object[]{jar, url, name})
            .get();
    }
    
    // ==================== ENTRY ENUMERATION ====================
    
    /**
     * Get enumeration of entry names filtered by code sources
     * @param jar JAR file
     * @param cs code sources to filter by
     * @return enumeration of matching entry names
     */
    public Enumeration<String> entryNames(JarFile jar, CodeSource[] cs) {
        if (!available) return java.util.Collections.emptyEnumeration();
        return ReflectBuilder.of(nativeAccess)
            .method("entryNames", new Class<?>[]{JarFile.class, CodeSource[].class}, new Object[]{jar, cs})
            .get();
    }
    
    /**
     * Get enumeration of JAR entries (alternative implementation)
     * @param jar JAR file
     * @return enumeration of JAR entries
     */
    public Enumeration<JarEntry> entries2(JarFile jar) {
        if (!available) return java.util.Collections.emptyEnumeration();
        return ReflectBuilder.of(nativeAccess)
            .method("entries2", new Class<?>[]{JarFile.class}, new Object[]{jar})
            .get();
    }
    
    // ==================== VALIDATION AND INITIALIZATION ====================
    
    /**
     * Set eager validation mode for JAR file
     * @param jar JAR file
     * @param eager true to enable eager validation
     */
    public void setEagerValidation(JarFile jar, boolean eager) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("setEagerValidation", new Class<?>[]{JarFile.class, boolean.class}, new Object[]{jar, eager})
            .get();
    }
    
    /**
     * Ensure JAR file is properly initialized
     * @param jar JAR file to initialize
     */
    public void ensureInitialization(JarFile jar) {
        if (!available) return;
        ReflectBuilder.of(nativeAccess)
            .method("ensureInitialization", new Class<?>[]{JarFile.class}, new Object[]{jar})
            .get();
    }
    
    // ==================== MANIFEST OPERATIONS ====================
    
    /**
     * Get manifest digests for JAR file
     * @param jar JAR file
     * @return list of manifest digest objects
     */
    public List<Object> getManifestDigests(JarFile jar) {
        if (!available) return java.util.Collections.emptyList();
        return ReflectBuilder.of(nativeAccess)
            .method("getManifestDigests", new Class<?>[]{JarFile.class}, new Object[]{jar})
            .get();
    }
    
    /**
     * Get trusted attributes from manifest
     * @param man manifest
     * @param name entry name
     * @return trusted attributes for the entry
     */
    public Attributes getTrustedAttributes(Manifest man, String name) {
        if (!available) return null;
        return ReflectBuilder.of(nativeAccess)
            .method("getTrustedAttributes", new Class<?>[]{Manifest.class, String.class}, new Object[]{man, name})
            .get();
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Get comprehensive information about a JAR file
     * @param jar JAR file to analyze
     * @return JAR file information
     */
    public JarFileInfo getJarFileInfo(JarFile jar) {
        if (!available) return new JarFileInfo();
        
        JarFileInfo info = new JarFileInfo();
        
        try {
            info.available = true;
            info.hasClassPath = jarFileHasClassPathAttribute(jar);
            info.isInitializing = isInitializing();
            info.codeSources = getCodeSources(jar, null);
            info.manifestDigests = getManifestDigests(jar);
            info.name = jar.getName();
            
            // Count entries
            Enumeration<JarEntry> entries = entries2(jar);
            int count = 0;
            while (entries.hasMoreElements()) {
                entries.nextElement();
                count++;
            }
            info.entryCount = count;
            
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
     * Information about a JAR file
     */
    public static class JarFileInfo {
        public boolean available;
        public String name;
        public boolean hasClassPath;
        public boolean isInitializing;
        public CodeSource[] codeSources;
        public List<Object> manifestDigests;
        public int entryCount = -1;
        public String error;
        
        @Override
        public String toString() {
            if (!available) {
                return "JarFileInfo: Not Available";
            }
            
            StringBuilder sb = new StringBuilder();
            sb.append("JAR File Information:\n");
            if (error != null) {
                sb.append("  Error: ").append(error).append("\n");
                return sb.toString();
            }
            
            sb.append("  Name: ").append(name != null ? name : "Unknown").append("\n");
            sb.append("  Has Class-Path: ").append(hasClassPath).append("\n");
            sb.append("  Is Initializing: ").append(isInitializing).append("\n");
            sb.append("  Code Sources: ").append(codeSources != null ? codeSources.length : 0).append("\n");
            sb.append("  Manifest Digests: ").append(manifestDigests != null ? manifestDigests.size() : 0).append("\n");
            if (entryCount >= 0) {
                sb.append("  Entry Count: ").append(entryCount);
            } else {
                sb.append("  Entry Count: Unknown");
            }
            
            return sb.toString();
        }
    }
}