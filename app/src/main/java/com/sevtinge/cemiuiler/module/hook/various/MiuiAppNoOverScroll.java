package com.sevtinge.cemiuiler.module.hook.various;

import android.view.View;

import com.sevtinge.cemiuiler.module.base.BaseHook;
import com.sevtinge.cemiuiler.utils.hook.XposedHelpers;
import com.sevtinge.cemiuiler.utils.log.XposedLogUtils;

import io.github.libxposed.api.XposedInterface.AfterHookCallback;
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
                XposedHelpers.hookAllConstructors(mSpringBackCls, new MethodHook() {
                    @Override
                    protected void after(AfterHookCallback param) {
                        XposedHelpers.setBooleanField(param.getThisObject(), "mSpringBackEnable", false);
                    }
                });

                findAndHookMethodSilently(mSpringBackCls, "setSpringBackEnable", boolean.class, hookParam);
            }


            if (mRemixRvCls != null) {
                XposedHelpers.hookAllConstructors(mRemixRvCls, new MethodHook() {
                    @Override
                    protected void after(AfterHookCallback param) {
                        ((View) param.getThisObject()).setOverScrollMode(View.OVER_SCROLL_NEVER);
                        XposedHelpers.setBooleanField(param.getThisObject(), "mSpringBackEnable", false);
                    }
                });
                findAndHookMethodSilently(mRemixRvCls, "setSpringEnabled", boolean.class, hookParam);
            }
        } catch (Exception e) {
            XposedLogUtils.INSTANCE.logE(TAG,"TAG" + lpparam.getPackageName(), null, e);
        }
    }
}
