package com.sevtinge.cemiuiler.utils.log

import com.sevtinge.cemiuiler.BuildConfig
import com.sevtinge.cemiuiler.module.base.BaseHook
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers

object XposedLogUtils {
    private val isDebugVersion = BuildConfig.BUILD_TYPE.contains("debug")
    private val isNotReleaseVersion = !BuildConfig.BUILD_TYPE.contains("release")
    private val detailLog = BaseHook.mPrefsMap.getBoolean("settings_disable_detailed_log")

    // Xposed debug 日志输出
    fun logI(tag: String, msg: String) {
        if (!isDebugVersion) return
        if (detailLog) return
        XposedHelpers.log("[Cemiuiler][I][$tag]: $msg")
    }

    fun logI(msg: String) {
        if (!isDebugVersion) return
        if (detailLog) return
        XposedHelpers.log("[Cemiuiler][I]: $msg")
    }

    fun logW(tag: String, log: Throwable) {
        if (detailLog) return
        XposedHelpers.log("[Cemiuiler][W][$tag]: $log")
    }

    fun logW(tag: String, msg: String, log: Throwable) {
        if (detailLog) return
        XposedHelpers.log("[Cemiuiler][W][$tag]: $msg, warning by $log")
    }

    fun logE(tag: String, log: Throwable?, exp: Exception?) {
        val logMessage = "[Cemiuiler][E][$tag]: " +
            when {
                log != null -> ", hook failed by $log"
                exp != null -> ", hook failed by $exp"
                else -> ""
            }
        XposedHelpers.log(logMessage)
    }

    fun logE(tag: String, msg: String, log: Throwable?, exp: Exception?) {
        val logMessage = "[Cemiuiler][E][$tag]: $msg" +
            when {
                log != null -> ", hook failed by $log"
                exp != null -> ", hook failed by $exp"
                else -> ""
            }
        XposedHelpers.log(logMessage)
    }

    fun logD(tag: String, msg: String) {
        if (!isDebugVersion) return
        XposedHelpers.log("[Cemiuiler][D][$tag]: $msg")
    }

    fun logD(msg: String) {
        if (!isDebugVersion) return
        XposedHelpers.log("[Cemiuiler][D]: $msg")
    }

}
