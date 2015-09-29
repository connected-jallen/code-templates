package com.connectedlab.templates.inject;

import android.app.Application;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

/**
 * Inject application context.
 */
public class ApplicationModule {

    private static ApplicationModule sInstance;
    private final Application mApplication;

    public static void setInstance(@NonNull ApplicationModule instance) {
        sInstance = instance;
    }

    public ApplicationModule(@NonNull Application application) {
        mApplication = application;
    }

    public static @NonNull ApplicationModule getInstance() {
        Validate.notNull(sInstance, "Call setInstance in your application.onCreate()");
        return sInstance;
    }

    public @NonNull Application getApplication() {
        return mApplication;
    }
}
