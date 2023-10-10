package com.sevtinge.cemiuiler.module.base;

import com.sevtinge.cemiuiler.BuildConfig;
import com.sevtinge.cemiuiler.XposedInit;
import com.sevtinge.cemiuiler.utils.PrefsMap;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;

public class BaseHook extends XposedHelpers {
    public String TAG = getClass().getSimpleName();
    private static final boolean isDebugVersion = BuildConfig.BUILD_TYPE.contains("debug");
    private static final boolean isNotReleaseVersion = !BuildConfig.BUILD_TYPE.contains("release");
    private final boolean detailLog = !mPrefsMap.getBoolean("settings_disable_detailed_log");
    public static ResourcesHook mResHook = XposedInit.mResHook;
    public static final PrefsMap<String, Object> mPrefsMap = XposedInit.mPrefsMap;

    public static final String ACTION_PREFIX = "com.sevtinge.cemiuiler.module.action.";

    @Override
    public void init() {
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
}
