package com.example.pickfile;

import android.app.Application;

public class PickFileApplication extends Application {
    private static PickFileApplication singleton;

    public static PickFileApplication getInstance(){
        return singleton;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }

}
