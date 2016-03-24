package com.example.wayne.huanxin;

import android.app.Application;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMOptions;

/**
 * Created by Wayne on 2016/3/21.
 */
public class HuanxinApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化
        EMOptions options = new EMOptions();
        options.setAcceptInvitationAlways(false);
        // 设置是否需要已读回执
        options.setRequireAck(true);
        // 设置是否需要已送达回执
        options.setRequireDeliveryAck(false);
        // 设置从db初始化加载时, 每个conversation需要加载msg的个数
        options.setNumberOfMessagesLoaded(1);

        EMClient.getInstance().init(this,options);
    }
}
