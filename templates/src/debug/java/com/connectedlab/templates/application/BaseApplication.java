package com.connectedlab.templates.application;

import android.support.multidex.MultiDexApplication;

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
}
