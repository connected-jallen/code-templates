package com.codetemplates.error;

import com.codetemplates.BuildConfig;
import com.codetemplates.logging.LogUtil;

/**
 * Warning: SHOULD BE USED RARELY, fail-fast should be the more common approach.
 *
 * Allows different error strategies in debug and release builds.
 * Should be used for those rare circumstances where you would rather suppress an exception then crash in release
 * builds.
 * Debug builds crash to support a fail-fast error strategy.
 *
 * @see <a href="https://en.wikipedia.org/wiki/Fail-fast">Fail-fast</a>
 */
public class ErrorStrategy {

    /**
     * Logs an error in release builds.
     *
     * @throws RuntimeException in DEBUG builds.
     */
    public void suppressError(String message, Throwable ex) {
        if (BuildConfig.DEBUG) {
            // Log as well in case a thread pool is not logging uncaught exceptions.
            LogUtil.e("Throwing error instead of suppressing: %s", message, ex);
            throw new RuntimeException(message, ex);
        }
        LogUtil.e(message, ex);
    }
}
