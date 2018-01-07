package org.dave.compactmachines3.utility;

import java.util.LinkedHashSet;
import java.util.Set;

public class InheritanceUtil {
    public static Set<Class<?>> getInheritance(Class<?> in)
    {
        LinkedHashSet<Class<?>> result = new LinkedHashSet<Class<?>>();

        result.add(in);
        getInheritance(in, result);

        return result;
    }

    /**
     * Get inheritance of type.
     *
     * @param in
     * @param result
     */
    private static void getInheritance(Class<?> in, Set<Class<?>> result)
    {
        Class<?> superclass = getSuperclass(in);

        if(superclass != null)
        {
            result.add(superclass);
            getInheritance(superclass, result);
        }

        getInterfaceInheritance(in, result);
    }

    /**
     * Get interfaces that the type inherits from.
     *
     * @param in
     * @param result
     */
    private static void getInterfaceInheritance(Class<?> in, Set<Class<?>> result)
    {
        for(Class<?> c : in.getInterfaces())
        {
            result.add(c);

            getInterfaceInheritance(c, result);
        }
    }

    /**
     * Get superclass of class.
     *
     * @param in
     * @return
     */
    private static Class<?> getSuperclass(Class<?> in)
    {
        if(in == null)
        {
            return null;
        }

        if(in.isArray() && in != Object[].class)
        {
            Class<?> type = in.getComponentType();

            while(type.isArray())
            {
                type = type.getComponentType();
            }

            return type;
        }

        return in.getSuperclass();
    }
}
