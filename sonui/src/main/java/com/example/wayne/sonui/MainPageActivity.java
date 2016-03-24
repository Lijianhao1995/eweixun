package com.example.wayne.sonui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Wayne on 2016/3/14.
 */
public class MainPageActivity extends Activity {

   // private MainPageFragment mainPageFragment;
   // private Fragment[] fragments;
    private Fragment[] fragments;

    MainPageFragment mainPageFragment;
    MineFragment mineFragment;
    SettingFragment settingFragment;
    public Button[] mTabs;

    private int index;
    // 当前fragment的index
    private int currentTabIndex;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ewx_activity_main);

        initView();
        mainPageFragment = new MainPageFragment();
        mineFragment = new MineFragment();
        settingFragment = new SettingFragment();

        fragments = new Fragment[]{mainPageFragment, mineFragment, settingFragment};

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);

        if(fragment==null)
        {
            fm.beginTransaction()
                    .add(R.id.fragment_container, mainPageFragment)
                    .add(R.id.fragment_container, mineFragment).add(R.id.fragment_container, settingFragment)
                    .hide(settingFragment).hide(mineFragment).show(mainPageFragment)
                    .commit();
        }
        else{
            fm.beginTransaction().show(mainPageFragment).commit();
        }


    }

    /**
     * 初始化组件
     */
    private void initView() {

        mTabs = new Button[3];
        mTabs[0] = (Button) findViewById(R.id.btn_main_page);
        mTabs[1] = (Button) findViewById(R.id.btn_mine);
        mTabs[2] = (Button) findViewById(R.id.btn_setting);
        // 把第一个tab设为选中状态
        mTabs[0].setSelected(true);
    }

    /**
     * button点击事件
     *
     * @param view
     */
    public void onTabClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_main_page:
                index = 0;
                break;
            case R.id.btn_mine:
                index = 1;
                break;
            case R.id.btn_setting:
                index = 2;
                break;
        }
        if (currentTabIndex != index) {
            FragmentTransaction trx = getFragmentManager().beginTransaction();
            trx.hide(fragments[currentTabIndex]);
            if (!fragments[index].isAdded()) {
                trx.add(R.id.fragment_container, fragments[index]);
            }
            trx.show(fragments[index]).commit();
        }
        mTabs[currentTabIndex].setSelected(false);
        // 把当前tab设为选中状态
        mTabs[index].setSelected(true);
        currentTabIndex = index;
    }




}
