package com.sevtinge.cemiuiler.utils.hook;

import static com.sevtinge.cemiuiler.utils.hook.XposedHelpers.log;
import static com.sevtinge.cemiuiler.utils.log.AndroidLogUtils.LogI;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.Settings;
import android.text.TextUtils;
import android.text.format.DateFormat;

import androidx.annotation.Nullable;

import com.sevtinge.cemiuiler.utils.Helpers;
import com.sevtinge.cemiuiler.utils.hook.HookerClassHelper.CustomMethodUnhooker;
import com.sevtinge.cemiuiler.utils.hook.HookerClassHelper.MethodHook;

import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.TimeZone;

import io.github.libxposed.api.XposedModuleInterface;

public class ModuleHelper {
    public static final String NOT_EXIST_SYMBOL = "ObjectFieldNotExist";

    public static final String prefsName = "cemiuiler_prefs";

    @SuppressLint("StaticFieldLeak")
    public static Context mModuleContext = null;

    static HashSet<PreferenceObserver> prefObservers = new HashSet<>();

    public static void printCallStack() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (StackTraceElement el: stackTrace)
            if (el != null) {
                LogI("printCallStack", el.getClassName() + " $$ " + el.getMethodName());
            }
    }

    public static void hookMethod(Method method, MethodHook callback) {
        try {
            XposedHelpers.doHookMethod(method, callback);
        } catch (Throwable t) {
            LogI("hookMethod", "Failed to hook " + method.getName() + " method");
        }
    }

    public static CustomMethodUnhooker findAndHookMethod(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            LogI("findAndHookMethod", "Failed to hook " + methodName + " method in " + className);
            return null;
        }
    }

    public static CustomMethodUnhooker findAndHookMethod(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            return XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
        } catch (Throwable t) {
            LogI("findAndHookMethod", "Failed to hook " + methodName + " method in " + clazz.getCanonicalName());
            return null;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean findAndHookMethodSilently(String className, ClassLoader classLoader, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(className, classLoader, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public static boolean findAndHookMethodSilently(Class<?> clazz, String methodName, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookMethod(clazz, methodName, parameterTypesAndCallback);
            return true;
        } catch (Throwable t) {
            return false;
        }
    }

    public static void findAndHookConstructor(String className, ClassLoader classLoader, Object... parameterTypesAndCallback) {
        try {
            XposedHelpers.findAndHookConstructor(className, classLoader, parameterTypesAndCallback);
        } catch (Throwable t) {
            LogI("hookMethod", "Failed to hook constructor in " + className);
        }
    }

    public static void hookAllConstructors(String className, ClassLoader classLoader, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            if (hookClass == null || XposedHelpers.hookAllConstructors(hookClass, callback).size() == 0)
                LogI("hookAllConstructors", "Failed to hook " + className + " constructor");
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllConstructors(Class<?> hookClass, MethodHook callback) {
        try {
            if (XposedHelpers.hookAllConstructors(hookClass, callback).size() == 0)
                LogI("hookAllConstructors", "Failed to hook " + hookClass.getCanonicalName() + " constructor");
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllMethods(String className, ClassLoader classLoader, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            if (hookClass == null || XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() == 0)
                LogI("hookAllMethods", "Failed to hook " + methodName + " method in " + className);
        } catch (Throwable t) {
            log(t);
        }
    }

    public static void hookAllMethods(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            if (XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() == 0)
                LogI("hookAllMethods", "Failed to hook " + methodName + " method in " + hookClass.getCanonicalName());
        } catch (Throwable t) {
            log(t);
        }
    }

    public static Object proxySystemProperties(String method, String prop, String val, ClassLoader classLoader) {
        return XposedHelpers.callStaticMethod(XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader),
            method, prop, val);
    }

    public static Object proxySystemProperties(String method, String prop, int val, ClassLoader classLoader) {
        return XposedHelpers.callStaticMethod(XposedHelpers.findClassIfExists("android.os.SystemProperties", classLoader),
            method, prop, val);
    }

    public static boolean hookAllMethodsSilently(String className, ClassLoader classLoader, String methodName, MethodHook callback) {
        try {
            Class<?> hookClass = XposedHelpers.findClassIfExists(className, classLoader);
            return hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static boolean hookAllMethodsSilently(Class<?> hookClass, String methodName, MethodHook callback) {
        try {
            return hookClass != null && XposedHelpers.hookAllMethods(hookClass, methodName, callback).size() > 0;
        } catch (Throwable t) {
            return false;
        }
    }

    public static Object getStaticObjectFieldSilently(Class <?> clazz, String fieldName) {
        try {
            return XposedHelpers.getStaticObjectField(clazz, fieldName);
        } catch (Throwable t) {
            return NOT_EXIST_SYMBOL;
        }
    }

    public static Object getObjectFieldSilently(Object obj, String fieldName) {
        try {
            return XposedHelpers.getObjectField(obj, fieldName);
        } catch (Throwable t) {
            return NOT_EXIST_SYMBOL;
        }
    }

    public static Context findContext() {
        Context context = null;
        try {
            context = (Application)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
                if (currentActivityThread != null) context = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
            }
        } catch (Throwable ignore) {}
        return context;
    }

    public static Context findContext(XposedModuleInterface.PackageLoadedParam lpparam) {
        Context context = null;
        try {
            context = (Application)XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", lpparam.getClassLoader()), "currentApplication");
            if (context == null) {
                Object currentActivityThread = XposedHelpers.callStaticMethod(XposedHelpers.findClass("android.app.ActivityThread", null), "currentActivityThread");
                if (currentActivityThread != null) context = (Context)XposedHelpers.callMethod(currentActivityThread, "getSystemContext");
            }
        } catch (Throwable ignore) {}
        return context;
    }

    public static String stringifyBundle(Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        String string = "Bundle{";
        for (String key : bundle.keySet()) {
            string = string + " " + key + " -> " + bundle.get(key) + ";";
        }
        string += " }Bundle";
        return string;
    }

    public static long getNextMIUIAlarmTime(Context context) {
        String nextAlarm = Settings.System.getString(context.getContentResolver(), "next_alarm_clock_formatted");
        long nextTime = 0;
        if (!TextUtils.isEmpty(nextAlarm)) try {
            TimeZone timeZone = TimeZone.getTimeZone("UTC");
            SimpleDateFormat dateFormat = new SimpleDateFormat(DateFormat.getBestDateTimePattern(Locale.getDefault(), DateFormat.is24HourFormat(context) ? "EHm" : "Ehma"), Locale.getDefault());
            dateFormat.setTimeZone(timeZone);
            long nextTimePart = dateFormat.parse(nextAlarm).getTime();

            Calendar cal = Calendar.getInstance(timeZone);
            cal.setFirstDayOfWeek(Calendar.MONDAY);
            cal.setTimeInMillis(nextTimePart);
            int targetDay = cal.get(Calendar.DAY_OF_WEEK);
            int targetHour = cal.get(Calendar.HOUR_OF_DAY);
            int targetMinute = cal.get(Calendar.MINUTE);

            cal = Calendar.getInstance();
            int diff = targetDay - cal.get(Calendar.DAY_OF_WEEK);
            if (diff < 0) diff += 7;

            cal.add(Calendar.DAY_OF_MONTH, diff);
            cal.set(Calendar.HOUR_OF_DAY, targetHour);
            cal.set(Calendar.MINUTE, targetMinute);
            cal.clear(Calendar.SECOND);
            cal.clear(Calendar.MILLISECOND);

            nextTime = cal.getTimeInMillis();
        } catch (Throwable t) {
            log(t);
        }
        return nextTime;
    }
    public static void openAppInfo(Context context, String pkg, int user) {
        try {
            Intent intent = new Intent("miui.intent.action.APP_MANAGER_APPLICATION_DETAIL");
            intent.setPackage("com.miui.securitycenter");
            intent.putExtra("package_name", pkg);
            if (user != 0) intent.putExtra("miui.intent.extra.USER_ID", user);
            context.startActivity(intent);
        } catch (Throwable t) {
            try {
                Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                intent.setData(Uri.parse("package:" + pkg));
                if (user != 0)
                    XposedHelpers.callMethod(context, "startActivityAsUser", intent, XposedHelpers.newInstance(UserHandle.class, user));
                else
                    context.startActivity(intent);
            } catch (Throwable t2) {
                log(t2);
            }
        }
    }

    public interface PreferenceObserver {
        void onChange(String key);
    }

    public static void observePreferenceChange(PreferenceObserver prefObserver) {
        prefObservers.add(prefObserver);
    }

    public static void handlePreferenceChanged(@Nullable String key) {
        for (PreferenceObserver prefObserver:prefObservers) {
            prefObserver.onChange(key);
        }
    }

    public static synchronized Context getModuleContext(Context context) throws Throwable {
        return getModuleContext(context, null);
    }

    public static synchronized Context getModuleContext(Context context, Configuration config) throws Throwable {
        if (mModuleContext == null) {
            mModuleContext = context.createPackageContext(Helpers.mAppModulePkg, Context.CONTEXT_IGNORE_SECURITY).createDeviceProtectedStorageContext();
        }
        return config == null ? mModuleContext : mModuleContext.createConfigurationContext(config);
    }

    public static synchronized Resources getModuleRes(Context context) throws Throwable {
        Configuration config = context.getResources().getConfiguration();
        Context moduleContext = getModuleContext(context);
        return (config == null ? moduleContext.getResources() : moduleContext.createConfigurationContext(config).getResources());
    }

}
