package com.connectedlab.templates.network.listenermodel;

import com.connectedlab.templates.error.ErrorInfo;

/**
 * Listener for async requests.
 */
public interface AsyncListener<T> {

    /**
     * Request succeeded with a result.
     * @throws Exception If there was a error processing the request.
     */
    void onSuccess(Object request, T result) throws Exception;

    /**
     * The request failed, the parsing failed, or one of the callbacks (onSuccess/onError/onNoData) threw an exception.
     */
    void onError(Object request, ErrorInfo error);

}
