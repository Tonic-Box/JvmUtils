package com.jvmu.jdi.events;

import com.jvmu.jdi.threads.SelfThreadReference;

import com.jvmu.jdi.types.SelfField;

import com.jvmu.jdi.vm.SelfVirtualMachine;


/**
 * Self Access Watchpoint Request - Request for field access events
 */
public class SelfAccessWatchpointRequest extends SelfEventRequest {

    private final SelfField field;
    /**
     * -- GETTER --
     * Get thread filter
     */
    private SelfThreadReference threadFilter;

    public SelfAccessWatchpointRequest(SelfVirtualMachine vm, int id, SelfField field) {
        super(vm, id);
        this.field = field;
    }

    /**
     * Get the watched field
     */
    public SelfField field() {
        return field;
    }

    /**
     * Restrict to specific thread
     */
    public void addThreadFilter(SelfThreadReference thread) {
        this.threadFilter = thread;
    }

    @Override
    public String toString() {
        return String.format("SelfAccessWatchpointRequest[id=%d, enabled=%b, field=%s, thread=%s]",
                id, enabled, field.name(), threadFilter != null ? threadFilter.name() : "any");
    }
}
