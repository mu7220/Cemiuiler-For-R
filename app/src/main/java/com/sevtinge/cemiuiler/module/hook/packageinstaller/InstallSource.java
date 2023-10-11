package com.sevtinge.cemiuiler.module.hook.packageinstaller;

import com.sevtinge.cemiuiler.module.base.BaseHook;
import com.sevtinge.cemiuiler.utils.hook.HookerClassHelper;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;

public class InstallSource extends BaseHook {

    String mInstallSourcePackageName;

    @Override
    public void init() {

        mInstallSourcePackageName = mPrefsMap.getString("miui_package_installer_install_source", "com.android.fileexplorer");

        XposedHelpers.findAndHookMethodSilently("com.miui.packageInstaller.InstallStart",
            "getCallingPackage",
            HookerClassHelper.returnConstant(mInstallSourcePackageName));
    }
}
