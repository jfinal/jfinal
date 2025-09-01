/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jfinal.kit;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.Map.Entry;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.servlet.http.HttpServletRequest;

/**
 * HttpKit
 */
public class HttpKit {

    private HttpKit() {}

    /**
     * https 域名校验
     */
    private static class TrustAnyHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    /**
     * https 证书管理
     */
    private static class TrustAnyTrustManager implements X509TrustManager {
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
    }

    private static final String GET  = "GET";
    private static final String POST = "POST";
    private static String CHARSET = "UTF-8";

    private static int connectTimeout = 19000;	// 连接超时，单位毫秒
    private static int readTimeout = 19000;		// 读取超时，单位毫秒

    private static final SSLSocketFactory sslSocketFactory = initSSLSocketFactory();
    private static final TrustAnyHostnameVerifier trustAnyHostnameVerifier = new HttpKit.TrustAnyHostnameVerifier();

    private static SSLSocketFactory initSSLSocketFactory() {
        try {
            TrustManager[] tm = {new HttpKit.TrustAnyTrustManager() };
            SSLContext sslContext = SSLContext.getInstance("TLS");	// ("TLS", "SunJSSE");
            sslContext.init(null, tm, new java.security.SecureRandom());
            return sslContext.getSocketFactory();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void setCharSet(String charSet) {
        if (StrKit.isBlank(charSet)) {
            throw new IllegalArgumentException("charSet can not be blank.");
        }
        HttpKit.CHARSET = charSet;
    }

    public static void setConnectTimeout(int connectTimeout) {
        HttpKit.connectTimeout = connectTimeout;
    }

    public static void setReadTimeout(int readTimeout) {
        HttpKit.readTimeout = readTimeout;
    }

    private static HttpURLConnection getHttpConnection(String url, String method, Map<String, String> headers) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, KeyManagementException {
        URL _url = new URL(url);
        HttpURLConnection conn = (HttpURLConnection)_url.openConnection();
        if (conn instanceof HttpsURLConnection) {
            ((HttpsURLConnection)conn).setSSLSocketFactory(sslSocketFactory);
            ((HttpsURLConnection)conn).setHostnameVerifier(trustAnyHostnameVerifier);
        }

        conn.setRequestMethod(method);
        conn.setDoOutput(true);
        conn.setDoInput(true);

        conn.setConnectTimeout(connectTimeout);
        conn.setReadTimeout(readTimeout);

        conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/33.0.1750.146 Safari/537.36");

        if (headers != null && !headers.isEmpty()) {
            for (Entry<String, String> entry : headers.entrySet()) {
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return conn;
    }

    /**
     * Send GET request
     */
    public static String get(String url, Map<String, String> queryParas, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), GET, headers);
            conn.connect();
            return readResponseString(conn);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String get(String url, Map<String, String> queryParas) {
        return get(url, queryParas, null);
    }

    public static String get(String url) {
        return get(url, null, null);
    }

    /**
     * Send POST request
     */
    public static String post(String url, Map<String, String> queryParas, String data, Map<String, String> headers) {
        HttpURLConnection conn = null;
        try {
            conn = getHttpConnection(buildUrlWithQueryString(url, queryParas), POST, headers);
            conn.connect();

            if (data != null) {
                OutputStream out = conn.getOutputStream();
                out.write(data.getBytes(CHARSET));
                out.flush();
                out.close();
            }

            return readResponseString(conn);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static String post(String url, Map<String, String> queryParas, String data) {
        return post(url, queryParas, data, null);
    }

    public static String post(String url, String data, Map<String, String> headers) {
        return post(url, null, data, headers);
    }

    public static String post(String url, String data) {
        return post(url, null, data, null);
    }

    private static String readResponseString(HttpURLConnection conn) {
        try (InputStreamReader isr = new InputStreamReader(conn.getInputStream(), CHARSET)) {
            StringBuilder ret = new StringBuilder();
            char[] buf = new char[1024];
            for (int num; (num = isr.read(buf, 0, buf.length)) != -1;) {
                ret.append(buf, 0, num);
            }
            return ret.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build queryString of the url
     */
    private static String buildUrlWithQueryString(String url, Map<String, String> queryParas) {
        if (queryParas == null || queryParas.isEmpty()) {
            return url;
        }

        StringBuilder sb = new StringBuilder(url);
        boolean isFirst;
        if (url.indexOf('?') == -1) {
            isFirst = true;
            sb.append('?');
        }
        else {
            isFirst = false;
        }

        for (Entry<String, String> entry : queryParas.entrySet()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append('&');
            }

            String key = entry.getKey();
            String value = entry.getValue();
            if (StrKit.notBlank(value)) {
                try {value = URLEncoder.encode(value, CHARSET);} catch (UnsupportedEncodingException e) {throw new RuntimeException(e);}
            }
            sb.append(key).append('=').append(value);
        }
        return sb.toString();
    }

    public static String readData(HttpServletRequest request) {
        try {
            String ce = request.getCharacterEncoding();
            InputStreamReader isr = new InputStreamReader(request.getInputStream(), ce != null ? ce : CHARSET);
            StringBuilder ret = new StringBuilder();
            char[] buf = new char[2048];
            for (int num; (num = isr.read(buf, 0, buf.length)) != -1;) {
                ret.append(buf, 0, num);
            }
            return ret.toString();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        /* 去掉 close() 否则后续 ActionReporter 中的 getPara() 在部分 tomcat 中会报 IOException : Stream closed
        finally {
            if (br != null) {
                try {br.close();} catch (IOException e) {LogKit.error(e.getMessage(), e);}
            }
        }*/
    }

    @Deprecated
    public static String readIncommingRequestData(HttpServletRequest request) {
        return readData(request);
    }

    /**
     * 检测是否为 https 请求
     *
     * nginx 代理实现 https 的场景，需要对 nginx 进行如下配置：
     *     proxy_set_header X-Forwarded-Proto https;
     * 或者配置:
     *     proxy_set_header X-Forwarded-Proto $scheme;
     */
    public static boolean isHttps(HttpServletRequest request) {
        return  "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto"))
                ||
                "https".equalsIgnoreCase(request.getScheme());
    }
}






