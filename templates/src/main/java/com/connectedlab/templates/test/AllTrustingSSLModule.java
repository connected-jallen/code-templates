package com.connectedlab.templates.test;

import com.connectedlab.templates.inject.SSLModule;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * SSLContext for testing that trusts self-signed certs and does no host name verification.
 */
public class AllTrustingSSLModule extends SSLModule {

    public SSLContext getContext(String algorithm, KeyManager[] km, TrustManager[] tm, SecureRandom random)
            throws NoSuchAlgorithmException, KeyManagementException {

        SSLContext context = SSLContext.getInstance("TLS");

        // Trust self-signed certificates.
        TrustManager trustEveryone = new X509TrustManager() {

            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
            }

            public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        };
        try {
            context.init(null, new TrustManager[]{trustEveryone}, null);
        } catch (KeyManagementException ex) {
            throw new RuntimeException(ex);
        }

        // Allow localhost and anyone else.
        javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(new javax.net.ssl.HostnameVerifier() {

            public boolean verify(String hostname, javax.net.ssl.SSLSession sslSession) {
                return true;
            }
        });

        return context;
    }
}
