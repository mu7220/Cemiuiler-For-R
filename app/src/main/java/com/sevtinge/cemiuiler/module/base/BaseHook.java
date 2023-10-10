package com.sevtinge.cemiuiler.module.base;

import static com.sevtinge.cemiuiler.utils.log.AndroidLogUtils.LogD;

import com.sevtinge.cemiuiler.BuildConfig;
import com.sevtinge.cemiuiler.XposedInit;
import com.sevtinge.cemiuiler.utils.PrefsMap;
import com.sevtinge.cemiuiler.utils.ResourcesHook;
import com.sevtinge.cemiuiler.utils.hook.HookerClassHelper.MethodHook;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;
import com.sevtinge.cemiuiler.utils.log.XposedLogUtils;

import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;

public abstract class BaseHook {
    public String TAG = getClass().getSimpleName();
    private static final boolean isDebugVersion = BuildConfig.BUILD_TYPE.contains("debug");
    private static final boolean isNotReleaseVersion = !BuildConfig.BUILD_TYPE.contains("release");
    private final boolean detailLog = !mPrefsMap.getBoolean("settings_disable_detailed_log");

    public PackageLoadedParam lpparam;
    public static ResourcesHook mResHook = XposedInit.mResHook;
    public static final PrefsMap<String, Object> mPrefsMap = XposedInit.mPrefsMap;

    public static final String ACTION_PREFIX = "com.sevtinge.cemiuiler.module.action.";

    public abstract void init();

    public void onCreate(PackageLoadedParam lpparam) {
        try {
            setLoadPackageParam(lpparam);
            init();
            if (detailLog && isNotReleaseVersion) {
                XposedLogUtils.INSTANCE.logI(TAG, "Hook Success.");
            }
        } catch (Throwable t) {
            XposedLogUtils.INSTANCE.logE(TAG, "Hook Failed", t, null);
        }
    }

    public void setLoadPackageParam(PackageLoadedParam param) {
        lpparam = param;
    }

    public void logI(String log) {
        if (detailLog && isNotReleaseVersion) {
            XposedHelpers.log("[Cemiuiler][I][" + TAG + "]: " + log);
        }
    }

    public void logE(Exception e) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: hook failed by " + e);
    }

    public void logE(Throwable t) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: hook failed by " + t);
    }

    public void logE(String log) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: hook failed by " + log);
    }

    public void logE(String tag, Exception e) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: " + tag + " hook failed by " + e);
    }

    public void logE(String tag, Throwable t) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: " + tag + " hook failed by " + t);
    }

    public void logE(String tag, String log) {
        XposedHelpers.log("[Cemiuiler][E][" + TAG + "]: " + tag + " hook failed by " + log);
    }

    public Class<?> findClass(String className) {
        return findClass(className, lpparam.getClassLoader());
    }

    public Class<?> findClass(String className, ClassLoader classLoader) {
        return XposedHelpers.findClass(className, classLoader);
    }

    public Class<?> findClassIfExists(String className) {
        try {
            return findClass(className);
        } catch (XposedHelpers.ClassNotFoundError e) {
            LogD("findClassIfExists", "find " + className + " is Null", e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String newClassName, String oldClassName) {
        try {
            return findClass(findClassIfExists(newClassName) != null ? newClassName : oldClassName);
        } catch (XposedHelpers.ClassNotFoundError e) {
            LogD("findClassIfExists", "find " + newClassName + " and " + oldClassName + " is Null", e);
            return null;
        }
    }

    public Class<?> findClassIfExists(String className, ClassLoader classLoader) {
        try {
            return findClass(className, classLoader);
        } catch (XposedHelpers.ClassNotFoundError e) {
            LogD("findClassIfExists", "find " + className + " is Null", e);
            return null;
        }
    }

    public void findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
    }

    public void findAndHookMethod(String className, String methodName, Object... parameterTypesAndCallback) {
        findAndHookMethod(findClassIfExists(className), methodName, parameterTypesAndCallback);
    }

    public boolean findAndHookMethodSilently(String className, String methodName, Object... parameterTypesAndCallback) {
        try {
            findAndHookMethod(className, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            LogD("findAndHookMethodSilently", className + methodName + " is null", t);
            return false;
        }
    }

    public boolean findAndHookMethodSilently(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            LogD("findAndHookMethodSilently", clazz + methodName + " is null", t);
            return false;
        }
    }

    public void findAndHookConstructor(String className, Object... parameterTypesAndCallback) {
        findAndHookConstructor(findClassIfExists(className), parameterTypesAndCallback);
    }

    public void findAndHookConstructor(Class<?> hookClass, Object... parameterTypesAndCallback) {
        XposedHelpers.findAndHookConstructor(hookClass, parameterTypesAndCallback);
    }

    public void hookMethod(Method method, MethodHook callback) {
        XposedHelpers.doHookMethod(method, callback);
    }

    public void hookAllMethods(String className, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            if (hookClass != null) {
                XposedHelpers.hookAllMethods(hookClass, methodName, callback).size();
            }

        } catch (Throwable t) {
            LogD("HookAllMethods", className + " is " + methodName + " abnormal", t);
        }
    }

    public void hookAllMethods(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            XposedHelpers.hookAllMethods(hookClass, methodName, callback).size();
        } catch (Throwable t) {
            LogD("HookAllMethods", hookClass + " is " + methodName + " abnormal", t);
        }
    }

    public void hookAllMethodsSilently(String className, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            if (hookClass != null) {
                XposedHelpers.hookAllMethods(hookClass, methodName, callback).size();
            }
        } catch (Throwable ignored) {
        }
    }

    public boolean hookAllMethodsSilently(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            if (hookClass != null) {
                XposedHelpers.hookAllMethods(hookClass, methodName, callback).size();
            }
            return false;
        } catch (Throwable t) {
            return false;
        }
    }

    public void hookAllConstructors(String className, MethodHook callback) {
        try {
            Class<?> hookClass = findClassIfExists(className);
            if (hookClass != null) {
                XposedHelpers.hookAllConstructors(hookClass, callback).size();
            }
        } catch (Throwable t) {
            LogD("hookAllConstructors", className + " is  abnormal", t);
        }
    }

    public void hookAllConstructors(Class<?> hookClass, MethodHook callback) {
        try {
            XposedHelpers.hookAllConstructors(hookClass, callback).size();
        } catch (Throwable t) {
            LogD("hookAllConstructors", hookClass + " is  abnormal", t);
        }
    }


    public Object getStaticObjectFieldSilently(Class<?> clazz, String fieldName) {
        try {
            return XposedHelpers.getStaticObjectField(clazz, fieldName);
        } catch (Throwable t) {
            return null;
        }
    }
}
