package com.jvmu.jdi.events;

import com.jvmu.jdi.threads.SelfThreadReference;

import com.jvmu.jdi.types.SelfMethod;

import com.jvmu.jdi.types.SelfReferenceType;

import com.jvmu.jdi.vm.SelfVirtualMachine;

/**
 * Self Method Exit Request - Request for method exit events
 */
public class SelfMethodExitRequest extends SelfEventRequest {

    private SelfReferenceType classFilter;
    private SelfThreadReference threadFilter;

    public SelfMethodExitRequest(SelfVirtualMachine vm, int id) {
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
        return String.format("SelfMethodExitRequest[id=%d, enabled=%b, class=%s, thread=%s]",
                id, enabled, classFilter != null ? classFilter.name() : "any",
                threadFilter != null ? threadFilter.name() : "any");
    }
}
