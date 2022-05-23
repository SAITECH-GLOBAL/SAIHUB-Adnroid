package com.linktech.saihub.util.system;

import android.app.Activity;

import java.util.Stack;

public class ActivitiesStack {

    private Stack<Activity> mStack;
    private static ActivitiesStack activitiesStack;

    private ActivitiesStack() {
        mStack = new Stack<>();
    }

    public static ActivitiesStack getInstance() {
        if (activitiesStack == null) {
            synchronized (ActivitiesStack.class) {
                if (activitiesStack == null) {
                    activitiesStack = new ActivitiesStack();
                }
            }
        }
        return activitiesStack;
    }

    public void push(Activity activity) {
        if (activity == null) {

        }
        mStack.push(activity);
    }

    /**
     * 所有activity出栈
     *
     * @param isExit - 是否是退出操作
     */
    public void popAll(boolean isExit) {
        while (!mStack.isEmpty()) {
            Activity activity = mStack.pop();
            if (isExit && activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
    }

    public void pop(Activity activity) {
        if (activity != null) {
            mStack.remove(activity);
        }
    }

    /**
     * 获取栈顶（当前）Activity
     *
     * @return
     */
    public Activity getCurrentActivity() {
        Activity activity = mStack.peek();
        if (activity == null) {
        }
        return activity;
    }
}