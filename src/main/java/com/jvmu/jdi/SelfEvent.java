package com.jvmu.jdi;

import com.jvmu.jdi.SelfEventRequest;
import com.jvmu.jdi.SelfVirtualMachine;

/**
 * Self Event - Base class for debugging events
 */
public abstract class SelfEvent {

    // Suspend policies
    public static final int SUSPEND_NONE = 0;
    public static final int SUSPEND_THREAD = 1;
    public static final int SUSPEND_ALL = 2;

    protected final SelfVirtualMachine vm;
    protected final SelfEventRequest request;
    protected final int suspendPolicy;

    public SelfEvent(SelfVirtualMachine vm, SelfEventRequest request, int suspendPolicy) {
        this.vm = vm;
        this.request = request;
        this.suspendPolicy = suspendPolicy;
    }

    /**
     * Get the event request that generated this event
     */
    public SelfEventRequest request() {
        return request;
    }

    /**
     * Get the suspend policy for this event
     */
    public int suspendPolicy() {
        return suspendPolicy;
    }

    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }

    @Override
    public String toString() {
        return String.format("SelfEvent[type=%s, suspendPolicy=%d]",
                getClass().getSimpleName(), suspendPolicy);
    }
}
