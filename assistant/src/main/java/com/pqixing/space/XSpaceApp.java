package com.pqixing.space;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;

import com.pqixing.space.utils.Constans;


public class XSpaceApp extends Application {
    public static  Application app;
    public static Handler uiHandler;
    public static final String TAG = "ShellApp";
    private static SharedPreferences defSp;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        uiHandler = new Handler();
    }

    public static SharedPreferences getDefSp() {
        if (defSp == null) synchronized (TAG) {
            if (defSp == null) defSp = app.getSharedPreferences(Constans.SP_FILE_NAME, 0);
        }
        return defSp;
    }
}
