package com.connectedlab.templates.application;

import android.content.Context;
import android.support.multidex.MultiDexApplication;

import com.connectedlab.templates.inject.ApplicationModule;

/**
 * BaseApplication customized for debug builds.
 * Different from release builds in that it inherits from MultiDexApplication.
 *
 * Sets ApplicationModule instance.
 */
public class BaseApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationModule.setInstance(new ApplicationModule(this));
    }
    @Override protected void attachBaseContext(Context base) {
        try {
            super.attachBaseContext(base);
        } catch (RuntimeException ignored) {
            // Multidex support doesn't play well with Robolectric yet
        }
    }
}
