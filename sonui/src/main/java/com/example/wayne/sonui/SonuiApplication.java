package com.example.wayne.sonui;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

import android.content.Context;
/**
 * Created by Wayne on 2016/3/21.
 */
public class SonuiApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        EMOptions options = new EMOptions();
        EMClient.getInstance().init(this,options);
    }
}
