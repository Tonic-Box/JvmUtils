package com.jvmu.jdi;

/**
 * Self Thread Start Request - Request for thread start events
 */
public class SelfThreadStartRequest extends SelfEventRequest {

    public SelfThreadStartRequest(SelfVirtualMachine vm, int id) {
        super(vm, id);
    }

    @Override
    public String toString() {
        return String.format("SelfThreadStartRequest[id=%d, enabled=%b]", id, enabled);
    }
}
