package ru.radiationx.anilibria.ui.widgets.gestures;

import android.view.MotionEvent;

public interface VideoGestureEventsListener {

    void onStart();

    void onEnd();

    void onTap(MotionEvent event);

    void onDoubleTap(MotionEvent event);

    void onHorizontalScroll(MotionEvent event, float delta);

    void onVerticalScroll(MotionEvent event, float delta);

    void onSwipeRight();

    void onSwipeLeft();

    void onSwipeBottom();

    void onSwipeTop();

}
