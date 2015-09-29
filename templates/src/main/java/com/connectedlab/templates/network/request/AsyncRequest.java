package com.connectedlab.templates.network.request;

import com.connectedlab.templates.network.listenermodel.AsyncListener;

/**
 * A request to a server, such as for ChannelSchedules or for Listings.
 */
public interface AsyncRequest<T> {

    boolean addListener(AsyncListener<? super T> listener);

    boolean removeListener(AsyncListener<? super T> listener);

    /**
     * Makes an async request and calls any added listeners with success/failure info.
     */
    public void execute();

}
