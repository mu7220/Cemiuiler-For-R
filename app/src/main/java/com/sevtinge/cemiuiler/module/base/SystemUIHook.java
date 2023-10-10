package com.sevtinge.cemiuiler.module.base;


import static com.sevtinge.cemiuiler.utils.devicesdk.SystemSDKKt.isMoreAndroidVersion;

import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;

public abstract class SystemUIHook extends BaseHook {

    public Class<?> mPluginLoaderClass;
    public String mPluginLoaderClassName;

    @Override
    public void setLoadPackageParam(PackageLoadedParam param) {
        super.setLoadPackageParam(param);

        if (isMoreAndroidVersion(33)) {
            mPluginLoaderClassName = "com.android.systemui.shared.plugins.PluginInstance$Factory";
        } else {
            mPluginLoaderClassName = "com.android.systemui.shared.plugins.PluginManagerImpl";
        }

        mPluginLoaderClass = findClassIfExists(mPluginLoaderClassName);
    }
}
