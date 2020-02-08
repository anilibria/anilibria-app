package ru.radiationx.anilibria.ui.widgets;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.NestedScrollingChild;
import android.support.v4.view.NestedScrollingChildHelper;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.webkit.WebView;


/*
* Обработка событий аккуратно слизана с RecyclerView с некоторыми доработками.
* */
public class NestedWebView extends WebView implements NestedScrollingChild {

    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_NESTED_SCROLL = 1;
    public static final int SCROLL_STATE_SCROLL = 2;
    private int mScrollState = SCROLL_STATE_IDLE;

    private int mLastTouchX;
    private int mLastTouchY;

    private final int[] mScrollOffset = new int[2];
    private final int[] mScrollConsumed = new int[2];
    private final int[] mNestedOffsets = new int[2];

    private final NestedScrollingChildHelper mChildHelper;

    public NestedWebView(Context context) {
        this(context, null);
    }

    public NestedWebView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.webViewStyle);
    }

    private final int mTouchSlop;

    public NestedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mChildHelper = new NestedScrollingChildHelper(this);
        setNestedScrollingEnabled(true);
        final ViewConfiguration vc = ViewConfiguration.get(context);
        mTouchSlop = vc.getScaledTouchSlop();
    }

    private final OnLongClickListener longClickListener = v -> true;

    private void changeLongClickable(boolean enable) {
        setOnLongClickListener(enable ? null : longClickListener);
        setLongClickable(enable);
        setHapticFeedbackEnabled(enable);
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent e) {
        final MotionEvent ev = MotionEvent.obtain(e);
        final int action = MotionEventCompat.getActionMasked(e);

        if (action == MotionEvent.ACTION_DOWN) {
            mNestedOffsets[0] = mNestedOffsets[1] = 0;
        }
        ev.offsetLocation(mNestedOffsets[0], mNestedOffsets[1]);
        switch (action) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchX = (int) (e.getX() + 0.5f);
                mLastTouchY = (int) (e.getY() + 0.5f);

                int nestedScrollAxis = ViewCompat.SCROLL_AXIS_NONE;
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_HORIZONTAL;
                nestedScrollAxis |= ViewCompat.SCROLL_AXIS_VERTICAL;
                startNestedScroll(nestedScrollAxis);
                super.onTouchEvent(e);
            }
            break;

            case MotionEvent.ACTION_MOVE: {
                final int x = (int) (e.getX() + 0.5f);
                final int y = (int) (e.getY() + 0.5f);
                int dx = mLastTouchX - x;
                int dy = mLastTouchY - y;

                if (mScrollState == SCROLL_STATE_IDLE) {
                    if (Math.abs(dx) < mTouchSlop && Math.abs(dy) < mTouchSlop) {
                        break;
                    }
                }
                final boolean preScrollConsumed = dispatchNestedPreScroll(dx, dy, mScrollConsumed, mScrollOffset);

                if (preScrollConsumed) {
                    dx -= mScrollConsumed[0];
                    dy -= mScrollConsumed[1];
                    ev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                    mNestedOffsets[0] += mScrollOffset[0];
                    mNestedOffsets[1] += mScrollOffset[1];
                }

                if (preScrollConsumed) {
                    setScrollState(SCROLL_STATE_NESTED_SCROLL);
                } else {
                    mLastTouchX = x - mScrollOffset[0];
                    mLastTouchY = y - mScrollOffset[1];

                    if (dy < 0 && getScrollY() == 0) {
                        final boolean scrollConsumed = dispatchNestedScroll(0, 0, dx, dy, mScrollOffset);
                        if (scrollConsumed) {
                            mLastTouchX -= mScrollOffset[0];
                            mLastTouchY -= mScrollOffset[1];
                            ev.offsetLocation(mScrollOffset[0], mScrollOffset[1]);
                            mNestedOffsets[0] += mScrollOffset[0];
                            mNestedOffsets[1] += mScrollOffset[1];
                            setScrollState(SCROLL_STATE_NESTED_SCROLL);
                        }
                    } else {
                        if (dy != 0) {
                            setScrollState(SCROLL_STATE_SCROLL);
                        }
                        super.onTouchEvent(e);
                    }
                }
                if (mScrollState != SCROLL_STATE_IDLE) {
                    if (isLongClickable()) {
                        changeLongClickable(false);
                    }
                }
            }
            break;


            case MotionEvent.ACTION_UP: {
                //long dt = System.currentTimeMillis() - lastTouchTime;
                if (mScrollState == SCROLL_STATE_NESTED_SCROLL) {
                    e.setAction(MotionEvent.ACTION_CANCEL);
                }
                super.onTouchEvent(e);
                resetTouch();
                changeLongClickable(true);
            }
            break;

            case MotionEvent.ACTION_CANCEL: {
                super.onTouchEvent(e);
                resetTouch();
                changeLongClickable(true);
            }
            break;
        }
        ev.recycle();
        return true;
    }


    private void resetTouch() {
        stopNestedScroll();
        setScrollState(SCROLL_STATE_IDLE);
    }

    void setScrollState(int state) {
        if (state == mScrollState) {
            return;
        }
        mScrollState = state;
    }

    @Override
    public void setNestedScrollingEnabled(boolean enabled) {
        mChildHelper.setNestedScrollingEnabled(enabled);
    }

    @Override
    public boolean isNestedScrollingEnabled() {
        return mChildHelper.isNestedScrollingEnabled();
    }

    @Override
    public boolean startNestedScroll(int axes) {
        return mChildHelper.startNestedScroll(axes);
    }

    @Override
    public void stopNestedScroll() {
        mChildHelper.stopNestedScroll();
    }

    @Override
    public boolean hasNestedScrollingParent() {
        return mChildHelper.hasNestedScrollingParent();
    }

    @Override
    public boolean dispatchNestedScroll(int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedScroll(dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedPreScroll(int dx, int dy, int[] consumed, int[] offsetInWindow) {
        return mChildHelper.dispatchNestedPreScroll(dx, dy, consumed, offsetInWindow);
    }

    @Override
    public boolean dispatchNestedFling(float velocityX, float velocityY, boolean consumed) {
        return mChildHelper.dispatchNestedFling(velocityX, velocityY, consumed);
    }

    @Override
    public boolean dispatchNestedPreFling(float velocityX, float velocityY) {
        return mChildHelper.dispatchNestedPreFling(velocityX, velocityY);
    }
}