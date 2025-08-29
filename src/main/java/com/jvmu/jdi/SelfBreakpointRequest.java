package com.jvmu.jdi;

import lombok.Getter;

/**
 * Self Breakpoint Request - Request for breakpoint events
 */
public class SelfBreakpointRequest extends SelfEventRequest {

    private final SelfLocation location;
    /**
     * -- GETTER --
     * Get thread filter
     */
    @Getter
    private SelfThreadReference threadFilter;

    public SelfBreakpointRequest(SelfVirtualMachine vm, int id, SelfLocation location) {
        super(vm, id);
        this.location = location;
    }

    /**
     * Get the breakpoint location
     */
    public SelfLocation location() {
        return location;
    }

    /**
     * Restrict to specific thread
     */
    public void addThreadFilter(SelfThreadReference thread) {
        this.threadFilter = thread;
    }

    @Override
    public String toString() {
        return String.format("SelfBreakpointRequest[id=%d, enabled=%b, location=%s, thread=%s]",
                id, enabled, location, threadFilter != null ? threadFilter.name() : "any");
    }
}
