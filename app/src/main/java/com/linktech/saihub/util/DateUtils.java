/*
 * *******************************************************************
 *   @项目名称: TECH Android
 *   @文件名称: DateUtils.java
 *   @Date: 18-11-29 下午4:05
 *   @Author:
 *   @Description:
 *   @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 */

package com.linktech.saihub.util;

import android.content.Context;

import com.linktech.saihub.manager.RateAndLocalManager;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class DateUtils {

    public static final String DATE_FORMAT_1 = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT_12 = "MM-dd HH:mm:ss";
    //09/08/2021 16:21:34
    public static final String DATE_FORMAT_2 = "MM/dd/yyyy HH:mm:ss";
    public static final String DATE_FORMAT_3 = "yyyy-MM-dd";

    public static final String DATE_FORMAT_4 = "yyyy年MM月dd日";
    public static final String DATE_FORMAT_5 = "MM月dd日";
    public static final String DATE_FORMAT_6 = "MM-dd HH:mm";
    public static final String DATE_FORMAT_7 = "yyyy/MM/dd HH:mm:ss";
    public static final String DATE_FORMAT_8 = "MM/dd HH:mm:ss";

    public static final String DATE_FORMAT_9 = "yyyy.MM.dd";

    public static final String DATE_FORMAT_10 = "yyyy年MM月dd日 HH:mm:ss";

    public static final String DATE_FORMAT_11 = "mm:ss";
    public static final String DATE_FORMAT_13 = "HH:mm:ss";
    public static final String DATE_FORMAT_14 = "MM.dd";
    public static final String DATE_FORMAT_15 = "yyyy.MM.dd HH:mm";
    public static final String DATE_FORMAT_16 = "MM/dd HH:mm";
    public static final String DATE_FORMAT_17 = "yyyy/MM/dd HH:mm";
    public static final String DATE_FORMAT_18 = "HH:mm";
    public static final String DATE_FORMAT_19 = "MM-dd";
    public static final String DATE_FORMAT_20 = "yyyy-MM-dd  HH:mm";


    /**
     * 返回文字描述的日期
     *
     * @param date
     * @return
     */
    public static String getTimeFormatText(Date date) {
        if (date == null) {
            return null;
        }

        Calendar calendar = Calendar.getInstance();

        int currentDayIndex = calendar.get(Calendar.DAY_OF_YEAR);
        int currentYear = calendar.get(Calendar.YEAR);

        calendar.setTime(date);
        int msgYear = calendar.get(Calendar.YEAR);
        int msgDayIndex = calendar.get(Calendar.DAY_OF_YEAR);
        int msgMinute = calendar.get(Calendar.MINUTE);

        String msgTimeStr = calendar.get(Calendar.HOUR_OF_DAY) + ":";

        if (msgMinute < 10) {
            msgTimeStr = msgTimeStr + "0" + msgMinute;
        } else {
            msgTimeStr = msgTimeStr + msgMinute;
        }

        int msgDayInWeek = calendar.get(Calendar.DAY_OF_WEEK);

        if (currentDayIndex == msgDayIndex) {
            return msgTimeStr;
        } else {
            if (currentDayIndex - msgDayIndex == 1 && currentYear == msgYear) {
                msgTimeStr = "昨天 " + msgTimeStr;
            } else if (currentDayIndex - msgDayIndex > 1 && currentYear == msgYear) { //本年消息
                //不同周显示具体月，日，注意函数：calendar.get(Calendar.MONTH) 一月对应0，十二月对应11
                msgTimeStr = (Integer.valueOf(calendar.get(Calendar.MONTH) + 1)) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日 " + msgTimeStr + " ";
            } else { // 1、非正常时间，如currentYear < msgYear，或者currentDayIndex < msgDayIndex
                //2、非本年消息（currentYear > msgYear），如：历史消息是2018，今年是2019，显示年、月、日
                msgTimeStr = msgYear + "年" + (Integer.valueOf(calendar.get(Calendar.MONTH) + 1)) + "月" + calendar.get(Calendar.DAY_OF_MONTH) + "日" + msgTimeStr + " ";
            }
        }
        return msgTimeStr;
    }

    /**
     * 判断时间是否可以赎回
     */
    public static boolean isRansomOrderDate() {
        try {
            String format = "HH:mm:ss";
            String stringForFormat = DateUtils.getStringForFormat(System.currentTimeMillis(), format);
            Date nowTime = new SimpleDateFormat(format).parse(stringForFormat);
            Date startTime = new SimpleDateFormat(format).parse("23:00:00");
            Date endTime = new SimpleDateFormat(format).parse("23:59:59");
            return isEffectiveDate(nowTime, startTime, endTime);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 判断当前时间是否在[startTime, endTime]区间，注意时间格式要一致
     *
     * @param nowTime   当前时间
     * @param startTime 开始时间
     * @param endTime   结束时间
     * @return
     * @author jqlin
     */
    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);
        Calendar end = Calendar.getInstance();
        end.setTime(endTime);
        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static Calendar getStartTimeOfCalendar(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static Calendar getEndTimeOfCalendar(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
        return calendar;
    }

    public static Date str2Date(String timeStr) {
        long time = Long.valueOf(timeStr);
        Date date = new Date((time / 1000 - (time / 1000) % 60) * 1000);
        return date;
    }

    //日期时间格式化
    public static String getNewDateFormat(String content, String format, String to) {
        String newFormat = "";
        try {
            DateFormat fmt = new SimpleDateFormat(format);
            Date date = fmt.parse(content);
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            newFormat = sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newFormat;
    }

    public static String getSimpleTimeFormat(long time, String formatStr) {
        String strTemp = "";
        Date dateCreate = new Date(time);
        SimpleDateFormat myFmt2 = new SimpleDateFormat(formatStr);
        myFmt2.setTimeZone(TimeZone.getDefault());
        strTemp = myFmt2.format(dateCreate);
        return strTemp;
    }

    public static String getSimpleTimeFormatS(long time, String formatStr) {
        String strTemp = "";
        Date dateCreate = new Date(time);
        SimpleDateFormat myFmt2 = new SimpleDateFormat(formatStr);
        myFmt2.setTimeZone(TimeZone.getDefault());
        strTemp = myFmt2.format(dateCreate);
        return strTemp;
    }

    public static String getSimpleTimeFormat(String time, String formatStr) {
        String strTemp = "";
        try {
            Date dateCreate = new Date(Long.valueOf(time));
            SimpleDateFormat myFmt2 = new SimpleDateFormat(formatStr);
            myFmt2.setTimeZone(TimeZone.getDefault());
            strTemp = myFmt2.format(dateCreate);
            return strTemp;
        } catch (Exception e) {
            return strTemp;
        }
    }


    /**
     * 转换今天 昨天
     *
     * @param content
     * @param format
     * @param to
     * @return
     */
    public static String getNewDateFormat2(String content, String format, String to) {
        String newFormat = "";
        try {
            DateFormat fmt = new SimpleDateFormat(format);
            Date date = fmt.parse(content);
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            DateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            long currentTimeMillis = System.currentTimeMillis();
            Date today = new Date(currentTimeMillis);
//			Date today = fmt1.parse(content);
            newFormat = sdf.format(date);
            if (Math.abs(((today.getTime() - date.getTime()) / (24 * 3600 * 1000))) == 0) {
                newFormat = "今天";
            }
            if (Math.abs(((today.getTime() - date.getTime()) / (24 * 3600 * 1000))) == 1) {
                newFormat = "昨天";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newFormat;
    }

    public static String getYesterdayFormat(String content, String format, String to, String todayStr) {
        String newFormat = "";
        try {
            DateFormat fmt = new SimpleDateFormat(format);
            Date date = fmt.parse(content);
            DateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date today = fmt1.parse(todayStr);
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            if (Math.abs(((today.getTime() - date.getTime()) / (24 * 3600 * 1000))) == 1) {
                newFormat = "Y";
            } else {
                newFormat = sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newFormat;
    }

    public static String getTodayFormat(String content, String format, String to, String todayStr) {
        String newFormat = "";
        try {
            DateFormat fmt = new SimpleDateFormat(format);
            Date date = fmt.parse(content);
            DateFormat fmt1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date today = fmt1.parse(todayStr);
            SimpleDateFormat sdf = new SimpleDateFormat(to);
            if (Math.abs(((today.getTime() - date.getTime()) / (24 * 3600 * 1000))) == 0) {
                newFormat = "Y";
            } else {
                newFormat = sdf.format(date);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return newFormat;
    }

    /**
     * 计算开始时间
     *
     * @param klineType
     * @return
     */
    public static long calKlineFromTime(String klineType) {
        //默认开始时间向前推一周
        long perDayS = 86400000;
        long from = new BigDecimal(7l).multiply(new BigDecimal(perDayS)).longValue();
        if (klineType.equals("1m")) {
            from = new BigDecimal(2l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("5m")) {
            from = new BigDecimal(5l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("15m")) {
            from = new BigDecimal(10l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("30m")) {
            //一天48根数据 48*6 大约将近300根
            from = new BigDecimal(20l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("1h")) {
            from = new BigDecimal(40l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("1d")) {
            from = new BigDecimal(1000l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("1w")) {
            from = new BigDecimal(1000l).multiply(new BigDecimal(perDayS)).longValue();
        } else if (klineType.equals("1")) {
            from = new BigDecimal(1100l).multiply(new BigDecimal(perDayS)).longValue();
        }
        return from;
    }


    /**
     * 计算天差（相差天数）
     *
     * @param endTime
     * @param startTime
     * @return
     * @throws ParseException
     */
    public static int calDaysByCalendar(String endTime, String startTime) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            Date endDate = new Date(Long.valueOf(endTime));
            Date startDate = new Date(Long.valueOf(startTime));

            Calendar c1 = Calendar.getInstance();
            c1.setTime(endDate);
            Calendar c2 = Calendar.getInstance();
            c2.setTime(startDate);

            int endDay = c1.get(Calendar.DAY_OF_YEAR);
            int startDay = c2.get(Calendar.DAY_OF_YEAR);
            return endDay - startDay;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }

    }

    /**
     * 是否大于1天
     *
     * @return
     */
    public static boolean afterOneDay(long currentTime, long createTime, int day) {
        return currentTime > createTime + day * 24 * 60 * 60 * 1000;
    }


    /**
     * 增加天数
     *
     * @param time
     * @return
     */
    public static String addDays(String time, int days, String format) {
        try {
            long resultTime = Long.valueOf(time) + days * 24 * 60 * 60 * 1000;
            SimpleDateFormat formatter = new SimpleDateFormat(format);
            Date date = new Date(resultTime);
            String timeF = formatter.format(date);
            return timeF;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 增加天数
     *
     * @param time
     * @return
     */
    public static String calDaysByCalendar(long time) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
            Date date = new Date(time);
            String timeF = formatter.format(date);
            return timeF;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String getDateAndWeek(String time, int days) {
        long resultTime = Long.valueOf(time) + days * 24 * 60 * 60 * 1000;
        String date = calDaysByCalendar(resultTime);
        String week = getWeekSimple(resultTime);
        return date + " " + week;
    }

    public static String getString2ForLong(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_2);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringForLongFormat12(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_1);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringForLong2(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_12);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringForMonthDay(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_5);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringGang(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_14);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringForChart(Long timestamp) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_15);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getStringForFormat(Long timestamp, String format) {
        if ((timestamp + "").length() > 13) {
            timestamp = Pub.GetLong((timestamp + "").substring(0, 13));
        }

        SimpleDateFormat formatter = new SimpleDateFormat(format);
        Date date = new Date(timestamp);
        return formatter.format(date);
    }

    public static String getTimeString(String timestamp) {
        int timelength = timestamp.length();
        if (timestamp.length() < 13) {
            for (int i = 0; i <= 13 - timelength; i++) {
                timestamp += "0";
            }
        }
        Long aLong = Pub.GetLong((timestamp + "").substring(0, 13));
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_1);
        Date date = new Date(aLong);
        return formatter.format(date);
    }

    public static String getStringFor7(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_7);
        Date date = new Date(time);
        return formatter.format(date);
    }

    public static String getFormat6(Long time) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_FORMAT_6);
        Date date = new Date(time);
        return formatter.format(date);
    }


    //根据日期取得星期几
    public static String getWeek(Date date) {
        String[] weeks = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五", "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int week_index = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (week_index < 0) {
            week_index = 0;
        }
        return weeks[week_index];
    }

    //根据日期取得星期几
    public static String getWeekSimple(long time) {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date(time);
            SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
            String week = sdf.format(date);
            return week;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }


    public static String getCurrentHHMMSS() {
        try {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            Date date = new Date();

            String currentStr = formatter.format(date);
            return currentStr;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static long getLongTimeStamp(String timeStamp) {
        int timelength = timeStamp.length();
        if (timelength < 13) {
            for (int i = 0; i <= 13 - timelength; i++) {
                timeStamp += "0";
            }
        }
        Long aLong = Pub.GetLong((timeStamp + "").substring(0, 13));
        return aLong;
    }


    /**
     * 判断时间戳是否是今天
     *
     * @param time
     * @return
     */
    public static boolean isToday(long time) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(time);
        Date old = null;
        Date now = null;
        try {
            old = sdf.parse(sdf.format(date));
            now = sdf.parse(sdf.format(new Date()));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long oldTime = old.getTime();
        long nowTime = now.getTime();

        long day = (nowTime - oldTime) / (24 * 60 * 60 * 1000);

        if (day < 1) {  //今天
            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            return true;
//        } else if (day == 1) {     //昨天
//            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
//            return "昨天 " + format.format(date);
        } else {    //可依次类推
//            SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
            return false;
        }
    }

    /**
     * 获取指定日期 的 零点时间戳
     *
     * @param time
     * @return 时间戳 毫秒
     */
    public static long getStartOfToday(long time) {
        long now = time / 1000L;
        long daySecond = 60 * 60 * 24;
        long dayTime = now - (now + 8 * 3600) % daySecond;
        return dayTime;
    }

    //1632267007000  1632289628524
    public static Long getMonth(long timeStamp) {
        Calendar cd = Calendar.getInstance();
        Date date = new Date(timeStamp);
        cd.setTime(date);
        return Long.valueOf(cd.get(Calendar.MONTH) + 1);
    }


    public static String getMonthStr(Context context, long timeStamp) {
        Calendar cd = Calendar.getInstance();
        cd.setTime(new Date(timeStamp));
        int monthInt = cd.get(Calendar.MONTH) + 1;
        int yeerInt = cd.get(Calendar.YEAR);

        Calendar cdCurrent = Calendar.getInstance();
        cdCurrent.setTime(new Date(System.currentTimeMillis()));
        int monthCurrentInt = cdCurrent.get(Calendar.MONTH) + 1;

        String monthStr = "";
        boolean isEnglish = RateAndLocalManager.getInstance(context).getCurLocalLanguageKind().code.equals(RateAndLocalManager.LocalKind.en_US.code);
        switch (monthInt) {
            case 1:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "January" : "1月");
                break;
            case 2:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "February" : "2月");
                break;
            case 3:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "March" : "3月");
                break;
            case 4:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "April" : "4月");
                break;
            case 5:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "May" : "5月");
                break;
            case 6:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "June" : "6月");
                break;
            case 7:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "July" : "7月");
                break;
            case 8:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "August" : "8月");
                break;
            case 9:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "September" : "9月");
                break;
            case 10:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "October" : "10月");
                break;
            case 11:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "November" : "11月");
                break;
            case 12:
                monthStr = monthInt == monthCurrentInt ? (!isEnglish ? "最近" : "Lately") : (isEnglish ? "December" : "12月");
                break;
        }
        return monthStr;
    }

    /**
     * 获取指定日期所在月份开始的时间戳
     *
     * @param date 指定日期
     * @return
     */
    public static long getMonthBegin(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date time = null;
        try {
            time = format.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(time);

        //设置为1号,当前日期既为本月第一天
        c.set(Calendar.DAY_OF_MONTH, 1);
        //将小时至0
        c.set(Calendar.HOUR_OF_DAY, 0);
        //将分钟至0
        c.set(Calendar.MINUTE, 0);
        //将秒至0
        c.set(Calendar.SECOND, 0);
        //将毫秒至0
        c.set(Calendar.MILLISECOND, 0);
        // 获取本月第一天的时间戳
        return c.getTimeInMillis();
    }

    /**
     * 获取指定日期所在月份结束的时间戳
     *
     * @param date 指定日期
     * @return
     */
    public static long getMonthEnd(String date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date time = null;
        try {
            time = format.parse(date);
        } catch (ParseException e) {
            return 0;
        }
        Calendar c = Calendar.getInstance();
        c.setTime(time);

        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND, 59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 获取本月最后一天的时间戳
        return c.getTimeInMillis();
    }
}
