package com.faceunity.greendao;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

/**
 * Created by tujh on 2018/3/12.
 */
public class GreenDaoUtils {
    private volatile static GreenDaoUtils sGreenDaoUtils;

    private DaoSession mDaoSession;

    private GreenDaoUtils(Context context) {
        DbOpenHelper openHelper = new DbOpenHelper(context, "fulive.db");
        SQLiteDatabase database = openHelper.getWritableDatabase();
        DaoMaster daoMaster = new DaoMaster(database);
        mDaoSession = daoMaster.newSession();
    }

    public static GreenDaoUtils initGreenDao(Context context) {
        if (sGreenDaoUtils == null) {
            synchronized (GreenDaoUtils.class) {
                if (sGreenDaoUtils == null) {
                    sGreenDaoUtils = new GreenDaoUtils(context);
                }
            }
        }
        return sGreenDaoUtils;
    }

    public static GreenDaoUtils getInstance() {
        return initGreenDao(null);
    }

    public DaoSession getDaoSession() {
        return mDaoSession;
    }
}
