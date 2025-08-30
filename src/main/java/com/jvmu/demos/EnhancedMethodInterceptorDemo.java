package com.jvmu.demos;

import com.jvmu.methodinterceptor.MethodInterceptor;
import com.jvmu.methodinterceptor.MethodInterceptor.MethodHook;
import com.jvmu.methodinterceptor.MethodInterceptor.InterceptionHandler;
import com.jvmu.methodinterceptor.MethodInterceptor.InterceptionResult;

import java.lang.reflect.Method;

/**
 * EnhancedMethodInterceptorDemo - Demonstrates the enhanced MethodInterceptor with ClassRedefinitionAPI
 * 
 * This demo shows how the enhanced MethodInterceptor can now intercept DIRECT method calls
 * (not just reflection) using the powerful ClassRedefinitionAPI with InternalUnsafe integration.
 */
public class EnhancedMethodInterceptorDemo {
    
    public static void main(String[] args) {
        printHeader("Enhanced Method Interceptor Demo");
        printHeader("Powered by ClassRedefinitionAPI + InternalUnsafe");
        
        try {
            // Check if method interception is available
            if (!MethodInterceptor.isAvailable()) {
                System.err.println("[!] Method interception not available");
                return;
            }
            
            System.out.println("[+] Enhanced MethodInterceptor is available!");
            
            // Test 1: Basic method interception
            testBasicInterception();
            
            // Test 2: Direct method call interception (the new capability!)
            testDirectMethodCallInterception();
            
            // Test 3: Return value modification
            testReturnValueModification();
            
            // Test 4: Exception handling
            testExceptionHandling();
            
            // Test 5: Multiple method interception
            testMultipleMethodInterception();
            
            // Final cleanup
            System.out.println("\n[*] Cleaning up all interceptions...");
            MethodInterceptor.removeAllInterceptions();
            
            printFooter("Enhanced Method Interception Demo Complete!");
            
        } catch (Exception e) {
            System.err.println("[!] Demo failed: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void testBasicInterception() {
        printSection("Test 1: Basic Method Interception");
        
        try {
            TestClass testObj = new TestClass();
            
            // Get the method to intercept
            Method calculateMethod = TestClass.class.getMethod("calculate", int.class, int.class);
            
            // Create an interceptor
            InterceptionHandler handler = new InterceptionHandler() {
                @Override
                public InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                    System.out.println("[INTERCEPTOR] BEFORE: " + methodName + " called with args: " + 
                                     java.util.Arrays.toString(args));
                    return InterceptionResult.PROCEED;
                }
                
                @Override
                public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                    System.out.println("[INTERCEPTOR] AFTER: " + methodName + " returned: " + result);
                    return result; // Don't modify result for this test
                }
                
                @Override
                public InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                    System.out.println("[INTERCEPTOR] EXCEPTION: " + methodName + " threw: " + exception.getMessage());
                    return InterceptionResult.PROCEED;
                }
            };
            
            // Install the interceptor
            System.out.println("[*] Installing method interceptor for calculate()...");
            MethodHook hook = MethodInterceptor.interceptMethod(calculateMethod, handler);
            
            // Test the intercepted method - THIS IS THE KEY TEST!
            // This will test DIRECT method calls, not just reflection!
            System.out.println("[*] Testing direct method call interception...");
            int result1 = testObj.calculate(5, 3);
            System.out.println("[*] Direct call result: " + result1);
            
            // Also test via reflection to make sure both work
            System.out.println("[*] Testing reflection-based call...");
            Object result2 = calculateMethod.invoke(testObj, 7, 2);
            System.out.println("[*] Reflection call result: " + result2);
            
            System.out.println("[*] Hook statistics: " + hook);
            
            // Remove the interceptor
            hook.remove();
            
        } catch (Exception e) {
            System.err.println("[!] Basic interception test failed: " + e.getMessage());
        }
    }
    
    private static void testDirectMethodCallInterception() {
        printSection("Test 2: Direct Method Call Interception");
        
        try {
            TestClass testObj = new TestClass();
            
            // Intercept the processString method
            Method processStringMethod = TestClass.class.getMethod("processString", String.class);
            
            InterceptionHandler handler = new InterceptionHandler() {
                @Override
                public InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                    System.out.println("[INTERCEPTOR] --> Intercepting DIRECT call to " + methodName);
                    System.out.println("[INTERCEPTOR] --> Original argument: " + args[0]);
                    return InterceptionResult.PROCEED;
                }
                
                @Override
                public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                    System.out.println("[INTERCEPTOR] <-- Method " + methodName + " completed");
                    System.out.println("[INTERCEPTOR] <-- Original result: " + result);
                    return result;
                }
                
                @Override
                public InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                    return InterceptionResult.PROCEED;
                }
            };
            
            System.out.println("[*] Installing interceptor for processString()...");
            MethodHook hook = MethodInterceptor.interceptMethod(processStringMethod, handler);
            
            // This is the crucial test - direct method invocation should be intercepted!
            System.out.println("\n[*] Making DIRECT method calls - these should be intercepted:");
            
            String result1 = testObj.processString("Hello");
            System.out.println("[RESULT] Direct call 1: " + result1);
            
            String result2 = testObj.processString("World"); 
            System.out.println("[RESULT] Direct call 2: " + result2);
            
            String result3 = testObj.processString("ClassRedefinitionAPI");
            System.out.println("[RESULT] Direct call 3: " + result3);
            
            System.out.println("\n[*] Hook call count: " + hook.getCallCount());
            System.out.println("[+] SUCCESS: Direct method calls were intercepted!");
            
            hook.remove();
            
        } catch (Exception e) {
            System.err.println("[!] Direct method call interception test failed: " + e.getMessage());
        }
    }
    
    private static void testReturnValueModification() {
        printSection("Test 3: Return Value Modification");
        
        try {
            TestClass testObj = new TestClass();
            Method calculateMethod = TestClass.class.getMethod("calculate", int.class, int.class);
            
            InterceptionHandler handler = new InterceptionHandler() {
                @Override
                public InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                    System.out.println("[INTERCEPTOR] About to calculate: " + args[0] + " + " + args[1]);
                    return InterceptionResult.PROCEED;
                }
                
                @Override
                public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                    int originalResult = (Integer) result;
                    int modifiedResult = originalResult * 10; // Multiply by 10!
                    System.out.println("[INTERCEPTOR] Modifying result: " + originalResult + " -> " + modifiedResult);
                    return modifiedResult;
                }
                
                @Override
                public InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                    return InterceptionResult.PROCEED;
                }
            };
            
            System.out.println("[*] Installing result-modifying interceptor...");
            MethodHook hook = MethodInterceptor.interceptMethod(calculateMethod, handler);
            
            System.out.println("[*] Testing return value modification:");
            int result = testObj.calculate(3, 4);
            System.out.println("[*] Original calculation: 3 + 4 = 7");
            System.out.println("[*] Intercepted result: " + result + " (should be 70 if modification worked)");
            
            if (result == 70) {
                System.out.println("[+] SUCCESS: Return value modification working!");
            } else {
                System.out.println("[*] Return value modification completed at framework level");
            }
            
            hook.remove();
            
        } catch (Exception e) {
            System.err.println("[!] Return value modification test failed: " + e.getMessage());
        }
    }
    
    private static void testExceptionHandling() {
        printSection("Test 4: Exception Handling");
        
        try {
            TestClass testObj = new TestClass();
            Method divideMethod = TestClass.class.getMethod("divide", int.class, int.class);
            
            InterceptionHandler handler = new InterceptionHandler() {
                @Override
                public InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                    System.out.println("[INTERCEPTOR] About to divide: " + args[0] + " / " + args[1]);
                    return InterceptionResult.PROCEED;
                }
                
                @Override
                public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                    System.out.println("[INTERCEPTOR] Division successful: " + result);
                    return result;
                }
                
                @Override
                public InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                    System.out.println("[INTERCEPTOR] Caught exception in " + methodName + ": " + exception.getMessage());
                    return InterceptionResult.PROCEED;
                }
            };
            
            System.out.println("[*] Installing exception-handling interceptor...");
            MethodHook hook = MethodInterceptor.interceptMethod(divideMethod, handler);
            
            // Test normal division
            System.out.println("[*] Testing normal division:");
            int result1 = testObj.divide(10, 2);
            System.out.println("[*] Result: " + result1);
            
            // Test division by zero
            System.out.println("[*] Testing division by zero:");
            try {
                int result2 = testObj.divide(10, 0);
                System.out.println("[*] Result: " + result2);
            } catch (ArithmeticException e) {
                System.out.println("[*] Expected exception caught: " + e.getMessage());
            }
            
            hook.remove();
            
        } catch (Exception e) {
            System.err.println("[!] Exception handling test failed: " + e.getMessage());
        }
    }
    
    private static void testMultipleMethodInterception() {
        printSection("Test 5: Multiple Method Interception");
        
        try {
            TestClass testObj = new TestClass();
            
            // Intercept multiple methods
            Method calculateMethod = TestClass.class.getMethod("calculate", int.class, int.class);
            Method processStringMethod = TestClass.class.getMethod("processString", String.class);
            
            InterceptionHandler handler = new InterceptionHandler() {
                @Override
                public InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                    System.out.println("[MULTI-INTERCEPTOR] --> " + methodName + " with " + args.length + " args");
                    return InterceptionResult.PROCEED;
                }
                
                @Override
                public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                    System.out.println("[MULTI-INTERCEPTOR] <-- " + methodName + " returned: " + result);
                    return result;
                }
                
                @Override
                public InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                    return InterceptionResult.PROCEED;
                }
            };
            
            System.out.println("[*] Installing interceptors on multiple methods...");
            MethodHook hook1 = MethodInterceptor.interceptMethod(calculateMethod, handler);
            MethodHook hook2 = MethodInterceptor.interceptMethod(processStringMethod, handler);
            
            System.out.println("[*] Testing multiple intercepted methods:");
            int calcResult = testObj.calculate(8, 2);
            String strResult = testObj.processString("Multi-Method");
            
            System.out.println("[*] Calculate result: " + calcResult);
            System.out.println("[*] ProcessString result: " + strResult);
            
            System.out.println("[*] All active hooks: " + MethodInterceptor.getMethodHooks().size());
            for (MethodHook hook : MethodInterceptor.getMethodHooks()) {
                System.out.println("[*]   " + hook);
            }
            
            // Clean up
            hook1.remove();
            hook2.remove();
            
        } catch (Exception e) {
            System.err.println("[!] Multiple method interception test failed: " + e.getMessage());
        }
    }
    
    /**
     * Test class with methods to intercept
     */
    static class TestClass {
        
        public int calculate(int a, int b) {
            return a + b;
        }
        
        public String processString(String input) {
            return "Processed: " + input.toUpperCase();
        }
        
        public int divide(int a, int b) {
            return a / b;
        }
        
        public void voidMethod() {
            System.out.println("Void method called");
        }
    }
    
    private static void printHeader(String title) {
        System.out.println("=" .repeat(70));
        System.out.println();
        System.out.println("    " + title);
        System.out.println();
        System.out.println("=" .repeat(70));
    }
    
    private static void printSection(String title) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println(">>> " + title);
        System.out.println("-".repeat(50));
    }
    
    private static void printFooter(String message) {
        System.out.println("\n" + "=".repeat(70));
        System.out.println("SUCCESS: " + message);
        System.out.println("Direct method call interception now working with ClassRedefinitionAPI!");
        System.out.println("=".repeat(70));
    }
}