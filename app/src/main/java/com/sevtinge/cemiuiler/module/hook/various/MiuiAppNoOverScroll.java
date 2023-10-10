package com.sevtinge.cemiuiler.module.hook.various;

import android.view.View;

import com.sevtinge.cemiuiler.module.base.BaseHook;
import com.sevtinge.cemiuiler.utils.hook.HookerClassHelper.MethodHook;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;
import com.sevtinge.cemiuiler.utils.log.XposedLogUtils;

import io.github.libxposed.api.XposedInterface.BeforeHookCallback;

public class MiuiAppNoOverScroll extends BaseHook {


    @Override
    public void init() {

        Class<?> mSpringBackCls = findClassIfExists("miuix.springback.view.SpringBackLayout");
        Class<?> mRemixRvCls = findClassIfExists("androidx.recyclerview.widget.RemixRecyclerView");

        try {
            MethodHook hookParam = new MethodHook() {
                @Override
                protected void before(BeforeHookCallback param) {
                    XposedHelpers.setBooleanField(param.getThisObject(), "mSpringBackEnable", false);
                    param.getArgs()[0] = false;
                }
            };

            if (mSpringBackCls != null) {

                hookAllConstructors(mSpringBackCls, new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) {
                        XposedHelpers.setBooleanField(param.thisObject, "mSpringBackEnable", false);
                    }
                });

                findAndHookMethodSilently(mSpringBackCls, "setSpringBackEnable", boolean.class, hookParam);
            }


            if (mRemixRvCls != null) {
                hookAllConstructors(mRemixRvCls, new MethodHook() {
                    @Override
                    protected void after(MethodHookParam param) {
                        ((View) param.thisObject).setOverScrollMode(View.OVER_SCROLL_NEVER);
                        XposedHelpers.setBooleanField(param.thisObject, "mSpringBackEnable", false);
                    }
                });
                findAndHookMethodSilently(mRemixRvCls, "setSpringEnabled", boolean.class, hookParam);
            }
        } catch (Exception e) {
            XposedLogUtils.INSTANCE.logE(TAG,"TAG" + lpparam.packageName, null, e);
        }
    }
}
