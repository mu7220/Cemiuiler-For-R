-verbose

# Xposed
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keepattributes RuntimeVisibleAnnotations
-keep,allowoptimization,allowobfuscation public class * extends io.github.libxposed.api.XposedModule {
    public <init>(...);
    public void onPackageLoaded(...);
    public void onSystemServerLoaded(...);
}

-keep,allowoptimization,allowobfuscation @io.github.libxposed.api.annotations.* class * {
    @io.github.libxposed.api.annotations.BeforeInvocation <methods>;
    @io.github.libxposed.api.annotations.AfterInvocation <methods>;
}

-keep class com.sevtinge.cemiuiler.XposedInit
-keep class com.sevtinge.cemiuiler.module.app.SystemFrameworkForCorePatch
-keep class moralnorm.**{*;}
-keep class com.sevtinge.cemiuiler.utils.Helpers{boolean isModuleActive;}
-keep class com.sevtinge.cemiuiler.utils.Helpers{int XposedVersion;}
-keep class * extends com.sevtinge.cemiuiler.module.base.BaseHook
-keep class com.sevtinge.cemiuiler.ui.HideAppActivity
-keep class * extends com.sevtinge.cemiuiler.ui.fragment.base.*
-keep class miui.drm.**{*;}
-dontwarn android.app.ActivityTaskManager$RootTaskInfo
-dontwarn miui.app.MiuiFreeFormManager$MiuiFreeFormStackInfo
-dontwarn com.android.internal.view.menu.MenuBuilder
-allowaccessmodification
-overloadaggressively
