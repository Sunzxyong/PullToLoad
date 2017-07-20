package com.zxy.android.pulltoload.test;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.zxy.android.pulltoload.loadmore.RecyclerViewAdapterWrapper;
import com.zxy.android.pulltoload.refresh.PullToRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PAGE_NUM = 20;

    private static final int REFRESH_TYPE = 1;

    private static final int LOAD_MORE_TYPE = 2;

    private RecyclerView mRecyclerView;

    private MyAdapter mAdapter;

    private RecyclerViewAdapterWrapper mAdapterWrapper;

    private int mPage = 1;

    private PullToRefreshLayout mPullToRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mPullToRefreshLayout = (PullToRefreshLayout) findViewById(R.id.refresh_layout);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(linearLayoutManager);

        mAdapter = new MyAdapter();

        mAdapterWrapper = new RecyclerViewAdapterWrapper(mAdapter);

        mRecyclerView.setAdapter(mAdapterWrapper);
        //custom load more view.
//        mAdapterWrapper.setLoadMoreView(LayoutInflater.from(this).inflate(R.layout.load_more_status, null));
//        mAdapterWrapper.setNoMoreView(LayoutInflater.from(this).inflate(R.layout.no_more_status, null));

        mAdapterWrapper.setOnLoadMoreListener(new RecyclerViewAdapterWrapper.OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(LOAD_MORE_TYPE);
                    }
                }, 2000);
            }
        });

//        mPullToRefreshLayout.setRefreshView(getRefreshView("hello"));

//        final TextView prepareTv = (TextView) getRefreshView("下拉刷新");
//        mPullToRefreshLayout.setOnPrepareView(prepareTv);
//        mPullToRefreshLayout.setOnReadyView(getRefreshView("释放刷新"));
//        mPullToRefreshLayout.setOnRefreshingView(getRefreshView("正在刷新..."));
//        mPullToRefreshLayout.setOnFinishView(getRefreshView("刷新完成"));

        mPullToRefreshLayout.setRefresh(true);

        mPullToRefreshLayout.setRefreshListener(new PullToRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.e("zxy", "onRefresh.");
                new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadData(REFRESH_TYPE);
                        mPullToRefreshLayout.setRefresh(false);
                    }
                }, 2000);
            }
        });

        mPullToRefreshLayout.addRefreshStateListener(new PullToRefreshLayout.OnRefreshStateListener() {
            @Override
            public void onPrepare(int curOffset, int maxOffset) {
//                Log.e("zxy", "onPrepare:curOffset->" + curOffset + ",maxOffset->" + maxOffset);
//                prepareTv.setText("下拉刷新(" + (curOffset * 100 / maxOffset) + "%)");
            }

            @Override
            public void onReady(int offset) {
//                Log.e("zxy", "onReady:offset->" + offset);
            }

            @Override
            public void onRefreshing(int offset) {
//                Log.e("zxy", "onRefreshing:offset->" + offset);
            }

            @Override
            public void onFinish() {
//                Log.e("zxy", "onFinish.");
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
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

    private void loadData(final int type) {

        if (type == REFRESH_TYPE) {
            mPage = 1;
        }

        if (type == REFRESH_TYPE) {
            mAdapter.setData(makeData(PAGE_NUM));
        } else if (type == LOAD_MORE_TYPE) {
            if (makeData(PAGE_NUM).isEmpty()) {
                mAdapterWrapper.setupNoMoreDataStatus();
            } else {
                mAdapter.addData(makeData(PAGE_NUM));
            }
        }

        mPage++;
    }

    private List<String> makeData(int num) {
        List<String> data = new ArrayList<>();
        for (int i = num * (mPage - 1); i < num * mPage; i++) {
            data.add("the number -> " + i);
        }

        if (mPage >= 4)
            return new ArrayList<>();
        return data;
    }

    static final class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private List<String> mData = new ArrayList<>();

        public void setData(List<String> data) {
            if (data == null || data.isEmpty())
                return;
            mData.clear();
            mData.addAll(data);
            notifyDataSetChanged();
        }

        public void addData(String data) {
            mData.add(data);
            notifyItemInserted(mData.size() - 1);
        }

        public void addData(List<String> data) {
            if (data == null || data.isEmpty())
                return;
            mData.addAll(data);
            notifyDataSetChanged();
        }

        public void remove(int position) {
            if (position < 0 || position >= getItemCount())
                return;
            mData.remove(position);
            notifyItemRemoved(position);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MyViewHolder(LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false));
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.mTextView.setText(mData.get(position));
            holder.itemView.setBackgroundColor(0xffffffff);
        }

        @Override
        public int getItemCount() {
            return mData.size();
        }
    }

    static final class MyViewHolder extends RecyclerView.ViewHolder {
        TextView mTextView;

        public MyViewHolder(View itemView) {
            super(itemView);
            mTextView = (TextView) itemView.findViewById(android.R.id.text1);
        }
    }

}
