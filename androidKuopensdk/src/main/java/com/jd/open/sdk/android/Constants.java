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

/**
 * SDK使用的常量
 */
public interface Constants {

    /** 请求条件参数 **/
    String JSON_PARAM_KEY = "360buy_param_json";

    /** 请求参数签名 **/
    String SIGN_KEY = "sign";

    /** 请求属性userAgent **/
    String USAGE_AGENT = " 360buy-sdk-java";

    /** 请求属性acceptEncoding **/
    String ACCEPT_ENCODING = "gzip";

    /** post请求 **/
    String POST_METHOD = "POST";

    /** get请求 **/
    String GET_METHOD = "GET";

    /** 默认字符集UTF-8 **/
    String DEFAULT_CHARSET = "UTF-8";

    /** 版本 **/
    String VERSION = "2.0";

    /** 认证完成后的重定向地址 **/
    String REDIRECT_URI = "http://jdoauth.success";
    // String REDIRECT_URI = "http://www.baidu.com";

    /** 保存Access Token的key名称 **/
    public static final String TOKEN = "access_token";

    /** 保存Auth key的key名称 **/
    public static final String AUTH_KEY = "code";

    /** 保存过期时间长度key名称 **/
    public static final String EXPIRES_IN = "expires_in";

    /** 保存Refresh Token的key名称 **/
    public static final String REFRESH_TOKEN = "refresh_token";

    /** 保存过期起始时间的key名称 **/
    public static final String EXPIRES_TIME = "time";

    /** 保存过期时间key名称 **/
    public static final String EXPIRES = "expires";
}
