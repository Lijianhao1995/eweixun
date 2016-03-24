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

import com.jd.open.sdk.android.Constants;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 加密类
 *
 */
public class CodecUtil {

    private CodecUtil() {
        throw new UnsupportedOperationException();
    }

    /**
     * MD5加密
     * @param source
     *          被加密的字符串
     * @return  加密后的字符串
     */
    public static String md5(String source) {
        if (source == null) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(source.getBytes(Constants.DEFAULT_CHARSET));
            return byte2hex(bytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        } catch (UnsupportedEncodingException e) {
            return null;
        }

    }

    /**
     * 字节数组转化为十六进制字符串
     * @param bytes
     *          要转化的字节数组
     * @return
     *          转化后的十六进制字符串
     */
    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (byte data : bytes) {
            String hex = Integer.toHexString(data & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }
}
