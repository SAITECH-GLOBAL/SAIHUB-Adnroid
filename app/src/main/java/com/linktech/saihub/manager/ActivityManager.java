package com.linktech.saihub.manager;

import android.app.Activity;

import com.linktech.saihub.ui.activity.MainActivity;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;


public class ActivityManager {
    private static ActivityManager sInstance = new ActivityManager();
    private WeakReference<Activity> sCurrentActivityWeakRef;
    private List<Activity> activityList = new LinkedList<Activity>();

    private ActivityManager() {
    }

    public synchronized static ActivityManager getInstance() {
        return sInstance;
    }

    public Activity getCurrentActivity() {
        Activity currentActivity = null;
        if (sCurrentActivityWeakRef != null) {
            currentActivity = sCurrentActivityWeakRef.get();
        }
        return currentActivity;
    }

    public void setCurrentActivity(Activity activity) {
        sCurrentActivityWeakRef = new WeakReference<>(activity);
    }

    // add Activity
    public void addActivity(Activity activity) {
        if (!activityList.contains(activity)) {
            activityList.add(activity);
        }
    }

    // remove Activity
    public void removeActivity(Activity activity) {
        if (activityList.contains(activity)) {
            activityList.remove(activity);
        }
    }

    public void exitToHome() {
        try {
            for (Activity activity : activityList) {
                if (activity != null) {
                    String className = activity.getClass().getSimpleName();
                    if (!className.equals(MainActivity.class.getSimpleName())) {
                        activity.finish();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    //关闭每一个list内的activity
    public void finishActivityList() {
        for (Activity activity : activityList) {
            activity.finish();
        }
    }

    //关闭每一个list内的activity
    public void finishActivityListNoCurrent(Activity currentActivity) {
        try {
            for (Activity activity : activityList) {
                if (activity != null) {
                    String className = activity.getClass().getSimpleName();
                    if (!className.equals(currentActivity.getClass().getSimpleName())) {
                        activity.finish();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    //关闭每一个list内的activity
    public void finishActivityListNoHomeCurrent(Activity currentActivity) {
        try {
            for (Activity activity : activityList) {
                if (activity != null) {
                    String className = activity.getClass().getSimpleName();
                    if (className.equals(currentActivity.getClass().getSimpleName())) {
                        continue;
                    }
                    if (className.equals(MainActivity.class.getSimpleName())) {
                        continue;
                    }
                    activity.finish();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }
    }

    public int getActivityListCount() {
        return activityList.size();
    }


}
