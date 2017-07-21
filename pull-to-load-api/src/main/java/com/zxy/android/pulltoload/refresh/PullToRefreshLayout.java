package com.zxy.android.pulltoload.refresh;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.NestedScrollingParent;
import android.support.v4.view.NestedScrollingParentHelper;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Scroller;
import android.widget.TextView;

import com.zxy.android.pulltoload.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengxiaoyong on 2017/7/14.
 */
public class PullToRefreshLayout extends ViewGroup implements NestedScrollingParent, NestedScrollingChild {

    private static final float DEFAULT_SCROLL_FACTOR = 0.25f;
    private static final float DEFAULT_REFRESH_STATE_FACTOR = 1.2f;
    private static final int DEFAULT_REFRESH_STATE_DURATION = 300;
    private static final int DEFAULT_FINISH_STATE_DURATION = 450;

    private static final int MESSAGE_VIEW_RESIZE = 100;
    private static final int MESSAGE_VIEW_TRAVERSE = 101;

    private NestedScrollingParentHelper mNestedScrollingParentHelper;
    private NestedScrollingChildHelper mNestedScrollingChildHelper;
    private final int[] mParentOffsetInWindow = new int[2];

    private final RelativeLayout mRefreshContainer = new RelativeLayout(getContext());
    private float mDampFactor = DEFAULT_SCROLL_FACTOR;
    private View mContentView;
    private boolean isRefreshing = false;
    private REFRESH_STATE mRefreshState = REFRESH_STATE.NO_STATE;

    private View mPrepareView;
    private View mReadyView;
    private View mRefreshingView;
    private View mFinishView;

    private int mRefreshHeight;
    private int mPendingHeight;
    private Scroller mScroller;
    private int mTouchSlop;
    private float mStartX;
    private float mStartY;
    private float mLastMoveY;

    private View mDefaultRefreshView;

    private OnRefreshListener mRefreshListener;
    private List<OnRefreshStateListener> mRefreshStateListeners = new ArrayList<>();
    private OnChildScrollUpCallback mChildScrollUpCallback;

    private DefaultScrollUpProcessor mDefaultScrollUpProcessor;

    private OnRefreshStateListener mRefreshStateWatcher = new OnRefreshStateListener() {
        @Override
        public void onPrepare(int curOffset, int maxOffset) {
            mRefreshState = REFRESH_STATE.PREPARE;

            if (!getCurrentRefreshView().equals(mPrepareView))
                updateRefreshView(mPrepareView);
        }

        @Override
        public void onReady(int offset) {
            mRefreshState = REFRESH_STATE.READY;

            if (!getCurrentRefreshView().equals(mReadyView))
                updateRefreshView(mReadyView);
        }

        @Override
        public void onRefreshing(int offset) {
            mRefreshState = REFRESH_STATE.REFRESHING;
            if (!getCurrentRefreshView().equals(mRefreshingView))
                updateRefreshView(mRefreshingView);
        }

        @Override
        public void onFinish() {
            mRefreshState = REFRESH_STATE.FINISH;
            if (!getCurrentRefreshView().equals(mFinishView))
                updateRefreshView(mFinishView);
        }
    };

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == MESSAGE_VIEW_RESIZE) {
                mRefreshContainer.setLayoutParams(new PullToRefreshLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, getMeasuredHeight()));
            } else if (msg.what == MESSAGE_VIEW_TRAVERSE) {
                mDefaultScrollUpProcessor.collectViewGroup(mContentView);
            }
        }
    };

    public enum REFRESH_STATE {
        PREPARE(1),
        READY(2),
        REFRESHING(3),
        FINISH(4),
        NO_STATE(0);

        REFRESH_STATE(int value) {
        }
    }

    public PullToRefreshLayout(Context context) {
        super(context);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mNestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        mNestedScrollingChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);

        mScroller = new Scroller(getContext(), new DecelerateInterpolator());
        ViewConfiguration configuration = ViewConfiguration.get(getContext());
        mTouchSlop = configuration.getScaledTouchSlop();

        mRefreshContainer.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.BOTTOM);

        mDefaultScrollUpProcessor = new DefaultScrollUpProcessor();

        addRefreshStateListener(mRefreshStateWatcher);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        ensureChildCount();

        enableRefreshView();

        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        final int heightSize = MeasureSpec.getSize(heightMeasureSpec);

        final int count = getChildCount();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE)
                continue;
            measureChildWithMargins(getChildAt(i), widthMeasureSpec, 0, heightMeasureSpec, 0);
        }

        if (count <= 1) {
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(0, 0);
            } else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(0, heightSize);
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, 0);
            }
            return;
        }

        View realChild = getChildAt(1);
        if (realChild == null) {
            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(0, 0);
            } else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(0, heightSize);
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, 0);
            }
        } else {
            MarginLayoutParams params = (MarginLayoutParams) realChild.getLayoutParams();
            int childWidth = realChild.getMeasuredWidth();
            int childHeight = realChild.getMeasuredHeight();

            if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(childWidth + params.leftMargin + params.rightMargin, childHeight + params.topMargin + params.bottomMargin);
            } else if (widthMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(childWidth + params.leftMargin + params.rightMargin, heightSize);
            } else if (heightMode == MeasureSpec.AT_MOST) {
                setMeasuredDimension(widthSize, childHeight + params.topMargin + params.bottomMargin);
            }
        }

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        ensureChildCount();

        enableRefreshView();

        if (changed && getMeasuredHeight() != mRefreshContainer.getMeasuredHeight())
            mHandler.sendEmptyMessage(MESSAGE_VIEW_RESIZE);

        View contentChild = getChildAt(1);
        if (mContentView != contentChild) {
            mContentView = contentChild;
            mHandler.sendEmptyMessage(MESSAGE_VIEW_TRAVERSE);
        }

        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child == null || child.getVisibility() == GONE)
                continue;
            MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
            int childLeft = getPaddingLeft() + lp.leftMargin;
            int childTop = getPaddingTop() + lp.topMargin;

            int childWidth = child.getMeasuredWidth();
            int layoutHeight = getMeasuredHeight() - lp.topMargin - lp.bottomMargin - getPaddingBottom() - getPaddingTop();
            child.layout(childLeft, (i - 1) * getMeasuredHeight() + childTop, childWidth + childLeft, (i - 1) * getMeasuredHeight() + layoutHeight + childTop);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction() & MotionEvent.ACTION_MASK;
        if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP)
            return super.onInterceptTouchEvent(ev); // do not intercept touch event, let the child handle it.
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mStartY = ev.getRawY();
                mStartX = ev.getRawX();

                mDefaultScrollUpProcessor.filterViewGroup(ev);
                break;
            case MotionEvent.ACTION_MOVE:
                mLastMoveY = ev.getRawY();

                int dx = (int) (ev.getRawX() - mStartX);
                int dy = (int) (mLastMoveY - mStartY);
                if (Math.abs(dx) > Math.abs(dy))
                    return super.onInterceptTouchEvent(ev);

                if (isRefreshing) {
                    int scrollY = (int) (mStartY - mLastMoveY);
                    if (scrollY > 0 && mPendingHeight > 0) {
                        if (scrollY > mPendingHeight)
                            scrollY = mPendingHeight;
                        scrollBy(0, scrollY);
                        mPendingHeight -= scrollY;
                        mStartY = mLastMoveY;
                    }

                    if (mPendingHeight <= 0) {
                        mPendingHeight = 0;
                        isRefreshing = false;
                    }
                } else {
                    if (Math.abs(dy) > mTouchSlop) {
                        if (dy > 0) {
                            return !canChildScrollUp();
                        } else {
                            return super.onInterceptTouchEvent(ev);
                        }
                    }
                }
                break;
            default:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_MOVE:
                int scrollY = (int) (mLastMoveY - ev.getRawY());
                if (scrollY >= 0) {
                    scrollY = computeScrollOffset(scrollY, getScrollY(), getMeasuredHeight());
                } else {
                    scrollY = -computeScrollOffset(scrollY, getScrollY(), getMeasuredHeight());
                }

                //handling the pull-up after pull-down.
                if (scrollY > 0 && getScrollY() < 0 && scrollY > Math.abs(getScrollY())) {
                    scrollY = -getScrollY();
                }
                if (getScrollY() == 0 && scrollY > 0)
                    scrollY = 0;

                scrollBy(0, scrollY);
                mLastMoveY = ev.getRawY();

                if (Math.abs(getScrollY()) < mRefreshHeight * DEFAULT_REFRESH_STATE_FACTOR) {
                    for (int i = 0; i < mRefreshStateListeners.size(); i++) {
                        OnRefreshStateListener listener = mRefreshStateListeners.get(i);
                        if (listener == null)
                            continue;
                        listener.onPrepare(Math.abs(getScrollY()), (int) (mRefreshHeight * DEFAULT_REFRESH_STATE_FACTOR));
                    }
                } else {
                    for (int i = 0; i < mRefreshStateListeners.size(); i++) {
                        OnRefreshStateListener listener = mRefreshStateListeners.get(i);
                        if (listener == null)
                            continue;
                        listener.onReady(Math.abs(getScrollY()));
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (getScrollY() < 0) {
                    if (Math.abs(getScrollY()) >= mRefreshHeight * DEFAULT_REFRESH_STATE_FACTOR) {
                        //refreshing.
                        isRefreshing = true;
                        mPendingHeight = mRefreshHeight;

                        mScroller.startScroll(0, getScrollY(), 0, -getScrollY() - mRefreshHeight, DEFAULT_REFRESH_STATE_DURATION);

                        if (mRefreshListener != null)
                            mRefreshListener.onRefresh();
                        for (int i = 0; i < mRefreshStateListeners.size(); i++) {
                            OnRefreshStateListener listener = mRefreshStateListeners.get(i);
                            if (listener == null)
                                continue;
                            listener.onRefreshing(mRefreshHeight);
                        }
                    } else {
                        //recover.
                        isRefreshing = false;
                        mPendingHeight = 0;
                        mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), DEFAULT_REFRESH_STATE_DURATION);
                    }
                    invalidate();
                }
                break;
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    private TextView mRefreshInfoTv;
    private ImageView mRefreshInfoImg;
    private ProgressBar mRefreshProgressBar;

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        addRefreshView();
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (!mScroller.computeScrollOffset())
            return;
        scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
        invalidate();
    }

    private int computeScrollOffset(int value, int curOffset, int maxOffset) {
        if (curOffset >= maxOffset)
            curOffset = (int) (maxOffset * 0.9);
        return (int) (Math.abs(value) * (Math.pow(1 - (Math.abs((double) curOffset / (double) maxOffset)), 0.75 + (1 + mDampFactor))));
    }

    @Override
    protected LayoutParams generateDefaultLayoutParams() {
        return new PullToRefreshLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new PullToRefreshLayout.LayoutParams(getContext(), attrs);
    }

    @Override
    protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new PullToRefreshLayout.LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof PullToRefreshLayout.LayoutParams;
    }

    //impl NestedScrollingParent.

    @Override
    public boolean onStartNestedScroll(View child, View target, int nestedScrollAxes) {
        //whether need to cooperate with the child for processing scrolling together.
        return isEnabled() && !isRefreshing && (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
    }

    @Override
    public void onNestedScrollAccepted(View child, View target, int axes) {
        mNestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
        startNestedScroll(axes & ViewCompat.SCROLL_AXIS_VERTICAL);
    }

    @Override
    public void onStopNestedScroll(View child) {
        mNestedScrollingParentHelper.onStopNestedScroll(child);
        stopNestedScroll();
    }

    @Override
    public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
        dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed,
                mParentOffsetInWindow);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
        dispatchNestedPreScroll(dx, dy, consumed, null);
    }

    @Override
    public boolean onNestedFling(View target, float velocityX, float velocityY,
                                 boolean consumed) {
        return dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean onNestedPreFling(View target, float velocityX,
                                    float velocityY) {
        return dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public int getNestedScrollAxes() {
        return mNestedScrollingParentHelper.getNestedScrollAxes();
    }

    //impl NestedScrollingChild.

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mNestedScrollingChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mNestedScrollingChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mNestedScrollingChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mNestedScrollingChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mNestedScrollingChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed,
                                        int dyUnconsumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed,
                dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mNestedScrollingChildHelper.dispatchNestedPreScroll(
                dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mNestedScrollingChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mNestedScrollingChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        if (mContentView != null && ((android.os.Build.VERSION.SDK_INT < 21 && mContentView instanceof AbsListView)
                || !ViewCompat.isNestedScrollingEnabled(mContentView))) {
            // Nope.
        } else {
            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }
    }

    private void enableRefreshView() {
        View child = getChildAt(0);
        if (child == null || child != mRefreshContainer)
            addRefreshView();
    }

    private void addRefreshView() {
        //add refresh container.
        addView(mRefreshContainer, 0);

        mRefreshContainer.setBackgroundColor(0xFFF0F0F0);
        mRefreshContainer.removeAllViews();
        mDefaultRefreshView = LayoutInflater.from(getContext()).inflate(R.layout.ptl_view_refresh_status, null);
        mRefreshInfoTv = (TextView) mDefaultRefreshView.findViewById(R.id.tv_refresh_info);
        mRefreshInfoImg = (ImageView) mDefaultRefreshView.findViewById(R.id.img_refresh_info);
        mRefreshProgressBar = (ProgressBar) mDefaultRefreshView.findViewById(R.id.progress_refreshing);
        mRefreshContainer.addView(mDefaultRefreshView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        addRefreshStateListener(new OnRefreshStateListener() {
            @Override
            public void onPrepare(int curOffset, int maxOffset) {
                mRefreshInfoTv.setText(getResources().getString(R.string.ptl_prepare_to_refresh));
                mRefreshInfoImg.setVisibility(VISIBLE);
                mRefreshProgressBar.setVisibility(GONE);
                mRefreshInfoImg.setImageResource(R.drawable.ptl_ic_arrow_downward);
            }

            @Override
            public void onReady(int offset) {
                mRefreshInfoTv.setText(getResources().getString(R.string.ptl_ready_to_refresh));
                mRefreshInfoImg.setVisibility(VISIBLE);
                mRefreshProgressBar.setVisibility(GONE);
                mRefreshInfoImg.setImageResource(R.drawable.ptl_ic_arrow_upward);
            }

            @Override
            public void onRefreshing(int offset) {
                mRefreshInfoTv.setText(getResources().getString(R.string.ptl_is_refreshing));
                mRefreshProgressBar.setVisibility(VISIBLE);
                mRefreshInfoImg.setVisibility(GONE);
            }

            @Override
            public void onFinish() {
                mRefreshInfoTv.setText(getResources().getString(R.string.ptl_finish_to_refresh));
                mRefreshProgressBar.setVisibility(GONE);
                mRefreshInfoImg.setVisibility(GONE);
            }
        });

        enableRefreshHeight(mDefaultRefreshView);
        ensureChildCount();
    }

    public void setRefresh(final boolean refresh) {
        post(new Runnable() {
            @Override
            public void run() {
                isRefreshing = refresh;
                if (refresh) {
                    mPendingHeight = mRefreshHeight;
                    mScroller.startScroll(0, getScrollY(), 0, -getScrollY() - mRefreshHeight, DEFAULT_REFRESH_STATE_DURATION);

                    if (mRefreshListener != null)
                        mRefreshListener.onRefresh();

                    for (int i = 0; i < mRefreshStateListeners.size(); i++) {
                        OnRefreshStateListener listener = mRefreshStateListeners.get(i);
                        if (listener == null)
                            continue;
                        listener.onRefreshing(mRefreshHeight);
                    }
                } else {
                    for (int i = 0; i < mRefreshStateListeners.size(); i++) {
                        OnRefreshStateListener listener = mRefreshStateListeners.get(i);
                        if (listener == null)
                            continue;
                        listener.onFinish();
                    }
                    mScroller.startScroll(0, getScrollY(), 0, -getScrollY(), DEFAULT_FINISH_STATE_DURATION);
                }
                invalidate();
            }
        });
    }

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshListener(OnRefreshListener refreshListener) {
        mRefreshListener = refreshListener;
    }

    public void addRefreshStateListener(OnRefreshStateListener refreshStateListener) {
        mRefreshStateListeners.add(refreshStateListener);
    }

    public void removeOnRefreshStateListener(OnRefreshStateListener refreshStateListener) {
        if (mRefreshStateListeners.contains(refreshStateListener))
            mRefreshStateListeners.remove(refreshStateListener);
    }

    public void setChildScrollUpCallback(OnChildScrollUpCallback childScrollUpCallback) {
        mChildScrollUpCallback = childScrollUpCallback;
    }

    public void setRefreshHeight(int height) {
        if (height <= 0)
            return;
        mRefreshHeight = height;
    }

    public void setRefreshContainerBackground(Drawable drawable) {
        if (drawable == null)
            return;
        mRefreshContainer.setBackgroundDrawable(drawable);
    }

    public void setRefreshContainerBackground(int color) {
        mRefreshContainer.setBackgroundColor(color);
    }

    public REFRESH_STATE getRefreshState() {
        return mRefreshState;
    }

    public void setRefreshView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        enableRefreshHeight(view);
        mRefreshContainer.removeAllViews();
        mRefreshContainer.addView(view);
    }

    public void setDampFactor(float factor) {
        if (factor < 0f || factor > 1)
            return;
        mDampFactor = factor;
    }

    public void setOnPrepareView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        enableRefreshHeight(view);
        mPrepareView = view;
    }

    public void setOnReadyView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        enableRefreshHeight(view);
        mReadyView = view;
    }

    public void setOnRefreshingView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        enableRefreshHeight(view);
        mRefreshingView = view;
    }

    public void setOnFinishView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        enableRefreshHeight(view);
        mFinishView = view;
    }

    private View getCurrentRefreshView() {
        View view = mRefreshContainer.getChildAt(0);
        if (view == null) {
            mRefreshContainer.removeAllViews();
            view = mDefaultRefreshView;
            mRefreshContainer.addView(view);
            enableRefreshHeight(view);
        }
        return view;
    }

    private void updateRefreshView(View view) {
        if (view == null || view.getVisibility() != VISIBLE)
            return;
        mRefreshContainer.removeAllViews();
        mRefreshContainer.addView(view);
    }

    private void enableRefreshHeight(View view) {
        view.measure(0, 0);
        mRefreshHeight = view.getMeasuredHeight();
    }

    private boolean canChildScrollUp() {
        if (mChildScrollUpCallback != null)
            return mChildScrollUpCallback.canChildScrollUp(this, mContentView);
        return mDefaultScrollUpProcessor.canScrollUp();
    }

    private void ensureChildCount() {
        int count = getChildCount();
        if (count > 2)
            throw new IllegalStateException("PullToRefreshLayout can contain one child view only!");
    }

    public interface OnRefreshListener {
        void onRefresh();
    }

    public interface OnRefreshStateListener {
        void onPrepare(int curOffset, int maxOffset);

        void onReady(int offset);

        void onRefreshing(int offset);

        void onFinish();
    }

    private static final class DefaultScrollUpProcessor {

        private List<ViewGroup> mChildViewGroups = new ArrayList<>();

        private List<ViewGroup> mInAreaViewGroups = new ArrayList<>();

        private ViewTraverser mViewTraverser = ViewTraverser.create();

        private View mRootView;

        private ViewTraverser.Accessor mViewAccessor = new ViewTraverser.Accessor() {
            @Override
            public void access(View view) {
                if (view == null || !view.isEnabled() || view.getVisibility() == GONE)
                    return;
                if (view instanceof ViewGroup)
                    mChildViewGroups.add((ViewGroup) view);
            }
        };

        void collectViewGroup(View view) {
            mRootView = view;
            if (mRootView == null) {
                mChildViewGroups.clear();
                return;
            }
            mChildViewGroups.clear();
            if (mRootView instanceof ViewGroup) {
                mViewTraverser.setAccessor(mViewAccessor).traverse((ViewGroup) mRootView);
            }
        }

        void filterViewGroup(MotionEvent ev) {
            int downX = Math.round(ev.getX());
            int downY = Math.round(ev.getY());
            int size = mChildViewGroups.size();
            mInAreaViewGroups.clear();
            for (int i = 0; i < size; i++) {
                ViewGroup child = mChildViewGroups.get(i);
                if (child == null || child.getVisibility() == View.GONE)
                    continue;
                if (downX > child.getLeft() && downX < child.getRight() && downY > child.getTop() && downY < child.getBottom())
                    mInAreaViewGroups.add(child);
            }
        }

        boolean canScrollUp() {
            int size = mInAreaViewGroups.size();
            for (int i = 0; i < size; i++) {
                ViewGroup viewGroup = mInAreaViewGroups.get(i);
                if (viewGroup != null && viewGroup.isEnabled() && viewGroup.getVisibility() != GONE) {
                    if (viewGroup instanceof RecyclerView
                            || viewGroup instanceof AbsListView
                            || viewGroup instanceof ScrollView)
                        return ViewCompat.canScrollVertically(viewGroup, -1);
                }
            }
            return mRootView == null || ViewCompat.canScrollVertically(mRootView, -1);
        }
    }

    private static final class ViewTraverser {
        private Accessor mAccessor;

        interface Accessor {
            void access(View view);
        }

        private ViewTraverser() {
        }

        static ViewTraverser create() {
            return new ViewTraverser();
        }

        ViewTraverser setAccessor(Accessor accessor) {
            mAccessor = accessor;
            return this;
        }

        void traverse(ViewGroup viewGroup) {
            if (viewGroup == null) {
                if (mAccessor != null)
                    mAccessor.access(null);
                return;
            }
            final int childCount = viewGroup.getChildCount();
            for (int i = 0; i < childCount; i++) {
                final View child = viewGroup.getChildAt(i);
                if (mAccessor != null)
                    mAccessor.access(child);
                if (child instanceof ViewGroup) {
                    traverse((ViewGroup) child);
                }
            }
        }
    }

    public interface OnChildScrollUpCallback {
        boolean canChildScrollUp(PullToRefreshLayout parent, View child);
    }

    public static final class LayoutParams extends MarginLayoutParams {

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }
    }

}
