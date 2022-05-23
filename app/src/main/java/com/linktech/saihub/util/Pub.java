package com.linktech.saihub.util;

import android.util.TypedValue;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pub {
    private static long lastClickTime;
    private static int lastViewId;
    private static long pageChangedTime;
    private static long addFragmentTime;
    private static long captchaMgrTime;
    private static long showProgressTime;
    private static String[] coins = new String[]{"BTC", "ETH"};
    private static TypedValue typedValue;

    public Pub() {
    }

    public static String fetchConiName(String origin) {
        String result = "";

        for (int i = 0; i < coins.length; ++i) {
            if (origin.startsWith(coins[i]) || origin.startsWith(coins[i].toLowerCase())) {
                result = coins[i];
                break;
            }
        }

        return result;
    }


    public static boolean isFastDoubleClick(int id) {
        return isFastDoubleClick(1000L, id);
    }

    public static boolean isFastDoubleClick() {
        return isFastDoubleClick(1000L);
    }

    public static boolean isFastAddFragment() {
        return isFastAddFragment(1000L);
    }

    public static boolean isFastAddFragment(long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - addFragmentTime;
        if (0L < timeD && timeD < diff) {
            return true;
        } else {
            addFragmentTime = time;
            return false;
        }
    }

    public static boolean isCaptchaMgr() {
        long time = System.currentTimeMillis();
        long timeD = time - captchaMgrTime;
        if (0L < timeD && timeD < 1000L) {
            return true;
        } else {
            captchaMgrTime = time;
            return false;
        }
    }

//    public static boolean isLongDoubleClick() {
//        return isFastDoubleClick(5000L);
//    }

    public static boolean isFastDoubleClick(long diff, int id) {
        //判断控件id是否相同
        if (lastViewId != id) {
            lastClickTime = 0;
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            if (0L < timeD && timeD < diff) {
                lastViewId = id;
                return true;
            } else {
                lastClickTime = time;
                lastViewId = id;
                return false;
            }
        } else {
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            if (0L < timeD && timeD < diff) {
                return true;
            } else {
                lastClickTime = time;
                return false;
            }
        }

    }

    public static boolean isFastDoubleClick(long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0L < timeD && timeD < diff) {
            return true;
        } else {
            lastClickTime = time;
            return false;
        }

    }

    public static boolean isFastPageChanged() {
        return isFastPageChanged(2000L);
    }

    public static boolean isFastShowProgress() {
        return isFastShowProgress(2000L);
    }

    public static boolean isFastShowProgress(long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - showProgressTime;
        if (0L < timeD && timeD < diff) {
            return true;
        } else {
            showProgressTime = time;
            return false;
        }
    }

    public static boolean isFastPageChanged(long diff) {
        long time = System.currentTimeMillis();
        long timeD = time - pageChangedTime;
        if (0L < timeD && timeD < diff) {
            return true;
        } else {
            pageChangedTime = time;
            return false;
        }
    }

    public static boolean InArray(int[] arr, int item) {
        if (arr != null && arr.length > 0) {
            for (int i = 0; i < arr.length; ++i) {
                if (arr[i] == item) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static boolean InArray(String[] arr, String item) {
        if (arr != null && arr.length > 0) {
            for (int i = 0; i < arr.length; ++i) {
                if (arr[i].endsWith(item)) {
                    return true;
                }
            }

            return false;
        } else {
            return false;
        }
    }

    public static String getPriceFormat(double dNum) {
        return getPriceFormat(dNum, 2);
    }

    public static String getPriceFormat(String dNum, int point) {
        return getPriceFormat(GetDouble(dNum), point);
    }

    public static String getPriceFormat(double dNum, int point) {
        return getPriceFormat(dNum, point, RoundingMode.DOWN);
    }

    public static String getPriceFormat(double dNum, int point, RoundingMode roudingModel) {
        StringBuilder sb = new StringBuilder();
        sb.append("#");
        if (point > 0) {
            sb.append(".");
        }

        for (int i = 0; i < point; ++i) {
            sb.append("0");
        }

        DecimalFormat df = new DecimalFormat(sb.toString());
        df.setRoundingMode(roudingModel);
        if (dNum < 1.0D && dNum >= 0.0D && point > 0) {
            return "0" + df.format(dNum);
        } else {
            return dNum < 0.0D && dNum > -1.0D ? df.format(dNum).replace("-.", "-0.") : df.format(dNum);
        }
    }

    public static String cutZoom(String dNum) {
        return zoomZero(getPriceFormat(dNum));
    }

    public static String cutZoom(String dNum, int p) {
        return zoomZero(getPriceFormat(dNum, p));
    }

    public static String getPriceFormat(String nNum) {
        double dNum = GetDouble(nNum, 0.0D);
        return getPriceFormat(dNum);
    }

    public static boolean IsNumber(String nNum) {
        if (nNum != null && nNum.length() > 0) {
            for (int i = 0; i < nNum.length(); ++i) {
                char c = nNum.charAt(i);
                if (c < '0' || c > '9') {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean isNumberEffective(String nNum) {
        Pattern p = Pattern.compile("^\\d+$|^\\d+\\.\\d+$");
        Matcher m = p.matcher(nNum);
        return m.matches();
    }

    public static boolean IsNumOrLetter(String letter) {
        if (letter != null && letter.length() > 0) {
            for (int i = 0; i < letter.length(); ++i) {
                char c = letter.charAt(i);
                if ((c < 'a' || c > 'z') && (c < 'A' || c > 'Z') && (c < '0' || c > '9')) {
                    return false;
                }
            }

            return true;
        } else {
            return false;
        }
    }

    public static boolean IsIntFormat(String nNum, boolean bPositive) {
        String intformat = "[1-9]\\d*";
        Pattern p = Pattern.compile(intformat);
        Matcher m = p.matcher(nNum);
        boolean bMatche = m.matches();
        boolean bValue = !bPositive || parseInt(nNum) > 0;
        return bMatche && bValue;
    }

    public static boolean IsFloatFormat(String nNum, boolean bPositive) {
        if (IsIntFormat(nNum, bPositive)) {
            return true;
        } else {
            String intformat = "[1-9]\\d*\\.\\d+";
            Pattern p = Pattern.compile(intformat);
            Matcher m = p.matcher(nNum);
            String intformat2 = "[0]\\.\\d+";
            Pattern p2 = Pattern.compile(intformat2);
            Matcher m2 = p2.matcher(nNum);
            boolean bMatche = m.matches() || m2.matches();
            boolean bValue = !bPositive || parseFloat(nNum) > 0.0F;
            return bMatche && bValue;
        }
    }

    public static boolean isStringEmpty(String Str) {
        return Str == null || Str.length() <= 0;
    }

    public static <T> boolean isListExists(List<T> list) {
        return list != null && list.size() > 0;
    }

    public static boolean isCellphone(String phoneNum) {
        if (phoneNum != null && phoneNum.length() > 0) {
            Pattern pattern = Pattern.compile("1[0-9]{10}");
            Matcher matcher = pattern.matcher(phoneNum);
            return matcher.matches();
        } else {
            return false;
        }
    }

    public static boolean isPhoneNum(String phoneNum) {
        if (IsNumber(phoneNum) && phoneNum.length() == 11) {
            String str = "^(13[0-9]|15[012356789]|17[678]|18[0-9]|14[57])[0-9]{8}$";
            Pattern p = Pattern.compile(str);
            Matcher m = p.matcher(phoneNum);
            return m.matches();
        } else {
            return false;
        }
    }

    public static boolean isIdentityCode(String text) {
        String regx = "[0-9]{17}x";
        String reg1 = "[0-9]{15}";
        String regex = "[0-9]{18}";
        return text.matches(regx) || text.matches(reg1) || text.matches(regex);
    }

    public static int parseInt(String nStr) {
        int n;
        try {
            n = Integer.parseInt(nStr);
        } catch (Exception var3) {
            n = -1;
        }

        return n;
    }

    public static float parseFloat(String nStr) {
        float n;
        try {
            n = Float.parseFloat(nStr);
        } catch (Exception var3) {
            n = 0.0F;
        }

        return n;
    }

    public static double GetDouble(String str, double ddefault) {
        double dResult;
        try {
            dResult = Double.parseDouble(str);
        } catch (Exception var6) {
            dResult = ddefault;
        }

        return dResult;
    }

    public static String zoomZero(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }

        return s;
    }


    public static String GetDoubleStr(String str) {
        return GetDoubleStr(str, 0.0D);
    }

    public static String GetDoubleStr(String str, double ddefault) {
        double dResult;
        try {
            dResult = Double.parseDouble(str);
        } catch (Exception var6) {
            dResult = ddefault;
        }

        return String.valueOf(dResult);
    }


    public static double GetDouble(String str) {
        return GetDouble(str, 0.0D);
    }

    public static double add(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.add(b2).doubleValue();
    }

    public static double add(String d1, String d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.add(b2).doubleValue();
    }

    public static double sub(String d1, String d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();
    }

    public static double sub(double d1, String d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.subtract(b2).doubleValue();
    }

    public static double sub(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.subtract(b2).doubleValue();
    }

    public static double multiply(double d1, int d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    public static double multiply(String d1, String d2) {
        if (isStringEmpty(d1)) {
            d1 = "0.0";
        }

        if (isStringEmpty(d2)) {
            d2 = "0.0";
        }

        try {
            BigDecimal b1 = new BigDecimal(d1);
            BigDecimal b2 = new BigDecimal(d2);
            return b1.multiply(b2).doubleValue();
        } catch (Exception var4) {
            return 0.0D;
        }
    }

    public static double multiply(double d1, double d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.multiply(b2).doubleValue();
    }

    public static double divide(double d1, double d2, int point) {
        MathContext mc = new MathContext(point, RoundingMode.HALF_DOWN);
        BigDecimal b1 = new BigDecimal(Double.toString(d1));
        BigDecimal b2 = new BigDecimal(Double.toString(d2));
        return b1.divide(b2, mc).doubleValue();
    }

    public static double divide(double d1, int d2) {
        BigDecimal b1 = new BigDecimal(d1);
        BigDecimal b2 = new BigDecimal(d2);
        return b1.divide(b2).doubleValue();
    }

    public static int GetInt(String str, int ddefault) {
        int dResult;
        try {
            dResult = Integer.parseInt(str);
        } catch (Exception var4) {
            dResult = ddefault;
        }

        return dResult;
    }


    public static int GetInt(String str) {
        return GetInt(str, 0);
    }

    public static float GetFloat(String str, float ff) {
        float dResult;
        try {
            dResult = Float.parseFloat(str);
        } catch (Exception var4) {
            dResult = ff;
        }

        return dResult;
    }

    public static Long GetLong(String str) {
        Long dResult;
        try {
            dResult = Long.parseLong(str);
        } catch (Exception var4) {
            dResult = 0L;
        }

        return dResult;
    }

    public static Long GetLong(String str, Long ddefault) {
        Long dResult;
        try {
            dResult = Long.parseLong(str);
        } catch (Exception var4) {
            dResult = ddefault;
        }

        return dResult;
    }

    public static boolean isPicture(String image) {
        if (isStringEmpty(image)) {
            return false;
        } else if (image.endsWith(".jpg")) {
            return true;
        } else if (image.endsWith(".jpeg")) {
            return true;
        } else if (image.endsWith(".gif")) {
            return true;
        } else if (image.endsWith(".png")) {
            return true;
        } else if (image.endsWith(".bmp")) {
            return true;
        } else if (image.endsWith(".JPG")) {
            return true;
        } else if (image.endsWith(".JPEG")) {
            return true;
        } else if (image.endsWith(".GIF")) {
            return true;
        } else {
            return image.endsWith(".PNG") || image.endsWith(".BMP");
        }
    }

    public static boolean equals(String text, String text2) {
        try {
            return text != null && text2 != null && text.equals(text2);
        } catch (Exception var3) {
            return false;
        }
    }


}
