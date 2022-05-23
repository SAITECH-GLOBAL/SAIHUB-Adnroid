package com.linktech.saihub.util.system;

import android.app.Activity;
import android.view.View;

import com.linktech.saihub.util.Pub;

import java.text.DecimalFormat;

public class WalMainUtils {

    /**
     * 设置状态栏颜色
     *
     * @param activity
     * @param isWhite
     */
    public static void switchStatusBarTextColor(Activity activity, boolean isWhite) {
        if (isWhite) {
            activity.getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        } else {
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        activity.getWindow().getDecorView().findViewById(android.R.id.content).setPadding(0, 0, 0, 0);
    }

//    public static void saveReadNotificatonId(String id) {
//        String saveIds = MMKVManager.getInstance().mmkv().decodeString(Constants.READ_NOTIFICATION_ID, "");
//        if (!saveIds.contains("," + id)) {
//            MMKVManager.getInstance().mmkv().encode(Constants.READ_NOTIFICATION_ID, saveIds + "," + id);
//        }
//    }
//
//
//    public static void saveReadTransferNotificatonId(String id) {
//        String saveIds = MMKVManager.getInstance().mmkv().decodeString(Constants.READ_TRANSFER_NOTIFICATION_ID, "");
//        if (!saveIds.contains("," + id)) {
//            MMKVManager.getInstance().mmkv().encode(Constants.READ_TRANSFER_NOTIFICATION_ID, saveIds + "," + id);
//        }
//    }
//
//    public static void saveAvoidPassword(long walletId, boolean isAvoid) {
//        MMKVManager.getInstance().mmkv().encode(walletId + Constants.AVOID_PASSWORD, isAvoid);
//    }
//
//    public static boolean getAvoidPassword(long walletId) {
//        boolean avoid_password = MMKVManager.getInstance().mmkv().decodeBool(walletId + Constants.AVOID_PASSWORD);
//        return avoid_password;
//    }


    /**
     * 逗号分隔,并保留6位小数
     *
     * @param str
     * @return
     */
    public static String addComma2(String str) {
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.000000");
        return decimalFormat.format(Pub.GetDouble(str));
    }

    /**
     * 逗号分隔,并保留2位小数
     *
     * @param str
     * @return
     */
    public static String addComma3(String str) {
        DecimalFormat decimalFormat = new DecimalFormat("###,##0.00");
        return decimalFormat.format(Pub.GetDouble(str));
    }

    public static String addCommaCommon(String str) {
        if (!str.contains(".")) {
            DecimalFormat decimalFormat = new DecimalFormat(",###");
            return decimalFormat.format(Pub.GetDouble(str));
        }
        String[] split = str.split("\\.");
        System.out.println(split[1].length());
        StringBuilder pattern = new StringBuilder("###,##0.");
        for (int i = 0; i < split[1].length(); i++) {
            pattern.append("0");
        }
        DecimalFormat decimalFormat = new DecimalFormat(pattern.toString());
        return decimalFormat.format(Pub.GetDouble(str));
    }

}
