package com.jvmu.jdi;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Self Event Queue - JDI-like EventQueue for debugging events
 * 
 * This implementation provides event queuing functionality for debugging
 * events within the current JVM, similar to JDI EventQueue but without
 * requiring external JDWP connection.
 */
public class SelfEventQueue {
    
    private final SelfVirtualMachine vm;
    private final BlockingQueue<SelfEvent> eventQueue;
    private volatile boolean active;
    
    public SelfEventQueue(SelfVirtualMachine vm) {
        this.vm = vm;
        this.eventQueue = new LinkedBlockingQueue<>();
        this.active = true;
    }
    
    /**
     * Remove and return the next available event set
     * This method blocks until an event is available
     */
    public SelfEventSet remove() throws InterruptedException {
        List<SelfEvent> events = new ArrayList<>();
        
        // Wait for at least one event
        SelfEvent firstEvent = eventQueue.take();
        events.add(firstEvent);
        
        // Collect any additional events that are immediately available
        List<SelfEvent> additionalEvents = new ArrayList<>();
        eventQueue.drainTo(additionalEvents);
        events.addAll(additionalEvents);
        
        return new SelfEventSet(vm, events);
    }
    
    /**
     * Remove and return the next available event set with timeout
     */
    public SelfEventSet remove(long timeout) throws InterruptedException {
        List<SelfEvent> events = new ArrayList<>();
        
        // Wait for first event with timeout
        SelfEvent firstEvent = eventQueue.poll(timeout, TimeUnit.MILLISECONDS);
        if (firstEvent == null) {
            return null; // Timeout occurred
        }
        
        events.add(firstEvent);
        
        // Collect any additional events that are immediately available
        List<SelfEvent> additionalEvents = new ArrayList<>();
        eventQueue.drainTo(additionalEvents);
        events.addAll(additionalEvents);
        
        return new SelfEventSet(vm, events);
    }
    
    /**
     * Add an event to the queue
     */
    public void addEvent(SelfEvent event) {
        if (active) {
            eventQueue.offer(event);
        }
    }
    
    /**
     * Add multiple events to the queue
     */
    public void addEvents(List<SelfEvent> events) {
        if (active) {
            for (SelfEvent event : events) {
                eventQueue.offer(event);
            }
        }
    }
    
    /**
     * Get the number of events in the queue
     */
    public int size() {
        return eventQueue.size();
    }
    
    /**
     * Check if the queue is empty
     */
    public boolean isEmpty() {
        return eventQueue.isEmpty();
    }
    
    /**
     * Clear all events from the queue
     */
    public void clear() {
        eventQueue.clear();
    }
    
    /**
     * Check if the event queue is active
     */
    public boolean isActive() {
        return active;
    }
    
    /**
     * Suspend event processing
     */
    public void suspend() {
        active = false;
    }
    
    /**
     * Resume event processing
     */
    public void resume() {
        active = true;
    }
    
    /**
     * Get the virtual machine
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public String toString() {
        return String.format("SelfEventQueue[size=%d, active=%b]", size(), active);
    }
}

