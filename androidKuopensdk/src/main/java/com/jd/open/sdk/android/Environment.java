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

package com.jd.open.sdk.android;

import java.util.HashMap;
import java.util.Map;

/**
 * Android客户端调用服务器请求的地址
 */
public enum Environment {

    /** 生产环境地址 **/
    PRODUCTION("https://auth.360buy.com/oauth", "http://gw.api.360buy.com/routerjson"),

    /** 沙箱地址 **/
    SANDBOX("http://auth.sandbox.360buy.com/oauth", "http://gw.api.sandbox.360buy.com/routerjson"),

    /** 测试环境 **/
    TESTBOX("http://auth.360buy.net/oauth", "http://gw.api.360buy.net/routerjson");

    private final Map<String, String> URL_CONTAINER = new HashMap<String, String>();

    /**
     * 设置不同环境的OAuth认证地址和API调用地址
     * 
     * @param authURL 认证的地址
     * @param apiURL API调用的地址
     */
    Environment(String authURL, String apiURL) {
        URL_CONTAINER.put("oauth", authURL);
        URL_CONTAINER.put("api", apiURL);
    }

    /**
     * 获取API调用的请求地址
     * 
     * @return 请求地址
     */
    public String getApiURL() {
        return URL_CONTAINER.get("api");
    }

    /**
     * 获取OAuth认证的请求地址
     * 
     * @return 请求地址
     */
    public String getOauthURL() {
        return URL_CONTAINER.get("oauth") + "/authorize";
    }

    /**
     * 获取token的请求地址
     * 
     * @return 请求地址
     */
    public String getTokenURL() {
        return URL_CONTAINER.get("oauth") + "/token";
    }
}
