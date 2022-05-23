package com.linktech.saihub.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.github.yuweiguocn.library.greendao.MigrationHelper;
import com.linktech.saihub.greendao.ChildAddressBeanDao;
import com.linktech.saihub.greendao.DaoMaster;
import com.linktech.saihub.greendao.PollBeanDao;
import com.linktech.saihub.greendao.PowerBeanDao;
import com.linktech.saihub.greendao.TokenInfoBeanDao;
import com.linktech.saihub.greendao.TransferServerBeanDao;
import com.linktech.saihub.greendao.WalletAddressBeanDao;
import com.linktech.saihub.greendao.WalletBeanDao;
import com.linktech.saihub.util.LogUtils;

import org.greenrobot.greendao.database.Database;


public class MySqlLiteOpenHelper extends DaoMaster.OpenHelper {

    public MySqlLiteOpenHelper(Context context, String name) {
        super(context, name);
    }

    public MySqlLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory) {
        super(context, name, factory);
    }

    @Override
    public void onCreate(Database db) {
        super.onCreate(db);
    }

    @Override
    public void onUpgrade(Database db, int oldVersion, int newVersion) {
        super.onUpgrade(db, oldVersion, newVersion);
        MigrationHelper.migrate(db, new MigrationHelper.ReCreateAllTableListener() {

                    @Override
                    public void onCreateAllTables(Database db, boolean ifNotExists) {
                        DaoMaster.createAllTables(db, ifNotExists);
                    }

                    @Override
                    public void onDropAllTables(Database db, boolean ifExists) {
                        DaoMaster.dropAllTables(db, ifExists);
                    }
                }, WalletBeanDao.class, TokenInfoBeanDao.class, ChildAddressBeanDao.class, PollBeanDao.class, TransferServerBeanDao.class
                , WalletAddressBeanDao.class, PowerBeanDao.class);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        super.onDowngrade(db, oldVersion, newVersion);
        LogUtils.e("MigrationHelper", "------onDowngrade--------");
    }
}
