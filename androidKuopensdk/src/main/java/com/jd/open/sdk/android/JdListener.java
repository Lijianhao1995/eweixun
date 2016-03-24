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

import android.os.Bundle;
import com.jd.open.sdk.android.api.InvokeError;
import com.jd.open.sdk.android.auth.DialogError;
import org.json.JSONObject;

/**
 *  请求回调的接口
 */
public interface JdListener {
    /**
     * 在请求过程中发生错误时被回调
     *
     * 在调用请求方法的所在线程中被调用
     *
     */
    public void onJdError(JdException e);

    /**
     * 在授权之后回调的接口
     *
     */
    public static interface DialogListener extends JdListener {

        /**
         * 在授权完成之后被回调的方法
         *
         * 在调用授权方法所在的线程中被调用
         *
         * @param values
         *            请求返回的键值对
         */
        public void onComplete(Bundle values);

        /**
         * 在用户取消授权时被调用
         *
         * 在调用授权方法所在的线程中被调用
         *
         */
        public void onCancel();

        /**
         * 授权返回的结果是错误时回调的方法
         *
         * 在调用授权方法所在的线程中被调用
         *
         */
        public void onError(DialogError e);
    }

    /**
     * 在API调用之后回调的接口
     *
     */
    public static interface RequestListener extends JdListener {
        /**
         * 在API调用返回之后被回调的方法.
         *
         * 在调用API请求所在的线程中被调用
         *
         * @param result
         *            请求返回的键值对
         */
        public void onComplete(JSONObject result);

        /**
         * 在API调用响应的结果有错时被回调
         *
         * 在调用API请求所在的线程中被调用
         *
         */
        public void onError(InvokeError e);
    }
}


