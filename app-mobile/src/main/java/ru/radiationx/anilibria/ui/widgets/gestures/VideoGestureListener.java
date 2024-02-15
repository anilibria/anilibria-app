package ru.radiationx.anilibria.ui.widgets.gestures;

import android.gesture.GestureOverlayView;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

import androidx.annotation.NonNull;

import timber.log.Timber;

public class VideoGestureListener extends GestureDetector.SimpleOnGestureListener {

    private static final int SWIPE_THRESHOLD = 100;
    private final int minFlingVelocity;

    public static final String TAG = "VideoGestureListener";
    private final VideoGestureEventsListener listener;

    public VideoGestureListener(VideoGestureEventsListener listener, ViewConfiguration viewConfiguration) {
        this.listener = listener;
        minFlingVelocity = viewConfiguration.getScaledMinimumFlingVelocity();
    }
    @Override
    public boolean onSingleTapUp(@NonNull MotionEvent e) {
        listener.onTap(e);
        return super.onSingleTapUp(e);
    }
    @Override
    public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
        //listener.onTap(e);
        return super.onSingleTapConfirmed(e);
    }

    @Override
    public boolean onDoubleTap(@NonNull MotionEvent e) {
        listener.onDoubleTap(e);
        return true;
    }

    @Override
    public void onLongPress(@NonNull MotionEvent e) {
        listener.onLongPress(e);
        // Touch has been long enough to indicate a long press.
        // Does not indicate motion is complete yet (no up event necessarily)
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        float deltaY = e2.getY() - e1.getY();
        float deltaX = e2.getX() - e1.getX();

        if (Math.abs(deltaX) > Math.abs(deltaY)) {
            listener.onHorizontalScroll(e2, deltaX);
        } else {
            listener.onVerticalScroll(e2, deltaY);
        }
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, @NonNull MotionEvent e2, float velocityX, float velocityY) {
        // Fling event occurred.  Notification of this one happens after an "up" event.
        boolean result = false;
        try {
            float diffY = e2.getY() - e1.getY();
            float diffX = e2.getX() - e1.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > minFlingVelocity) {
                    if (diffX > 0) {
                        listener.onSwipeRight();
                    } else {
                        listener.onSwipeLeft();
                    }
                }
                result = true;
            } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > minFlingVelocity) {
                if (diffY > 0) {
                    listener.onSwipeBottom();
                } else {
                    listener.onSwipeTop();
                }
            }
            result = true;

        } catch (Exception exception) {
            Timber.e(exception);
        }
        return result;
    }

    @Override
    public void onShowPress(@NonNull MotionEvent e) {
    }

    @Override
    public boolean onDown(@NonNull MotionEvent e) {
        return false;
    }


}
