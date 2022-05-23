package com.linktech.saihub.util.system;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by tromo on 2021/12/7.
 */
public class AppFrontBackHelper {
    private OnAppStatusListener mOnAppStatusListener;

    public AppFrontBackHelper() {

    }

    /**
     * 注册状态监听，仅在Application中使用
     *
     * @param application 全局application
     * @param listener    监听器
     */
    public void register(Application application, OnAppStatusListener listener) {
        mOnAppStatusListener = listener;
        application.registerActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    public void unRegister(Application application) {
        application.unregisterActivityLifecycleCallbacks(activityLifecycleCallbacks);
    }

    private Application.ActivityLifecycleCallbacks activityLifecycleCallbacks = new Application.ActivityLifecycleCallbacks() {
        //打开的Activity数量统计
        private int activityStartCount = 0;

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }
        @Override
        public void onActivityStarted(Activity activity) {
            activityStartCount++;
            //数值从0 变到 1 说明是从后台切到前台
            if (activityStartCount == 1) {
                //从后台切到前台
                if (mOnAppStatusListener != null) {
                    mOnAppStatusListener.onFront();
                }
            }
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityStartCount--;
            //数值从1到0说明是从前台切到后台
            if (activityStartCount == 0) {
                //从前台切到后台
                if (mOnAppStatusListener != null) {
                    mOnAppStatusListener.onBack();
                }
            }
        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }
        @Override
        public void onActivityDestroyed(Activity activity) {
        }
    };

    public interface OnAppStatusListener {
        /**
         * 前台运行
         */
        void onFront();
        /**
         * 后台运行
         */
        void onBack();
    }
}
