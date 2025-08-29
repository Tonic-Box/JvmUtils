package com.jvmu.jdi;

/**
 * Self Method Exit Event - Triggered when exiting a method
 */
public class SelfMethodExitEvent extends SelfEvent {

    private final SelfThreadReference thread;
    private final SelfLocation location;
    private final Object returnValue;

    public SelfMethodExitEvent(SelfVirtualMachine vm, SelfEventRequest request,
                               int suspendPolicy, SelfThreadReference thread,
                               SelfLocation location, Object returnValue) {
        super(vm, request, suspendPolicy);
        this.thread = thread;
        this.location = location;
        this.returnValue = returnValue;
    }

    /**
     * Get the thread where method exit occurred
     */
    public SelfThreadReference thread() {
        return thread;
    }

    /**
     * Get the location of method exit
     */
    public SelfLocation location() {
        return location;
    }

    /**
     * Get the method being exited
     */
    public SelfMethod method() {
        return location.method();
    }

    /**
     * Get the return value (null for void methods)
     */
    public Object returnValue() {
        return returnValue;
    }

    @Override
    public String toString() {
        return String.format("SelfMethodExitEvent[thread=%s, method=%s, returnValue=%s]",
                thread.name(), method().name(), returnValue);
    }
}
