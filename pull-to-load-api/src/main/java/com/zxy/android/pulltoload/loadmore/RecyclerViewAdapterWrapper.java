package com.zxy.android.pulltoload.loadmore;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.zxy.android.pulltoload.R;

import java.util.List;

/**
 * Created by zhengxiaoyong on 2017/7/14.
 */
public class RecyclerViewAdapterWrapper extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int LOAD_MORE_TYPE = Integer.MAX_VALUE - 1;

    private static final FrameLayout.LayoutParams DEFAULT_VIEW_LAYOUT_PARAMS = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

    private final RecyclerView.Adapter mDelegateAdapter;

    private View mLoadMoreView;

    private View mNoMoreView;

    private View mViewContainer;

    private OnLoadMoreListener mOnLoadMoreListener;

    private volatile boolean isLocked = false;

    private volatile boolean isNoMoreData = false;

    private final RecyclerView.AdapterDataObserver mAdapterDataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            isLocked = false;
            setupHasMoreDataStatus();
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount) {
            super.onItemRangeChanged(positionStart, itemCount);
        }

        @Override
        public void onItemRangeChanged(int positionStart, int itemCount, Object payload) {
            super.onItemRangeChanged(positionStart, itemCount, payload);
        }

        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            super.onItemRangeInserted(positionStart, itemCount);
            isLocked = false;
        }

        @Override
        public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
            super.onItemRangeMoved(fromPosition, toPosition, itemCount);
        }

        @Override
        public void onItemRangeRemoved(int positionStart, int itemCount) {
            super.onItemRangeRemoved(positionStart, itemCount);
        }
    };

    private final RecyclerView.OnScrollListener mScrollListener = new RecyclerView.OnScrollListener() {
        @Override
        public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
            super.onScrolled(recyclerView, dx, dy);
            if (isNoMoreData)
                return;
            if (mDelegateAdapter.getItemCount() == 0)
                return;
            final View child = recyclerView.getLayoutManager().findViewByPosition(getItemCount() - 1);
            if (child != null && child == mViewContainer && !isLocked) {
                isLocked = true;
                if (mOnLoadMoreListener != null)
                    mOnLoadMoreListener.onLoadMore();
            }
        }
    };

    public RecyclerViewAdapterWrapper(RecyclerView.Adapter adapter) {
        if (adapter == null)
            throw new RuntimeException("delegate adapter can not be null!");
        mDelegateAdapter = adapter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == LOAD_MORE_TYPE) {
            mViewContainer = new FrameLayout(parent.getContext());
            mViewContainer.setLayoutParams(DEFAULT_VIEW_LAYOUT_PARAMS);
            return new ViewHolder(mViewContainer);
        }
        return mDelegateAdapter.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == LOAD_MORE_TYPE) {
            if (holder.itemView == null || holder.itemView != mViewContainer)
                return;

            if (mDelegateAdapter.getItemCount() == 0) {
                if (mViewContainer.getVisibility() != View.GONE)
                    mViewContainer.setVisibility(View.GONE);
            } else {
                if (mViewContainer.getVisibility() != View.VISIBLE)
                    mViewContainer.setVisibility(View.VISIBLE);
            }

            ((FrameLayout) holder.itemView).removeAllViews();
            if (isNoMoreData) {
                ((FrameLayout) holder.itemView).addView(mNoMoreView, DEFAULT_VIEW_LAYOUT_PARAMS);
            } else {
                ((FrameLayout) holder.itemView).addView(mLoadMoreView, DEFAULT_VIEW_LAYOUT_PARAMS);
            }

        } else {
            mDelegateAdapter.onBindViewHolder(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return mDelegateAdapter.getItemCount() + 1;
    }

    @Override
    public long getItemId(int position) {
        return mDelegateAdapter.getItemId(position);
    }

    @Override
    public int getItemViewType(int position) {
        if (mDelegateAdapter.getItemCount() == position)
            return LOAD_MORE_TYPE;
        return mDelegateAdapter.getItemViewType(position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        mDelegateAdapter.onAttachedToRecyclerView(recyclerView);
        if (mLoadMoreView == null)
            mLoadMoreView = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.ptl_view_load_more_status, null);
        if (mNoMoreView == null)
            mNoMoreView = LayoutInflater.from(recyclerView.getContext()).inflate(R.layout.ptl_view_no_more_status, null);

        recyclerView.addOnScrollListener(mScrollListener);
        registerAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position, List<Object> payloads) {
        super.onBindViewHolder(holder, position, payloads);
    }

    @Override
    public void onDetachedFromRecyclerView(RecyclerView recyclerView) {
        mDelegateAdapter.onDetachedFromRecyclerView(recyclerView);
        recyclerView.removeOnScrollListener(mScrollListener);
        unregisterAdapterDataObserver(mAdapterDataObserver);
    }

    @Override
    public boolean onFailedToRecycleView(RecyclerView.ViewHolder holder) {
        return mDelegateAdapter.onFailedToRecycleView(holder);
    }

    @Override
    public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
        mDelegateAdapter.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(RecyclerView.ViewHolder holder) {
        mDelegateAdapter.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(RecyclerView.ViewHolder holder) {
        mDelegateAdapter.onViewRecycled(holder);
    }

    @Override
    public void registerAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mDelegateAdapter.registerAdapterDataObserver(observer);
    }

    @Override
    public void setHasStableIds(boolean hasStableIds) {
        mDelegateAdapter.setHasStableIds(hasStableIds);
    }

    @Override
    public void unregisterAdapterDataObserver(RecyclerView.AdapterDataObserver observer) {
        mDelegateAdapter.unregisterAdapterDataObserver(observer);
    }

    public void setupHasMoreDataStatus() {
        isNoMoreData = false;
        if (mViewContainer == null)
            return;
        ((FrameLayout) mViewContainer).removeAllViews();
        ((FrameLayout) mViewContainer).addView(mLoadMoreView, DEFAULT_VIEW_LAYOUT_PARAMS);
    }

    public void setupNoMoreDataStatus() {
        isNoMoreData = true;
        if (mViewContainer == null)
            return;
        ((FrameLayout) mViewContainer).removeAllViews();
        ((FrameLayout) mViewContainer).addView(mNoMoreView, DEFAULT_VIEW_LAYOUT_PARAMS);
    }

    public void setupLoadFailedStatus() {
        setupHasMoreDataStatus();
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        mOnLoadMoreListener = listener;
    }

    public View getLoadMoreView() {
        return mLoadMoreView;
    }

    public void setLoadMoreView(View view) {
        if (view == null)
            return;
        mLoadMoreView = view;
    }

    public View getNoMoreView() {
        return mNoMoreView;
    }

    public void setNoMoreView(View view) {
        if (view == null)
            return;
        mNoMoreView = view;
    }

    public interface OnLoadMoreListener {
        void onLoadMore();
    }

    private static final class ViewHolder extends RecyclerView.ViewHolder {

        ViewHolder(View itemView) {
            super(itemView);
        }
    }
}
