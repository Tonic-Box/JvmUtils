package com.jvmu.jdi;

/**
 * Self Method Entry Event - Triggered when entering a method
 */
public class SelfMethodEntryEvent extends SelfEvent {

    private final SelfThreadReference thread;
    private final SelfLocation location;

    public SelfMethodEntryEvent(SelfVirtualMachine vm, SelfEventRequest request,
                                int suspendPolicy, SelfThreadReference thread, SelfLocation location) {
        super(vm, request, suspendPolicy);
        this.thread = thread;
        this.location = location;
    }

    /**
     * Get the thread where method entry occurred
     */
    public SelfThreadReference thread() {
        return thread;
    }

    /**
     * Get the location of method entry
     */
    public SelfLocation location() {
        return location;
    }

    /**
     * Get the method being entered
     */
    public SelfMethod method() {
        return location.method();
    }

    @Override
    public String toString() {
        return String.format("SelfMethodEntryEvent[thread=%s, method=%s]",
                thread.name(), method().name());
    }
}
