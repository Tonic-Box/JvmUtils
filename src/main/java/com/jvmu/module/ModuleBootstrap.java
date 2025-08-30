package com.jvmu.module;

import sun.misc.Unsafe;

import java.lang.invoke.*;
import java.lang.reflect.*;
import java.util.*;

public class ModuleBootstrap {
    private static Object unsafe;
    private static Object internalUnsafe;
    private static MethodHandles.Lookup trustedLookup;
    private static Object sharedSecrets;
    private static Object reflectionFactory;
    private static Object vm;
    private static Object modules;
    private static Object classLoaderHelper;
    private static Object perf;
    private static Object magicAccessorImpl;
    private static Object methodAccessorGenerator;
    private static Object classDefiner;

    static {
        try {
            bootstrapUnsafeAccess();
        } catch (Exception e) {
            throw new RuntimeException("Failed to bootstrap Unsafe access", e);
        }
    }

    private static void bootstrapUnsafeAccess() throws Exception {
        try {
            Field theUnsafeField = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = theUnsafeField.get(null);
        } catch (Exception e) {
            throw new RuntimeException("Cannot get Unsafe - this method requires Unsafe access", e);
        }

        Class<?> unsafeClass = unsafe.getClass();
        Method staticFieldBase = unsafeClass.getMethod("staticFieldBase", Field.class);
        Method staticFieldOffset = unsafeClass.getMethod("staticFieldOffset", Field.class);
        Method getObject = unsafeClass.getMethod("getObject", Object.class, long.class);

        Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
        Object fieldBase = staticFieldBase.invoke(unsafe, implLookupField);
        long fieldOffset = (Long) staticFieldOffset.invoke(unsafe, implLookupField);
        trustedLookup = (MethodHandles.Lookup) getObject.invoke(unsafe, fieldBase, fieldOffset);

        // Now get the internal jdk.internal.misc.Unsafe
        bootstrapInternalUnsafe();

        // Get other powerful internal APIs
        bootstrapInternalAPIs();

        openModulePackages();
    }

    private static void bootstrapInternalUnsafe() throws Exception {
        try {
            // Get the internal jdk.internal.misc.Unsafe class
            Class<?> internalUnsafeClass = Class.forName("jdk.internal.misc.Unsafe");

            // Use trusted lookup to access the internal Unsafe field
            Field theInternalUnsafeField = internalUnsafeClass.getDeclaredField("theUnsafe");

            // Use trusted lookup to get unrestricted access to the field
            MethodHandle fieldHandle = trustedLookup.unreflectGetter(theInternalUnsafeField);
            internalUnsafe = fieldHandle.invoke();


        } catch (Throwable e) {
            System.out.println("[WARNING] Could not get internal Unsafe: " + e.getClass().getSimpleName());
            // Fallback: try direct field access using our sun.misc.Unsafe
            try {
                Class<?> internalUnsafeClass = Class.forName("jdk.internal.misc.Unsafe");
                Field theInternalUnsafeField = internalUnsafeClass.getDeclaredField("theUnsafe");

                // Use sun.misc.Unsafe to access the internal field directly
                Method staticFieldBase = unsafe.getClass().getMethod("staticFieldBase", Field.class);
                Method staticFieldOffset = unsafe.getClass().getMethod("staticFieldOffset", Field.class);
                Method getObject = unsafe.getClass().getMethod("getObject", Object.class, long.class);

                Object fieldBase = staticFieldBase.invoke(unsafe, theInternalUnsafeField);
                long fieldOffset = (Long) staticFieldOffset.invoke(unsafe, theInternalUnsafeField);
                internalUnsafe = getObject.invoke(unsafe, fieldBase, fieldOffset);


            } catch (Exception e2) {
                System.out.println("[INFO] Could not access internal Unsafe via any method: " + e2.getClass().getSimpleName());
                internalUnsafe = null;
            }
        }
    }

    private static void bootstrapInternalAPIs() throws Exception {
        // SharedSecrets - access to internal JDK secrets
        try {
            Class<?> sharedSecretsClass = Class.forName("jdk.internal.access.SharedSecrets");
            MethodHandle getSharedSecretsHandle = trustedLookup.findStatic(sharedSecretsClass, "getJavaLangAccess",
                    MethodType.methodType(Class.forName("jdk.internal.access.JavaLangAccess")));
            sharedSecrets = getSharedSecretsHandle.invoke();
        } catch (Throwable e) {
            sharedSecrets = null;
        }

        // ReflectionFactory - powerful reflection manipulation
        try {
            Class<?> reflectionFactoryClass = Class.forName("jdk.internal.reflect.ReflectionFactory");
            MethodHandle getReflectionFactoryHandle = trustedLookup.findStatic(reflectionFactoryClass, "getReflectionFactory",
                    MethodType.methodType(reflectionFactoryClass));
            reflectionFactory = getReflectionFactoryHandle.invoke();
        } catch (Throwable e) {
            reflectionFactory = null;
        }

        // VM - VM internals access
        try {
            Class<?> vmClass = Class.forName("jdk.internal.misc.VM");
            vm = vmClass; // Static class, no instance needed
        } catch (Throwable e) {
            vm = null;
        }

        // Modules - direct module manipulation
        try {
            modules = Class.forName("jdk.internal.module.Modules");
        } catch (Throwable e) {
            modules = null;
        }

        // ClassLoaderHelper - classloading internals
        try {
            classLoaderHelper = Class.forName("jdk.internal.loader.ClassLoaderHelper");
        } catch (Throwable e) {
            classLoaderHelper = null;
        }

        // Perf - performance counters and JVM metrics
        try {
            Class<?> perfClass = Class.forName("jdk.internal.perf.Perf");
            MethodHandle getPerfHandle = trustedLookup.findStatic(perfClass, "getPerf", MethodType.methodType(perfClass));
            perf = getPerfHandle.invoke();
        } catch (Throwable e) {
            perf = null;
        }

        // MagicAccessorImpl - bytecode generation for reflection
        try {
            magicAccessorImpl = Class.forName("jdk.internal.reflect.MagicAccessorImpl");
        } catch (Throwable e) {
            magicAccessorImpl = null;
        }

        // MethodAccessorGenerator - bytecode generation engine
        try {
            Class<?> genClass = Class.forName("jdk.internal.reflect.MethodAccessorGenerator");
            methodAccessorGenerator = genClass.getDeclaredConstructor().newInstance();
        } catch (Throwable e) {
            methodAccessorGenerator = null;
        }

        // ClassDefiner - bytecode loading via Unsafe.defineClass
        try {
            classDefiner = Class.forName("jdk.internal.reflect.ClassDefiner");
        } catch (Throwable e) {
            classDefiner = null;
        }
    }

    private static void openModulePackages() throws Exception {
        Class<?> modulesClass = Class.forName("jdk.internal.module.Modules");
        Method addOpensMethod = modulesClass.getDeclaredMethod("addOpensToAllUnnamed", Module.class, String.class);
        MethodHandle addOpensHandle = trustedLookup.unreflect(addOpensMethod);
        ModuleLayer layer = ModuleLayer.boot();
        Set<Module> allModules = layer.modules();

        for (Module module : allModules) {
            Set<String> packages = module.getPackages();
            if (!packages.isEmpty()) {
                for (String pkg : packages) {
                    try {
                        addOpensHandle.invoke(module, pkg);
                    } catch (Throwable e) {
                    }
                }
            }
        }
    }

    // Getter methods
    public static Object getUnsafe() { return unsafe; }
    public static Object getInternalUnsafe() { return internalUnsafe; }
    public static MethodHandles.Lookup getTrustedLookup() { return trustedLookup; }
    public static Object getSharedSecrets() { return sharedSecrets; }
    public static Object getReflectionFactory() { return reflectionFactory; }
    public static Object getVm() { return vm; }
    public static Object getModules() { return modules; }
    public static Object getClassLoaderHelper() { return classLoaderHelper; }
    public static Object getPerf() { return perf; }
    public static Object getMagicAccessorImpl() { return magicAccessorImpl; }
    public static Object getMethodAccessorGenerator() { return methodAccessorGenerator; }
    public static Object getClassDefiner() { return classDefiner; }
}