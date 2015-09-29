package com.connectedlab.templates.network.volley;

import android.os.Bundle;

import com.android.volley.VolleyError;

import com.connectedlab.templates.error.ErrorInfo;

/**
 * VolleyError that has an ErrorInfo.
 */
class VolleyErrorInfo extends VolleyError {

    private final ErrorInfo mError;

    public VolleyErrorInfo(String url, ErrorInfo error) {
        Bundle data = new Bundle();
        data.putString("url", url);
        data.putParcelable("caused_by", error);
        mError = new ErrorInfo.Builder()
                .setContextData(data)
                .build();
    }

    public VolleyErrorInfo(String url, Exception ex) {
        Bundle data = new Bundle();
        data.putString("url", url);
        mError = new ErrorInfo.Builder()
                .setContextData(data)
                .setException(ex)
                .build();
    }

    public ErrorInfo getError() {
        return mError;
    }

    @Override
    public String toString() {
        return mError.toString();
    }
}
