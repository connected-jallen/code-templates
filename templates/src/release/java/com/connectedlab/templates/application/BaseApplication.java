package com.connectedlab.templates.application;

import android.app.Application;

import com.connectedlab.templates.inject.ApplicationModule;

/**
 * BaseApplication customized for release builds.
 * Different from debug builds in that it does not inherit from MultiDexApplication.
 *
 * Sets ApplicationModule instance.
 */
public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        ApplicationModule.setInstance(new ApplicationModule(this));
    }
}
