package com.codetemplates.application;

import android.app.Application;

/**
 * BaseApplication customized for release builds.
 * Different from debug builds in that it does not inherit from MultiDexApplication.
 */
public class BaseApplication extends Application {
}
