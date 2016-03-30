package com.connectedlab.templates.thread;

import com.connectedlab.templates.network.listenermodel.ThreadPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {

    private static ThreadPool mWorkPool;
    private static ScheduledExecutorService mScheduledPool;

    /**
     * @return Thread Pool that can be used for any worker tasks.
     * @see java.util.concurrent.Executors#newCachedThreadPool()
     */
    public static synchronized ThreadPool getWorkPool(){
        if (mWorkPool == null) {
            mWorkPool = new ThreadPool(0, Integer.MAX_VALUE,
                    60L, TimeUnit.SECONDS,
                    new SynchronousQueue<Runnable>());
        }
        return mWorkPool;
    }

    /**
     * @return Thread Pool that can be used for any scheduled tasks.
     */
    public static synchronized ScheduledExecutorService getScheduledPool() {
        if (mScheduledPool == null) {
            mScheduledPool = new ScheduledThreadPoolExecutor(1) {
                /**
                 * Use a work pool to run the actual command because if one of the scheduled tasks takes a long time it
                 * will delay the other tasks.
                 */
                @Override
                public ScheduledFuture<?> schedule(final Runnable command, long delay, TimeUnit unit) {
                    Runnable runnable = new Runnable() {
                        @Override
                        public void run() {
                            getWorkPool().execute(command);
                        }
                    };
                    return super.schedule(runnable, delay, unit);
                }
            };
        }
        return mScheduledPool;
    }

}
