package com.jvmu.jdi;

import lombok.Getter;

/**
 * Self Modification Watchpoint Request - Request for field modification events
 */
public class SelfModificationWatchpointRequest extends SelfEventRequest {

    private final SelfField field;
    /**
     * -- GETTER --
     * Get thread filter
     */
    @Getter
    private SelfThreadReference threadFilter;

    public SelfModificationWatchpointRequest(SelfVirtualMachine vm, int id, SelfField field) {
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
        return String.format("SelfModificationWatchpointRequest[id=%d, enabled=%b, field=%s, thread=%s]",
                id, enabled, field.name(), threadFilter != null ? threadFilter.name() : "any");
    }
}
