package com.connectedlab.templates.network.volley;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.http.AndroidHttpClient;
import android.os.Build;

import com.android.volley.Network;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.DiskBasedCache;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.HurlStack;

import com.codetemplates.BuildConfig;
import com.connectedlab.templates.logging.LogUtil;

import java.io.File;

/**
 * Works around bug in Volley that sometimes crashes disk cache on initialize.
 */
public class SafeRequestQueue {

    private static final String DEFAULT_CACHE_DIR = "volley";

    /**
     * Creates a default instance of the worker pool and calls {@link RequestQueue#start()} on it.
     *
     * @param context A {@link Context} to use for creatinithe cache dir.
     * @param stack   An {@link HttpStack} to use for the network, or null for default.
     * @return A started {@link RequestQueue} instance.
     */
    public static RequestQueue newRequestQueue(Context context, HttpStack stack) {
        File cacheDir = new File(context.getCacheDir(), DEFAULT_CACHE_DIR);

        String userAgent = "volley/0";
        try {
            String packageName = context.getPackageName();
            PackageInfo info = context.getPackageManager().getPackageInfo(packageName, 0);
            userAgent = packageName + "/" + info.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
        }

        if (stack == null) {
            if (Build.VERSION.SDK_INT >= 9) {
                stack = new HurlStack();
            }
            else {
                // Prior to Gingerbread, HttpUrlConnection was unreliable.
                // See: http://android-developers.blogspot.com/2011/09/androids-http-clients.html
                stack = new HttpClientStack(AndroidHttpClient.newInstance(userAgent));
            }
        }

        Network network = new BasicNetwork(stack);
        RequestQueue queue = new RequestQueue(new SafeDiskCache(cacheDir, BuildConfig.VOLLEY_CACHE_SIZE), network);
        queue.start();

        return queue;
    }

    /**
     * Ensures that initialize always works, sometimes it crashes on corrupt data.
     */
    static class SafeDiskCache extends DiskBasedCache {

        public SafeDiskCache(File rootDirectory, int maxCacheSizeInBytes) {
            super(rootDirectory, maxCacheSizeInBytes);
        }

        public SafeDiskCache(File rootDirectory) {
            super(rootDirectory);
        }

        @Override
        public synchronized void initialize() {
            try {
                super.initialize();
            } catch (Throwable ex) {
                LogUtil.i("Could not initialize, retrying after clear", ex);
                clear();
                super.initialize();
            }
        }

        /**
         * Logs when we have a cache hit.
         */
        @Override
        public Entry get(String key) {
            Entry entry = super.get(key);
            if (entry != null) {
                String expired = entry.isExpired() ? " (expired)" : entry.refreshNeeded() ? " (refresh needed)" : "";
                int length = entry.data != null ? entry.data.length : 0;
                LogUtil.d("Returning cache%s, %s bytes for %s", expired, length, key);
            }
            return entry;
        }
    }
}
