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

package com.jd.open.sdk.android.api;

/**
 * API请求出错时返回出错信息
 *
 */
public class InvokeError {

    private String errorCode;

    private String errorZHMessage;

    private String errorENMessage;

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorZHMessage() {
        return errorZHMessage;
    }

    public void setErrorZHMessage(String errorZHMessage) {
        this.errorZHMessage = errorZHMessage;
    }

    public String getErrorENMessage() {
        return errorENMessage;
    }

    public void setErrorENMessage(String errorENMessage) {
        this.errorENMessage = errorENMessage;
    }
}
