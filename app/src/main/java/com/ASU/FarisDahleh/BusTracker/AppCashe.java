package com.ASU.FarisDahleh.BusTracker;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;


public class AppCashe extends Application {

    protected static AppCashe application;
    private SharedPreferences preferences;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onCreate() {
        super.onCreate();
        application = this;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static AppCashe getInstance() {
        return application;
    }

    public SharedPreferences getPreferences() {
        return this.preferences;
    }


}
