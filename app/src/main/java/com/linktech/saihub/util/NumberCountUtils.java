

package com.linktech.saihub.util;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;


public class NumberCountUtils {
    /**
     * 由于Java的简单类型不能够精确的对浮点数进行运算，这个工具类提供精
     * 确的浮点数运算，包括加减乘除和四舍五入。
     */
    private static final int DEF_DIV_SCALE = 10; //这个类不能实例化


    /**
     * 0 显示0.00
     */
    public static String roundTwoZEROBackStr(String v, int scale) {
        if (TextUtils.isEmpty(v)) {
            return "0.00";
        }
        if (v.equals("0")) {
            return "0.00";
        }
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        try {
            BigDecimal b = new BigDecimal(v);
            return roundTwoZEROBackStr(b, scale);
        } catch (Exception e) {
            return "0.00";
        }
    }

    /**
     * 0 显示0.0
     * 省略后面的0
     */
    public static String roundTwoZEROBackStr(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        if (v.doubleValue() == 0) {
            return "0.00";
        }
        String s = v.setScale(scale, BigDecimal.ROUND_DOWN).toString();
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }
        if ("0".equals(v)) {
            return "0.00";
        }
        return s;
    }


    public static BigDecimal mulD(String v1, String v2) {
        try {
            BigDecimal b1 = new BigDecimal(v1);
            BigDecimal b2 = new BigDecimal(v2);
            return b1.multiply(b2);
        } catch (Exception e) {
            return null;
        }
    }


    /**
     * 0  显示0.00   0.01000 显示0.01  1.0 显示1
     *
     * @param v
     * @param scale
     * @return
     */
    public static String roundStr(BigDecimal v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        if (v.doubleValue() == 0) {
            return "0.00";
        }
        String s = v.setScale(scale, BigDecimal.ROUND_DOWN).toString();
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }
        if ("0".equals(v)) {
            return "0.00";
        }
        return s;
    }


    /**
     * 提供精确的小数位四舍五入处理
     *
     * @param v     需要四舍五入的数字
     * @param scale 小数点后保留几位
     * @return 四舍五入后的结果
     */
    public static double round(String v, int scale) {
        if (scale < 0) {
            throw new IllegalArgumentException("The scale must be a positive integer or zero");
        }
        BigDecimal b = new BigDecimal(v);
        return b.setScale(scale, BigDecimal.ROUND_FLOOR).doubleValue();
    }

    /**
     * 去除末尾多余的0
     *
     * @param num
     * @return
     */
    public static String stripTrailingZeros(String num) {
        num = new BigDecimal(num).stripTrailingZeros().toPlainString();
        return num;
    }

    /**
     * 获得百分比数
     *
     * @param first
     * @param count
     */
    public static int getProgress(double first, double count) {
        try {
            BigDecimal bigDecimalFirst = new BigDecimal(String.valueOf(first));
            BigDecimal bigDecimalCount = new BigDecimal(String.valueOf(count));
            BigDecimal bigDecimal = bigDecimalFirst.divide(bigDecimalCount, 2, BigDecimal.ROUND_DOWN);
            int i = bigDecimal.multiply(new BigDecimal(String.valueOf(100))).toBigInteger().intValue();
            return i;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 获得百分比数
     *
     * @param first
     * @param count
     */
    public static int getProgress(long first, long count) {
        try {
            BigDecimal bigDecimalFirst = new BigDecimal(String.valueOf(first));
            BigDecimal bigDecimalCount = new BigDecimal(String.valueOf(count));
            BigDecimal bigDecimal = bigDecimalFirst.divide(bigDecimalCount, 2, BigDecimal.ROUND_DOWN);
            int i = bigDecimal.multiply(new BigDecimal(100)).toBigInteger().intValue();
            return i;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 保留整数并返回千分位显示
     *
     * @param num
     * @return
     */
    public static String getNumberIntegerThousands(String num) {
        if (TextUtils.isEmpty(num)) {
            return "";
        }
        BigDecimal bigDecimal = new BigDecimal(num);
        BigDecimal integerBd = bigDecimal.setScale(0, BigDecimal.ROUND_DOWN);
        DecimalFormat df = new DecimalFormat("###,###");
        return df.format(integerBd);
    }

    /**
     * 保留两位小数 不足补0
     *
     * @param num
     * @return
     */
    public static String getNumberTwoScale(String num) {
        if (TextUtils.isEmpty(num)) {
            return "";
        }
        // 四舍五入
        BigDecimal value = new BigDecimal(num).setScale(2, BigDecimal.ROUND_DOWN);
        // 不足两位小数补0
        DecimalFormat decimalFormat = new DecimalFormat("0.00#");
        return decimalFormat.format(value);
    }

    /**
     * K ：千     3=<整数<6
     * M ：百万   6=<整数<10
     * B ：十亿   10=<整数
     * 保留两位小数 不足补0
     *
     * @param num
     * @return
     */
    public static String getNumberFormatTwoZero(String num) {
        if (TextUtils.isEmpty(num)) {
            return "";
        }
        BigDecimal bigDecimal = new BigDecimal(num).abs();
        if (bigDecimal.compareTo(new BigDecimal(1000)) < 0) {
            //不足1000保留六位小数
            // 四舍五入
            BigDecimal value = bigDecimal.setScale(6, BigDecimal.ROUND_HALF_UP);
            return value.toString();
        } else if (bigDecimal.compareTo(new BigDecimal(1000000)) < 0) {
            //3=<整数<6 K
            BigDecimal subtract = bigDecimal.divide(new BigDecimal(1000));
            // 四舍五入
            BigDecimal value = subtract.setScale(2, BigDecimal.ROUND_HALF_UP);
            // 不足两位小数补0
            DecimalFormat decimalFormat = new DecimalFormat("0.00#");
            return decimalFormat.format(value) + "K";
        } else if (bigDecimal.compareTo(new BigDecimal(1000000000)) < 0) {
            // 6=<整数<10 M
            BigDecimal subtract = bigDecimal.divide(new BigDecimal(1000000));
            // 四舍五入
            BigDecimal value = subtract.setScale(2, BigDecimal.ROUND_HALF_UP);
            // 不足两位小数补0
            DecimalFormat decimalFormat = new DecimalFormat("0.00#");
            return decimalFormat.format(value) + "M";
        } else {
            //B ：十亿   10=<整数
            BigDecimal subtract = bigDecimal.divide(new BigDecimal(1000000000));
            // 四舍五入
            BigDecimal value = subtract.setScale(2, BigDecimal.ROUND_HALF_UP);
            // 不足两位小数补0
            DecimalFormat decimalFormat = new DecimalFormat("0.00#");
            return decimalFormat.format(value) + "B";
        }
    }

    /**
     * @param num 按K处理数字，保留两位小数
     * @return
     */
    public static String getNumberFormatThousand(String num) {
        if (TextUtils.isEmpty(num)) {
            return "";
        }
        BigDecimal bigDecimal = new BigDecimal(num).abs();
        if (bigDecimal.compareTo(new BigDecimal(1000)) < 0) {
            //不足1000不做处理
            return num;
        } else {
            //1000=<整数
            BigDecimal subtract = bigDecimal.divide(new BigDecimal(1000));
            BigDecimal valueScale = subtract.setScale(2, BigDecimal.ROUND_DOWN);
            return valueScale.stripTrailingZeros().toPlainString() + "K";
        }
    }


    /**
     * 传入num和精度 获取小数 并保留？位小数
     *
     * @param num
     * @return
     */
    public static String getNumberScaleByPow(String num, int pow, int scale) {
        if (TextUtils.isEmpty(num)) {
            return "";
        }
        BigDecimal value = new BigDecimal(num);
        BigDecimal divide = value.divide(new BigDecimal(10).pow(pow));
        BigDecimal bdLast = divide.setScale(scale, BigDecimal.ROUND_DOWN);
        return stripTrailingZeros(bdLast.toPlainString());
    }

    /**
     * 乘法传入bigdecimal str 精度
     *
     * @param num
     * @param rate
     * @return
     */
    public static String mulStr(String num, String rate) {
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(rate)) {
            return "0.00";
        }
        BigDecimal value = new BigDecimal(num);
        BigDecimal b2 = new BigDecimal(rate);
        return getNumberTwoScale(value.multiply(b2).toPlainString());
    }


    /**
     * 计算总资产 两数相加
     *
     * @param num1
     * @param num2
     * @return
     */
    public static String addTotal(String num1, String num2) {
        if (TextUtils.isEmpty(num1)) num1 = "0";
        if (TextUtils.isEmpty(num1)) num2 = "0";

        BigDecimal b1 = new BigDecimal(num1);
        BigDecimal b2 = new BigDecimal(num2);
        return b1.add(b2).toPlainString();
    }

    /**
     * 传入num和精度 获取值
     *
     * @param num
     * @return
     */
    public static String getNumberPow(BigInteger num, String pow) {
        if (num == null) {
            return "";
        }
        BigDecimal value = new BigDecimal(num);
        BigDecimal divide = value.divide(new BigDecimal(10).pow(Integer.parseInt(pow)));
        return divide.toPlainString();
    }


    /**
     * 传入num 保留?位小数
     *
     * @param num
     * @return
     */
    public static String getNumberScale(String num, int scale) {
        if (TextUtils.isEmpty(num)) {
            return "0";
        }
        BigDecimal value = new BigDecimal(num);
        BigDecimal bdLast = value.setScale(scale, BigDecimal.ROUND_DOWN);
        return stripTrailingZeros(bdLast.toPlainString());
    }


    /**
     * 两数相乘 保留2位小数
     *
     * @param num
     * @param rate
     * @return
     */
    public static String getConvert(String num, String rate) {
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(rate)) {
            return "0.00";
        }
        BigDecimal multi = new BigDecimal(num).multiply(new BigDecimal(rate));
        BigDecimal bdLast = multi.setScale(2, BigDecimal.ROUND_DOWN);
        // 不足两位小数补0
        DecimalFormat df = new DecimalFormat("0.00#");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        return df.format(bdLast);
    }

    /**
     * 两数相乘 保留N位小数
     *
     * @param num
     * @param rate
     * @return
     */
    public static String getConvert(String num, String rate, int scale) {
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(rate)) {
            return "0.00";
        }
        BigDecimal multi = new BigDecimal(num).multiply(new BigDecimal(rate));
        BigDecimal bdLast = multi.setScale(scale, BigDecimal.ROUND_DOWN);
        // 不足两位小数补0
        DecimalFormat df = new DecimalFormat("0.00#");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        return df.format(bdLast);
    }

    /**
     * 闪电钱包法币折合
     * 两数相乘 保留6位小数 不补零
     *
     * @param num  单位聪
     * @param rate
     * @return
     */
    public static String getLNConvert(String num, String rate) {
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(rate)) {
            return "0";
        }
        BigDecimal divide = new BigDecimal(num).divide(new BigDecimal(10).pow(8));
        BigDecimal multi = divide.multiply(new BigDecimal(rate));
        BigDecimal bdLast = multi.setScale(6, BigDecimal.ROUND_DOWN);
        //不补零
        DecimalFormat df = new DecimalFormat("0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
//        return df.format(bdLast);
        return bdLast.compareTo(BigDecimal.ZERO) == 0 ? "0" : bdLast.stripTrailingZeros().toPlainString();
    }

    /**
     * 聪转化为BTC
     *
     * @param num
     * @return
     */
    public static String transformBTC(String num) {
        if (TextUtils.isEmpty(num)) {
            return "0";
        }
        BigDecimal divide = new BigDecimal(num).divide(new BigDecimal(10).pow(8));
        //不补零
        DecimalFormat df = new DecimalFormat("0");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
//        return df.format(divide);
        return divide.toPlainString();
    }

    /**
     * BTC转化为聪
     *
     * @param num
     * @return
     */
    public static String transformSatoshi(String num) {
        if (TextUtils.isEmpty(num)) {
            return "0";
        }
        BigDecimal multiply = new BigDecimal(num).multiply(new BigDecimal(10).pow(8));
        return multiply.stripTrailingZeros().toPlainString();
    }


    /**
     * 矿工费
     * 两数相乘
     *
     * @param num
     * @param rate
     * @param pow
     * @return
     */
    public static String getConvert(String num, String rate, int pow, int scale) {
        if (TextUtils.isEmpty(num) || TextUtils.isEmpty(rate)) {
            return "0.00";
        }
        BigDecimal multi = new BigDecimal(num).multiply(new BigDecimal(rate));
        BigDecimal divide = multi.divide(new BigDecimal(10).pow(pow));
        BigDecimal bdLast = divide.setScale(scale, BigDecimal.ROUND_DOWN);
        return bdLast.stripTrailingZeros().toPlainString();
    }

    public static BigDecimal getCong(String num, int pow) {
        if (TextUtils.isEmpty(num)) {
            return BigDecimal.ZERO;
        }
        BigDecimal multiply = new BigDecimal(num).multiply(new BigDecimal(10).pow(pow));
        return multiply;
    }

    /**
     * 两数相加 保留2位小数 资产计算规则
     *
     * @param num1
     * @param num2
     * @return
     */
    public static String addNum(String num1, String num2) {
        if (TextUtils.isEmpty(num1) || TextUtils.isEmpty(num2)) {
            return "0.00";
        }
        BigDecimal add = new BigDecimal(num1).add(new BigDecimal(num2));
        BigDecimal bdLast = add.setScale(2, BigDecimal.ROUND_DOWN);
        DecimalFormat df = new DecimalFormat("0.00#");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        df.setDecimalFormatSymbols(symbols);
        if (bdLast.compareTo(new BigDecimal(1000000000)) < 0) {
            //不超过 10亿单位，完全展示
            // 不足两位小数补0
            return df.format(bdLast);
        } else if (bdLast.compareTo(new BigDecimal(1000000000)) > 0 && bdLast.compareTo(new BigDecimal(100000000000L)) < 0) {
            //超过10亿单位，展示XXXXM
            BigDecimal subtract = bdLast.divide(new BigDecimal(1000000));
            BigDecimal value = subtract.setScale(2, BigDecimal.ROUND_DOWN);
            // 不足两位小数补0
            return df.format(value) + "M";
        } else {
            // 超过1000亿单位，展示XXXXB
            BigDecimal subtract = bdLast.divide(new BigDecimal(100000000));
            BigDecimal value = subtract.setScale(2, BigDecimal.ROUND_DOWN);
            // 不足两位小数补0
            return df.format(value) + "B";
        }
    }

    public static Double getMaxFromList(List<Double> num) {
        double max = num.get(0);
        for (int i = 0; i < num.size(); i++) {
            if (num.get(i) > max) {
                max = num.get(i);
            }
        }
        return max;
    }

    public static Double getMinFromList(List<Double> num) {
        double min = num.get(0);
        for (int i = 0; i < num.size(); i++) {
            if (num.get(i) < min) {
                min = num.get(i);
            }
        }
        return min;
    }

    //闪电钱包计算预估费用
    public static String getLnFee(String num) {
        if (TextUtils.isEmpty(num)) {
            return "0-0";
        }
        BigDecimal sats = new BigDecimal(num);
        long min = sats.multiply(new BigDecimal("0.003")).setScale(0, BigDecimal.ROUND_DOWN).longValue();
        long max = sats.multiply(new BigDecimal("0.01")).setScale(0, BigDecimal.ROUND_DOWN).longValue() + 1;
        return min + "-" + max;
    }


}
