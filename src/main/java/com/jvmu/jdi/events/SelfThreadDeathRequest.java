package com.jvmu.jdi.events;

import com.jvmu.jdi.vm.SelfVirtualMachine;

/**
 * Self Thread Death Request - Request for thread death events
 */
public class SelfThreadDeathRequest extends SelfEventRequest {

    public SelfThreadDeathRequest(SelfVirtualMachine vm, int id) {
        super(vm, id);
    }

    @Override
    public String toString() {
        return String.format("SelfThreadDeathRequest[id=%d, enabled=%b]", id, enabled);
    }
}
