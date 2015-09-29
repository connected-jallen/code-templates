package com.connectedlab.templates.network.request;

import android.support.annotation.NonNull;

import org.apache.commons.lang3.Validate;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * BaseUrl that does not change once created and has no parameters or headers.
 */
public  class SimpleBaseUrl implements BaseUrl {

    private static long DEFAULT_CACHE_EXPIRE = TimeUnit.MILLISECONDS.convert(1, TimeUnit.DAYS);
    private static long DEFAULT_SOFT_CACHE_EXPIRE = TimeUnit.MILLISECONDS.convert(3, TimeUnit.MINUTES);

    private final String mBaseUrl;
    private final long mCacheExpire;
    private final long mSoftCacheExpire;

    public SimpleBaseUrl(String baseUrl) {
        this(baseUrl, DEFAULT_CACHE_EXPIRE, DEFAULT_SOFT_CACHE_EXPIRE);
    }

    public SimpleBaseUrl(String baseUrl, long cacheExpire, long softCacheExpire) {
        Validate.notNull(baseUrl, "baseUrl");
        mBaseUrl = baseUrl;
        mCacheExpire = cacheExpire;
        mSoftCacheExpire = softCacheExpire;
    }

    @NonNull
    @Override
    public String getBaseUrl() {
        return mBaseUrl;
    }

    @NonNull
    @Override
    public Map<String, String> getQueryParams() {
        return Collections.emptyMap();
    }

    @NonNull
    @Override
    public Map<String, String> getHeaders() {
        return Collections.emptyMap();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCacheExpire() {
        return mCacheExpire;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getSoftCacheExpire() {
        return mSoftCacheExpire;
    }
}
