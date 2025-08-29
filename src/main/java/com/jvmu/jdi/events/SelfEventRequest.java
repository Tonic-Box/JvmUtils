package com.jvmu.jdi.events;

import com.jvmu.jdi.vm.SelfVirtualMachine;


import java.util.HashMap;
import java.util.Map;

/**
 * Self Event Request - Base class for all event requests
 */
public abstract class SelfEventRequest {

    // Suspend policies
    public static final int SUSPEND_NONE = 0;
    public static final int SUSPEND_THREAD = 1;
    public static final int SUSPEND_ALL = 2;

    protected final SelfVirtualMachine vm;
    protected final int id;
    /**
     * -- GETTER --
     *  Check if request is enabled
     */
    protected boolean enabled;
    protected int suspendPolicy;
    protected int count;
    protected final Map<Object, Object> properties;

    public SelfEventRequest(SelfVirtualMachine vm, int id) {
        this.vm = vm;
        this.id = id;
        this.enabled = false;
        this.suspendPolicy = SUSPEND_ALL;
        this.count = -1;
        this.properties = new HashMap<>();
    }

    /**
     * Get request ID
     */
    public int id() {
        return id;
    }

    /**
     * Enable this request
     */
    public void enable() {
        this.enabled = true;
    }

    /**
     * Disable this request
     */
    public void disable() {
        this.enabled = false;
    }

    /**
     * Set suspend policy
     */
    public void setSuspendPolicy(int policy) {
        this.suspendPolicy = policy;
    }

    /**
     * Get suspend policy
     */
    public int suspendPolicy() {
        return suspendPolicy;
    }

    /**
     * Set count filter
     */
    public void addCountFilter(int count) {
        this.count = count;
    }

    /**
     * Get count filter
     */
    public int getCountFilter() {
        return count;
    }

    /**
     * Set a property
     */
    public void putProperty(Object key, Object value) {
        properties.put(key, value);
    }

    /**
     * Get a property
     */
    public Object getProperty(Object key) {
        return properties.get(key);
    }

    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }

    @Override
    public String toString() {
        return String.format("SelfEventRequest[id=%d, enabled=%b, suspendPolicy=%d]",
                id, enabled, suspendPolicy);
    }
}
