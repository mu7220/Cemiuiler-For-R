package com.sevtinge.cemiuiler.module.base;

import com.sevtinge.cemiuiler.XposedInit;
import com.sevtinge.cemiuiler.utils.PrefsMap;

import io.github.libxposed.api.XposedModuleInterface.PackageLoadedParam;

public abstract class BaseModule implements IXposedHook {

    public PackageLoadedParam mLoadPackageParam = null;
    public final PrefsMap<String, Object> mPrefsMap = XposedInit.mPrefsMap;

    public void init(PackageLoadedParam lpparam) {
        mLoadPackageParam = lpparam;
        initZygote();
        handleLoadPackage();
    }

    @Override
    public void initZygote() {
    }

    public void initHook(BaseHook baseHook) {
        initHook(baseHook, true);
    }

    public void initHook(BaseHook baseHook, boolean isInit) {
        if (isInit) {
            baseHook.onCreate(mLoadPackageParam);
        }
    }
}
