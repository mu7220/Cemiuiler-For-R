package com.sevtinge.cemiuiler.utils.hook;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.reflect.MemberUtilsX;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import io.github.libxposed.api.XposedInterface;

public class XposedAdd extends XposedField {
    public static XposedInterface moduleInst;

    private static final ConcurrentHashMap<MemberCacheKey.Method, Optional<Method>> methodCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<MemberCacheKey.Constructor, Optional<Constructor<?>>> constructorCache = new ConcurrentHashMap<>();


    /**
     * Look up a class with the specified class loader.
     *
     * <p>There are various allowed syntaxes for the class name, but it's recommended to use one of
     * these:
     * <ul>
     *   <li>{@code java.lang.String}
     *   <li>{@code java.lang.String[]} (array)
     *   <li>{@code android.app.ActivityThread.ResourcesKey}
     *   <li>{@code android.app.ActivityThread$ResourcesKey}
     * </ul>
     *
     * @param className The class name in one of the formats mentioned above.
     * @return A reference to the class.
     * @throws XposedHelpers.ClassNotFoundError In case the class was not found.
     */
    public static Class<?> findClass(String className, ClassLoader classLoader) {
        if (classLoader == null) {
            classLoader = moduleInst.getClass().getClassLoader();
        }
        try {
            return ClassUtils.getClass(classLoader, className, false);
        } catch (ClassNotFoundException e) {
            throw new XposedHelpers.ClassNotFoundError(e);
        }
    }

    /**
     * Look up and return a class if it exists.
     * Like {@link #findClass}, but doesn't throw an exception if the class doesn't exist.
     *
     * @param className The class name.
     * @return A reference to the class, or {@code null} if it doesn't exist.
     */
    public static Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            return null;
        }
    }

    /**
     * Look up a method and hook it. The last argument must be the callback for the hook.
     *
     * @param clazz                     The class which implements the method.
     * @param methodName                The target method name.
     * @param parameterTypesAndCallback The parameter types of the target method, plus the callback.
     * @return An object which can be used to remove the callback again.
     * @throws NoSuchMethodError                In case the method was not found.
     * @throws XposedHelpers.ClassNotFoundError In case the target class or one of the parameter types couldn't be resolved.
     */
    public static HookerClassHelper.CustomMethodUnhooker findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length - 1] instanceof HookerClassHelper.MethodHook callback))
            throw new IllegalArgumentException("no callback defined");

        Method m = findMethodExact(clazz, methodName, getParameterClasses(parameterTypesAndCallback, clazz.getClassLoader()));
        return doHookMethod(m, callback);
    }

    /**
     * Look up a method in a class and set it to accessible.
     */
    public static Method findMethodExact(Class<?> clazz, String methodName, Object... parameterTypes) {
        return findMethodExact(clazz, methodName, getParameterClasses(parameterTypes, clazz.getClassLoader()));
    }

    /**
     * Look up and return a method if it exists.
     */
    public static Method findMethodExactIfExists(Class<?> clazz, String methodName, Object... parameterTypes) {
        try {
            return findMethodExact(clazz, methodName, parameterTypes);
        } catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError e) {
            return null;
        }
    }

    /**
     * Look up a method in a class and set it to accessible.
     *
     * <p>This variant requires that you already have reference to all the parameter types.
     */
    public static Method findMethodExact(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        MemberCacheKey.Method key = new MemberCacheKey.Method(clazz, methodName, parameterTypes, true);

        return methodCache.computeIfAbsent(key, k -> {
            try {
                Method method = k.clazz.getDeclaredMethod(k.name, k.parameters);
                method.setAccessible(true);
                return Optional.of(method);
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }).orElseThrow(() -> new NoSuchMethodError(key.toString()));
    }

    /**
     * Returns an array of all methods declared/overridden in a class with the specified parameter types.
     *
     * <p>The return type is optional, it will not be compared if it is {@code null}.
     * Use {@code void.class} if you want to search for methods returning nothing.
     *
     * @param clazz          The class to look in.
     * @param returnType     The return type, or {@code null} (see above).
     * @param parameterTypes The parameter types.
     * @return An array with matching methods, all set to accessible already.
     */
    public static Method[] findMethodsByExactParameters(Class<?> clazz, Class<?> returnType, Class<?>... parameterTypes) {
        List<Method> result = new LinkedList<>();
        for (Method method : clazz.getDeclaredMethods()) {
            if (returnType != null && returnType != method.getReturnType())
                continue;

            Class<?>[] methodParameterTypes = method.getParameterTypes();
            if (parameterTypes.length != methodParameterTypes.length)
                continue;

            boolean match = true;
            for (int i = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i] != methodParameterTypes[i]) {
                    match = false;
                    break;
                }
            }

            if (!match)
                continue;

            method.setAccessible(true);
            result.add(method);
        }
        return result.toArray(new Method[result.size()]);
    }

    /**
     * Look up a method in a class and set it to accessible.
     *
     * <p>This does'nt only look for exact matches, but for the best match. All considered candidates
     * must be compatible with the given parameter types, i.e. the parameters must be assignable
     * to the method's formal parameters. Inherited methods are considered here.
     *
     * @param clazz          The class which declares, inherits or overrides the method.
     * @param methodName     The method name.
     * @param parameterTypes The types of the method's parameters.
     * @return A reference to the best-matching method.
     * @throws NoSuchMethodError In case no suitable method was found.
     */
    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        // find the exact matching method first
        try {
            return findMethodExact(clazz, methodName, parameterTypes);
        } catch (NoSuchMethodError ignored) {
        }

        // then find the best match
        MemberCacheKey.Method key = new MemberCacheKey.Method(clazz, methodName, parameterTypes, false);

        return methodCache.computeIfAbsent(key, k -> {
            Method bestMatch = null;
            Class<?> clz = k.clazz;
            boolean considerPrivateMethods = true;
            do {
                for (Method method : clz.getDeclaredMethods()) {
                    // don't consider private methods of superclasses
                    if (!considerPrivateMethods && Modifier.isPrivate(method.getModifiers()))
                        continue;

                    // compare name and parameters
                    if (method.getName().equals(k.name) && ClassUtils.isAssignable(
                        k.parameters,
                        method.getParameterTypes(),
                        true)) {
                        // get accessible version of method
                        if (bestMatch == null || MemberUtilsX.compareMethodFit(
                            method,
                            bestMatch,
                            k.parameters) < 0) {
                            bestMatch = method;
                        }
                    }
                }
                considerPrivateMethods = false;
            } while ((clz = clz.getSuperclass()) != null);

            if (bestMatch != null) {
                bestMatch.setAccessible(true);
                return Optional.of(bestMatch);
            } else {
                return Optional.empty();
            }
        }).orElseThrow(() -> new NoSuchMethodError(key.toString()));
    }

    /**
     * Look up a method in a class and set it to accessible.
     *
     * <p>See {@link #findMethodBestMatch(Class, String, Class...)} for details. This variant
     * determines the parameter types from the classes of the given objects.
     */
    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Object... args) {
        return findMethodBestMatch(clazz, methodName, getParameterTypes(args));
    }

    /**
     * Look up a method in a class and set it to accessible.
     *
     * <p>See {@link #findMethodBestMatch(Class, String, Class...)} for details. This variant
     * determines the parameter types from the classes of the given objects. For any item that is
     * {@code null}, the type is taken from {@code parameterTypes} instead.
     */
    public static Method findMethodBestMatch(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object[] args) {
        Class<?>[] argsClasses = null;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != null)
                continue;
            if (argsClasses == null)
                argsClasses = getParameterTypes(args);
            parameterTypes[i] = argsClasses[i];
        }
        return findMethodBestMatch(clazz, methodName, parameterTypes);
    }

    /**
     * Hook all constructors of the specified class.
     *
     * @param hookClass The class to check for constructors.
     * @param callback  The callback to be executed when the hooked constructors are called.
     * @return A set containing one object for each found constructor which can be used to unhook it.
     */
    public static Set<HookerClassHelper.CustomMethodUnhooker> hookAllConstructors(Class<?> hookClass, HookerClassHelper.MethodHook callback) {
        Set<HookerClassHelper.CustomMethodUnhooker> unhooks = new HashSet<>();
        for (Constructor<?> constructor : hookClass.getDeclaredConstructors())
            unhooks.add(doHookConstructor(constructor, callback));
        return unhooks;
    }

    /**
     * Hooks all methods with a certain name that were declared in the specified class. Inherited
     * methods and constructors are not considered. For constructors, use
     * {@link #hookAllConstructors} instead.
     *
     * @param hookClass  The class to check for declared methods.
     * @param methodName The name of the method(s) to hook.
     * @param callback   The callback to be executed when the hooked methods are called.
     * @return A set containing one object for each found method which can be used to unhook it.
     */
    public static Set<HookerClassHelper.CustomMethodUnhooker> hookAllMethods(Class<?> hookClass, String methodName, HookerClassHelper.MethodHook callback) {
        Set<HookerClassHelper.CustomMethodUnhooker> unhooks = new HashSet<>();
        for (Method method : hookClass.getDeclaredMethods())
            if (method.getName().equals(methodName))
                unhooks.add(doHookMethod(method, callback));
        return unhooks;
    }

    /**
     * Returns an array with the classes of the given objects.
     */
    public static Class<?>[] getParameterTypes(Object... args) {
        Class<?>[] clazzes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            clazzes[i] = (args[i] != null) ? args[i].getClass() : null;
        }
        return clazzes;
    }

    /**
     * Retrieve classes from an array, where each element might either be a Class
     * already, or a String with the full class name.
     */
    public static Class<?>[] getParameterClasses(Object[] parameterTypesAndCallback, ClassLoader classLoader) {
        Class<?>[] parameterClasses = null;
        for (int i = parameterTypesAndCallback.length - 1; i >= 0; i--) {
            Object type = parameterTypesAndCallback[i];
            if (type == null)
                throw new XposedHelpers.ClassNotFoundError("parameter type must not be null", null);

            // ignore trailing callback
            if (type instanceof HookerClassHelper.MethodHook)
                continue;

            if (parameterClasses == null)
                parameterClasses = new Class<?>[i + 1];

            if (type instanceof Class)
                parameterClasses[i] = (Class<?>) type;
            else if (type instanceof String)

                parameterClasses[i] = findClass((String) type, classLoader);
            else
                throw new XposedHelpers.ClassNotFoundError("parameter type must either be specified as Class or String", null);
        }

        // if there are no arguments for the method
        if (parameterClasses == null)
            parameterClasses = new Class<?>[0];

        return parameterClasses;
    }

    /**
     * Returns an array of the given classes.
     */
    public static Class<?>[] getClassesAsArray(Class<?>... clazzes) {
        return clazzes;
    }

    private static String getParametersString(Class<?>... clazzes) {
        StringBuilder sb = new StringBuilder("(");
        boolean first = true;
        for (Class<?> clazz : clazzes) {
            if (first)
                first = false;
            else
                sb.append(",");

            if (clazz != null)
                sb.append(clazz.getCanonicalName());
            else
                sb.append("null");
        }
        sb.append(")");
        return sb.toString();
    }

    /**
     * Look up a constructor of a class and set it to accessible.
     */
    public static Constructor<?> findConstructorExact(Class<?> clazz, Object... parameterTypes) {
        return findConstructorExact(clazz, getParameterClasses(parameterTypes, clazz.getClassLoader()));
    }

    /**
     * Look up and return a constructor if it exists.
     */
    public static Constructor<?> findConstructorExactIfExists(Class<?> clazz, Object... parameterTypes) {
        try {
            return findConstructorExact(clazz, parameterTypes);
        } catch (XposedHelpers.ClassNotFoundError | NoSuchMethodError e) {
            return null;
        }
    }

    /**
     * Look up a constructor of a class and set it to accessible.
     * See {@link #findMethodExact(Class, String, Object...)} for details.
     */
    public static Constructor<?> findConstructorExact(Class<?> clazz, Class<?>... parameterTypes) {
        MemberCacheKey.Constructor key = new MemberCacheKey.Constructor(clazz, parameterTypes, true);

        return constructorCache.computeIfAbsent(key, k -> {
            try {
                Constructor<?> constructor = k.clazz.getDeclaredConstructor(k.parameters);
                constructor.setAccessible(true);
                return Optional.of(constructor);
            } catch (NoSuchMethodException e) {
                return Optional.empty();
            }
        }).orElseThrow(() -> new NoSuchMethodError(key.toString()));
    }

    /**
     * Look up a constructor and hook it. See {@link #findAndHookMethod(Class, String, Object...)}
     * for details.
     */
    public static HookerClassHelper.CustomMethodUnhooker findAndHookConstructor(Class<?> clazz, Object... parameterTypesAndCallback) {
        if (parameterTypesAndCallback.length == 0 || !(parameterTypesAndCallback[parameterTypesAndCallback.length - 1] instanceof HookerClassHelper.MethodHook callback))
            throw new IllegalArgumentException("no callback defined");

        Constructor<?> m = findConstructorExact(clazz, getParameterClasses(parameterTypesAndCallback, clazz.getClassLoader()));
        return doHookConstructor(m, callback);
    }

    /**
     * Look up a constructor in a class and set it to accessible.
     *
     * <p>See {@link #findMethodBestMatch(Class, String, Class...)} for details.
     */
    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>... parameterTypes) {
        // find the exact matching constructor first
        try {
            return findConstructorExact(clazz, parameterTypes);
        } catch (NoSuchMethodError ignored) {
        }

        // then find the best match
        MemberCacheKey.Constructor key = new MemberCacheKey.Constructor(clazz, parameterTypes, false);

        return constructorCache.computeIfAbsent(key, k -> {
            Constructor<?> bestMatch = null;
            Constructor<?>[] constructors = k.clazz.getDeclaredConstructors();
            for (Constructor<?> constructor : constructors) {
                // compare name and parameters
                if (ClassUtils.isAssignable(
                    k.parameters,
                    constructor.getParameterTypes(),
                    true)) {
                    // get accessible version of method
                    if (bestMatch == null || MemberUtilsX.compareConstructorFit(
                        constructor,
                        bestMatch,
                        k.parameters) < 0) {
                        bestMatch = constructor;
                    }
                }
            }

            if (bestMatch != null) {
                bestMatch.setAccessible(true);
                return Optional.of(bestMatch);
            } else {
                return Optional.empty();
            }
        }).orElseThrow(() -> new NoSuchMethodError(key.toString()));
    }

    /**
     * Look up a constructor in a class and set it to accessible.
     *
     * <p>See {@link #findMethodBestMatch(Class, String, Class...)} for details. This variant
     * determines the parameter types from the classes of the given objects.
     */
    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Object... args) {
        return findConstructorBestMatch(clazz, getParameterTypes(args));
    }

    /**
     * Look up a constructor in a class and set it to accessible.
     *
     * <p>See {@link #findMethodBestMatch(Class, String, Class...)} for details. This variant
     * determines the parameter types from the classes of the given objects. For any item that is
     * {@code null}, the type is taken from {@code parameterTypes} instead.
     */
    public static Constructor<?> findConstructorBestMatch(Class<?> clazz, Class<?>[] parameterTypes, Object[] args) {
        Class<?>[] argsClasses = null;
        for (int i = 0; i < parameterTypes.length; i++) {
            if (parameterTypes[i] != null)
                continue;
            if (argsClasses == null)
                argsClasses = getParameterTypes(args);
            parameterTypes[i] = argsClasses[i];
        }
        return findConstructorBestMatch(clazz, parameterTypes);
    }

    public static HookerClassHelper.CustomMethodUnhooker doHookMethod(Method m, HookerClassHelper.MethodHook hook) {
        HookerClassHelper.CustomMethodUnhooker unhooker;
        boolean hooked;
        if (hook.mPriority > XposedInterface.PRIORITY_DEFAULT) {
            hooked = HookerClassHelper.HighestPriorityHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.HighestPriorityHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.HighestPriorityHooker.class);
            }
        } else if (hook.mPriority < XposedInterface.PRIORITY_DEFAULT) {
            hooked = HookerClassHelper.LowestPriorityHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.LowestPriorityHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.LowestPriorityHooker.class);
            }
        } else {
            hooked = HookerClassHelper.CustomHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.CustomHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.CustomHooker.class);
            }
        }

        return unhooker;
    }

    public static HookerClassHelper.CustomMethodUnhooker doHookConstructor(Constructor<?> m, HookerClassHelper.MethodHook hook) {
        HookerClassHelper.CustomMethodUnhooker unhooker;
        boolean hooked;
        if (hook.mPriority > XposedInterface.PRIORITY_DEFAULT) {
            hooked = HookerClassHelper.HighestPriorityHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.HighestPriorityHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.HighestPriorityHooker.class);
            }
        } else if (hook.mPriority < XposedInterface.PRIORITY_DEFAULT) {
            hooked = HookerClassHelper.LowestPriorityHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.LowestPriorityHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.LowestPriorityHooker.class);
            }
        } else {
            hooked = HookerClassHelper.CustomHooker.memberIsRegistered(m);
            unhooker = HookerClassHelper.CustomHooker.addCallback(m, hook);
            if (!hooked) {
                moduleInst.hook(m, HookerClassHelper.CustomHooker.class);
            }
        }

        return unhooker;
    }
}
