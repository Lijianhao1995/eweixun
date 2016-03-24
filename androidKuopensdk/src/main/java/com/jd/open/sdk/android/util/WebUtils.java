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

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONObject;

import android.os.Bundle;
import android.text.TextUtils;

import com.jd.open.sdk.android.Constants;

/**
 * web请求相关的工具类
 */
public class WebUtils {

    private static final String TAG = "WebUtils";

    private static DateFormat mformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA);

    /**
     * 调用http请求获取数据
     * 
     * @param requestParam 通过Bundle类型设置http请求的请求参数
     * @param systemParam 通过Bundle类型设置的系统参数
     * @return 返回http请求的内容
     * @throws IOException 请求过程出现异常
     */
    public static String request(Bundle requestParam, Bundle systemParam) throws IOException {
        String appKey = systemParam.getString("appKey");
        String appSecret = systemParam.getString("appSecret");
        // 请求地址
        String requestURL = systemParam.getString("requestURL");
        // 请求方法：POST或GET
        String httpMethod = systemParam.getString("httpMethod");
        // API调用方法，用来区分不同的API
        String apiMethod = systemParam.getString("apiMethod");
        // 认证后得到的access_token
        String accessToken = systemParam.getString("access_token");
        // 数据返回的格式
        String formate = systemParam.getString("format");

        Bundle param = new Bundle();
        param.putString("timestamp", mformat.format(new Date()));
        param.putString("format", formate);
        param.putString("v", Constants.VERSION);
        param.putString("method", apiMethod);
        param.putString("app_key", appKey);

        // appSecret
        String appendAppSecret = appSecret;

        if (!TextUtils.isEmpty(accessToken)) {
            param.putString("access_token", accessToken);
        }

        // 生成请求地址
        String urlStr = buildURL(requestParam, param, requestURL, appendAppSecret);

        Bundle params = new Bundle();
        params.putString(Constants.JSON_PARAM_KEY, getRequestParameterJson(requestParam));

        Log.d(TAG, "=========request url is:  " + urlStr);
        return Utils.openUrl(urlStr, httpMethod, params, false);
    }

    /**
     * 获取json格式的api请求的参数
     * 
     * @param requestParam http请求的参数
     * @return json格式的api请求参数
     */
    private static String getRequestParameterJson(Bundle requestParam) {

        Map<String, String> map = new TreeMap<String, String>();
        for (String key : requestParam.keySet()) {
            // 请求参数可以是Integer，Object等类型的，如果为null，则使用null，否则使用String.valueOf转换
            String value = null;
            Object object = null;
            try {
                object = requestParam.get(key);
                value = (String) object;
            } catch (ClassCastException e) {
                value = String.valueOf(object);
                StringBuilder message = new StringBuilder();
                message.append("request param: ");
                message.append(key);
                message.append(" was a ");
                message.append(object.getClass().getName());
                message.append(", String.valueOf(key) was returned");
                Log.w(TAG, message.toString());
            }

            map.put(key, value);
        }

        JSONObject jsonObject = new JSONObject(map);
        return jsonObject.toString();
    }

    /**
     * 根据请求地址和请求参数生成完整的请求地址
     * 
     * @param requestParam http请求参数
     * @param systemParam 系统参数
     * @param requestURL 请求地址
     * @param appendAppSecret 应用对应的appSecret
     * @return 生成完整的请求地址
     */
    private static String buildURL(Bundle requestParam, Bundle systemParam, String requestURL,
            String appendAppSecret) {
        Map<String, String> map = new TreeMap<String, String>();
        map.put(Constants.JSON_PARAM_KEY, getRequestParameterJson(requestParam));

        for (String key : systemParam.keySet()) {
            map.put(key, systemParam.getString(key));
        }

        systemParam.putString(Constants.SIGN_KEY, sign(map, appendAppSecret));

        StringBuilder url = new StringBuilder(requestURL);
        url.append("?");
        url.append(Utils.buildQuery(systemParam, Constants.DEFAULT_CHARSET));
        return url.toString();
    }

    /**
     * 对Map类型的数据加密
     * 
     * @param map 被加密的数据
     * @param appendAppSecret 应用对应的appendAppSecret
     * @return 加密后的数据
     */
    private static String sign(Map<String, String> map, String appendAppSecret) {
        StringBuilder sb = new StringBuilder();

        // 如果是access_token非空时，被加密的字符串头部要加上appSecret
        if (appendAppSecret != null) {
            sb.append(appendAppSecret);
        }

        for (Map.Entry<String, String> entry : map.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();
            if (!(TextUtils.isEmpty(name) || TextUtils.isEmpty(value))) {
                sb.append(name).append(value);
            }
        }

        // 如果是access_token非空时，被加密的字符串尾部要加上appSecret
        if (appendAppSecret != null) {
            sb.append(appendAppSecret);
        }

        Log.d(TAG, "sign param = " + sb.toString());
        return CodecUtil.md5(sb.toString());
    }
}
