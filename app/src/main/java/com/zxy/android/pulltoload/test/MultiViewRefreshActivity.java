package com.zxy.android.pulltoload.test;

import android.graphics.Rect;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.zxy.android.pulltoload.refresh.PullToRefreshLayout;

import java.util.List;

public class MultiViewRefreshActivity extends AppCompatActivity {

    private PullToRefreshLayout mRefreshLayout;

    private RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_view_refresh);

        mRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_layout);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildPosition(view) == 0)
                    return;
                outRect.top = 16;
            }
        });

        final MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter();
        mRecyclerView.setAdapter(adapter);

        DataLoader.getInstance().loadData(new DataLoader.OnDataLoadCallback<String>() {
            @Override
            public void onSuccess(List<String> data) {
                adapter.setData(data);
            }

            @Override
            public void onFailed() {

            }
        });

        mRefreshLayout.setRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                DataLoader.getInstance().loadData(new DataLoader.OnDataLoadCallback<String>() {
                    @Override
                    public void onSuccess(List<String> data) {
                        adapter.setData(data);
                        mRefreshLayout.setRefresh(false);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
            }
        });
    }


}
