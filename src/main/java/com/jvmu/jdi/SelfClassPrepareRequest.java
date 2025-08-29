package com.jvmu.jdi;

/**
 * Self Class Prepare Request - Request for class preparation events
 */
public class SelfClassPrepareRequest extends SelfEventRequest {

    private String classPattern;

    public SelfClassPrepareRequest(SelfVirtualMachine vm, int id) {
        super(vm, id);
    }

    /**
     * Add class name filter
     */
    public void addClassFilter(String classPattern) {
        this.classPattern = classPattern;
    }

    /**
     * Get class pattern
     */
    public String getClassPattern() {
        return classPattern;
    }

    @Override
    public String toString() {
        return String.format("SelfClassPrepareRequest[id=%d, enabled=%b, pattern=%s]",
                id, enabled, classPattern != null ? classPattern : "any");
    }
}
