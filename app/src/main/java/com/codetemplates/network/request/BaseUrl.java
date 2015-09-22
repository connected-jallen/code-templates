package com.codetemplates.network.request;


import android.support.annotation.NonNull;

import java.util.Map;

/**
 * Server URL and common parameters/headers. Used by VolleyRequestBase to create HTTP request.
 */
public interface BaseUrl {

    /**
     * @return Base URL before any request specific paths are added
     */
    @NonNull
    String getBaseUrl();

    /**
     * @return Query parameters that are added to the query string for GET requests and body for POST requests.
     */
    @NonNull
    Map<String,String> getQueryParams();

    /**
     * @return HTTP Headers that should be sent.
     */
    @NonNull
    Map<String,String> getHeaders();

    /**
     * @return In this time the cache expires completely.
     *         If this or cacheHitButRefresh is 0 than the cache headers are used instead.
     */
    long getCacheExpire();

    /**
     * @return In this time the cache will be hit, but also refreshed in the background.
     *         If this or cacheExpire is 0 than the cache headers are used instead.
     */
    long getSoftCacheExpire();

}
