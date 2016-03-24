package com.example.wayne.sonui.jingdong;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wayne.sonui.R;
import com.jd.open.sdk.android.Constants;
import com.jd.open.sdk.android.JdAndroidClient;
import com.jd.open.sdk.android.JdException;
import com.jd.open.sdk.android.JdListener;
import com.jd.open.sdk.android.api.InvokeError;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商品详细信息页面
 * Created by Wayne on 2016/3/7.
 */
public class WareDetailActivity extends Activity {

    private static final String TAG="WareDetailActivity";
    // 数据加载提示
    protected static final String LOADING = "数据加载中...";
    private ProgressDialog mDialog;
    private JdAndroidClient client;

    //商品信息
    private Map<String, Object> waresData;
    // 商品图片
    private ImageView imageView;

    //商品ID
    private String wareId;

    private int progress;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ware_detail);

        mDialog = new ProgressDialog(this);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setMessage(LOADING);

        client=JdAndroidClient.getInstance();
        client.setAppKey("147B792B4B8AAA386A2FFFB981736C0E");
        client.setAppSecret("f2e2ce536db64388b835645f61568ab3");
        client.setAccessToken("1a160b68-50cb-42c8-bef6-65673cc99a19");

        waresData=new HashMap<String,Object>();


        Intent intent=getIntent();
        wareId=intent.getStringExtra("ware_id");
        mDialog.show();

        getBaseProduct();
        getProductPrice();
        getProductImage();

        // 校验是否完成
        checkFinished();
        //点击购买按钮
        userBuy();

    }

    /**
     * 获取商品基本信息
     */
    private void getBaseProduct(){
        Bundle param=new Bundle();
        final String fields="name,productArea";
        param.putString("ids",wareId);
        param.putString("basefields", fields);
        String method="jingdong.new.ware.baseproduct.get";
        client.invoke(method, param, new JdListener.RequestListener() {
            @Override
            public void onComplete(JSONObject result) {
                JSONObject root = result.optJSONObject("jingdong_new_ware_baseproduct_get_responce");
                if (root != null) {
                    JSONArray baseProductList = root.optJSONArray("listproductbase_result");
                    if (baseProductList != null && baseProductList.length() > 0) {
                        JSONObject baseProduct = baseProductList.optJSONObject(0);
                        waresData.put("name", baseProduct.optString("name"));
                        waresData.put("productArea", baseProduct.optString("productArea"));

                    }
                }
      /*          //商品名称
                TextView titleTextView=(TextView)findViewById(R.id.ware_detail_name);
                titleTextView.setText((String) waresData.get("name"));
                TextView regionTextView=(TextView)findViewById(R.id.ware_detail_region);
                regionTextView.setText((String)waresData.get("productArea"));*/
                // 校验是否完成
                checkFinished();
            }

            @Override
            public void onError(InvokeError e) {
                Log.e(TAG, "code: " + e.getErrorCode() + ",message: " + e.getErrorZHMessage());
                // 校验是否完成
                checkFinished();
            }

            @Override
            public void onJdError(JdException e) {
                Log.e(TAG, e.getMessage());
                // 校验是否完成
                checkFinished();
            }
        });
    }

    /**
     * 获取商品价格
     */
    private void getProductPrice(){
        Bundle param=new Bundle();
        param.putString("sku_id", "J_"+wareId);
        String method="jingdong.ware.price.get";
        client.invoke(method, param, new JdListener.RequestListener() {
            @Override
            public void onComplete(JSONObject result) {
                JSONObject root=result.optJSONObject("jingdong_ware_price_get_responce");
                if(root!=null){
                    JSONArray changesPrice=root.optJSONArray("price_changes");
                    if(changesPrice!=null && changesPrice.length()>0){
                        waresData.put("price",changesPrice.optJSONObject(0).optString("price"));
                    }
                }
              /*  //商品价格
                TextView priceTextView=(TextView)findViewById(R.id.ware_detail_price);
                priceTextView.setText((String)waresData.get("price"));*/
                // 校验是否完成
                checkFinished();
            }

            @Override
            public void onError(InvokeError e) {
                Log.e(TAG, "code: " + e.getErrorCode() + ",message: " + e.getErrorZHMessage());
                // 校验是否完成
                checkFinished();
            }

            @Override
            public void onJdError(JdException e) {
                Log.e(TAG, e.getMessage());
                // 校验是否完成
                checkFinished();
            }
        });
    }

    /**
     * 获取商品图片
     */
    private void getProductImage(){
        Bundle param=new Bundle();
        param.putString("sku_id", wareId);
        String method="jingdong.ware.productimage.get";
        client.invoke(method, param, new JdListener.RequestListener() {
            @Override
            public void onComplete(JSONObject result) {
                List<Map<String,String>> imageMapList=new ArrayList<Map<String, String>>();
                Map<String,String> imageMap=new HashMap<String, String>();
                JSONObject root=result.optJSONObject("jingdong_ware_productimage_get_responce");
                if(root!=null){
                    JSONArray imagePathList=root.optJSONArray("image_path_list");
                    if(imagePathList!=null && imagePathList.length()>0){
                        JSONArray imageList=imagePathList.optJSONObject(0).optJSONArray("image_list");
                        if(imageList!=null && imageList.length()>0){
                            for(int i=0;i<imageList.length();i++) {
                                JSONObject image = imageList.optJSONObject(i);
                                if(image!=null){
                                    //图片id
                                    imageMap.put("imgId",image.optString("id"));
                                    //图片地址
                                    imageMap.put("url",image.optString("path"));
                                    imageMapList.add(imageMap);
                                }
                            }
                        }
                    }
                }
                waresData.put("imgs",imageMapList);

                //商品图片
                List<Map<String, String>> wareImages = (List<Map<String, String>>) waresData.get("imgs");
                Map<String, String> wareImage = null;
                if (wareImages != null && wareImages.size() > 0) {
                    wareImage = wareImages.get(0);
                }

                // 图片地址
                String imageUrl = null;
                if (wareImage != null) {
                    imageUrl = wareImage.get("url");
                }


                new AsyncTask<String,Void,Bitmap>(){
                    @Override
                    protected void onPostExecute(Bitmap result){
                        waresData.put("wareImg", result);
                       // imageView.setImageBitmap(result);
                        // 校验是否完成
                        checkFinished();
                    }
                    @Override
                    protected Bitmap doInBackground(String... params){
                        String imageUrl=params[0];
                        InputStream stream=null;
                        try{
                            stream=getImage(imageUrl);
                        }catch(IOException e){
                            Log.e(TAG,e.getMessage());
                        }
                        return BitmapFactory.decodeStream(stream);
                    }
                }.execute(imageUrl);
            }

            @Override
            public void onError(InvokeError e) {
                Log.e(TAG, "code: " + e.getErrorCode() + ",message: " + e.getErrorZHMessage());
                // 校验是否完成
                checkFinished();
            }

            @Override
            public void onJdError(JdException e) {
                Log.e(TAG, e.getMessage());
                // 校验是否完成
                checkFinished();
            }
        });

    }

    /**
     * 获取图片资源
     *
     * @param path 图片资源地址
     * @return 图片资源的字节流
     * @throws IOException
     */
    private InputStream getImage(String path) throws IOException {
        URL url = new URL(path);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod(Constants.GET_METHOD);
        InputStream stream = null;
        if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
            stream = conn.getInputStream();
        }

        return stream;
    }

    /**
     * 点击购买按钮
     */
    private void userBuy(){
        Button buy=(Button)findViewById(R.id.go_mpage_button);
        buy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(WareDetailActivity.this,WareBuyWebViewActivity.class);
                intent.putExtra("url","http://item.m.jd.com/product/"+wareId+".html");
                startActivity(intent);
            }
        });
    }

    private void checkFinished() {
        // 完成一项
        if (progress++ >= 2) {
            //商品名称
            TextView titleTextView=(TextView)findViewById(R.id.ware_detail_name);
            titleTextView.setText((String) waresData.get("name"));
            TextView regionTextView=(TextView)findViewById(R.id.ware_detail_region);
            regionTextView.setText((String)waresData.get("productArea"));

            //商品价格
            TextView priceTextView=(TextView)findViewById(R.id.ware_detail_price);
            priceTextView.setText((String) waresData.get("price"));

            //商品图片
            imageView=(ImageView)findViewById(R.id.ware_detail_image);
            imageView.setImageBitmap((Bitmap)waresData.get("wareImg"));

            mDialog.dismiss();
        }
    }
}

