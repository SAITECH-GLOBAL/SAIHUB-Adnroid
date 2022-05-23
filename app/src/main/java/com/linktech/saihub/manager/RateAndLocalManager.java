
package com.linktech.saihub.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;

import com.linktech.saihub.R;
import com.linktech.saihub.app.SaiHubApplication;

import java.util.Locale;

/**
 * 设置语言
 */
public class RateAndLocalManager {

    private LocalKind mCurLocalLanguageKind;
    private RateKind mCurRateKind;

    private static volatile RateAndLocalManager mInstance;
    public static Context mContext;
    private SharedPreferences mPrefs;
    private static SharedPreferences.Editor mEditor;


    public static RateAndLocalManager getInstance(Context context) {
        if (mInstance == null) {
            synchronized (RateAndLocalManager.class) {
                if (mInstance == null) {
                    mInstance = new RateAndLocalManager(context);
                }
            }
        }
        return mInstance;

    }

    private RateAndLocalManager(Context context) {
        mContext = context;
        mPrefs = Preferences.getInstance(context).getSharePref();
        mEditor = mPrefs.edit();
        intLocal();
        try {
            initRate();
        } catch (Exception e) {
        }
    }


    /**
     * ------------------------------------------------------------------
     * --
     * --                            币种
     * --
     * ------------------------------------------------------------------
     */
    public static final String PREFERENCE_CURRATE = "curRate";


    public enum RateKind {
        USD("USD", SaiHubApplication.Companion.getInstance().getString(R.string.string_usd), "$"),
        CNY("CNY", SaiHubApplication.Companion.getInstance().getString(R.string.string_cny), "¥"),
        RUB("RUB", SaiHubApplication.Companion.getInstance().getString(R.string.string_rub), "₽");


        public String code;
        public String name;
        public String symbol;

        RateKind(String code, String name, String symbol) {
            this.code = code;
            this.name = name;
            this.symbol = symbol;
        }

        public static void setRateLocaKind() {
            USD.name = mContext.getString(R.string.string_usd);
            CNY.name = mContext.getString(R.string.string_cny);
            RUB.name = mContext.getString(R.string.string_rub);
        }
    }


    public RateKind getCurRateKind() {
        return mCurRateKind == null ? RateKind.USD : mCurRateKind;
    }

    public void changeRateKind(String rate) {
        if (rate.equals(RateKind.CNY.code)) {
            mCurRateKind = RateKind.CNY;
        } else if (rate.equals(RateKind.RUB.code)) {
            mCurRateKind = RateKind.RUB;
        } else {
            mCurRateKind = RateKind.USD;
        }
        mEditor.putString(PREFERENCE_CURRATE, mCurRateKind.name());
        mEditor.apply();
    }

    public void initRateKind(Context context) {
        mContext = context;
        RateKind.setRateLocaKind();
    }


    private void initRate() {
        String prefRate = mPrefs.getString(PREFERENCE_CURRATE, "");

        if (prefRate.isEmpty()) {
            mCurRateKind = RateKind.USD;
            /*switch (mCurLocalLanguageKind) {
                case zh_cn:
                    mCurRateKind = RateKind.CNY;
                    break;
                case zh_tw:
                    mCurRateKind = RateKind.CNY;
                case en:
                    mCurRateKind = RateKind.USD;
                    break;
                case ru:
                    mCurRateKind = RateKind.RUB;
                    break;
                default:
                    mCurRateKind = RateKind.USD;
            }*/
        } else {
            mCurRateKind = RateKind.valueOf(prefRate);
        }
    }


    /**
     * ------------------------------------------------------------------
     * --
     * --                              语言
     * --
     * ------------------------------------------------------------------
     */
    public static final String PREFERENCE_CURLOCAL = "curLocalLanguage";

    public static LocalKind getLocalKindByLangCode(String lang) {
        if (LocalKind.en_US.code.equalsIgnoreCase(lang)) {
            return LocalKind.en_US;
        } else if (LocalKind.zh_CN.code.equalsIgnoreCase(lang)) {
            return LocalKind.zh_CN;
        } else if (LocalKind.zh_HK.code.equalsIgnoreCase(lang)) {
            return LocalKind.zh_HK;
        } else if (LocalKind.ru_RU.code.equalsIgnoreCase(lang)) {
            return LocalKind.ru_RU;
        } else {
            return LocalKind.en_US;
        }
    }

    public void setCurLocalKindExt(Context context, LocalKind localKind) {
        mCurLocalLanguageKind = localKind;
        mEditor.putString(PREFERENCE_CURLOCAL, mCurLocalLanguageKind.code);
        mEditor.apply();
    }

    public enum LocalKind {
        en_US("en_US", "English"),
        zh_CN("zh_CN", "简体中文"),
        zh_HK("zh_HK", "繁体中文"),
        ru_RU("ru_RU", "Русский");
        public String code;
        public String name;

        LocalKind(String code, String name) {
            this.code = code;
            this.name = name;
        }
    }

    public LocalKind getCurLocalLanguageKind() {
        return mCurLocalLanguageKind == null ? LocalKind.en_US : mCurLocalLanguageKind;
    }

    private void intLocal() {
        String prefLocal = mPrefs.getString(PREFERENCE_CURLOCAL, "");
        if (prefLocal.isEmpty()) {

        /*  String local = getLangLocalConfig();
          if (local.equalsIgnoreCase(LocalKind.zh.name()) || local.contains("zh")) {
                mCurLocalLanguageKind = LocalKind.zh;
            } else {
                mCurLocalLanguageKind = LocalKind.en;
            }*/
            //默认语言为应为英文
            mCurLocalLanguageKind = LocalKind.en_US;
            setLangLocal(mCurLocalLanguageKind.code);
        } else {
            mCurLocalLanguageKind = LocalKind.valueOf(prefLocal);
        }
    }

    public void changeLocalLanguageKind(String lang) {
        if (LocalKind.en_US.code.equalsIgnoreCase(lang)) {
            mCurLocalLanguageKind = LocalKind.en_US;
        } else if (LocalKind.zh_CN.code.equalsIgnoreCase(lang)) {
            mCurLocalLanguageKind = LocalKind.zh_CN;
        } else if (LocalKind.zh_HK.code.equalsIgnoreCase(lang)) {
            mCurLocalLanguageKind = LocalKind.zh_HK;
        } else if (LocalKind.ru_RU.code.equalsIgnoreCase(lang)) {
            mCurLocalLanguageKind = LocalKind.ru_RU;
        } else {
            mCurLocalLanguageKind = LocalKind.en_US;
        }
        setLangLocal(mCurLocalLanguageKind.code);
        mEditor.putString(PREFERENCE_CURLOCAL, mCurLocalLanguageKind.code);
        mEditor.apply();
    }

    private void setLangLocal(String langLocal) {
        Locale locale = new Locale(langLocal);
        Resources resources = mContext.getResources();
        if (resources == null) {
            return;
        }
        if (langLocal.equalsIgnoreCase(getLangLocalConfig())) {
            return;
        }
        DisplayMetrics dm = resources.getDisplayMetrics();
        Configuration config = resources.getConfiguration();
        if (config == null) {
            return;
        }
        //Build.VERSION_CODES.N == 24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale);
            LocaleList localeList = new LocaleList(locale);
            LocaleList.setDefault(localeList);
            config.setLocales(localeList);
        } else if (Build.VERSION.SDK_INT >= 17) {//Build.VERSION_CODES.JELLY_BEAN_MR1 == 17
            config.setLocale(locale);
            Locale.setDefault(locale);
            config.setLayoutDirection(locale);
        } else {
            config.locale = locale;
        }
        resources.updateConfiguration(config, dm);
    }


    private String getLangLocalConfig() {
        Configuration conf = mContext.getResources().getConfiguration();
        return conf.locale.getLanguage();
    }

    public Boolean isChina() {
        return mCurLocalLanguageKind.code.equals(LocalKind.zh_CN.code) || mCurLocalLanguageKind.code.equals(LocalKind.zh_HK.code);
    }

    public Boolean isRmb() {
        return mCurRateKind.symbol.equals(RateAndLocalManager.RateKind.CNY.symbol);
    }


}
