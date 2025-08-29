package com.jvmu.internals;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.util.ReflectBuilder;

import java.lang.reflect.Field;
import java.security.ProtectionDomain;

public class InternalUnsafe {
    public static long getAddress(long address) {
        return ReflectBuilder.of(ModuleBootstrap.getInternalUnsafe().getClass())
                .method("getAddress", new Class<?>[]{long.class}, new Object[]{address})
                .get();
    }

    public static void putAddress(long address, long x) {
        ReflectBuilder.unsafe()
                .method(
                        "putAddress",
                        new Class<?>[]{long.class, long.class},
                        new Object[]{address, x}
                ).get();
    }

    public static long allocateMemory(long bytes) {
        return ReflectBuilder.unsafe()
                .method("allocateMemory", new Class<?>[]{long.class}, new Object[]{bytes})
                .get();
    }

    public static Object getUncompressedObject(long address) {
        return ReflectBuilder.unsafe()
                .method("getUncompressedObject", new Class<?>[]{long.class}, new Object[]{address})
                .get();
    }

    public static long reallocateMemory(long address, long bytes) {
        return ReflectBuilder.unsafe()
                .method("reallocateMemory", new Class<?>[]{long.class, long.class}, new Object[]{address, bytes})
                .get();
    }

    public static void setMemory(Object o, long offset, long bytes, byte value) {
        ReflectBuilder.unsafe()
                .method("setMemory", new Class<?>[]{Object.class, long.class, long.class, byte.class}, new Object[]{o, offset, bytes, value})
                .get();
    }

    public static void setMemory(long address, long bytes, byte value) {
        ReflectBuilder.unsafe()
                .method("setMemory", new Class<?>[]{long.class, long.class, byte.class}, new Object[]{address, bytes, value})
                .get();
    }

    public static void copyMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes) {
        ReflectBuilder.unsafe()
                .method(
                        "copyMemory",
                        new Class<?>[]{Object.class, long.class, Object.class, long.class, long.class},
                        new Object[]{srcBase, srcOffset, destBase, destOffset, bytes}
                ).get();
    }

    public static void copyMemory(long srcAddress, long destAddress, long bytes) {
        ReflectBuilder.unsafe()
                .method(
                        "copyMemory",
                        new Class<?>[]{long.class, long.class, long.class},
                        new Object[]{srcAddress, destAddress, bytes}
                ).get();
    }

    public static void copySwapMemory(Object srcBase, long srcOffset, Object destBase, long destOffset, long bytes, long elemSize) {
        ReflectBuilder.unsafe()
                .method(
                        "copySwapMemory",
                        new Class<?>[]{Object.class, long.class, Object.class, long.class, long.class, long.class},
                        new Object[]{srcBase, srcOffset, destBase, destOffset, bytes, elemSize}
                ).get();
    }

    public static void copySwapMemory(long srcAddress, long destAddress, long bytes, long elemSize)
    {
        ReflectBuilder.unsafe()
                .method(
                        "copySwapMemory",
                        new Class<?>[]{long.class, long.class, long.class, long.class},
                        new Object[]{srcAddress, destAddress, bytes, elemSize}
                ).get();
    }

    public static void freeMemory(long address) {
        ReflectBuilder.unsafe()
                .method("freeMemory", new Class<?>[]{long.class}, new Object[]{address})
                .get();
    }

    public static long objectFieldOffset(Field f)
    {
        return ReflectBuilder.unsafe()
                .method("objectFieldOffset", new Class<?>[]{Field.class}, new Object[]{f})
                .get();
    }

    public static long objectFieldOffset(Class<?> c, String name)
    {
        return ReflectBuilder.unsafe()
                .method("objectFieldOffset", new Class<?>[]{Class.class, String.class}, new Object[]{c, name})
                .get();
    }

    public static long staticFieldOffset(Field f)
    {
        return ReflectBuilder.unsafe()
                .method("staticFieldOffset", new Class<?>[]{Field.class}, new Object[]{f})
                .get();
    }

    public static Object staticFieldBase(Field f)
    {
        return ReflectBuilder.unsafe()
                .method("staticFieldBase", new Class<?>[]{Field.class}, new Object[]{f})
                .get();
    }

    public static Class<?> defineClass(String name, byte[] b, int off, int len, ClassLoader loader, ProtectionDomain protectionDomain) {
        return ReflectBuilder.unsafe()
                .method(
                        "defineClass",
                        new Class<?>[]{String.class, byte[].class, int.class, int.class, ClassLoader.class, ProtectionDomain.class},
                        new Object[]{name, b, off, len, loader, protectionDomain}
                ).get();
    }

    public static Class<?> defineAnonymousClass(Class<?> hostClass, byte[] data, Object[] cpPatches) {
        return ReflectBuilder.unsafe()
                .method(
                        "defineAnonymousClass",
                        new Class<?>[]{Class.class, byte[].class, Object[].class},
                        new Object[]{hostClass, data, cpPatches}
                ).get();
    }

    public static Object allocateInstance(Class<?> cls)
    {
        return ReflectBuilder.unsafe()
                .method(
                        "allocateInstance",
                        new Class<?>[]{Class.class},
                        new Object[]{cls}
                ).get();
    }

    public static boolean compareAndSetObject(Object o, long offset, Object expected, Object x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndSetObject",
                        new Class<?>[]{Object.class, long.class, Object.class, Object.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static Object compareAndExchangeObject(Object o, long offset, Object expected, Object x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndExchangeObject",
                        new Class[]{Object.class, long.class, Object.class, Object.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static boolean compareAndSetInt(Object o, long offset, int expected, int x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndSetInt",
                        new Class<?>[]{Object.class, long.class, int.class, int.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static int compareAndExchangeInt(Object o, long offset, int expected, int x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndExchangeInt",
                        new Class<?>[]{Object.class, long.class, int.class, int.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static boolean compareAndSetByte(Object o, long offset, byte expected, byte x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndSetByte",
                        new Class<?>[]{Object.class, long.class, byte.class, byte.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static byte compareAndExchangeByte(Object o, long offset, byte expected, byte x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndExchangeByte",
                        new Class<?>[]{Object.class, long.class, byte.class, byte.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static boolean compareAndSetShort(Object o, long offset, int expected, int x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndSetShort",
                        new Class<?>[]{Object.class, long.class, int.class, int.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static short compareAndExchangeShort(Object o, long offset, short expected, short x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndExchangeShort",
                        new Class<?>[]{Object.class, long.class, short.class, short.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static boolean compareAndSetChar(Object o, long offset, char expected, char x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndSetChar",
                        new Class<?>[]{Object.class, long.class, char.class, char.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }

    public static char compareAndExchangeChar(Object o, long offset, char expected, char x) {
        return ReflectBuilder.unsafe()
                .method(
                        "compareAndExchangeChar",
                        new Class<?>[]{Object.class, long.class, char.class, char.class},
                        new Object[]{o, offset, expected, x}
                ).get();
    }
}
