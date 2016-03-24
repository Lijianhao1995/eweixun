
package com.jd.open.sdk.android.util;

public class Log {

    private static boolean DEBUG = true;

    /**
     * 输出debug级别调试信息
     * 
     * @param tag　TAG
     * @param msg　输出信息
     */
    public static void d(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.d(tag, msg);
        }
    }

    /**
     * 输入error级别的信息
     * 
     * @param tag TAG
     * @param msg　要输入的信息
     */
    public static void e(String tag, String msg) {
        android.util.Log.e(tag, msg);
    }

    /**
     * 输入error级别的信息
     * 
     * @param tag TAG
     * @param msg　要输入的信息
     */
    public static void e(String tag, String msg, Exception e) {
        android.util.Log.e(tag, msg, e);
    }

    /**
     * 输入警告级别log
     * 
     * @param tag TAG
     * @param msg 要输入的信息
     */
    public static void w(String tag, String msg) {
        if (DEBUG) {
            android.util.Log.w(tag, msg);
        }
    }

    /**
     * 设置是否为debug模式
     * 
     * @param isDebug　true:debug模式　
     */
    public static void setDebugMode(boolean isDebug) {
        DEBUG = isDebug;
    }

}
