package com.zxy.android.pulltoload.test;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn1:
                startActivity(new Intent(this, SimpleViewRefreshActivity.class));
                break;
            case R.id.btn2:
                startActivity(new Intent(this, RecyclerViewRefreshActivity.class));
                break;
            case R.id.btn3:
                startActivity(new Intent(this, ScrollViewRefreshActivity.class));
                break;
            case R.id.btn4:
                startActivity(new Intent(this, MultiViewRefreshActivity.class));
                break;
            case R.id.btn5:
                startActivity(new Intent(this, MultiScrollRefreshActivity.class));
                break;
        }
    }
}
