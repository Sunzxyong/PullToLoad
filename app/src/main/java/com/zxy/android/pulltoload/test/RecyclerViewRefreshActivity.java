package com.zxy.android.pulltoload.test;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.zxy.android.pulltoload.loadmore.RecyclerViewAdapterWrapper;
import com.zxy.android.pulltoload.refresh.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewRefreshActivity extends AppCompatActivity {

    private static final int REFRESH_TYPE = 1;

    private static final int LOAD_MORE_TYPE = 2;

    private RecyclerView mRecyclerView;

    private MyRecyclerViewAdapter mRealAdapter;

    private RecyclerViewAdapterWrapper mLoadMoreAdapterWrapper;

    private int mPage = 1;

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycler_view_simple_refresh);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_layout);

        setupView();
    }

    private void setupView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mRealAdapter = new MyRecyclerViewAdapter();

        setupLoadMore();

//        setupCustomRefreshView();

        // set auto refresh.
        mPullToRefreshLayout.setRefresh(true);

        mPullToRefreshLayout.setRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData(REFRESH_TYPE, new DataLoader.OnDataLoadCallback<String>() {
                    @Override
                    public void onSuccess(List<String> data) {
                        mPullToRefreshLayout.setRefresh(false);
                        mRealAdapter.setData(data);
                    }

                    @Override
                    public void onFailed() {

                    }
                });
            }
        });

    }

    private void setupLoadMore() {

        //wrap the real adapter.
        mLoadMoreAdapterWrapper = new RecyclerViewAdapterWrapper(mRealAdapter);
        mRecyclerView.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
                super.getItemOffsets(outRect, view, parent, state);
                if (parent.getChildPosition(view) == 0)
                    return;
                outRect.top = 16;
            }
        });
        mRecyclerView.setAdapter(mLoadMoreAdapterWrapper);

        mLoadMoreAdapterWrapper.setOnLoadMoreListener(new RecyclerViewAdapterWrapper.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                loadData(LOAD_MORE_TYPE, new DataLoader.OnDataLoadCallback<String>() {
                    @Override
                    public void onSuccess(List<String> data) {
                        if (data == null || data.isEmpty()) {
                            //without more data.
                            mLoadMoreAdapterWrapper.setupNoMoreDataStatus();
                        } else {
                            mRealAdapter.addData(data);
                        }
                    }

                    @Override
                    public void onFailed() {

                    }
                });
            }
        });
    }

    private void setupCustomRefreshView() {
        //setup a fixed refresh view.
//        mPullToRefreshLayout.setRefreshView(getRefreshView("我是固定的刷新视图"));

        final TextView prepareTv = (TextView) getRefreshView("下拉刷新");
        mPullToRefreshLayout.setOnPrepareView(prepareTv);
        mPullToRefreshLayout.setOnReadyView(getRefreshView("释放刷新"));
        mPullToRefreshLayout.setOnRefreshingView(getRefreshView("正在刷新..."));
        mPullToRefreshLayout.setOnFinishView(getRefreshView("刷新完成"));

        //or listen to pull-down status.
        mPullToRefreshLayout.addRefreshStateListener(new PullToRefreshLayout.OnRefreshStateListener() {
            @Override
            public void onPrepare(int curOffset, int maxOffset) {
                prepareTv.setText("下拉刷新(" + (curOffset * 100 / maxOffset) + "%)");
            }

            @Override
            public void onReady(int offset) {
            }

            @Override
            public void onRefreshing(int offset) {
            }

            @Override
            public void onFinish() {
            }
        });
    }

    private View getRefreshView(String message) {
        TextView view = new TextView(this);
        view.setText(message);
        view.setPadding(0, 50, 0, 50);
        view.setTextSize(16);
        view.setTextColor(Color.rgb(120, 120, 120));
        return view;
    }

    private void loadData(int type, final DataLoader.OnDataLoadCallback<String> callback) {
        if (type == REFRESH_TYPE) {
            mPage = 1;
        }

        if (type == REFRESH_TYPE) {
            DataLoader.getInstance().loadData(callback);
        } else if (type == LOAD_MORE_TYPE) {
            DataLoader.getInstance().loadData(new DataLoader.OnDataLoadCallback<String>() {
                @Override
                public void onSuccess(List<String> data) {
                    if (mPage > 4) {
                        callback.onSuccess(new ArrayList<String>());
                    } else {
                        callback.onSuccess(data);
                    }
                }

                @Override
                public void onFailed() {

                }
            });
        }

        mPage++;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.refresh_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh:
                mPullToRefreshLayout.setRefresh(true);
                break;
            case R.id.finish:
                mPullToRefreshLayout.setRefresh(false);
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
