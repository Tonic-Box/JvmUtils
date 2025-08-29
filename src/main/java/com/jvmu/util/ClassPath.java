package com.jvmu.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.function.Predicate;

public class ClassPath
{
    public static List<Class<?>> getClasses(ClassLoader classLoader)
    {
        return getClasses(classLoader, c -> true);
    }

    public static List<Class<?>> getClassesFromPackage(ClassLoader classLoader, String packageName)
    {
        if(packageName.endsWith("."))
            packageName = packageName.substring(0, packageName.length() - 1);

        String finalPackageName = packageName;
        return getClasses(classLoader, c -> {
            String name = c.getPackageName();
            if(!name.startsWith(finalPackageName))
                return false;
            return name.replace(finalPackageName, "").isEmpty();
        });
    }

    public static List<Class<?>> getClassesFromPackageRecursive(ClassLoader classLoader, String packageName)
    {
        return getClasses(classLoader, c -> c.getPackageName().startsWith(packageName));
    }

    public static List<Class<?>> getClasses(ClassLoader classLoader, Predicate<Class<?>> predicate)
    {
        List<Class<?>> outClasses = new ArrayList<>();
        try {
            Field classesField = ClassLoader.class.getDeclaredField("classes");
            classesField.setAccessible(true);
            @SuppressWarnings("unchecked")
            Vector<Class<?>> classes = (Vector<Class<?>>) classesField.get(classLoader);
            for (Class<?> clazz : classes) {
                if (predicate.test(clazz)) {
                    outClasses.add(clazz);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outClasses;
    }
}
