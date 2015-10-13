package com.connectedlab.templates.inject;

import android.support.annotation.NonNull;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;

/**
 * Injects SSLContext so it can be swapped out during testing.
 */
public class SSLModule {

    private static SSLModule sInstance = new SSLModule();

    public static synchronized SSLModule getInstance() {
        if (sInstance == null) {
            return sInstance = new SSLModule();
        }
        return sInstance;
    }

    public static synchronized void setInstance(@NonNull SSLModule instance) {
        sInstance = instance;
    }

    public SSLContext getContext(String algorithm, KeyManager[] km, TrustManager[] tm, SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(km, tm, random);
        return context;
    }
}
