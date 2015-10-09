package com.connectedlab.templates.inject;

import android.support.annotation.NonNull;

import com.android.volley.RequestQueue;
import com.connectedlab.templates.network.volley.SafeRequestQueue;

/**
 * Created by cl-dev1 on 15-10-02.
 */
public class RequestModule {
    private static RequestModule sInstance = null;
    private RequestQueue mRequestQueue;

    private RequestModule() {
        mRequestQueue = SafeRequestQueue.newRequestQueue(ApplicationModule.getInstance().getApplication(), null);
    }

    public static @NonNull RequestModule getInstance() {
        if (sInstance == null){
            sInstance = new RequestModule();
        }
        return sInstance;
    }

    public RequestQueue getRequestQueue(){
        return mRequestQueue;
    }
}
