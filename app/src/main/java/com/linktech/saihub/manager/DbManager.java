package com.linktech.saihub.manager;

import android.app.Application;
import android.database.sqlite.SQLiteDatabase;

import com.alibaba.android.arouter.exception.InitException;
import com.linktech.saihub.app.SaiHubApplication;
import com.linktech.saihub.db.MySqlLiteOpenHelper;
import com.linktech.saihub.greendao.DaoMaster;
import com.linktech.saihub.greendao.DaoSession;


public class DbManager {

    private volatile static boolean hasInit = false;
    private volatile static DbManager instance = null;
    private static DaoSession daoSession;
    private static SQLiteDatabase db;

    private DbManager() {
    }

    public static void init(Application application) {
        if (!hasInit) {
            MySqlLiteOpenHelper mHelper = new MySqlLiteOpenHelper(application, "saihub", null);
            db = mHelper.getWritableDatabase();
            daoSession = new DaoMaster(db).newSession();
            daoSession.clear();
            hasInit = true;
        }
    }

    public static DbManager getInstance() {
        if (!hasInit) {
            throw new InitException("ARouter::Init::Invoke init(context) first!");
        } else {
            if (instance == null) {
                synchronized (DbManager.class) {
                    if (instance == null) {
                        instance = new DbManager();
                    }
                }
            }
            return instance;
        }
    }

    public DaoSession getDaoSession() {
        if (daoSession == null) {
            hasInit = false;
            init((SaiHubApplication.Companion.getInstance()));
        }
        return daoSession;
    }


    public SQLiteDatabase getDb() {
        if (db == null) {
            hasInit = false;
            init((SaiHubApplication.Companion.getInstance()));
        }
        return db;
    }
}
