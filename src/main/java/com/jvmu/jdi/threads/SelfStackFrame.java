package com.jvmu.jdi.threads;

import java.lang.reflect.Method;

/**
 * Self Stack Frame - JDI-like StackFrame for internal stack frames
 */
public class SelfStackFrame {
    
    private final SelfThreadReference thread;
    private final StackTraceElement element;
    private final int frameIndex;
    
    public SelfStackFrame(SelfThreadReference thread, StackTraceElement element, int frameIndex) {
        this.thread = thread;
        this.element = element;
        this.frameIndex = frameIndex;
    }
    
    /**
     * Get the class name of this frame
     */
    public String getClassName() {
        return element.getClassName();
    }
    
    /**
     * Get the method name of this frame
     */
    public String getMethodName() {
        return element.getMethodName();
    }
    
    /**
     * Get the source file name
     */
    public String getSourceName() {
        return element.getFileName();
    }
    
    /**
     * Get the line number
     */
    public int getLineNumber() {
        return element.getLineNumber();
    }
    
    /**
     * Get frame index in the stack
     */
    public int getFrameIndex() {
        return frameIndex;
    }
    
    /**
     * Check if this is a native method frame
     */
    public boolean isNativeMethod() {
        return element.isNativeMethod();
    }
    
    /**
     * Get the thread this frame belongs to
     */
    public SelfThreadReference getThread() {
        return thread;
    }
    
    /**
     * Get the underlying StackTraceElement
     */
    public StackTraceElement getStackTraceElement() {
        return element;
    }
    
    /**
     * Try to get the actual Method object for this frame
     */
    public Method getMethod() {
        try {
            Class<?> clazz = Class.forName(element.getClassName());
            Method[] methods = clazz.getDeclaredMethods();
            
            for (Method method : methods) {
                if (method.getName().equals(element.getMethodName())) {
                    return method;
                }
            }
        } catch (Exception e) {
            // Method may not be accessible or class not found
        }
        
        return null;
    }
    
    @Override
    public String toString() {
        return String.format("SelfStackFrame[%s.%s(%s:%d)]",
            getClassName(), getMethodName(), getSourceName(), getLineNumber());
    }
}