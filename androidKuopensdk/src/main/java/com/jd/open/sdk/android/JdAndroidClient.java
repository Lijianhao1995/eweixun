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

import java.io.IOException;
import java.net.MalformedURLException;

import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.webkit.CookieSyncManager;

import com.jd.open.sdk.android.api.InvokeError;
import com.jd.open.sdk.android.auth.DialogError;
import com.jd.open.sdk.android.auth.JdAuthDialog;
import com.jd.open.sdk.android.util.Log;
import com.jd.open.sdk.android.util.Utils;
import com.jd.open.sdk.android.util.WebUtils;

/**
 * JOS SDK Android客户端，提供API调用和授权接口
 */
public class JdAndroidClient {

    private String mAccessToken = null;
    private long mAccessExpires = 0;

    private JdListener.DialogListener mAuthDialogListener;

    /** Android SDK的android.content.Context **/
    private Context context;

    /** 开发者注册应用对应的appKey **/
    private String appKey;

    /** 开发者注册应用对应的appSecret **/
    private String appSecret;

    /**
     * 数据返回的格式,现在把持json格式
     */
    private String mFormat = "json";

    // private Constants.RequestListener requestListener;

    /** 保存运行，沙箱环境的枚举类，用于获取请求调用地址 **/
    private Environment env = Environment.PRODUCTION;

    private static final String TAG = "JdAndroidClient";

    /** 单例的JdAndroidClient对象 **/
    private static JdAndroidClient client = new JdAndroidClient();

    private JdAndroidClient() {
    }

    /**
     * 获取JdAndroidClient的实例
     * 
     * @return JdAndroidClient
     */
    public static JdAndroidClient getInstance() {
        return client;
    }

    /**
     * OAuth认证方法. 启动一个包含webview的弹出式对话框，提示用户登入京东并授予应用访问权限
     * 
     * @param activity 弹出式对话框所在的activity
     * @param permissions 权限参数，API组名,用string数组表示，目前支持参数：read
     * @param listener 用于通知调用者的回调接口
     */
    public void authorize(Context activity, String[] permissions,
            final JdListener.DialogListener listener) {

        mAuthDialogListener = listener;

        Bundle params = new Bundle();
        if (permissions != null && permissions.length > 0) {
            params.putString("scope", TextUtils.join(",", permissions));
        }
        CookieSyncManager.createInstance(activity);
        dialog(activity, params, new JdListener.DialogListener() {

            public void onComplete(Bundle values) {
                // ensure any cookies set by the dialog are saved
                CookieSyncManager.getInstance().sync();
                setAccessToken(values.getString(Constants.TOKEN));
                setAccessExpiresIn(values.getString(Constants.EXPIRES));
                if (isSessionValid()) {
                    Log.d(TAG, "Login Success! access_token="
                            + getAccessToken() + " expires="
                            + getAccessExpires());
                    mAuthDialogListener.onComplete(values);
                } else {
                    Log.d(TAG, "Failed to receive access token.");
                    mAuthDialogListener.onJdError(new JdException(
                            "Failed to receive access token."));
                }
            }

            public void onError(DialogError error) {
                Log.d(TAG, "Login failed: " + error);
                mAuthDialogListener.onError(error);
            }

            public void onJdError(JdException error) {
                Log.e(TAG, "Login failed: " + error);
                mAuthDialogListener.onJdError(error);
            }

            public void onCancel() {
                Log.e(TAG, "Login canceled");
                mAuthDialogListener.onCancel();
            }

        });
    }

    /**
     * 更新OAuth access token.
     * 
     * @param permissions 权限参数，API组名,用string数组表示，目前支持参数：read
     * @param listener 用于通知调用者的回调接口
     */
    public boolean refreshAccessToken(String refreshToken, String[] permissions,
            final JdListener.DialogListener listener) {
        Bundle parameters = new Bundle();
        String tokenUrl = env.getTokenURL();

        parameters.putString("grant_type", "refresh_token");
        parameters.putString("client_id", appKey);
        parameters.putString("client_secret", appSecret);
        parameters.putString("refresh_token", refreshToken);
        parameters.putString("state", "GET_TOKEN");
        if (permissions != null && permissions.length > 0) {
            parameters.putString("scope", TextUtils.join(",", permissions));
        }

        String url = tokenUrl + "?" + Utils.encodeUrl(parameters);

        String sReponse;
        try {
            sReponse = Utils.openUrl(url, "POST", parameters);

            JSONObject json = new JSONObject(sReponse);

            Bundle bundle = new Bundle();
            bundle.putString(Constants.TOKEN, json.optString(Constants.TOKEN, ""));

            String sTime = json.optString(Constants.EXPIRES_TIME, "0");
            String sExpIn = json.optString(Constants.EXPIRES_IN, "0");
            long nExpires = 0;

            if (sTime != null && sExpIn != null) {
                nExpires = Long.parseLong(sTime) + Long.parseLong(sExpIn);
            }

            bundle.putString(Constants.REFRESH_TOKEN, json.optString(Constants.REFRESH_TOKEN, ""));
            bundle.putString(Constants.EXPIRES_TIME, sTime);
            bundle.putString(Constants.EXPIRES_IN, sExpIn);
            bundle.putString(Constants.EXPIRES, Long.toString(nExpires));

            setAccessToken(bundle.getString(Constants.TOKEN));
            setAccessExpiresIn(bundle.getString(Constants.EXPIRES));

            listener.onComplete(bundle);
            return true;
        } catch (MalformedURLException e) {
            Log.e(TAG, "Malformed URL Exception ", e);
            listener.onJdError(new JdException("Exception occurred: " + e.toString()));
        } catch (IOException e) {
            Log.e(TAG, "get IO exception ", e);
            listener.onJdError(new JdException("Exception occurred: " + e.toString()));
        } catch (JSONException e) {
            Log.e(TAG, "get JSON exception ", e);
            listener.onJdError(new JdException("Exception occurred: " + e.toString()));
        }

        return false;
    }

    /**
     * 生成一个OAuth认证对话框 请注意此方法为异步方法且回调发生在调用者线程
     * 
     * @param context 弹出式对话框所在的activity
     * @param parameters 作为URL参数传递的字符串名值对
     * @param listener 用来通知对话框结束的回调接口
     */
    public void dialog(Context context, Bundle parameters,
            final JdListener.DialogListener listener) {
        if (TextUtils.isEmpty(appKey)) {
            Log.e(TAG, "authorize: appKey must not be null.");
            throw new IllegalArgumentException("authorize: appKey must not be null.");
        }

        String endpoint = env.getOauthURL();
        parameters.putString("response_type", "code");
        parameters.putString("client_id", appKey);
        parameters.putString("redirect_uri", Constants.REDIRECT_URI);
        parameters.putString("state", "GET_AUTH_KEY");

        String authUrl = endpoint + "?" + Utils.encodeUrl(parameters);
        if (context.checkCallingOrSelfPermission(Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {
            Utils.showAlert(context, "Error", context.getResources()
                    .getString(R.string.alert_error));
        } else {
            String tokenUrl = env.getTokenURL();
            new JdAuthDialog(context, authUrl, tokenUrl, appKey, appSecret, listener).show();
        }
    }

    /**
     * @return boolean - 在access_token为空或者access_token非空并且未过期时返回true
     */
    public boolean isSessionValid() {
        return (getAccessToken() != null) &&
                ((getAccessExpires() == 0) ||
                (System.currentTimeMillis() < getAccessExpires()));
    }

    /**
     * Android客户端的API调用，默认采用异步请求调用并且是POST请求
     * 
     * @param apiMethod API请求方法，如jingdong.warecategory.get
     * @param param API请求参数
     * @param listener API调用返回时回调的监听器
     */
    public void invoke(String apiMethod, Bundle param, JdListener.RequestListener listener) {
        invoke(apiMethod, param, listener, true, "POST");
    }

    /**
     * Android客户端的API调用，默认采用POST请求
     * 
     * @param apiMethod API请求方法，如jingdong.warecategory.get
     * @param param API请求参数
     * @param listener API调用返回时回调的监听器
     * @param async 是否异步调用API，async为true表示异步调用，值为false表示同步调用，一般使用异步调用
     */
    public void invoke(String apiMethod, Bundle param, JdListener.RequestListener listener,
            boolean async) {
        invoke(apiMethod, param, listener, async, "POST");
    }

    /**
     * Android客户端的API调用，默认采用异步请求调用
     * 
     * @param apiMethod API请求方法，如jingdong.warecategory.get
     * @param param API请求参数
     * @param listener API调用返回时回调的监听器
     * @param method http请求的方法，可以使用的有POST，GET
     */
    public void invoke(String apiMethod, Bundle param, JdListener.RequestListener listener,
            String method) {
        invoke(apiMethod, param, listener, true, method);
    }

    /**
     * Android客户端的API调用
     * 
     * @param apiMethod API请求方法，如jingdong.warecategory.get
     * @param param API请求参数
     * @param listener API调用返回时回调的监听器
     * @param async 是否异步调用API，async为true表示异步调用，值为false表示同步调用，一般使用异步调用
     * @param httpMethod http请求的方法，可以使用的有POST，GET
     */
    public void invoke(String apiMethod, Bundle param, final JdListener.RequestListener listener,
            boolean async,
            String httpMethod) {

        if (TextUtils.isEmpty(appKey)) {
            Log.e(TAG, "api invoke: appKey must not be null.");
            throw new IllegalArgumentException("api invoke: appKey must not be null.");
        }

        if (TextUtils.isEmpty(appSecret)) {
            Log.e(TAG, "api invoke: appSecret must not be null.");
            throw new IllegalArgumentException("api invoke: appSecret must not be null.");
        }

        if (TextUtils.isEmpty(apiMethod)) {
            Log.e(TAG, "api invoke: api method must not be null.");
            throw new IllegalArgumentException("api invoke: api method must not be null.");
        }

        // 校验传递进来的http请求方法是否正确
        if (!isMethodValid(httpMethod)) {
            Log.e(TAG, "api invoke: http method is invalid.");
            throw new IllegalArgumentException("http method is invalid.");
        }

        final String method = httpMethod.toUpperCase();

        Bundle sysParam = new Bundle();
        sysParam.putString("appKey", appKey);
        sysParam.putString("appSecret", appSecret);
        // 设置请求的地址
        sysParam.putString("requestURL", env.getApiURL());
        // HTTP请求的方法（POST,GET)
        sysParam.putString("httpMethod", method);
        // 所调用API的方法
        sysParam.putString("apiMethod", apiMethod);
        // 数据返回格式
        sysParam.putString("format", mFormat);

        // 如果access_token是可用的，则设置access_token
        if (isSessionValid()) {
            sysParam.putString("access_token", getAccessToken());
        }

        // 异步调用请求
        if (async) {
            new AsyncTask<Bundle, Integer, Object>() {
                @Override
                protected void onPostExecute(Object result) {
                    handleResponse(result, listener);
                }

                @Override
                protected Object doInBackground(Bundle... params) {
                    return request(params);
                }
            }.execute(param, sysParam);
        } else {
            handleResponse(request(param, sysParam), listener);
        }
    }

    /**
     * 请求API数据
     * 
     * @param params 请求所需要的参数
     * @return 请求返回的数据
     */
    private Object request(Bundle... params) {
        try {
            Bundle requestParam = params[0];
            Bundle systemParam = params[1];
            return WebUtils.request(requestParam, systemParam);
        } catch (Exception e) {
            return e;
        }
    }

    /**
     * 处理接口返回的数据
     * 
     * @param response 调用api接口返回的数据
     * @param listener 请求回调的监听器
     */
    private void handleResponse(Object response, JdListener.RequestListener listener) {
        // listener不能为空
        if (listener == null) {
            Log.e(TAG, "RequestListener must not be null.");
            throw new IllegalArgumentException("RequestListener must not be null.");
        }

        // API调用过程出现异常
        if (response instanceof Exception) {
            listener.onJdError(new JdException("Exception occurred: " +
                    ((Exception) response).toString()));
        }
        // API调用成功
        else if (response instanceof String) {
            try {
                JSONObject json = new JSONObject((String) response);
                InvokeError error = parseJson(json);
                // 接口调用失败
                if (error != null) {
                    Log.e(TAG, error.getErrorZHMessage());
                    listener.onError(error);
                } else {
                    listener.onComplete(json);
                }
            } catch (JSONException e) {
                Log.e(TAG, "get JSON exception ", e);
                listener.onJdError(new JdException("Exception occurred: " + e.toString()));
            }
        } else {
            listener.onJdError(new JdException("Impossible result!"));
        }
    }

    /**
     * 解析json格式的数据，封装成APIError对象
     * 
     * @param json json格式的数据
     * @throws JSONException
     * @return InvokeError
     */
    private InvokeError parseJson(JSONObject json) throws JSONException {
        JSONObject jsonObject = json.optJSONObject("error_response");
        if (jsonObject == null) {
            return null;
        }

        String code = jsonObject.optString("code");
        String zhDesc = jsonObject.optString("zh_desc");
        String enDesc = jsonObject.optString("en_desc");

        if (TextUtils.isEmpty(code)) {
            return null;
        }

        InvokeError error = new InvokeError();
        error.setErrorCode(code);
        error.setErrorZHMessage(zhDesc);
        error.setErrorENMessage(enDesc);
        return error;
    }

    /**
     * 校验http请求方法是否正确
     * 
     * @param method http请求方法
     * @return boolean
     */
    private boolean isMethodValid(String method) {
        if (TextUtils.isEmpty(method)) {
            return false;
        }

        String methodSupper = method.toUpperCase();

        return Constants.POST_METHOD.equals(methodSupper) ||
                Constants.GET_METHOD.equals(methodSupper);
    }

    /**
     * 取得OAuth access token
     * 
     * @return String - access token
     */
    public String getAccessToken() {
        return mAccessToken;
    }

    /**
     * 返回当前access token的过期时间（微秒），如果access token未设置则为0
     * 
     * @return long - 当前access token的过期时间
     */
    public long getAccessExpires() {
        return mAccessExpires;
    }

    /**
     * 设置OAuth access token
     * 
     * @param token - access token
     */
    public void setAccessToken(String token) {
        mAccessToken = token;
    }

    /**
     * 设置当前access token的过期时间（微秒）
     * 
     * @param time - 微秒为单位的时间戳
     */
    public void setAccessExpires(long time) {
        mAccessExpires = time;
    }

    /**
     * 设置当前access token的过期时间（微秒）
     * 
     * @param expiresIn - 微秒为单位的时间戳
     */
    public void setAccessExpiresIn(String expiresIn) {
        if (expiresIn != null && !expiresIn.equals("0")) {
            setAccessExpires(Long.parseLong(expiresIn));
        }
    }

    /**
     * 获取Context
     * 
     * @return Context - android.content.Context
     */
    public Context getContext() {
        return context;
    }

    /**
     * 设置Context
     * 
     * @param context - android.content.Context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 返回应用appKey
     * 
     * @return String - 应用appKey
     */
    public String getAppKey() {
        return appKey;
    }

    /**
     * 设置应用appKey
     * 
     * @param appKey 待设置的appKey
     */
    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    /**
     * 返回应用appSecret
     * 
     * @return String - 应用appSecret
     */
    public String getAppSecret() {
        return appSecret;
    }

    /**
     * 设置应用appSecret
     * 
     * @param appSecret 待设置的appSecret
     */
    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    /**
     * 根据sandboxFlag设置应用的环境，sandboxFlag为true表示处于测试环境。
     * 
     * @param sandboxFlag 是否处于sandbox测试状态
     */
    public void setSandBoxEnv(boolean sandboxFlag) {
        if (sandboxFlag) {
            // 沙箱环境
            env = Environment.SANDBOX;

        } else {
            env = Environment.PRODUCTION;
        }
    }
}
