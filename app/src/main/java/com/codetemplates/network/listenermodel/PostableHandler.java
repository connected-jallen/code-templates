package com.codetemplates.network.listenermodel;

import android.os.Handler;
import android.os.Looper;

/**
 * Handler that implements Postable.
 */
public class PostableHandler extends Handler implements Postable {

    /**
     * Used to run a task on the UI Thread when you do not have easy access to Activity.runOnUiThread().
     */
    public static PostableHandler UI_THREAD = new PostableHandler(Looper.getMainLooper());

    public PostableHandler(Looper looper) {
        super(looper);
    }

    /**
     * If the current thread is the same as the handler than the task is executed immediately; otherwise it is posted
     * as normal.
     */
    public boolean postImmediately(Runnable task) {
        if (Thread.currentThread() == getLooper().getThread()) {
            task.run();
            return true;
        } else {
            return post(task);
        }
    }

}
