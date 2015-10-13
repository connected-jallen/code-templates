package com.connectedlab.templates.test;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Socket Factory for a server that uses a self signed certificate.
 */
public class SelfSignedSSLSocketFactory extends SSLSocketFactory {

    /**
     * Base64 encoded JKS file of self-signed cert.
     * Created via:
     * keytool -genkey -keyalg RSA -alias unittest -keystore unittest.jks -validity 999999 -keysize 2048
     * With keystore and certificate passwords being "password"
     */
    private static final String KEYSTORE_FILE =
            "/u3+7QAAAAIAAAABAAAAAQAIdW5pdHRlc3QAAAFQI8QrUwAABQIwggT+MA4GCisGAQQBKgIRAQEFAASCBOrfwQVl7" +
                    "WuA4WcCgFxqMjN4BDWTOIt9nTNkTxnbqI1734TjZHg564ZPiPkvMWgr1mWnqzjqIAsgGxW5vAeo3BmuZS" +
                    "FkS1cE+J+yndmA2rcFKQrOC/c4Rz2AqUXWnlFj49ZtqSF0YKdvgoUGNfhxewjw0lTIRegYPMwZQ3Yue5E" +
                    "ioc3RpdTG7LgomusFxq1onobC8OwYU6iLSfToxGptrbWstL6SlJU/vZ+V2ENv998NkWKvZ5AoERoc01BF" +
                    "G598/A0/SZ9t0pPf7fvmWy1lDdAyvh3zOlQ/8rZNQA6UP840CstEll/9nW0yrvRlUxLilbQC44KQFhOes" +
                    "2/ezAcN6hTV1O4wo7xt9q+tdgGw7cf35MYR1LPTMzaq5y8pRBuBH6CdXBXx5XEE8I9M2f43FyJIma2PJt" +
                    "lFmKyoUjoIQ4jvRFiznBHhYK1luEBAxq0YImuXM9zCV5etytbu6t4dQw7o9NQ/hG7xlMVQCDdXkeYckwa" +
                    "SU/wxH+OW/3v7TTHxEx9+MZC8Txprl1X2T/z68beK/Vr1du5Dh5kNcv2ge1VWaduuZrUsHMMjAIYwf+2f" +
                    "xDew3Nhfnk0BlCFbCbltfgvB2c/ODGMBxNllG/tbI8lAb2/na1epaw22AiGPgIbOwqykiP5urfw13v/6g" +
                    "/CkO3+H3XaVA1GcIbHxRbX9PUdtGnlFmvFE5pnjTQsb2BQYWvP/z+ZvQw7NePuVxSKrZZ40ZCvKKyJyo5" +
                    "sIwsLpNFPG8Ymru6DCDNqAk5zHjJ3NoUYP+USOXIhQn3LeHOhuF6eNE2ga+vYIjoJ4t+E3sxqgwuPkirU" +
                    "TtglH+8yJUSBbiyvHmsYG5mikPzmvFpCD/9y04DX43qmqa1RlK7BG29I1WELXwUbkEk71btyvOsX7CPlV" +
                    "h5X2XjcQqUrbJWBNapulROeNz+wZzYvSZxFqzOECJzjyXsmpu2xi1G33sK/LCPhzwjhdwCPGI03x3oS7J" +
                    "x+ZdmB78QiYLJgsmoQbuPTLcNr2Te0tDyS9bd7a3WgGQtcWmAv3/YpB2a82v2lZfO5LfVTkpnuwROzXSU" +
                    "P3NQO+VEAaRTColP/zbQW3OfTSRunFDZNMLjL57wyly2FQDA+XkF1dWUYlyt7J3aB4QAw4al3C4w1NE7S" +
                    "39jBlgrxfY88/ZvR1kfrAse/hcOkBLk/X3rmIEp/kqkSqAJ8fS2DIuMmRYO9ms9O2Ty7v1KT8apFJdu9h" +
                    "HxmiCpJGOPF+XwQp9qRJvDB41SXEAdbi4xX9Q7dlen2fIkhbpYLKuEUDlWxQ0WinHl3M4RTXRKsBoZ3ZV" +
                    "hk2D7dL/haCCeSI5D6Q5Q6j6QA9Ym4t9qu7mK73pILfE1/RVKLOeAo0tx3NWbqrFHfpf9xODVvKg6PO8Q" +
                    "4sEXybJGnlzjyb9wIIqrxUc9iRgoJNpgKRBnpk16UCkEWuFs9YQcNzfDj01YPa/IICGJ+ETMbE6unHM+x" +
                    "v71yaljWwGYJULK5hMAO9dHeYfdpSbG4NBqMj8xh42vVkAa41PH6JaoW8qlyDHggGiyExQeWddAbXBE7a" +
                    "9Yldeq1Zwt8Hnl0tAbPtHYtJccpr8W7GJUJV07pX1qjDhjeBckmyGissIIrEJuK7+tXR7/OqiGkdVfJRg" +
                    "hXjuUiMidSvmjnEXWKY2/51SH6ueI0rHIsVFyHSe4Mj6iV5l/udAAAAAQAFWC41MDkAAANtMIIDaTCCAl" +
                    "GgAwIBAgIEJyekozANBgkqhkiG9w0BAQsFADBkMQswCQYDVQQGEwJDQTEQMA4GA1UECBMHVW5rbm93bjE" +
                    "QMA4GA1UEBxMHVW5rbm93bjEQMA4GA1UEChMHVW5rbm93bjEQMA4GA1UECxMHVGVzdGluZzENMAsGA1UE" +
                    "AxMEVW5pdDAgFw0xNTEwMDExNDE3NDRaGA80NzUzMDgyNzE0MTc0NFowZDELMAkGA1UEBhMCQ0ExEDAOB" +
                    "gNVBAgTB1Vua25vd24xEDAOBgNVBAcTB1Vua25vd24xEDAOBgNVBAoTB1Vua25vd24xEDAOBgNVBAsTB1" +
                    "Rlc3RpbmcxDTALBgNVBAMTBFVuaXQwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQCsM1xDXJP" +
                    "dLnknsmqMnusHEpsou5sE95SUeNCcWO8blF8C3tvDLJKq8MqgmSQX2hvUVehZha9BbH4/6YVVWrr+zj4e" +
                    "7fW1cd4fMx66vrEb8frfbF4x8fQsRbXPjP/O08n4PPe62luQMpxvaGkp3qBGFSnWUQFOJLyeo8WqD0Wmp" +
                    "j1p6enWaPqpLGxHUJpqdNxT5z/KD7Hnsd4aCJtPVBUWPQu4a0oWSO0TNQu3Pdi2AavwM/kaPYblcczNI7" +
                    "woGmuUEx0/8HUM+44erF7J+unZEEgs33ObDWr+2hnZcJAH+92W7VJnGiBLbyv5vc+6NVAC9bzaciV2uRq" +
                    "kBzqar4IbAgMBAAGjITAfMB0GA1UdDgQWBBSg7QVS89eCNOsAPE1GCT/IacZtrzANBgkqhkiG9w0BAQsF" +
                    "AAOCAQEAoZKEQgpERdGoisMmzR40g5pi8x56yizhnWJL6fguEFGkdKQgmYI6F0OflWaZDs4tt0vqdeDjV" +
                    "wl/SZYRvl623oZI8G/gAm2AtVy18SefaFb18TSHCYoPYdk9fWxAJ/jUCJw/v7xPYVMNlF8xXtRagGPJLR" +
                    "fPHgsBtTxRBCJLBvh+Svg1BPjhui5mUMlQ2L2A3UgOVUKeqC6WBrxsMYqDO6AzW5yJndhLMcVJyJMG03L" +
                    "xHJBMiS5LJwZkX+4pnjyiGhF6Cu/9XJVOcdtUpuv4is+loD0nO5Nb/QMQPbemKPxO2iVK727bKoMBiksC" +
                    "CD3QgwPPwK8qbG96MITDPKAhSG7MDLLK1o7SwN2Gl/3hBh/7bGdE";

    private final SSLContext mContext;
    private final String[] mCiphers;

    public SelfSignedSSLSocketFactory() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext ctx = SSLContext.getInstance("TLS");
        try {
            KeyStore keyStore = KeyStore.getInstance("jks");
            String passphrase = "password";
            InputStream keystoreFile = new ByteArrayInputStream(Base64.decode(KEYSTORE_FILE, Base64.DEFAULT));
            keyStore.load(keystoreFile, passphrase.toCharArray());
            KeyManagerFactory factory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            factory.init(keyStore, passphrase.toCharArray());
            KeyManager[] keyManagers = factory.getKeyManagers();

            ctx.init(keyManagers, null, null);

            mContext = ctx;
            mCiphers = mContext.getSocketFactory().getSupportedCipherSuites();
        } catch (Exception ex) {
            throw new KeyManagementException(ex);
        }
    }

    @Override
    public String[] getDefaultCipherSuites() {
        return mCiphers.clone();
    }

    @Override
    public String[] getSupportedCipherSuites() {
        return mCiphers.clone();
    }

    @Override
    public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
        SSLSocketFactory factory = mContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(s, host, port, autoClose);
        ss.setEnabledCipherSuites(mCiphers);
        ss.setEnabledProtocols(ss.getSupportedProtocols());
        return ss;
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        SSLSocketFactory factory = mContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(address, port, localAddress, localPort);
        ss.setEnabledCipherSuites(mCiphers);
        ss.setEnabledProtocols(ss.getSupportedProtocols());
        return ss;
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        SSLSocketFactory factory = mContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port, localHost, localPort);
        ss.setEnabledCipherSuites(mCiphers);
        ss.setEnabledProtocols(ss.getSupportedProtocols());
        return ss;
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        SSLSocketFactory factory = mContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port);
        ss.setEnabledCipherSuites(mCiphers);
        ss.setEnabledProtocols(ss.getSupportedProtocols());
        return ss;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        SSLSocketFactory factory = mContext.getSocketFactory();
        SSLSocket ss = (SSLSocket) factory.createSocket(host, port);
        ss.setEnabledCipherSuites(mCiphers);
        ss.setEnabledProtocols(ss.getSupportedProtocols());
        return ss;
    }

}
