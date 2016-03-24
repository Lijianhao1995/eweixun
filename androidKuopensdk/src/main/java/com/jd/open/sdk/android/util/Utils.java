/*
 * Copyright 2012 360buy, Inc.
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

package com.jd.open.sdk.android.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import com.jd.open.sdk.android.Constants;

public class Utils {

    private static final String TAG = "Jingdong-Utils";

    /**
     * 根据传入的HTTP POST参数与分界线参数产生multi-part提交数据
     * 
     * @param parameters 需要提交的HTTP POST参数
     * @param boundary 作为分界线的随机字符串
     * @return multi-part提交数据，字符串格式
     */
    public static String encodePostBody(Bundle parameters, String boundary) {
        if (parameters == null)
            return "";
        StringBuilder sb = new StringBuilder();

        for (String key : parameters.keySet()) {
            if (parameters.getByteArray(key) != null) {
                continue;
            }

            sb.append("Content-Disposition: form-data; name=\"" + key +
                    "\"\r\n\r\n" + parameters.getString(key));
            sb.append("\r\n" + "--" + boundary + "\r\n");
        }

        return sb.toString();
    }

    /**
     * 生成请求条件
     * 
     * @param param 查询条件参数
     * @param charset 字符编码
     * @return 生成后的请求条件
     */
    public static String buildQuery(Bundle param, String charset) {
        if (param == null || param.isEmpty()) {
            return null;
        }

        if (TextUtils.isEmpty(charset)) {
            charset = Constants.DEFAULT_CHARSET;
        }

        StringBuilder query = new StringBuilder();
        boolean hasParam = false;
        for (String name : param.keySet()) {

            String value = param.getString(name);
            if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                if (hasParam) {
                    query.append("&");
                } else {
                    hasParam = true;
                }

                try {
                    query.append(name).append("=").append(URLEncoder.encode(value, charset));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "buildQuery throws UnsupportedEncodingException!");
                    e.printStackTrace();
                }
            }
        }

        return query.toString();
    }

    /**
     * 将以名值形式对保存的URL参数编码
     * 
     * @param parameters 以名值对形式保存的URL参数编码
     * @return 编码后的URL参数字符串
     */
    public static String encodeUrl(Bundle parameters) {
        if (parameters == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String key : parameters.keySet()) {
            if (first)
                first = false;
            else
                sb.append("&");
            try {
                sb.append(URLEncoder.encode(key, Constants.DEFAULT_CHARSET))
                        .append("=")
                        .append(URLEncoder.encode(parameters.getString(key),
                                Constants.DEFAULT_CHARSET));
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, e.getMessage());
            }

        }
        return sb.toString();
    }

    /**
     * 将String的值解码成Bundle类型
     * 
     * @param s 要被解码的字符串
     * @return 解码后的Bundle对象
     */
    private static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                try {
                    params.putString(URLDecoder.decode(v[0], Constants.DEFAULT_CHARSET),
                            URLDecoder.decode(v[1], Constants.DEFAULT_CHARSET));
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return params;
    }

    /**
     * 解析一个URL串并以名值对形式保存到bundle.
     * 
     * @param url 待解析的URL
     * @return 名值对形式保存的bundle
     */
    public static Bundle parseUrl(String url) {
        // 忽略MalformedURLException
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    /**
     * 连接到HTTP URL并返回响应字符串
     * 
     * @param url 需要打开的URL,必须为一个有效的URL
     * @param method 使用的HTTP调用方法 ("GET", "POST", 等)
     * @param params 查询参数 (例如 appKey=foo)
     * @return 字符串格式 的服务器端响应
     * @throws MalformedURLException 如果URL格式不合法
     * @throws IOException 如果网络问题发生
     */
    public static String openUrl(String url, String method, Bundle params)
            throws MalformedURLException, IOException {
        return openUrl(url, method, params, true);
    }

    /**
     * 连接到HTTP URL并返回响应字符串
     * 
     * @param url 需要打开的URL,必须为一个有效的URL
     * @param method 使用的HTTP调用方法 ("GET", "POST", 等)
     * @param params 查询参数 (例如 appKey=foo)
     * @param multipart
     *            值为true时设置Content-Type=multipart/form-data;boundary=boundary,
     *            为false时设置Content
     *            -Type=application/x-www-form-urlencoded;charset
     *            =UTF-8,API调用时设置为false
     * @return 字符串格式 的服务器端响应
     * @throws MalformedURLException 如果URL格式不合法
     * @throws IOException 如果网络问题发生
     */
    public static String openUrl(String url, String method, Bundle params, boolean multipart)
            throws MalformedURLException, IOException {
        // multi-part http请求的随机字符串
        String strBoundary = "5h6ndRfv9rT8iSHsAEouNdArNfORhyTPfgfj8qYf";
        String endLine = "\r\n";

        OutputStream os;

        if (method.equals("GET")) {
            url = buildGetURL(url, encodeUrl(params));
        }
        Log.d(TAG, method + " URL: " + url);

        // 根据http和ssl协议获取不同的HTTPURLConnection，默认通过不安全的SSL证书
        HttpURLConnection conn = getConnection(url);

        conn.setRequestProperty("User-Agent", System.getProperties().
                getProperty("http.agent") + Constants.USAGE_AGENT);
        if (!Constants.GET_METHOD.equals(method)) {
            Bundle dataparams = new Bundle();

            // 设置字节数组形式的数据
            for (String key : params.keySet()) {
                Object object = params.get(key);
                if (object instanceof byte[]) {
                    dataparams.putByteArray(key, params.getByteArray(key));
                }
            }

            if (params.containsKey("access_token")) {
                String decoded_token =
                        URLDecoder.decode(params.getString("access_token"));
                params.putString("access_token", decoded_token);
            }

            conn.setRequestMethod("POST");

            // 认证和API调用设置不同的Conent-Type
            if (multipart) {
                conn.setRequestProperty(
                        "Content-Type",
                        "multipart/form-data;boundary=" + strBoundary);
            } else {
                conn.setRequestProperty("Content-Type",
                        "application/x-www-form-urlencoded;charset=UTF-8");
            }

            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Encoding", Constants.ACCEPT_ENCODING);
            conn.connect();
            os = new BufferedOutputStream(conn.getOutputStream());

            if (multipart) {
                os.write(("--" + strBoundary + endLine).getBytes(Constants.DEFAULT_CHARSET));
                os.write((encodePostBody(params, strBoundary)).getBytes(Constants.DEFAULT_CHARSET));
                os.write((endLine + "--" + strBoundary + endLine)
                        .getBytes(Constants.DEFAULT_CHARSET));
            } else {
                os.write(buildQuery(params, Constants.DEFAULT_CHARSET).getBytes(
                        Constants.DEFAULT_CHARSET));
            }

            if (multipart && !dataparams.isEmpty()) {
                for (String key : dataparams.keySet()) {
                    os.write(("Content-Disposition: form-data; filename=\"" + key + "\"" + endLine)
                            .getBytes());
                    os.write(("Content-Type: content/unknown" + endLine + endLine).getBytes());
                    os.write(dataparams.getByteArray(key));
                    os.write((endLine + "--" + strBoundary + endLine).getBytes());
                }
            }
            os.flush();
        }

        // 获取响应数据的字符集
        String charset = getResponseCharset(conn.getContentType());
        // 判断是否响应数据是gzip压缩的
        String header = conn.getHeaderField("Content-Encoding");
        boolean isGzip = false;
        if (header != null && header.toLowerCase().contains(Constants.ACCEPT_ENCODING)) {
            isGzip = true;
        }
        String response = "";
        try {
            response = read(conn.getInputStream(), charset, isGzip);
        } catch (FileNotFoundException e) {
            // 响应出错时返回值
            response = read(conn.getErrorStream(), charset, isGzip);
        }
        return response;
    }

    /**
     * 生成get请求的请求地址
     * 
     * @param url 请求地址
     * @param query 请求条件
     * @return 完整的get请求的请求地址
     */
    private static String buildGetURL(String url, String query) {
        if (TextUtils.isEmpty(query)) {
            return url;
        }

        if (url.contains("?")) {
            if (url.endsWith("?") || url.endsWith("&")) {
                url = url + query;
            } else {
                url = url + "&" + query;
            }
        } else {
            url = url + "?" + query;
        }

        return url;
    }

    /**
     * 通过请求地址得到HTTPURLConnection
     * 
     * @param url 字符串形式的请求地址
     * @return HTTPURLConnection
     * @throws IOException
     */
    private static HttpURLConnection getConnection(String url) throws IOException {
        URL requestURL = new URL(url);
        if ("https".equals(requestURL.getProtocol())) {
            SSLContext ctx = null;
            try {
                ctx = SSLContext.getInstance("TLS");
                ctx.init(new KeyManager[0],
                        new TrustManager[] {
                            new X509TrustManager() {
                                @Override
                                public void checkClientTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException {

                                }

                                @Override
                                public void checkServerTrusted(X509Certificate[] chain,
                                        String authType) throws CertificateException {

                                }

                                @Override
                                public X509Certificate[] getAcceptedIssuers() {
                                    return null;
                                }
                            }
                        },
                        new SecureRandom());
            } catch (Exception e) {
                throw new IOException(e.getMessage());
            }

            HttpsURLConnection conn = (HttpsURLConnection) requestURL.openConnection();
            conn.setSSLSocketFactory(ctx.getSocketFactory());
            conn.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;// 默认都认证通过
                }
            });

            return conn;
        } else {
            HttpURLConnection conn = (HttpURLConnection) requestURL.openConnection();
            return conn;
        }
    }

    /**
     * 将输入流中内容读出到字符串
     * 
     * @param in 输入流
     * @param charset 字符接
     * @param isGzip 是否用gzip压缩
     * @return 输入流中内容
     * @throws IOException 如果网络问题发生
     */
    private static String read(InputStream in, String charset, boolean isGzip) throws IOException {
        StringBuilder sb = new StringBuilder();
        if (isGzip) {
            in = new GZIPInputStream(in);
        }

        BufferedReader r = new BufferedReader(new InputStreamReader(in, charset), 1000);
        for (String line = r.readLine(); line != null; line = r.readLine()) {
            sb.append(line);
        }
        in.close();
        return sb.toString();
    }

    /**
     * 显示一个简单弹出对话框，可以指定标题以及信息
     * 
     * @param context 弹出对话框所处的android context
     * @param title 弹出对话框标题
     * @param text 在对话框显示的信息
     */
    public static void showAlert(Context context, String title, String text) {
        Builder alertBuilder = new Builder(context);
        alertBuilder.setTitle(title);
        alertBuilder.setMessage(text);
        alertBuilder.create().show();
    }

    /**
     * 根据http响应头获取http响应的字符集
     * 
     * @param contentType http响应头
     * @return http响应的字符集
     */
    private static String getResponseCharset(String contentType) {
        String charset = Constants.DEFAULT_CHARSET;
        if (TextUtils.isEmpty(contentType)) {
            return charset;
        }

        String[] params = contentType.split(";");
        for (String param : params) {
            param = param.trim();
            if (!param.startsWith("charset")) {
                continue;
            }

            String[] pair = param.split("=", 2);
            if (pair.length == 2 && !TextUtils.isEmpty(pair[1])) {
                charset = pair[1].trim();
            }
        }

        return charset;
    }
}
