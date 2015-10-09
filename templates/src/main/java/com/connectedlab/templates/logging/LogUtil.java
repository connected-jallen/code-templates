package com.connectedlab.templates.logging;

import android.util.Log;

import com.connectedlab.templates.BuildConfig;
import com.connectedlab.templates.inject.ApplicationModule;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;

public class LogUtil {

    /**
     * Logs to the android logger at VERBOSE level.
     *
     * @param formatString printf style string used by String#format. Use %s for each argument for simplicity.
     * @param formatArgs   arguments that are used as replacements for formatString. If the last argument of this list
     *                     is a Throwable then do not include a placeholder for it in the formatString to have the
     *                     stack trace output.
     */
    public static void v(String formatString, Object... formatArgs) {
        androidUtilLog(Log.VERBOSE, formatString, formatArgs);
    }

    /**
     * Logs to the android logger at DEBUG level.
     *
     * @param formatString printf style string used by String#format. Use %s for each argument for simplicity.
     * @param formatArgs   arguments that are used as replacements for formatString. If the last argument of this list
     *                     is a Throwable then do not include a placeholder for it in the formatString to have the
     *                     stack trace output.
     */
    public static void d(String formatString, Object... formatArgs) {
        androidUtilLog(Log.DEBUG, formatString, formatArgs);
    }

    /**
     * Logs to the android logger at INFO level.
     *
     * @param formatString printf style string used by String#format. Use %s for each argument for simplicity.
     * @param formatArgs   arguments that are used as replacements for formatString. If the last argument of this list
     *                     is a Throwable then do not include a placeholder for it in the formatString to have the
     *                     stack trace output.
     */
    public static void i(String formatString, Object... formatArgs) {
        androidUtilLog(Log.INFO, formatString, formatArgs);
    }

    /**
     * Logs to the android logger at WARN level.
     *
     * @param formatString printf style string used by String#format. Use %s for each argument for simplicity.
     * @param formatArgs   arguments that are used as replacements for formatString. If the last argument of this list
     *                     is a Throwable then do not include a placeholder for it in the formatString to have the
     *                     stack trace output.
     */
    public static void w(String formatString, Object... formatArgs) {
        androidUtilLog(Log.WARN, formatString, formatArgs);
    }

    /**
     * Logs to the android logger at ERROR level.
     *
     * @param formatString printf style string used by String#format. Use %s for each argument for simplicity.
     * @param formatArgs   arguments that are used as replacements for formatString. If the last argument of this list
     *                     is a Throwable then do not include a placeholder for it in the formatString to have the
     *                     stack trace output.
     */
    public static void e(String formatString, Object... formatArgs) {
        androidUtilLog(Log.ERROR, formatString, formatArgs);
    }

    private static void androidUtilLog(int priority, String formatString, Object... formatArgs) {
        StringBuilder msg = new StringBuilder();
        if (BuildConfig.DEBUG) {
            StackTraceElement[] stack = Thread.currentThread().getStackTrace();
            if (stack != null && stack.length >= 5) {
                msg.append(formatStackTraceElement(stack[4]));
            }
        }
        Throwable ex = null;
        if (formatArgs != null
                && formatArgs.length > 0
                && formatArgs[formatArgs.length - 1] instanceof Throwable) {
            ex = (Throwable) formatArgs[formatArgs.length - 1];
            formatArgs = Arrays.copyOfRange(formatArgs, 0, formatArgs.length - 1);
        }
        if (StringUtils.countMatches(formatString, "%s") != formatArgs.length) {
            // Show entire message if %s is forgotten
            msg.append(String.valueOf(formatString)).append(' ').append(Arrays.toString(formatArgs));
        } else {
            try {
                msg.append(String.format(String.valueOf(formatString), formatArgs));
            } catch (RuntimeException formatException) {
                msg.append(String.valueOf(formatString)).append(Arrays.toString(formatArgs));
            }
        }

        if (ex != null) {
            msg.append("\n").append(Log.getStackTraceString(ex));
        }
        Log.println(priority, ApplicationModule.getInstance().getLogTag(), msg.toString());
    }

    private static String formatStackTraceElement(StackTraceElement ste) {
        StringBuffer sb = new StringBuffer();
        if (ste.getFileName() != null) {
            sb.append(ste.getFileName().replace(".java", ""));
        }
        sb.append(".");
        sb.append(ste.getMethodName());
        sb.append("(");
        sb.append(ste.getLineNumber());
        sb.append(") thread " + Thread.currentThread().getId() + "\t");
        return sb.toString();
    }

}
