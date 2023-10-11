package com.sevtinge.cemiuiler.module.hook.systemui;

import android.content.pm.ApplicationInfo;

import com.sevtinge.cemiuiler.R;
import com.sevtinge.cemiuiler.XposedInit;
import com.sevtinge.cemiuiler.module.base.SystemUIHook;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
import io.github.libxposed.api.XposedInterface.BeforeHookCallback;

public class NotificationVolumeSeparateSlider extends SystemUIHook {

    boolean isHooked = false;
    ClassLoader pluginLoader = null;

    Class<?> mMiuiVolumeDialogImpl;

    int notifVolumeOnResId;
    int notifVolumeOffResId;

    @Override
    public void init() {
        initRes();

        XposedHelpers.hookAllMethods(mPluginLoaderClass, "getClassLoader", new MethodHook() {
            @Override
            protected void after(AfterHookCallback param) {
                ApplicationInfo appInfo = (ApplicationInfo) param.getArgs()[0];
                if ("miui.systemui.plugin".equals(appInfo.packageName) && !isHooked) {
                    isHooked = true;
                    if (pluginLoader == null) pluginLoader = (ClassLoader) param.getResult();

                    mMiuiVolumeDialogImpl = findClassIfExists("com.android.systemui.miui.volume.MiuiVolumeDialogImpl", pluginLoader);
                    XposedHelpers.hookAllMethods(mMiuiVolumeDialogImpl, "addColumn", new MethodHook() {
                        @Override
                        protected void before(BeforeHookCallback param) {
                            if (param.getArgs().length != 4) return;
                            int streamType = (int) param.getArgs()[0];
                            if (streamType == 4) {
                                XposedHelpers.callMethod(param.getThisObject(), "addColumn", 5, notifVolumeOnResId, notifVolumeOffResId, true, false);
                            }
                        }
                    });
                }
            }
        });
    }

    public void initRes() {

        notifVolumeOnResId = XposedInit.mResHook.addResource("ic_miui_volume_notification", R.drawable.ic_miui_volume_notification);
        notifVolumeOffResId = XposedInit.mResHook.addResource("ic_miui_volume_notification_mute", R.drawable.ic_miui_volume_notification_mute);

        XposedInit.mResHook.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_content_width_expanded", R.dimen.miui_volume_content_width_expanded);
        XposedInit.mResHook.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_ringer_layout_width_expanded", R.dimen.miui_volume_ringer_layout_width_expanded);
        XposedInit.mResHook.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_width_expanded", R.dimen.miui_volume_column_width_expanded);
        XposedInit.mResHook.setResReplacement("miui.systemui.plugin", "dimen", "miui_volume_column_margin_horizontal_expanded", R.dimen.miui_volume_column_margin_horizontal_expanded);

    }
}
