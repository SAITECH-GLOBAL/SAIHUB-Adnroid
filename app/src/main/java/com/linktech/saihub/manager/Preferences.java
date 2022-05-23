/*
 *
 * *******************************************************************
 *   @项目名称: TECH Android
 *   @文件名称: Preferences.java
 *   @Date: 11/29/18 3:21 PM
 *   @Author: chenjun
 *   @Copyright（C）: 2018 BlueHelix Inc.   All rights reserved.
 *   注意：本内容仅限于内部传阅，禁止外泄以及用于其他的商业目的.
 *  *******************************************************************
 *
 */

package com.linktech.saihub.manager;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class Preferences {

    private static Preferences mInstance;
    private Context mContext;
    private SharedPreferences mPrefs;
    public static final String PREFERENCE_BHEX = "lite";


    public static Preferences getInstance(Context context) {
        if (mInstance == null)
            mInstance = new Preferences(context);
        return mInstance;
    }

    private Preferences(Context context) {
        mContext = context;
        doLoadPrefs();
    }

    public void doLoadPrefs() {
        mPrefs = mContext.getSharedPreferences(PREFERENCE_BHEX, Activity.MODE_PRIVATE);
        mPrefs.edit();
    }

    public SharedPreferences getSharePref() {
        if (mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(PREFERENCE_BHEX, Activity.MODE_PRIVATE);
        }
        return mPrefs;
    }

    /**
     * 清除Preferences的所有数据
     */
    public void cleanPrefs() {
        if (mPrefs == null) {
            mPrefs = mContext.getSharedPreferences(PREFERENCE_BHEX, Activity.MODE_PRIVATE);
        }
        Editor prefEditor = mPrefs.edit();
        prefEditor.clear();
        prefEditor.apply();
    }


}
