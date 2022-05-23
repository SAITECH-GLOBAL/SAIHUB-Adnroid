package com.linktech.saihub.util.system;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

/**
 * Created by tromo on 2021/9/26.
 */
public class ScreenUtils {

    //获取是否存在NavigationBar
    public static boolean isShowNavBar(Activity context) {
        if (null == context) {
            return false;
        }

        Rect outRect1 = new Rect();
        try {
            ((Activity) context).getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect1);
        } catch (ClassCastException e) {
            e.printStackTrace();
            return false;
        }
        int activityHeight = outRect1.height();

        Log.e("zrj", "状态栏高度:" + getStatusBarHeight(context));
        Log.e("zrj", "应用区域高度:" + activityHeight);
        Log.e("zrj", "导航栏高度:" + getNavigationBarHeight(context));
        Log.e("zrj", "屏幕物理高度:" + getRealHeight(context));
        /*
         * 屏幕物理高度 - 状态栏高度  导航栏高度 = 导航栏存在时的应用实际高度
         */
        int remainHeight = getRealHeight(context) - getNavigationBarHeight(context) - getStatusBarHeight(context);

        Log.e("zrj", "remainHeight:" + remainHeight);
        /*
         * 剩余高度跟应用区域高度相等 说明导航栏存在
         */
        return activityHeight == remainHeight;
    }

    // 获取NavigationBar高度
    public static int getNavigationBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    // 获取状态栏高度
    public static int getStatusBarHeight(Context context) {
        Resources resources = context.getResources();
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        return resources.getDimensionPixelSize(resourceId);
    }

    // 获取屏幕物理高度高度
    public static int getRealHeight(Context context) {
        WindowManager wm = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE));
        Point point = new Point();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            wm.getDefaultDisplay().getRealSize(point);
        } else {
            wm.getDefaultDisplay().getSize(point);
        }
        return point.y;
    }

}