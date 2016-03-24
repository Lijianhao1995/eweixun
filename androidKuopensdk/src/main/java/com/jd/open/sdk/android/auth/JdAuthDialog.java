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

package com.jd.open.sdk.android.auth;

import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.http.SslError;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.SslErrorHandler;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jd.open.sdk.android.Constants;
import com.jd.open.sdk.android.JdException;
import com.jd.open.sdk.android.JdListener;
import com.jd.open.sdk.android.R;
import com.jd.open.sdk.android.util.Log;
import com.jd.open.sdk.android.util.Utils;

public class JdAuthDialog extends Dialog {

    static final int JD_BLUE = 0xFF6D84B4;
    static final float[] DIMENSIONS_DIFF_LANDSCAPE = {
            20, 60
    };
    static final float[] DIMENSIONS_DIFF_PORTRAIT = {
            40, 60
    };
    static final FrameLayout.LayoutParams FILL =
            new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                    ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;

    private String mAuthUrl;
    private String mTokenUrl;
    private JdListener.DialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;

    private String mAppId;
    private String mAppSecret;

    public JdAuthDialog(Context context, String authUrl, String tokenUrl, String appId,
            String appSecret, JdListener.DialogListener listener) {
        super(context);
        mAuthUrl = authUrl;
        mTokenUrl = tokenUrl;
        mListener = listener;
        mAppId = appId;
        mAppSecret = appSecret;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("loading...");

        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale =
                getContext().getResources().getDisplayMetrics().density;
        int orientation =
                getContext().getResources().getConfiguration().orientation;
        float[] dimensions =
                (orientation == Configuration.ORIENTATION_LANDSCAPE)
                        ? DIMENSIONS_DIFF_LANDSCAPE : DIMENSIONS_DIFF_PORTRAIT;
        addContentView(mContent, new LinearLayout.LayoutParams(
                display.getWidth() - ((int) (dimensions[0] * scale + 0.5f)),
                display.getHeight() - ((int) (dimensions[1] * scale + 0.5f))));
    }

    private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable icon = getContext().getResources().getDrawable(
                R.drawable.jingdong_logo);
        mTitle = new TextView(getContext());
        mTitle.setText(R.string.jd_oauth_login);

        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(JD_BLUE);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mTitle.setCompoundDrawablesWithIntrinsicBounds(
                icon, null, null, null);
        mContent.addView(mTitle);
    }

    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new JdAuthDialog.JdWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);

        mWebView.getSettings().setSavePassword(true);
        mWebView.getSettings().setSaveFormData(true);

        mWebView.loadUrl(mAuthUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }

    private class JdWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d("Jingdong-WebView", "Redirect URL: " + url);
            if (url.startsWith(Constants.REDIRECT_URI)) {
                Bundle values = Utils.parseUrl(url);

                String error = values.getString("error");
                if (error == null) {
                    error = values.getString("error_type");
                }

                if (error == null) {
                    // 启动一个AsyncTask在后台请求AccessToken
                    String authKey = values.getString(Constants.AUTH_KEY);
                    new GetAccessTokenTask().execute(authKey);

                    return true;
                } else if (error.equals("access_denied") ||
                        error.equals("OAuthAccessDeniedException")) {
                    mListener.onCancel();
                } else {
                    mListener.onJdError(new JdException(error));
                }

                JdAuthDialog.this.dismiss();
                return true;
            }

            // launch non-dialog URLs in a full browser
            getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(
                    new DialogError(description, errorCode, failingUrl));
            JdAuthDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d("Jingdong-WebView", "Webview loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            mSpinner.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();
        }

        @Override
        public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
            handler.proceed();
        }
    }

    private class GetAccessTokenTask extends AsyncTask<String, Integer, Object> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mSpinner.show();
        }

        protected void onPostExecute(Object result) {
            mSpinner.dismiss();
            JdAuthDialog.this.dismiss();

            if (result instanceof Exception) {
                mListener.onJdError(new JdException("Exception occurred: " +
                        ((Exception) result).toString()));
            } else if (result instanceof JSONObject) {
                JSONObject json = (JSONObject) result;
                Bundle bundle = new Bundle();
                bundle.putString(Constants.TOKEN, json.optString(Constants.TOKEN, ""));

                String sTime = json.optString(Constants.EXPIRES_TIME, "0");
                String sExpIn = json.optString(Constants.EXPIRES_IN, "0");
                long nExpires = 0;

                if (sTime != null && sExpIn != null) {
                    nExpires = Long.parseLong(sTime) + Long.parseLong(sExpIn);
                }

                bundle.putString(Constants.REFRESH_TOKEN,
                        json.optString(Constants.REFRESH_TOKEN, ""));
                bundle.putString(Constants.EXPIRES_TIME, sTime);
                bundle.putString(Constants.EXPIRES_IN, sExpIn);
                bundle.putString(Constants.EXPIRES, Long.toString(nExpires));

                mListener.onComplete(bundle);
            } else {
                mListener.onJdError(new JdException("Impossible path!"));
            }
        }

        @Override
        protected Object doInBackground(String... params) {
            String authKey = params[0];

            Bundle parameters = new Bundle();
            parameters.putString("grant_type", "authorization_code");
            parameters.putString("client_id", mAppId);
            parameters.putString("redirect_uri", Constants.REDIRECT_URI);
            parameters.putString("client_secret", mAppSecret);
            parameters.putString("state", "GET_TOKEN");
            parameters.putString("code", authKey);

            String url = mTokenUrl + "?" + Utils.encodeUrl(parameters);

            try {
                String sReponse = Utils.openUrl(url, "POST", parameters);
                JSONObject json = new JSONObject(sReponse);

                return json;
            } catch (Exception e) {
                // 返回所有的Exception， 在请求结束的onPostExecute方法封装成JdException，返回给调用者
                Log.e("Jingdong-WebView", "can not get access code: ", e);
                return e;
            }
        }
    }
}
