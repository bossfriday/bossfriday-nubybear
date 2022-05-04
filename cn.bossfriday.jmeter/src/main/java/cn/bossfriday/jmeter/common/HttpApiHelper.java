package cn.bossfriday.jmeter.common;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class HttpApiHelper {
    private static final String HTTP = "http";
    private static final String HTTPS = "https";
    private static SSLConnectionSocketFactory sslsf = null;
    private static SSLContextBuilder builder = null;

    static {
        try {
            builder = new SSLContextBuilder();
            builder.loadTrustMaterial(null, new TrustStrategy() {
                @Override
                public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                    return true;    // TrustAll
                }
            });
            sslsf = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * getHttpClient
     *
     * @param isHttps
     * @return
     * @throws Exception
     */
    public static CloseableHttpClient getHttpClient(boolean isHttps) throws Exception {
        if (isHttps)
            return HttpClients.custom().setSSLSocketFactory(sslsf).build();

        return HttpClients.custom().build();
    }

    /**
     * getUrl
     *
     * @param serverRoot
     * @param path
     * @param isHttps
     * @return
     */
    public static String getUrl(String serverRoot, String path, boolean isHttps) {
        if (isHttps)
            return HTTPS + "://" + serverRoot + path;

        return HTTP + "://" + serverRoot + path;
    }
}
