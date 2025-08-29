package com.jvmu.demos;

import com.jvmu.methodinterceptor.MethodInterceptor;

import java.lang.reflect.Method;

/**
 * MethodInterceptionDemo - Clean demonstration of ASM-based method interception
 * 
 * This demo showcases the final implementation that can intercept BOTH:
 * 1. Direct method calls (obj.method(args))
 * 2. Reflective method calls (method.invoke(obj, args))
 * 
 * The interception is achieved through ASM bytecode manipulation that modifies
 * the actual method implementation at runtime.
 */
public class MethodInterceptionDemo {
    
    public static void main(String[] args) {
        printHeader("ASM-Based Method Interception Demo");
        
        try {
            // Check if method interception is available
            if (!MethodInterceptor.isAvailable()) {
                System.err.println("[!] Method interception not available!");
                System.err.println("    Requires: ASM library + ModuleBootstrap");
                return;
            }
            
            System.out.println("[+] Method interception system ready!");
            
            // Run the demonstration
            demonstrateMethodInterception();
            
            System.out.println("\n" + "=".repeat(60));
            System.out.println("SUCCESS: Method interception demonstration completed!");
            System.out.println("=".repeat(60));
            
        } catch (Exception e) {
            System.err.println("[!] Demo failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // Clean up all interceptions
            cleanup();
        }
    }
    
    private static void demonstrateMethodInterception() throws Exception {
        System.out.println("\n" + "-".repeat(50));
        System.out.println(">>> ASM Bytecode Method Interception Test");
        System.out.println("-".repeat(50));
        
        // Create test target
        TestCalculator calculator = new TestCalculator();
        Method calculateMethod = TestCalculator.class.getDeclaredMethod("calculate", int.class, int.class);
        
        // Create interception handler
        MethodInterceptor.InterceptionHandler handler = new MethodInterceptor.InterceptionHandler() {
            @Override
            public MethodInterceptor.InterceptionResult beforeInvocation(String methodName, Object instance, Object[] args) {
                System.out.println("  [BEFORE] " + methodName + " called with: " + java.util.Arrays.toString(args));
                return MethodInterceptor.InterceptionResult.PROCEED;
            }
            
            @Override
            public Object afterInvocation(String methodName, Object instance, Object[] args, Object result) {
                System.out.println("  [AFTER]  " + methodName + " returned: " + result);
                
                // Modify result to prove interception works
                if (result instanceof Integer) {
                    int modifiedResult = (Integer) result + 1000;
                    System.out.println("  [MODIFY] Result modified: " + result + " -> " + modifiedResult);
                    return modifiedResult;
                }
                return result;
            }
            
            @Override
            public MethodInterceptor.InterceptionResult onException(String methodName, Object instance, Object[] args, Throwable exception) {
                System.out.println("  [ERROR]  " + methodName + " threw: " + exception.getMessage());
                return MethodInterceptor.InterceptionResult.PROCEED;
            }
        };
        
        // Install method interception
        MethodInterceptor.MethodHook hook = MethodInterceptor.interceptMethod(calculateMethod, handler);
        System.out.println("\n[+] Installed interception: " + hook);
        
        // Test 1: Reflective calls (traditional interception)
        System.out.println("\n--- Test 1: Reflective Method Calls ---");
        for (int i = 1; i <= 3; i++) {
            System.out.println("Calling method.invoke(calculator, " + i + ", " + (i*10) + ")");
            Object result = calculateMethod.invoke(calculator, i, i * 10);
            System.out.println("Final result: " + result + " (should be original + 1000)");
            System.out.println();
        }
        
        // Test 2: Direct method calls (THE CRITICAL TEST)
        System.out.println("--- Test 2: Direct Method Calls (CRITICAL TEST) ---");
        System.out.println("This is the test that proves ASM bytecode interception works!");
        System.out.println();
        
        for (int i = 4; i <= 6; i++) {
            System.out.println("Calling calculator.calculate(" + i + ", " + (i*10) + ") directly");
            int result = calculator.calculate(i, i * 10);
            System.out.println("Final result: " + result);
            
            if (result > 1000) {
                System.out.println("[SUCCESS] Direct call was intercepted! (+1000 modifier applied)");
            } else {
                System.out.println("[WARNING] Direct call may not have been intercepted");
            }
            System.out.println();
        }
        
        // Test 3: Multiple operations
        System.out.println("--- Test 3: Complex Operations ---");
        System.out.println("Testing various operations to show comprehensive interception:");
        
        int sum = calculator.calculate(10, 20) + calculator.calculate(5, 15);
        System.out.println("Sum of two intercepted calls: " + sum);
        
        int product = calculator.multiply(calculator.calculate(2, 3), 4);
        System.out.println("Product using intercepted method: " + product);
        
        // Show interception statistics
        System.out.println("\n--- Interception Statistics ---");
        java.util.List<MethodInterceptor.MethodHook> hooks = MethodInterceptor.getMethodHooks();
        for (MethodInterceptor.MethodHook h : hooks) {
            System.out.println("Hook: " + h + " (Calls: " + h.getCallCount() + ")");
        }
        
        // Remove interception
        hook.remove();
        System.out.println("\n[+] Removed method interception");
        
        // Test 4: Verify interception is removed
        System.out.println("\n--- Test 4: Post-Interception Verification ---");
        int finalResult = calculator.calculate(100, 200);
        System.out.println("Result after hook removal: " + finalResult + " (should be normal)");
    }
    
    /**
     * Test target class - simple calculator
     */
    static class TestCalculator {
        
        /**
         * Main test method that we'll intercept
         */
        public int calculate(int a, int b) {
            // Simple calculation: a + b * 2
            return a + (b * 2);
        }
        
        /**
         * Helper method for testing
         */
        public int multiply(int a, int b) {
            return a * b;
        }
        
        /**
         * Another test method
         */
        public String processString(String input) {
            return "Processed: " + input.toUpperCase();
        }
    }
    
    private static void cleanup() {
        try {
            MethodInterceptor.removeAllInterceptions();
            System.out.println("\n[+] Cleaned up all method interceptions");
        } catch (Exception e) {
            System.err.println("[!] Cleanup failed: " + e.getMessage());
        }
    }
    
    private static void printHeader(String title) {
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("    " + title);
        System.out.println("    True Direct + Reflective Call Interception");
        System.out.println("    Using ASM Bytecode Manipulation");
        System.out.println();
        System.out.println("=".repeat(60));
    }
}