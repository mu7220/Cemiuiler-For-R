package com.sevtinge.cemiuiler.module.hook.systemsettings;

import com.sevtinge.cemiuiler.module.base.BaseHook;

public class AppsFreezerEnable extends BaseHook {
    @Override
    public void init() {
        findAndHookMethod("com.android.settings.development.CachedAppsFreezerPreferenceController",
            "isAvailable",
            new MethodHook() {
                @Override
                protected void after(MethodHookParam param) throws Throwable {
                    super.after(param);
                    param.setResult(true);
                }
            }
        );
    }
}
