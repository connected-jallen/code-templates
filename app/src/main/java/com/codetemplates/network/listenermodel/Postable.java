package com.codetemplates.network.listenermodel;

/**
 * Allows posting a task to a handler or a thread.
 */
public interface Postable {

    boolean post(Runnable task);

    /** Will run immediately if already being executed on the same thread as the handler. */
    boolean postImmediately(Runnable task);
}
