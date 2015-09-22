package com.codetemplates.network.volley;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.Cache;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;

import com.codetemplates.R;
import com.codetemplates.error.ErrorInfo;
import com.codetemplates.logging.LogUtil;
import com.codetemplates.network.listenermodel.AsyncListener;
import com.codetemplates.network.listenermodel.AsyncListenerModel;
import com.codetemplates.network.request.AsyncRequest;
import com.codetemplates.network.request.BaseUrl;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Base class for every HTTP request that will be made. Every request will have its own class.
 */
public abstract class VolleyRequestBase<T> implements AsyncRequest<T> {

    private final BaseUrl mBaseUrl;

    private final RequestQueue mQueue;
    private final String mPath;

    private AsyncListenerModel<T> mListeners = new AsyncListenerModel<T>();
    private List<String> mFields = new ArrayList<String>();
    private WeakReference<Fragment> mCancelIfFinished;

    public VolleyRequestBase(BaseUrl baseUrl, String path, RequestQueue queue) {
        Validate.notNull(baseUrl, "baseUrl");
        Validate.notNull(queue, "queue");
        mBaseUrl = baseUrl;
        mQueue = queue;
        mPath = path;
    }

    @Override
    public boolean addListener(AsyncListener<? super T> listener) {
        return mListeners.addListener(listener);
    }

    /**
     * @param fragment Holds a weak reference to the fragment, and if the fragments activity is null or finishing then
     *                 the listeners are not called back.
     */
    public void cancelIfFinished(Fragment fragment) {
        mCancelIfFinished = new WeakReference<Fragment>(fragment);
    }

    @Override
    public boolean removeListener(AsyncListener<? super T> listener) {
        return mListeners.removeListener(listener);
    }

    protected String getUrl(boolean includeParams) {
        StringBuilder url = new StringBuilder(mBaseUrl.getBaseUrl());
        if (mPath != null) {
            if (mBaseUrl.getBaseUrl().endsWith("/") && mPath.startsWith("/")) {
                // Some web servers cannot handle duplicate slashes
                url.append(mPath.substring(1));
            }
            else {
                url.append(mPath);
            }
        }
        if (includeParams) {
            String params = encodeParameters(getParams());
            if (!StringUtils.isEmpty(params)) {
                url.append('?' + params);
            }
        }
        return url.toString();
    }

    @Override
    public void execute() {
        Response.ErrorListener onError = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (isErrorLoggable(error)) {
                    LogUtil.e("%s url %s error",
                            VolleyRequestBase.this.getClass().getSimpleName(),
                            StringUtils.abbreviate(getUrl(true), 100), error);
                }
                notifyError(error);
            }
        };
        LogUtil.d("Requesting method %s URL %s", getMethod(), getUrl(true));
        final String url = getUrl(getMethod() == Request.Method.GET);
        Request<T> request = new Request<T>(getMethod(), url, onError) {
            private byte[] mLastNotifiedData = null;

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                return VolleyRequestBase.this.getHeaders();
            }

            @Override
            public Map<String, String> getParams() throws AuthFailureError {
                return VolleyRequestBase.this.getParams();
            }

            @Override
            protected Response<T> parseNetworkResponse(NetworkResponse response) {
                if (Arrays.equals(mLastNotifiedData, response.data)) {
                    return Response.error(new AlreadyLoadedError());
                }
                mLastNotifiedData = response.data;
                return VolleyRequestBase.this.parseNetworkResponse(response);
            }

            @Override
            protected void deliverResponse(T response) {
                try {
                    notifySuccess(response);
                }
                catch (Exception ex) {
                    if (isFinished()) {
                        LogUtil.d("Activity has already finished, not showing error", ex);
                        return;
                    }
                    mListeners.onError(this, new ErrorInfo.Builder()
                            .setErrorCode(R.id.errorCode_onSuccessFailure)
                            .setException(ex)
                            .build());
                }
            }

            @Override
            public String getCacheKey() {
                return VolleyRequestBase.this.getUrl(true);
            }
        };
        if (mBaseUrl.getCacheExpire() == 0 || mBaseUrl.getSoftCacheExpire() == 0) {
            request.setShouldCache(false);
        }
        // Note: Volley by default uses DefaultRetryPolicy with one retry.
        mQueue.add(request);
    }

    protected boolean isErrorLoggable(VolleyError error) {
        if (error instanceof AlreadyLoadedError) {
            return false;
        }
        if (error instanceof VolleyErrorInfo) {
            VolleyErrorInfo info = (VolleyErrorInfo) error;
            if (info.getError().hasRootErrorCode(R.id.errorCode_noData)) {
                return false;
            }
        }
        return true;
    }

    private Response<T> parseNetworkResponse(NetworkResponse response) {
        String string;
        try {
            string = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
        } catch (UnsupportedEncodingException e) {
            string = new String(response.data);
        }
        try {
            ErrorInfo error = checkForErrorMessage(string);
            if (error != null) {
                return Response.error(new VolleyErrorInfo(getUrl(true), error));
            }
            T parsed = parseResponse(string);
            if (isParsedResponseEmpty(parsed)) {
                ErrorInfo noData = new ErrorInfo.Builder()
                        .setContextData(string)
                        .setErrorCode(R.id.errorCode_noData)
                        .setDebugMessage("No data")
                        .build();
                return Response.error(new VolleyErrorInfo(getUrl(true), noData));
            }
            return Response.success(parsed, parseCacheHeaders(response));
        } catch (Exception ex) {
            return Response.error(new VolleyErrorInfo(getUrl(true), ex));
        }
    }

    /**
     * @return null. Subclasses should override to return an ErrorInfo if the HTTP success (HTTP code == 200) is
     * actually an error. This allows for maintaining complex error information from the server if required for
     * programmatic processing.
     * @throws Exception If the response is completely broken, or example if it is not JSON.
     */
    protected ErrorInfo checkForErrorMessage(@SuppressWarnings("unused") String response) throws Exception {
        return null;
    }

    /**
     * @return true if the value returned from parseResponse should be considered empty and onError(errorCode_noData)
     * should be sent to listeners. false if parsed response should be sent to onSuccess.
     * This version checks if null or is an empty Collection.
     */
    protected boolean isParsedResponseEmpty(T parsed) {
        return parsed == null || (parsed instanceof Collection && ((Collection) parsed).isEmpty());
    }

    protected int getMethod() {
        return Request.Method.GET;
    }

    /**
     * Notify listener. Called from main thread.
     */
    protected void notifySuccess(T response) throws Exception {
        if (isFinished()) {
            LogUtil.d("Activity has already finished, not showing response");
            return;
        }
        mListeners.onSuccess(this, response);
    }

    /**
     * Called when Volley returns onSuccess. Any listeners are called with the return value of the method.
     *
     * @param json Result returned from server.
     * @return Model object that the string maps to, or null if no data was returned.
     * @throws Exception If the response could not be converted to a T. In this case the listeners onError will be
     *                   called.
     */
    protected abstract T parseResponse(String json) throws Exception;

    private void notifyError(VolleyError error) {
        ErrorInfo info;
        if (error instanceof AlreadyLoadedError) {
            // Already loaded this data, just refreshing from cache from softTTL;
            return;
        }
        if (isFinished()) {
            LogUtil.d("Activity has already finished, not showing error", error);
            return;
        }
        else if (error instanceof VolleyErrorInfo) {
            info = ((VolleyErrorInfo) error).getError();
        }
        else {
            info = vollyErrorToErrorInfo(error);
        }
        try {
            mListeners.onError(this, info);
        } catch (RuntimeException ex) {
            // Otherwise we will crash.
            LogUtil.e("Could not show error", info, ex);
        }
    }

    private ErrorInfo vollyErrorToErrorInfo(VolleyError error) {
        // Serialize VolleyError into a Bundle rather than using it directly as it contains NetworkResponse which is
        // not serializable.
        ErrorInfo.Builder builder = new ErrorInfo.Builder();
        Bundle data = new Bundle();
        if (error.networkResponse != null) {
            builder.setErrorCode(R.id.errorCode_serverError);
            data.putInt("statusCode", error.networkResponse.statusCode);
            data.putByteArray("data", error.networkResponse.data);
            if (error.networkResponse.headers != null) {
                data.putSerializable("headers", new HashMap<String, String>(error.networkResponse.headers));
            }
            data.getBoolean("notModified", error.networkResponse.notModified);
            data.putLong("networkTimeMs", error.networkResponse.networkTimeMs);
        }
        else {
            builder.setErrorCode(R.id.errorCode_noConnection);
        }
        data.putSerializable("exceptionClass", error.getClass());
        data.putString("stackTrace", ExceptionUtils.getStackTrace(error));
        data.putString("url", getUrl(true));
        builder.setContextData(data);
        return builder.build();
    }

    private boolean isFinished() {
        if (mCancelIfFinished == null) {
            return false;
        }
        Fragment fragment = mCancelIfFinished.get();
        if (fragment == null) {
            return true;
        }
        Activity activity = fragment.getActivity();
        return activity == null || activity.isFinishing();
    }

    protected Map<String, String> getParams() {
        Map<String, String> params = new HashMap<String, String>(mBaseUrl.getQueryParams());
        if (!mFields.isEmpty()) {
            params.put("fields", StringUtils.join(mFields, "|"));
        }
        return params;
    }

    protected Map<String, String> getHeaders() {
        Map<String, String> headers = new HashMap<String, String>(mBaseUrl.getHeaders());
        if (getMethod() == Request.Method.POST) {
            headers.put("Content-Type", "application/x-www-form-urlencoded");
        }
        return headers;
    }

    /**
     * Converts <code>params</code> into an application/x-www-form-urlencoded encoded string.
     */
    private String encodeParameters(Map<String, String> params) {
        try {
            StringBuilder encodedParams = new StringBuilder();
            // Sort the parameters so it's easier to compare URLs.
            SortedSet<Map.Entry<String, String>> sorted = new TreeSet<Map.Entry<String, String>>(
                    new Comparator<Map.Entry<String, String>>() {
                        @Override
                        public int compare(Map.Entry<String, String> lhs, Map.Entry<String, String> rhs) {
                            return ObjectUtils.compare(lhs.getKey(), rhs.getKey());
                        }
                    });
            sorted.addAll(params.entrySet());
            for (Map.Entry<String, String> entry : sorted) {
                if (encodedParams.length() > 0) {
                    encodedParams.append('&');
                }
                encodedParams.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
                encodedParams.append('=');
                encodedParams.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
            }
            return encodedParams.toString();
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex);
        }
    }

    /**
     * @see <a href="http://stackoverflow.com/a/16852314/1596587">Modified from: Android Volley + JSONObjectRequest Caching</a>
     */
    public Cache.Entry parseCacheHeaders(NetworkResponse response) {
        long now = System.currentTimeMillis();

        Map<String, String> headers = response.headers;
        long serverDate = 0;
        String serverEtag;
        String headerValue;

        headerValue = headers.get("Date");
        if (headerValue != null) {
            serverDate = HttpHeaderParser.parseDateAsEpoch(headerValue);
            if (serverDate == 0) {
                LogUtil.d("Invalid Date from response header", headerValue);
            }
        }

        serverEtag = headers.get("ETag");

        final long softExpire = now + mBaseUrl.getSoftCacheExpire();
        final long ttl = now + mBaseUrl.getCacheExpire();

        Cache.Entry entry = new Cache.Entry();
        entry.data = response.data;
        entry.etag = serverEtag;
        entry.softTtl = softExpire;
        entry.ttl = ttl;
        entry.serverDate = serverDate;
        entry.responseHeaders = headers;

        return entry;
    }
}
