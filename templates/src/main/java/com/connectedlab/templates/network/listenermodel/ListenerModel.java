package com.connectedlab.templates.network.listenermodel;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.Validate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Supports add/remove other listeners of type T and also calling back to those listeners on a specific thread.
 */
public class ListenerModel<T> implements PostableContainer {

    private Postable mPostable;
    private Set<T> mListeners = Collections.synchronizedSet(new HashSet<T>());

    public ListenerModel(Postable postable) {
        mPostable = postable;
    }

    public ListenerModel() {
    }

    public void setPostable(Postable postable) {
        mPostable = postable;
    }

    /**
     * @param listener Listener to add. No-op if listener has already been added.
     * @return {@code true} if this set is modified, {@code false} otherwise.
     * @throws NullPointerException If listener is null.
     */
    public boolean addListener(T listener) {
        Validate.notNull(listener, "listener");
        return mListeners.add(listener);
    }

    /**
     * @return {@code true} if this set is modified, {@code false} otherwise.
     * @param listener Listener to remove. No-op if listener is null or has already been removed.
     */
    public boolean removeListener(T listener) {
        return mListeners.remove(listener);
    }

    /**
     * @return Listeners that have been added so far.
     * List is an immutable copy so calling addListener() will not change the list once it has been returned.
     */
    protected List<T> getListeners() {
        return Collections.unmodifiableList(new ArrayList<T>(mListeners));
    }

    protected void post(final ListenerModelCallback callback) {
        for (final T listener : getListeners()) {
            Postable postTo;
            if (listener instanceof PostableContainer) {
                postTo = ObjectUtils.firstNonNull(((PostableContainer) listener).getPostable(), mPostable);
            }
            else {
                postTo = mPostable;
            }
            if (postTo == null) {
                callback.callback(listener);
            }
            else {
                postTo.postImmediately(new Runnable() {
                    @Override
                    public void run() {
                        callback.callback(listener);
                    }
                });
            }
        }
    }

    @Override
    public Postable getPostable() {
        return mPostable;
    }
}


