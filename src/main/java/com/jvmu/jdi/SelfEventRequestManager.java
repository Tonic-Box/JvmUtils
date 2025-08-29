package com.jvmu.jdi;

import lombok.Getter;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Self Event Request Manager - JDI-like EventRequestManager for managing debugging event requests
 * 
 * This implementation provides event request management functionality for debugging
 * within the current JVM, similar to JDI EventRequestManager but without requiring
 * external JDWP connection.
 */
public class SelfEventRequestManager {
    
    private final SelfVirtualMachine vm;
    private final Map<Integer, SelfEventRequest> requests;
    private final AtomicInteger nextRequestId;
    
    public SelfEventRequestManager(SelfVirtualMachine vm) {
        this.vm = vm;
        this.requests = new ConcurrentHashMap<>();
        this.nextRequestId = new AtomicInteger(1);
    }
    
    /**
     * Create a method entry request
     */
    public SelfMethodEntryRequest createMethodEntryRequest() {
        int id = nextRequestId.getAndIncrement();
        SelfMethodEntryRequest request = new SelfMethodEntryRequest(vm, id);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a method exit request
     */
    public SelfMethodExitRequest createMethodExitRequest() {
        int id = nextRequestId.getAndIncrement();
        SelfMethodExitRequest request = new SelfMethodExitRequest(vm, id);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a breakpoint request
     */
    public SelfBreakpointRequest createBreakpointRequest(SelfLocation location) {
        int id = nextRequestId.getAndIncrement();
        SelfBreakpointRequest request = new SelfBreakpointRequest(vm, id, location);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a field access request
     */
    public SelfAccessWatchpointRequest createAccessWatchpointRequest(SelfField field) {
        int id = nextRequestId.getAndIncrement();
        SelfAccessWatchpointRequest request = new SelfAccessWatchpointRequest(vm, id, field);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a field modification request
     */
    public SelfModificationWatchpointRequest createModificationWatchpointRequest(SelfField field) {
        int id = nextRequestId.getAndIncrement();
        SelfModificationWatchpointRequest request = new SelfModificationWatchpointRequest(vm, id, field);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a class prepare request
     */
    public SelfClassPrepareRequest createClassPrepareRequest() {
        int id = nextRequestId.getAndIncrement();
        SelfClassPrepareRequest request = new SelfClassPrepareRequest(vm, id);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a thread start request
     */
    public SelfThreadStartRequest createThreadStartRequest() {
        int id = nextRequestId.getAndIncrement();
        SelfThreadStartRequest request = new SelfThreadStartRequest(vm, id);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Create a thread death request
     */
    public SelfThreadDeathRequest createThreadDeathRequest() {
        int id = nextRequestId.getAndIncrement();
        SelfThreadDeathRequest request = new SelfThreadDeathRequest(vm, id);
        requests.put(id, request);
        return request;
    }
    
    /**
     * Get all event requests
     */
    public List<SelfEventRequest> eventRequests() {
        return new ArrayList<>(requests.values());
    }
    
    /**
     * Get method entry requests
     */
    public List<SelfMethodEntryRequest> methodEntryRequests() {
        List<SelfMethodEntryRequest> result = new ArrayList<>();
        for (SelfEventRequest request : requests.values()) {
            if (request instanceof SelfMethodEntryRequest) {
                result.add((SelfMethodEntryRequest) request);
            }
        }
        return result;
    }
    
    /**
     * Get method exit requests
     */
    public List<SelfMethodExitRequest> methodExitRequests() {
        List<SelfMethodExitRequest> result = new ArrayList<>();
        for (SelfEventRequest request : requests.values()) {
            if (request instanceof SelfMethodExitRequest) {
                result.add((SelfMethodExitRequest) request);
            }
        }
        return result;
    }
    
    /**
     * Get breakpoint requests
     */
    public List<SelfBreakpointRequest> breakpointRequests() {
        List<SelfBreakpointRequest> result = new ArrayList<>();
        for (SelfEventRequest request : requests.values()) {
            if (request instanceof SelfBreakpointRequest) {
                result.add((SelfBreakpointRequest) request);
            }
        }
        return result;
    }
    
    /**
     * Delete an event request
     */
    public void deleteEventRequest(SelfEventRequest request) {
        requests.remove(request.id());
        request.disable();
    }
    
    /**
     * Delete multiple event requests
     */
    public void deleteEventRequests(List<SelfEventRequest> requestsToDelete) {
        for (SelfEventRequest request : requestsToDelete) {
            deleteEventRequest(request);
        }
    }
    
    /**
     * Delete all event requests
     */
    public void deleteAllEventRequests() {
        for (SelfEventRequest request : requests.values()) {
            request.disable();
        }
        requests.clear();
    }
    
    /**
     * Get request by ID
     */
    public SelfEventRequest getRequest(int id) {
        return requests.get(id);
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public String toString() {
        return String.format("SelfEventRequestManager[requests=%d]", requests.size());
    }
}

