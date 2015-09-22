package com.codetemplates.application;

import android.support.multidex.MultiDexApplication;

/**
 * BaseApplication customized for debug builds.
 * Different from release builds in that it inherits from MultiDexApplication.
 */
public class BaseApplication extends MultiDexApplication {
}
