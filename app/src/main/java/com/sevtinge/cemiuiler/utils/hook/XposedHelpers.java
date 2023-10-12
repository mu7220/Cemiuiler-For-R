/*
 * This file is from LSPosed.
 *
 * LSPosed is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * LSPosed is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LSPosed.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Copyright (C) 2020 EdXposed Contributors
 * Copyright (C) 2021 LSPosed Contributors
 */
package com.sevtinge.cemiuiler.utils.hook;

import static com.sevtinge.cemiuiler.utils.log.AndroidLogUtils.LogI;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.sevtinge.cemiuiler.utils.log.XposedLogUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModuleInterface;


/**
 * Helpers that simplify hooking and calling methods/constructors, getting and settings fields, ...
 */
public abstract class XposedHelpers extends HookerClassHelper {
    public static XposedInterface moduleInst;
    private static final String TAG = "LSPosed-Bridge";
    public static XposedModuleInterface.PackageLoadedParam lpparam;
    private static final HashMap<String, ThreadLocal<AtomicInteger>> sMethodDepth = new HashMap<>();

    public abstract void init();

    public void onCreate(XposedModuleInterface.PackageLoadedParam lpparam) {
        try {
            setLoadPackageParam(lpparam);
            init();
            // if (detailLog && isNotReleaseVersion) {
            XposedLogUtils.INSTANCE.logI(TAG, "Hook Success.");
            // }
        } catch (Throwable t) {
            XposedLogUtils.INSTANCE.logE(TAG, "Hook Failed", t, null);
        }
    }

    public void setLoadPackageParam(XposedModuleInterface.PackageLoadedParam param) {
        lpparam = param;
    }

    public static void log(String line) {
        Log.i(TAG, "[CustoMIUIzer] " + line);
    }

    public static void log(Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, "[CustoMIUIzer] " + logStr);
    }

    public static void log(String mod, String line) {
        Log.i(TAG, "[CustoMIUIzer][" + mod + "] " + line);
    }

    public static void log(String mod, Throwable t) {
        String logStr = Log.getStackTraceString(t);
        Log.e(TAG, "[CustoMIUIzer][" + mod + "] " + logStr);
    }

    public static Class<?> findClass(String className) {
        return findClass(className, lpparam.getClassLoader());
    }

    public static Class<?> findClassIfExists(String className) {
        return findClassIfExists(className, lpparam.getClassLoader());
    }

    public static Field findField(String clazz, String fieldName) {
        return findField(findClass(clazz), fieldName);
    }

    /**
     * Look up and return a field if it exists.
     * Like {@link #findField}, but doesn't throw an exception if the field doesn't exist.
     *
     * @param clazz     The class which either declares or inherits the field.
     * @param fieldName The field name.
     * @return A reference to the field, or {@code null} if it doesn't exist.
     */
    public static Field findFieldIfExists(String clazz, String fieldName) {
        try {
            return findField(clazz, fieldName);
        } catch (NoSuchFieldError e) {
            return null;
        }
    }

    public static CustomMethodUnhooker findAndHookMethod(String className, String methodName, Object... parameterTypesAndCallback) {
        return findAndHookMethod(findClass(className), methodName, parameterTypesAndCallback);
    }

    public static void hookMethod(Method method, MethodHook callback) {
        try {
            XposedHelpers.doHookMethod(method, callback);
        } catch (Throwable t) {
            LogI("hookMethod", "Failed to hook " + method.getName() + " method");
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean findAndHookMethodSilently(String className, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Method findMethodExact(String className, String methodName, Object... parameterTypes) {
        return findMethodExact(findClass(className), methodName, getParameterClasses(parameterTypes));
    }

    public static Method findMethodExactIfExists(String className, String methodName, Object... parameterTypes) {
        try {
            return findMethodExact(className, methodName, parameterTypes);
        } catch (ClassNotFoundError | NoSuchMethodError e) {
            return null;
        }
    }

    public static Set<CustomMethodUnhooker> hookAllConstructors(String className, MethodHook callback) {
        return hookAllConstructors(findClass(className), callback);
    }

    public static Set<CustomMethodUnhooker> hookAllMethods(String className, String methodName, MethodHook callback) {
        return hookAllMethods(findClass(className), methodName, callback);
    }

    private static Class<?>[] getParameterClasses(Object[] parameterTypesAndCallback) {
        return getParameterClasses(parameterTypesAndCallback, lpparam.getClassLoader());
    }

    public static Constructor<?> findConstructorExact(String className, Object... parameterTypes) {
        return findConstructorExact(findClass(className), getParameterClasses(parameterTypes));
    }

    public static Constructor<?> findConstructorExactIfExists(String className, Object... parameterTypes) {
        try {
            return findConstructorExact(className, parameterTypes);
        } catch (ClassNotFoundError | NoSuchMethodError e) {
            return null;
        }
    }

    public static CustomMethodUnhooker findAndHookConstructor(String clazz, Object... parameterTypesAndCallback) {
        return findAndHookConstructor(findClass(clazz), lpparam.getClassLoader(), parameterTypesAndCallback);
    }

    //#################################################################################################

    /**
     * Calls an instance or static method of the given object.
     * The method is resolved using {@link #findMethodBestMatch(Class, String, Object...)}.
     *
     * @param obj        The object instance. A class reference is not sufficient!
     * @param methodName The method name.
     * @param args       The arguments for the method call.
     * @throws NoSuchMethodError     In case no suitable method was found.
     * @throws InvocationTargetError In case an exception was thrown by the invoked method.
     */
    public static Object callMethod(Object obj, String methodName, Object... args) {
        try {
            return findMethodBestMatch(obj.getClass(), methodName, args).invoke(obj, args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        }
    }

    /**
     * Calls an instance or static method of the given object.
     * See {@link #callMethod(Object, String, Object...)}.
     *
     * <p>This variant allows you to specify parameter types, which can help in case there are multiple
     * methods with the same name, especially if you call it with {@code null} parameters.
     */
    public static Object callMethod(Object obj, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return findMethodBestMatch(obj.getClass(), methodName, parameterTypes, args).invoke(obj, args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        }
    }

    /**
     * Calls a static method of the given class.
     * The method is resolved using {@link #findMethodBestMatch(Class, String, Object...)}.
     *
     * @param clazz      The class reference.
     * @param methodName The method name.
     * @param args       The arguments for the method call.
     * @throws NoSuchMethodError     In case no suitable method was found.
     * @throws InvocationTargetError In case an exception was thrown by the invoked method.
     */
    public static Object callStaticMethod(Class<?> clazz, String methodName, Object... args) {
        try {
            return findMethodBestMatch(clazz, methodName, args).invoke(null, args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        }
    }

    /**
     * Calls a static method of the given the name of class.
     * See {@link #callStaticMethod(Class, String, Object...)}.
     */
    public static Object callStaticMethod(String clazz, String methodName, Object... args) {
        return callStaticMethod(findClass(clazz), methodName, args);
    }

    /**
     * Calls a static method of the given class.
     * See {@link #callStaticMethod(Class, String, Object...)}.
     *
     * <p>This variant allows you to specify parameter types, which can help in case there are multiple
     * methods with the same name, especially if you call it with {@code null} parameters.
     */
    public static Object callStaticMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        try {
            return findMethodBestMatch(clazz, methodName, parameterTypes, args).invoke(null, args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        }
    }

    /**
     * Calls a static method of the given the name of class.
     * See {@link #callStaticMethod(Class, String, Object...)}.
     */
    public static Object callStaticMethod(String clazz, String methodName, Class<?>[] parameterTypes, Object... args) {
        return callStaticMethod(findClass(clazz), methodName, parameterTypes, args);
    }

    /**
     * This class provides a wrapper for an exception thrown by a method invocation.
     *
     * @see #callMethod(Object, String, Object...)
     * @see #callStaticMethod(Class, String, Object...)
     * @see #newInstance(Class, Object...)
     */
    public static final class InvocationTargetError extends Error {
        @Serial
        private static final long serialVersionUID = -1070936889459514628L;

        /**
         * @hide
         */
        public InvocationTargetError(Throwable cause) {
            super(cause);
        }
    }

    //#################################################################################################

    /**
     * Creates a new instance of the given class.
     * The constructor is resolved using {@link #findConstructorBestMatch(Class, Object...)}.
     *
     * @param clazz The class reference.
     * @param args  The arguments for the constructor call.
     * @throws NoSuchMethodError     In case no suitable constructor was found.
     * @throws InvocationTargetError In case an exception was thrown by the invoked method.
     * @throws InstantiationError    In case the class cannot be instantiated.
     */
    public static Object newInstance(Class<?> clazz, Object... args) {
        try {
            return findConstructorBestMatch(clazz, args).newInstance(args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        }
    }

    /**
     * Creates a new instance of the given the name of class.
     * See {@link #newInstance(Class, Object...)}.
     */
    public static Object newInstance(String clazz, Object... args) {
        return newInstance(findClass(clazz), args);
    }

    /**
     * Creates a new instance of the given class.
     * See {@link #newInstance(Class, Object...)}.
     *
     * <p>This variant allows you to specify parameter types, which can help in case there are multiple
     * constructors with the same name, especially if you call it with {@code null} parameters.
     */
    public static Object newInstance(Class<?> clazz, Class<?>[] parameterTypes, Object... args) {
        try {
            return findConstructorBestMatch(clazz, parameterTypes, args).newInstance(args);
        } catch (IllegalAccessException e) {
            // should not happen
            XposedHelpers.log(e);
            throw new IllegalAccessError(e.getMessage());
        } catch (InvocationTargetException e) {
            throw new InvocationTargetError(e.getCause());
        } catch (InstantiationException e) {
            throw new InstantiationError(e.getMessage());
        }
    }

    /**
     * Creates a new instance of the given the name of class.
     * See {@link #newInstance(Class, Object...)}.
     */
    public static Object newInstance(String clazz, Class<?>[] parameterTypes, Object... args) {
        return newInstance(findClass(clazz), parameterTypes, args);
    }

    //#################################################################################################

    /**
     * Loads an asset from a resource object and returns the content as {@code byte} array.
     *
     * @param res  The resources from which the asset should be loaded.
     * @param path The path to the asset, as in {@link AssetManager#open}.
     * @return The content of the asset.
     */
    public static byte[] assetAsByteArray(Resources res, String path) throws IOException {
        return inputStreamToByteArray(res.getAssets().open(path));
    }

    /*package*/
    static byte[] inputStreamToByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        byte[] temp = new byte[1024];
        int read;

        while ((read = is.read(temp)) > 0) {
            buf.write(temp, 0, read);
        }
        is.close();
        return buf.toByteArray();
    }

    //#################################################################################################

    /**
     * Increments the depth counter for the given method.
     *
     * <p>The intention of the method depth counter is to keep track of the call depth for recursive
     * methods, e.g. to override parameters only for the outer call. The Xposed framework uses this
     * to load drawable replacements only once per call, even when multiple
     * {@link Resources#getDrawable} variants call each other.
     *
     * @param method The method name. Should be prefixed with a unique, module-specific string.
     * @return The updated depth.
     */
    public static int incrementMethodDepth(String method) {
        return getMethodDepthCounter(method).get().incrementAndGet();
    }

    /**
     * Decrements the depth counter for the given method.
     * See {@link #incrementMethodDepth} for details.
     *
     * @param method The method name. Should be prefixed with a unique, module-specific string.
     * @return The updated depth.
     */
    public static int decrementMethodDepth(String method) {
        return getMethodDepthCounter(method).get().decrementAndGet();
    }

    /**
     * Returns the current depth counter for the given method.
     * See {@link #incrementMethodDepth} for details.
     *
     * @param method The method name. Should be prefixed with a unique, module-specific string.
     * @return The updated depth.
     */
    public static int getMethodDepth(String method) {
        return getMethodDepthCounter(method).get().get();
    }

    private static ThreadLocal<AtomicInteger> getMethodDepthCounter(String method) {
        synchronized (sMethodDepth) {
            ThreadLocal<AtomicInteger> counter = sMethodDepth.get(method);
            if (counter == null) {
                counter = new ThreadLocal<AtomicInteger>() {
                    @Override
                    protected AtomicInteger initialValue() {
                        return new AtomicInteger();
                    }
                };
                sMethodDepth.put(method, counter);
            }
            return counter;
        }
    }

}
