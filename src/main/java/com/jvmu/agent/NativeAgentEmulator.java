package com.jvmu.agent;

import com.jvmu.internals.InternalUnsafe;

/**
 * NativeAgentEmulator - Creates and manages fake native agent pointers
 * 
 * This component is responsible for creating fake JVMTI agent pointers that
 * can fool Java-level security checks without requiring actual native code.
 */
public class NativeAgentEmulator {
    
    public NativeAgentEmulator() {
        // No dependencies needed - uses static InternalUnsafe methods
    }
    
    /**
     * Create a fake native agent pointer that appears valid to Java code
     * 
     * @return fake native agent pointer value
     */
    public long createFakeNativeAgentPointer() {
        try {
            // Create a dummy object to get a base address
            Object dummyObject = new Object();
            long objectAddress = InternalUnsafe.getObjectAddress(dummyObject);
            
            // Create a fake pointer by adding offset to object address
            // This gives us a non-zero value that looks like a valid pointer
            long fakePointer = objectAddress + 0x1000;
            
            System.out.println("[*] Created fake pointer based on object address: 0x" + Long.toHexString(fakePointer));
            return fakePointer;
            
        } catch (Exception e) {
            System.out.println("[!] Failed to create fake native agent pointer: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Install the fake native agent pointer into an Instrumentation instance
     * 
     * @param instrumentation the Instrumentation instance to modify
     * @param fakePointer the fake pointer value to install
     * @return true if installation succeeded
     */
    public boolean installFakeNativeAgent(Object instrumentation, long fakePointer) {
        try {
            // Set the mNativeAgent field to our fake pointer
            if (setInstrumentationField(instrumentation, "mNativeAgent", fakePointer)) {
                System.out.println("[+] Set fake native agent pointer: 0x" + Long.toHexString(fakePointer));
                return true;
            } else {
                System.out.println("[!] Failed to set mNativeAgent field");
                return false;
            }
            
        } catch (Exception e) {
            System.out.println("[!] Error installing fake native agent: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Get the current native agent pointer from an Instrumentation instance
     * 
     * @param instrumentation the Instrumentation instance to check
     * @return the native agent pointer value, or 0 if not found
     */
    public long getNativeAgentPointer(Object instrumentation) {
        try {
            Class<?> instrClass = instrumentation.getClass();
            java.lang.reflect.Field nativeAgentField = instrClass.getDeclaredField("mNativeAgent");
            nativeAgentField.setAccessible(true);
            
            Object value = nativeAgentField.get(instrumentation);
            if (value instanceof Long) {
                return (Long) value;
            }
            return 0;
            
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Check if the instrumentation has a valid-looking native agent pointer
     * 
     * @param instrumentation the Instrumentation instance to check
     * @return true if it has a non-zero native agent pointer
     */
    public boolean hasNativeAgent(Object instrumentation) {
        return getNativeAgentPointer(instrumentation) != 0;
    }
    
    /**
     * Create and install a complete fake native agent setup
     * 
     * @param instrumentation the Instrumentation instance to modify
     * @return true if the complete setup succeeded
     */
    public boolean emulateNativeAgent(Object instrumentation) {
        try {
            System.out.println("[*] Emulating native agent using JVM internals manipulation...");
            
            // Create fake native agent pointer
            System.out.println("[*] Creating fake native agent pointer...");
            long fakeNativeAgent = createFakeNativeAgentPointer();
            
            if (fakeNativeAgent != 0) {
                // Install the fake pointer
                if (installFakeNativeAgent(instrumentation, fakeNativeAgent)) {
                    System.out.println("[+] Successfully emulated native agent connection");
                    return true;
                } else {
                    System.out.println("[!] Failed to install fake native agent");
                }
            } else {
                System.out.println("[!] Failed to create fake native agent pointer");
            }
            
            return false;
            
        } catch (Exception e) {
            System.out.println("[!] Native agent emulation failed: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Set a field in the Instrumentation object using InternalUnsafe
     */
    private boolean setInstrumentationField(Object instrumentation, String fieldName, Object value) {
        try {
            Class<?> instrClass = instrumentation.getClass();
            java.lang.reflect.Field field = instrClass.getDeclaredField(fieldName);
            long fieldOffset = InternalUnsafe.objectFieldOffset(field);
            
            // Handle different field types using InternalUnsafe
            if (value instanceof Long && field.getType() == long.class) {
                InternalUnsafe.putLong(instrumentation, fieldOffset, (Long) value);
                System.out.println("[*] Set " + fieldName + " = " + value);
                return true;
            } else if (value instanceof Boolean && field.getType() == boolean.class) {
                InternalUnsafe.putBoolean(instrumentation, fieldOffset, (Boolean) value);
                System.out.println("[*] Set " + fieldName + " = " + value);
                return true;
            } else {
                InternalUnsafe.putObject(instrumentation, fieldOffset, value);
                System.out.println("[*] Set " + fieldName + " = " + value);
                return true;
            }
            
        } catch (Exception e) {
            System.out.println("[!] Failed to set field " + fieldName + ": " + e.getMessage());
            return false;
        }
    }
}