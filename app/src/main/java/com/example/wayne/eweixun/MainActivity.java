package com.example.wayne.eweixun;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


public class MainActivity extends Activity {

    public String TAG = getClass().getSimpleName();
    private Button searchButton;
    private EditText searchText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        searchText=(EditText)findViewById(R.id.search_text);
        searchButton=(Button)findViewById(R.id.search_button);

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String keyword = searchText.getText().toString().trim();
                Log.d(TAG, "search text is: " + keyword);

                Intent intent = new Intent(MainActivity.this, WareListActivity.class);
                intent.putExtra("keyword", keyword);
                startActivity(intent);
            }
        });



    }
}
