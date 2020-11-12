package com.pqixing.shell.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Objects;

public class AppListener extends BroadcastReceiver {
    OnAppChange appChange;

    public AppListener setAppChange(OnAppChange appChange) {
        this.appChange = appChange;
        return this;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //接收安装广播
        if (Objects.equals(intent.getAction(), "android.intent.action.PACKAGE_ADDED")) {
            String packageName = intent.getDataString();
            if (appChange != null) appChange.onPackage(true, packageName);
            System.out.println("安装了:" + packageName + "包名的程序");
        }
        //接收卸载广播
        if (Objects.equals(intent.getAction(), "android.intent.action.PACKAGE_REMOVED")) {
            String packageName = intent.getDataString();
            if (appChange != null) appChange.onPackage(false, packageName);
            System.out.println("卸载了:" + packageName + "包名的程序");
        }
    }

    public static interface OnAppChange {
        void onPackage(boolean add, String pkg);
    }
}
