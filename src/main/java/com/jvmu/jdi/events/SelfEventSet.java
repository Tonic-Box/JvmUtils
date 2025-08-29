package com.jvmu.jdi.events;

import com.jvmu.jdi.vm.SelfVirtualMachine;

import java.util.ArrayList;
import java.util.List;

/**
 * Self Event Set - Collection of related debugging events
 */
public class SelfEventSet {

    private final SelfVirtualMachine vm;
    private final List<SelfEvent> events;

    public SelfEventSet(SelfVirtualMachine vm, List<SelfEvent> events) {
        this.vm = vm;
        this.events = new ArrayList<>(events);
    }

    /**
     * Get all events in this set
     */
    public List<SelfEvent> events() {
        return new ArrayList<>(events);
    }

    /**
     * Get the number of events
     */
    public int size() {
        return events.size();
    }

    /**
     * Check if event set is empty
     */
    public boolean isEmpty() {
        return events.isEmpty();
    }

    /**
     * Get event by index
     */
    public SelfEvent get(int index) {
        return events.get(index);
    }

    /**
     * Get suspend policy for this event set
     */
    public int suspendPolicy() {
        // Return the most restrictive suspend policy from all events
        int policy = SelfEvent.SUSPEND_NONE;

        for (SelfEvent event : events) {
            int eventPolicy = event.suspendPolicy();
            if (eventPolicy == SelfEvent.SUSPEND_ALL) {
                policy = SelfEvent.SUSPEND_ALL;
            } else if (eventPolicy == SelfEvent.SUSPEND_THREAD && policy == SelfEvent.SUSPEND_NONE) {
                policy = SelfEvent.SUSPEND_THREAD;
            }
        }

        return policy;
    }

    /**
     * Resume execution after handling events
     */
    public void resume() {
        // In self-debugging, we can't actually suspend/resume
        // This is a no-op but maintains JDI compatibility
    }

    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }

    @Override
    public String toString() {
        return String.format("SelfEventSet[events=%d, suspendPolicy=%d]",
                size(), suspendPolicy());
    }
}
