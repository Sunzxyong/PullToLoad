package com.zxy.android.pulltoload.test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;

import com.zxy.android.pulltoload.refresh.PullToRefreshLayout;

public class ScrollViewRefreshActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scroll_view_refresh);

        final PullToRefreshLayout refreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_layout);

        //auto refresh.
//        refreshLayout.setRefresh(true);

        refreshLayout.setRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refreshLayout.setRefresh(false);
                    }
                }, 2000);
            }
        });

    }
}
