package com.linktech.saihub.util;

import static com.linktech.saihub.manager.RateAndLocalManager.PREFERENCE_CURLOCAL;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.linktech.saihub.manager.Preferences;

import java.util.Locale;

public class LocalManageUtil {

    private final static String TAG = LocalManageUtil.class.getSimpleName();

    /**
     * 赋予context对象 语言属性新值
     *
     * @param context
     * @param language
     * @return
     */
    public static Context attachBaseContext(Context context, String language) {
        Locale locale = getSetLanguageLocale(context);
        return createConfigurationResources(context, locale.getLanguage());
    }

    /*
     * 获取设置的语言
     * @param context
     * @return
     */
    public static Locale getSetLanguageLocale(Context context) {
        SharedPreferences mPrefs = Preferences.getInstance(context).getSharePref();
        String prefLocal = mPrefs.getString(PREFERENCE_CURLOCAL, "");
        String currentLanguge = prefLocal;
        //默认为英文
        if (TextUtils.isEmpty(prefLocal)) {
            currentLanguge = "en_US";
        }
        if (currentLanguge.equalsIgnoreCase("zh_CN")) {
            return Locale.CHINA;
        } else if (currentLanguge.equalsIgnoreCase("zh_HK")) {
            return new Locale("tw", "TW");
        } else if (currentLanguge.equalsIgnoreCase("en_US")) {
            return Locale.ENGLISH;
        } else if (currentLanguge.equalsIgnoreCase("ru_RU")) {
            return new Locale("ru", "RU");
        } else {
            return Locale.ENGLISH;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    public static Context createConfigurationResources(Context context, String language) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        DisplayMetrics dm = resources.getDisplayMetrics();

        Locale locale = getSetLanguageLocale(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            configuration.setLocales(localeList);
            Locale.setDefault(locale);
        } else {
            configuration.setLocale(locale);
        }

        return context.createConfigurationContext(configuration);
    }

    public static void setApplicationLanguage(Context context) {
        Resources resources = context.getApplicationContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        Locale locale = getSetLanguageLocale(context);
        config.locale = locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
            context.getApplicationContext().createConfigurationContext(config);
            Locale.setDefault(locale);
        }
        resources.updateConfiguration(config, dm);
    }

    public static void applyLanguage(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(newLanguage);
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, dm);
        } else {
            // updateConfiguration
            configuration.locale = locale;

            resources.updateConfiguration(configuration, dm);
        }
    }

    public static Configuration getLanguageConfig(Context context, String newLanguage) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = new Locale(newLanguage);
        DisplayMetrics dm = resources.getDisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // apply locale
            configuration.setLocale(locale);
            resources.updateConfiguration(configuration, dm);
        } else {
            // updateConfiguration
            configuration.locale = locale;
        }
        return configuration;
    }

    //获取本地应用的实际的多语言信息
    public static Locale getAppLocale(Context context) {
        //获取应用语言
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = configuration.getLocales().get(0);
        } else {
            locale = configuration.locale;
        }
        return locale;
    }

}
