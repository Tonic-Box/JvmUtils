package com.jvmu.jdi;

/**
 * Self Method Entry Request - Request for method entry events
 */
public class SelfMethodEntryRequest extends SelfEventRequest {

    private SelfReferenceType classFilter;
    private SelfThreadReference threadFilter;

    public SelfMethodEntryRequest(SelfVirtualMachine vm, int id) {
        super(vm, id);
    }

    /**
     * Restrict to specific class
     */
    public void addClassFilter(SelfReferenceType refType) {
        this.classFilter = refType;
    }

    /**
     * Restrict to specific thread
     */
    public void addThreadFilter(SelfThreadReference thread) {
        this.threadFilter = thread;
    }

    /**
     * Get class filter
     */
    public SelfReferenceType getClassFilter() {
        return classFilter;
    }

    /**
     * Get thread filter
     */
    public SelfThreadReference getThreadFilter() {
        return threadFilter;
    }

    @Override
    public String toString() {
        return String.format("SelfMethodEntryRequest[id=%d, enabled=%b, class=%s, thread=%s]",
                id, enabled, classFilter != null ? classFilter.name() : "any",
                threadFilter != null ? threadFilter.name() : "any");
    }
}
