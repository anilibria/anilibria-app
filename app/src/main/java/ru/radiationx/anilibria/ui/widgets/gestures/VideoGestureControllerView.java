package ru.radiationx.anilibria.ui.widgets.gestures;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;

public class VideoGestureControllerView extends View {

    private GestureDetector gestureDetector;
    private VideoGestureEventsListener listener;

    public VideoGestureControllerView(Context context) {
        super(context);
    }

    public VideoGestureControllerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoGestureControllerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setClickable(true);
        setFocusable(true);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (listener != null) {
                listener.onStart();
            }
        }
        mayNotifyGestureDetector(event);
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (listener != null) {
                listener.onEnd();
            }
        }
        return true;
    }

    private void mayNotifyGestureDetector(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
    }

    public void setEventsListener(VideoGestureEventsListener listener) {
        this.listener = listener;
        gestureDetector = new GestureDetector(getContext(), new VideoGestureListener(listener, ViewConfiguration.get(getContext())));
    }

}
