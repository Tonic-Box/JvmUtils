package com.jvmu.util.reflection;

import com.jvmu.util.ReflectUtil;

public class FieldElement implements Element
{
    private final boolean isStatic;
    private final String name;

    public FieldElement(boolean isStatic, String name) {
        this.isStatic = isStatic;
        this.name = name;
    }

    @Override
    public Object get(Object o) throws Exception {
        if(isStatic)
        {
            Class<?> clazz = o instanceof Class ? (Class<?>) o : o.getClass();
            return ReflectUtil.getStaticField(clazz, name);
        }
        else
        {
            return ReflectUtil.getField(o, name);
        }
    }
}