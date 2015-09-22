package com.codetemplates.application;

import com.codetemplates.BuildConfig;
import com.codetemplates.logging.LogUtil;

/**
 * Application for app.
 */
public class CodeTemplateApplication extends BaseApplication {

    private static CodeTemplateApplication mInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtil.d("Creating %s with debug %s", this, BuildConfig.DEBUG);
        mInstance = this;
    }

    /**
     * @return ApplicationContext that can be used when no UI is needed.
     *         For example getResources is okay, but creating Toasts is not.
     */
    public static CodeTemplateApplication getApplication() {
        return mInstance;
    }
}
