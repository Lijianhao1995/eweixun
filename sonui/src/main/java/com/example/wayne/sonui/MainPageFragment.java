package com.example.wayne.sonui;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;

import com.example.wayne.sonui.jingdong.JdMainActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * Created by Wayne on 2016/3/14.
 */
public class MainPageFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_main_page, container, false);

        ListView viewlist=(ListView)view.findViewById(R.id.ware_img_list);
        SimpleAdapter adapter=new SimpleAdapter(getActivity(),getImg(),R.layout.ware_img_item,
                new String[]{"img"},new int[]{R.id.img_item});
        viewlist.setAdapter(adapter);

        ImageView imageView=(ImageView)view.findViewById(R.id.weather_backgroud);
        imageView.setFocusable(true);
        imageView.setFocusableInTouchMode(true);
        imageView.requestFocus();

        ImageButton giftButton=(ImageButton)view.findViewById(R.id.imageButtonGift);
        giftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
   /*             String packageName="com.example.wayne.eweixun";
                String className=".MainActivity";
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                ComponentName cn = new ComponentName(packageName, className);
                intent.setComponent(cn);
                startActivity(intent);
            }*/
                Intent intent = new Intent(getActivity(), JdMainActivity.class);
                startActivity(intent);
            }
        });
        return view;
    }


    List<Map<String,Object>> getImg(){
        List<Map<String,Object>> list=new ArrayList<Map<String,Object>>();
        Map<String,Object> map=new HashMap<String,Object>();
        map.put("img", R.drawable.img1);
        list.add(map);
        map=new HashMap<String,Object>();
        map.put("img", R.drawable.img2);
        list.add(map);
        map=new HashMap<String,Object>();
        map.put("img", R.drawable.img3);
        list.add(map);
        map=new HashMap<String,Object>();
        map.put("img", R.drawable.img4);
        list.add(map);
        return list;
    }


}
