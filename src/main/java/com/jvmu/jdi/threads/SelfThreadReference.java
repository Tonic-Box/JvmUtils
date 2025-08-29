package com.jvmu.jdi.threads;

import com.jvmu.jdi.vm.SelfVirtualMachine;

import java.lang.management.ThreadInfo;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.ArrayList;

/**
 * Self Thread Reference - JDI-like ThreadReference for internal threads
 * 
 * This class wraps a Java Thread object to provide JDI ThreadReference-like
 * functionality for debugging and inspecting threads within the current JVM.
 */
public class SelfThreadReference {
    
    private final SelfVirtualMachine vm;
    private final Thread thread;
    private final long threadId;
    
    public SelfThreadReference(SelfVirtualMachine vm, Thread thread) {
        this.vm = vm;
        this.thread = thread;
        this.threadId = thread.getId();
    }
    
    /**
     * Get the thread name
     */
    public String name() {
        return thread.getName();
    }
    
    /**
     * Get the thread ID
     */
    public long uniqueID() {
        return threadId;
    }
    
    /**
     * Get the thread state
     */
    public String status() {
        Thread.State state = thread.getState();
        switch (state) {
            case NEW: return "ZOMBIE";
            case RUNNABLE: return "RUNNING"; 
            case BLOCKED: return "MONITOR";
            case WAITING: return "WAIT";
            case TIMED_WAITING: return "WAIT";
            case TERMINATED: return "ZOMBIE";
            default: return state.toString();
        }
    }
    
    /**
     * Check if thread is suspended
     */
    public boolean isSuspended() {
        // We can't safely suspend threads in the same VM, so always false
        return false;
    }
    
    /**
     * Get thread priority
     */
    public int priority() {
        return thread.getPriority();
    }
    
    /**
     * Check if thread is daemon
     */
    public boolean isDaemon() {
        return thread.isDaemon();
    }
    
    /**
     * Check if thread is alive
     */
    public boolean isAlive() {
        return thread.isAlive();
    }
    
    /**
     * Get the thread group this thread belongs to
     */
    public SelfThreadGroupReference threadGroup() {
        ThreadGroup group = thread.getThreadGroup();
        return group != null ? new SelfThreadGroupReference(vm, group) : null;
    }
    
    /**
     * Get the underlying Thread object
     */
    public Thread getThread() {
        return thread;
    }
    
    /**
     * Get stack frames for this thread
     */
    public List<SelfStackFrame> getStackFrames() {
        List<SelfStackFrame> frames = new ArrayList<>();
        
        try {
            StackTraceElement[] stackTrace = thread.getStackTrace();
            
            for (int i = 0; i < stackTrace.length; i++) {
                frames.add(new SelfStackFrame(this, stackTrace[i], i));
            }
            
        } catch (Exception e) {
            // Stack trace may not be available for all threads
        }
        
        return frames;
    }
    
    /**
     * Get the current stack frame (top of stack)
     */
    public SelfStackFrame getCurrentFrame() {
        List<SelfStackFrame> frames = getStackFrames();
        return frames.isEmpty() ? null : frames.get(0);
    }
    
    /**
     * Get stack frame count
     */
    public int getFrameCount() {
        try {
            return thread.getStackTrace().length;
        } catch (Exception e) {
            return 0;
        }
    }
    
    /**
     * Get thread group reference
     */
    public SelfThreadGroupReference getThreadGroup() {
        ThreadGroup group = thread.getThreadGroup();
        return group != null ? new SelfThreadGroupReference(vm, group) : null;
    }
    
    /**
     * Get detailed thread information using ThreadMXBean
     */
    public ThreadInfo getThreadInfo() {
        try {
            return ManagementFactory.getThreadMXBean().getThreadInfo(threadId);
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * Get CPU time for this thread (if supported)
     */
    public long getCpuTime() {
        try {
            var threadMXBean = ManagementFactory.getThreadMXBean();
            if (threadMXBean.isThreadCpuTimeSupported()) {
                return threadMXBean.getThreadCpuTime(threadId);
            }
        } catch (Exception e) {
            // Not supported or thread terminated
        }
        return -1;
    }
    
    /**
     * Get user time for this thread (if supported)
     */
    public long getUserTime() {
        try {
            var threadMXBean = ManagementFactory.getThreadMXBean();
            if (threadMXBean.isThreadCpuTimeSupported()) {
                return threadMXBean.getThreadUserTime(threadId);
            }
        } catch (Exception e) {
            // Not supported or thread terminated
        }
        return -1;
    }
    
    /**
     * Get blocked time for this thread (if supported)
     */
    public long getBlockedTime() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getBlockedTime() : -1;
    }
    
    /**
     * Get waited time for this thread (if supported)  
     */
    public long getWaitedTime() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getWaitedTime() : -1;
    }
    
    /**
     * Get blocked count for this thread
     */
    public long getBlockedCount() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getBlockedCount() : -1;
    }
    
    /**
     * Get waited count for this thread
     */
    public long getWaitedCount() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getWaitedCount() : -1;
    }
    
    /**
     * Get lock name this thread is blocked on
     */
    public String getLockName() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getLockName() : null;
    }
    
    /**
     * Get owner ID of lock this thread is blocked on
     */
    public long getLockOwnerId() {
        ThreadInfo info = getThreadInfo();
        return info != null ? info.getLockOwnerId() : -1;
    }
    
    /**
     * Check if thread is in native code
     */
    public boolean isInNative() {
        ThreadInfo info = getThreadInfo();
        return info != null && info.isInNative();
    }
    
    /**
     * Get the virtual machine this thread belongs to
     */
    public SelfVirtualMachine virtualMachine() {
        return vm;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof SelfThreadReference)) return false;
        SelfThreadReference other = (SelfThreadReference) obj;
        return threadId == other.threadId;
    }
    
    @Override
    public int hashCode() {
        return Long.hashCode(threadId);
    }
    
    @Override
    public String toString() {
        return String.format("SelfThreadReference[id=%d, name=%s, state=%s, priority=%d, daemon=%b]",
            threadId, name(), status(), priority(), isDaemon());
    }
}