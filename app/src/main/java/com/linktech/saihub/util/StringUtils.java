
package com.linktech.saihub.util;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.net.Uri;
import android.text.TextUtils;


public class StringUtils {
    /**
     * 16进制转10进制
     *
     * @param bytes
     * @return
     */
    public static String bytes2hex03(byte[] bytes) {
        final String HEX = "0123456789abcdef";
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(HEX.charAt((b >> 4) & 0x0f));
            sb.append(HEX.charAt(b & 0x0f));
        }

        return sb.toString();
    }

    /**
     * 10进制转16进制
     *
     * @param num
     * @return
     */
    public static String toHex(long num) {
        String str = Long.toHexString(num);
        return "0x" + str;
    }

    public static String toHex(long num, int totalLenght) {
        String str = Long.toHexString(num);
        str = splicingZero(str, totalLenght);
        return str;
    }

    /**
     * 字符串前面补零操作
     *
     * @param str         字符串本体
     * @param totalLenght 需要的字符串总长度
     * @return
     */
    public static String splicingZero(String str, int totalLenght) {
        int strLenght = str.length();
        String strReturn = str;
        for (int i = 0; i < totalLenght - strLenght; i++) {
            strReturn = "0" + strReturn;
        }
        return strReturn;
    }

    //字符串排空
    public static String replaceSpace(String data) {
        String str = "";
        if (!TextUtils.isEmpty(data)) {
            str = data.replaceAll(" ", "");
        }
        return str;
    }

    public static boolean isNotEmpty(String s) {
        return s != null && s.trim().length() > 0;
    }

    public static String formatPhone(String phone) {
        return phone.substring(0, 3) + "****" + phone.substring(7, 11);
    }

    public static String formatEmail(String email) {
        String[] split = email.split("@");
        if (split[0].length() < 3) {
            return split[0] + "***@" + split[1];
        } else {
            return split[0].substring(0, 3) + "***@" + split[1];
        }
    }

    /**
     * 合约/钱包地址截取显示
     *
     * @param address
     * @return
     */
    public static String formatAddress(String address) {
        if (address == null) {
            return "";
        }
        if (address.length() >= 12) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(address.substring(0, 6));
            stringBuilder.append("...");
            stringBuilder.append(address.substring(address.length() - 6));
            return stringBuilder.toString();
        } else {
            return address;
        }
    }

    /**
     * 跨链记录hash截取显示
     *
     * @param address
     * @return
     */
    public static String formatCrossAddress(String address) {
        if (address == null) {
            return "";
        }
        if (address.length() >= 20) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(address.substring(0, 10));
            stringBuilder.append("...");
            stringBuilder.append(address.substring(address.length() - 10));
            return stringBuilder.toString();
        } else {
            return address;
        }

    }

    public static String imageTranslateUri(int resId, Context context) {
        Resources r = context.getResources();
        Uri uri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://"
                + r.getResourcePackageName(resId) + "/"
                + r.getResourceTypeName(resId) + "/"
                + r.getResourceEntryName(resId));

        return uri.toString();
    }

}
