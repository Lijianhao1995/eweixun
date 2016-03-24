package com.example.wayne.sonui.jingdong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.wayne.sonui.R;
import com.jd.open.sdk.android.JdAndroidClient;
import com.jd.open.sdk.android.JdException;
import com.jd.open.sdk.android.JdListener;
import com.jd.open.sdk.android.api.InvokeError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Wayne on 2016/3/1.
 */
public class WareListActivity extends Activity {
    private static final String TAG = "WareListActivity";
    // 数据加载提示
    protected static final String LOADING = "数据加载中...";
    private ProgressDialog mDialog;
    private JdAndroidClient client;
    private List<Map<String,String>> data;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_list);

        mDialog = new ProgressDialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setMessage(LOADING);
        mDialog.show();

        client=JdAndroidClient.getInstance();
        client.setAppKey("147B792B4B8AAA386A2FFFB981736C0E");
        client.setAppSecret("f2e2ce536db64388b835645f61568ab3");
        client.setAccessToken("1a160b68-50cb-42c8-bef6-65673cc99a19");

        data=new ArrayList<Map<String,String>>();


        Bundle param=new Bundle();
        String method="jingdong.ware.search";
        Intent intent=getIntent();
        String keyword=intent.getStringExtra("keyword");
        try{
            String strGBK= URLEncoder.encode(keyword,"GBK");
            param.putString("key",strGBK);}
        catch (UnsupportedEncodingException e){
            e.printStackTrace();
        }


        client.invoke(method, param, new JdListener.RequestListener() {
            @Override
            public void onComplete(JSONObject result) {
                try{
                    Log.e(TAG, "result = " + result);

                    JSONObject root=result.optJSONObject("jingdong_ware_search_responce");
                    if(root!=null){
                        JSONArray paragraphs=root.optJSONArray("Paragraph");
                        if(paragraphs!=null && paragraphs.length()>0){
                            for(int i=0;i<paragraphs.length();i++){
                                JSONObject paragraph=paragraphs.optJSONObject(i);
                                JSONObject content=paragraph.optJSONObject("Content");

                                if(content!=null){

                                    Map<String,String> contentMap=new HashMap<String, String>();
                                    contentMap.put("wareid",paragraph.optString("wareid"));
                                    contentMap.put("title",content.optString("warename"));
                                    contentMap.put("m_url",content.optString("imageurl"));
                                    data.add(contentMap);
                                }
                            }
                        }
                    }

                    ListView view=(ListView)findViewById(R.id.ware_list);
                    SimpleAdapter adapter=new SimpleAdapter(WareListActivity.this,data,
                            R.layout.ware_list_item,new String[]{"title"},
                            new int[]{R.id.ware_list_name});
                    view.setAdapter(adapter);
                    view.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent=new Intent(WareListActivity.this,WareDetailActivity.class);
                            Map<String,String> wareInfo=data.get(position);
                            intent.putExtra("ware_id",wareInfo.get("wareid"));

                            startActivity(intent);
                        }
                    });
                }catch (Exception e){
                    e.printStackTrace();
                }
                mDialog.dismiss();
            }

            @Override
            public void onError(InvokeError e) {
                Log.e(TAG, "code: " + e.getErrorCode() + ",message: " + e.getErrorZHMessage());
                mDialog.dismiss();
            }

            @Override
            public void onJdError(JdException e) {
                Log.e(TAG, e.getMessage());
                mDialog.dismiss();
            }
        });
    }

}
