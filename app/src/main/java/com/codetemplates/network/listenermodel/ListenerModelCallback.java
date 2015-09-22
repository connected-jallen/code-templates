package com.codetemplates.network.listenermodel;

/**
 * Used by ListenerModel to help subclasses of ListenerModel.
 */
public interface ListenerModelCallback<T> {

    void callback(T listener);
}
