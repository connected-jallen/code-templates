package com.connectedlab.templates.network.listenermodel;

import com.codetemplates.R;
import com.connectedlab.templates.error.ErrorInfo;

/**
 * Supports add/remove other AsyncListeners and also calling back to those listeners on a specific thread.
 */
public class AsyncListenerModel<T> extends ListenerModel<AsyncListener> implements AsyncListener<T> {

    public AsyncListenerModel() {
    }

    public AsyncListenerModel(Postable postable) {
        super(postable);
    }

    @Override
    public void onSuccess(final Object request, final T result) throws Exception {
        post(new ListenerModelCallback<AsyncListener<T>>() {
           public void callback(AsyncListener<T> t) {
               try {
                   t.onSuccess(request, result);
               } catch (Throwable ex) {
                   t.onError(request, new ErrorInfo.Builder()
                           .setErrorCode(R.id.errorCode_onSuccessFailure)
                           .setContextData(String.valueOf(result))
                           .setException(ex)
                           .build());
               }
           }
        });
    }

    @Override
    public void onError(final Object request, final ErrorInfo error) {
        post(new ListenerModelCallback<AsyncListener<T>>() {
            public void callback(AsyncListener<T> t) {
                t.onError(request, error);
            }
        });
    }

}
