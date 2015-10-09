package com.connectedlab.templates.sample.application;

import com.connectedlab.templates.application.BaseApplication;
import com.connectedlab.templates.logging.LogUtil;
import com.connectedlab.templates.sample.BuildConfig;

/**
 * Application for app.
 */
public class SampleApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("Creating %s with debug %s", this, BuildConfig.DEBUG);
    }

}
