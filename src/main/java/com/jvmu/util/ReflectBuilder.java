package com.jvmu.util;

import com.jvmu.module.ModuleBootstrap;
import com.jvmu.util.reflection.*;
import java.util.ArrayDeque;
import java.util.Deque;

public class ReflectBuilder
{
    private final Deque<Element> reflectionChain = new ArrayDeque<>();
    private final Object start;

    public static ReflectBuilder of(Object start)
    {
        return new ReflectBuilder(start);
    }

    public static ReflectBuilder unsafe()
    {
        return new ReflectBuilder(ModuleBootstrap.getInternalUnsafe());
    }

    public static ReflectBuilder ofClass(String classFqdn)
    {
        try
        {
            Class<?> navButtonClass = ModuleBootstrap.class
                    .getClassLoader()
                    .loadClass(classFqdn);
            return new ReflectBuilder(navButtonClass);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to load class: " + classFqdn, e);
        }
    }

    public static Class<?> lookupClass(String classFqdn)
    {
        try
        {
            return ModuleBootstrap.class
                    .getClassLoader()
                    .loadClass(classFqdn);
        }
        catch (ClassNotFoundException e)
        {
            throw new RuntimeException("Failed to load class: " + classFqdn, e);
        }
    }

    private ReflectBuilder(Object start)
    {
        this.start = start;
    }

    public static ReflectBuilder newInstance(String classFqdn, Class<?>[] parameterTypes, Object[] args)
    {
        try
        {
            Class<?> clazz = ModuleBootstrap.class
                    .getClassLoader()
                    .loadClass(classFqdn);

            if(parameterTypes== null || parameterTypes.length == 0)
                return new ReflectBuilder(clazz.getDeclaredConstructor().newInstance());
            else
                return new ReflectBuilder(clazz.getDeclaredConstructor(parameterTypes).newInstance(args));
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to create instance of class: " + classFqdn, e);
        }
    }

    public ReflectBuilder staticField(String name)
    {
        reflectionChain.offer(new FieldElement(true, name));
        return this;
    }

    public ReflectBuilder field(String name)
    {
        reflectionChain.offer(new FieldElement(false, name));
        return this;
    }

    public ReflectBuilder staticMethod(String name, Class<?>[] parameterTypes, Object[] args)
    {
        if (parameterTypes == null)
        {
            parameterTypes = new Class<?>[]{};
        }
        if (args == null)
        {
            args = new Object[]{};
        }
        reflectionChain.offer(new MethodElement(true, name, parameterTypes, args));
        return this;
    }

    public ReflectBuilder method(String name, Class<?>[] parameterTypes, Object[] args)
    {
        if (parameterTypes == null)
        {
            parameterTypes = new Class<?>[]{};
        }
        if (args == null)
        {
            args = new Object[]{};
        }
        reflectionChain.offer(new MethodElement(false, name, parameterTypes, args));
        return this;
    }

    public <T> T get()
    {
        Object start = this.start;
        try
        {
            while(!reflectionChain.isEmpty())
            {
                Element element = reflectionChain.poll();
                start = element.get(start);
            }
            return (T) start;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Failed to get value from reflection chain", e);
        }
    }

    public String getAsString() {
        return String.valueOf((Object) get());
    }
}
