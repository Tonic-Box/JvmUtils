package com.jvmu.jdi.threads;

import com.jvmu.jdi.vm.SelfVirtualMachine;

import java.util.List;
import java.util.ArrayList;

/**
 * Self Thread Group Reference - JDI-like ThreadGroupReference for internal thread groups
 */
public class SelfThreadGroupReference {
    
    private final SelfVirtualMachine vm;
    private final ThreadGroup threadGroup;
    
    public SelfThreadGroupReference(SelfVirtualMachine vm, ThreadGroup threadGroup) {
        this.vm = vm;
        this.threadGroup = threadGroup;
    }
    
    /**
     * Get the thread group name
     */
    public String name() {
        return threadGroup.getName();
    }
    
    /**
     * Get the parent thread group
     */
    public SelfThreadGroupReference parent() {
        ThreadGroup parent = threadGroup.getParent();
        return parent != null ? new SelfThreadGroupReference(vm, parent) : null;
    }
    
    /**
     * Get all threads in this group
     */
    public List<SelfThreadReference> threads() {
        List<SelfThreadReference> threads = new ArrayList<>();
        
        Thread[] threadArray = new Thread[threadGroup.activeCount() * 2];
        int count = threadGroup.enumerate(threadArray, false);
        
        for (int i = 0; i < count; i++) {
            if (threadArray[i] != null) {
                threads.add(new SelfThreadReference(vm, threadArray[i]));
            }
        }
        
        return threads;
    }
    
    /**
     * Get all thread groups in this group
     */
    public List<SelfThreadGroupReference> threadGroups() {
        List<SelfThreadGroupReference> groups = new ArrayList<>();
        
        ThreadGroup[] groupArray = new ThreadGroup[threadGroup.activeGroupCount() * 2];
        int count = threadGroup.enumerate(groupArray, false);
        
        for (int i = 0; i < count; i++) {
            if (groupArray[i] != null) {
                groups.add(new SelfThreadGroupReference(vm, groupArray[i]));
            }
        }
        
        return groups;
    }
    
    /**
     * Get the maximum priority for threads in this group
     */
    public int maxPriority() {
        return threadGroup.getMaxPriority();
    }
    
    /**
     * Check if this is a daemon thread group
     */
    public boolean isDaemon() {
        return threadGroup.isDaemon();
    }
    
    /**
     * Get active thread count
     */
    public int activeCount() {
        return threadGroup.activeCount();
    }
    
    /**
     * Get active group count
     */
    public int activeGroupCount() {
        return threadGroup.activeGroupCount();
    }
    
    /**
     * Get the underlying ThreadGroup
     */
    public ThreadGroup getThreadGroup() {
        return threadGroup;
    }
    
    /**
     * Get the virtual machine this thread group belongs to
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public String toString() {
        return String.format("SelfThreadGroupReference[name=%s, activeCount=%d, daemon=%b]",
            name(), activeCount(), isDaemon());
    }
}