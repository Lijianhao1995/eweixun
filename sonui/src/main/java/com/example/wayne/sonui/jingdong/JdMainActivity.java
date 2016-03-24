package com.example.wayne.sonui.jingdong;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.example.wayne.sonui.MainActivity;
import com.example.wayne.sonui.R;


public class JdMainActivity extends Activity {

    public String TAG = getClass().getSimpleName();
    private Button searchButton;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.jd_activity_main);

        searchText=(EditText)findViewById(R.id.search_text);
        searchButton=(Button)findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = searchText.getText().toString().trim();
                Log.d(TAG, "search text is: " + keyword);

                Intent intent = new Intent(JdMainActivity.this, WareListActivity.class);
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            }
        });



    }
}
