/*
 * Copyright 2016 Michael Bely
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.michaelbel.bottomsheet;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Insets;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.BoolRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.NestedScrollingParent;
import androidx.core.view.NestedScrollingParentHelper;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.michaelbel.bottomsheet.menu.BottomSheetMenu;
import org.michaelbel.bottomsheetdialog.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Date: 17 FEB 2018
 * Time: 00:33 MSK
 *
 * @author Michael Bel
 */

@SuppressWarnings("all")
public class BottomSheet extends Dialog {

    public static final int LIST = 1;
    public static final int GRID = 2;

    public static final int LIGHT_THEME = 10;
    public static final int DARK_THEME = 11;

    public static final int FAB_SLIDE_UP = 20;
    public static final int FAB_SHOW_HIDE = 21;

    @RestrictTo(LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LIST, GRID})
    public @interface Type {
    }

    @RestrictTo(LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({LIGHT_THEME, DARK_THEME})
    public @interface Theme {
    }

    @RestrictTo(LIBRARY_GROUP)
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({FAB_SHOW_HIDE, FAB_SLIDE_UP})
    public @interface FabBehavior {
    }

    private boolean dividers;
    private boolean fullWidth;
    private boolean darkTheme;
    private boolean titleTextMultiline;

    private int cellHeight;
    private int itemSelector;
    private int dimmingValue = 80;

    private @Type int contentType = LIST;
    private @Theme int theme = LIGHT_THEME;
    private @FabBehavior int fabBehavior = FAB_SHOW_HIDE;

    private @ColorInt int titleTextColor;
    private @ColorInt int backgroundColor;
    private @ColorInt int iconColor;
    private @ColorInt int itemTextColor;

    private PorterDuff.Mode iconTintMode = PorterDuff.Mode.MULTIPLY;

    private View customView;
    private TextView titleTextView;
    private ListView listView;
    private GridView gridView;
    private ContainerView container;
    private LinearLayout containerView;

    private List<Drawable> ICONS = new ArrayList<>();
    private List<CharSequence> ITEMS = new ArrayList<>();

    private CharSequence titleText;
    private ArrayList<BottomSheetItem> bottomsheetItems = new ArrayList<>();

    private WindowInsets lastInsets;
    private Runnable startAnimationRunnable;
    private int layoutCount;
    private boolean dismissed;

    private ColorDrawable backDrawable = new ColorDrawable(0xFF000000);
    private boolean allowCustomAnimation = true;
    private int touchSlop;
    private boolean useFastDismiss;
    private boolean focusable;
    private Drawable shadowDrawable;
    private int backgroundPaddingTop;
    private int backgroundPaddingLeft;
    private AnimatorSet currentSheetAnimation;

    private Point displaySize = new Point();
    private DisplayMetrics metrics = new DisplayMetrics();
    private Handler handler = new Handler(Looper.getMainLooper());

    private OnClickListener onClickListener;
    private BottomSheetCallback bottomSheetCallback;

    private DialogInterface.OnShowListener onShowListener;
    private DialogInterface.OnDismissListener onDismissListener;

    private boolean allowDrawContent = true;
    private boolean useHardwareLayer = true;

    private FloatingActionButton floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (backgroundColor == 0) {
            backgroundColor = darkTheme ? 0xFF424242 : 0xFFFFFFFF;
        }

        if (titleTextColor == 0) {
            titleTextColor = darkTheme ? 0xB3FFFFFF : 0x8A000000;
        }

        if (itemTextColor == 0) {
            itemTextColor = darkTheme ? 0xFFFFFFFF : 0xDE000000;
        }

        if (iconColor == 0) {
            iconColor = darkTheme ? 0xFFFFFFFF : 0x8A000000;
        }

        if (itemSelector == 0) {
            itemSelector = darkTheme ? R.drawable.selectable_dark : R.drawable.selectable_light;
        }

        if (cellHeight == 0) {
            cellHeight = Utils.dp(getContext(), 48);
        }

        Window window = getWindow();
        if (window != null) {
            window.setWindowAnimations(R.style.DialogNoAnimation);
        }
        setContentView(container, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

        if (containerView == null) {
            containerView = new LinearLayout(getContext()) {
                @Override
                public boolean hasOverlappingRendering() {
                    return false;
                }

                @Override
                public void setTranslationY(float translationY) {
                    super.setTranslationY(translationY);
                    onContainerTranslationYChanged(translationY);
                }
            };
            containerView.setOrientation(LinearLayout.VERTICAL);
            containerView.setBackgroundDrawable(shadowDrawable);
            ViewCompat.setOnApplyWindowInsetsListener(containerView, new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars()).toPlatformInsets();
                    containerView.setPadding(0, backgroundPaddingTop, 0, Utils.dp(getContext(), 8) + systemInsets.bottom);
                    return insets;
                }
            });
        }

        containerView.setVisibility(View.INVISIBLE);
        containerView.setBackgroundColor(backgroundColor);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.BOTTOM;

        containerView.setLayoutParams(params);
        container.addView(containerView, 0);

        if (customView != null) {
            if (customView.getParent() != null) {
                ViewGroup viewGroup = (ViewGroup) customView.getParent();
                viewGroup.removeView(customView);
            }

            FrameLayout.LayoutParams params1 = (FrameLayout.LayoutParams) containerView.getLayoutParams();
            params1.width = ViewGroup.LayoutParams.MATCH_PARENT;
            params1.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            params1.gravity = Gravity.START | Gravity.TOP;
            containerView.addView(customView, params1);
        } else {
            if (titleText != null) {
                titleTextView = new TextView(getContext());
                titleTextView.setLines(1);
                titleTextView.setText(titleText);
                titleTextView.setTextColor(titleTextColor);
                titleTextView.setGravity(Gravity.CENTER_VERTICAL);
                titleTextView.setEllipsize(TextUtils.TruncateAt.END);
                titleTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

                if (titleTextMultiline) {
                    titleTextView.setSingleLine(false);
                } else {
                    titleTextView.setMaxLines(1);
                    titleTextView.setSingleLine(true);
                }

                LinearLayout.LayoutParams params0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                params0.gravity = Gravity.START | Gravity.TOP;
                params0.leftMargin = Utils.dp(getContext(), 16);
                params0.rightMargin = Utils.dp(getContext(), 16);
                params0.topMargin = Utils.dp(getContext(), 8);
                params0.bottomMargin = Utils.dp(getContext(), 16);

                titleTextView.setLayoutParams(params0);
                containerView.addView(titleTextView);
            }

            BottomSheetAdapter adapter = new BottomSheetAdapter();

            if (!ITEMS.isEmpty()) {
                if (contentType == LIST) {
                    LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    listView = new ListView(getContext());
                    listView.setSelector(itemSelector);
                    listView.setDividerHeight(0);
                    listView.setAdapter(adapter);
                    listView.setDrawSelectorOnTop(true);
                    listView.setVerticalScrollBarEnabled(false);
                    listView.setLayoutParams(params2);
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                            dismissWithButtonClick(position);
                        }
                    });

                    containerView.addView(listView);
                } else if (contentType == GRID) {
                    LinearLayout.LayoutParams params3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    gridView = new GridView(getContext());
                    gridView.setSelector(itemSelector);
                    gridView.setAdapter(adapter);
                    gridView.setNumColumns(3);
                    gridView.setVerticalScrollBarEnabled(false);
                    gridView.setVerticalSpacing(Utils.dp(getContext(), 16));
                    gridView.setPadding(Utils.dp(getContext(), 0), Utils.dp(getContext(), 8), Utils.dp(getContext(), 0), Utils.dp(getContext(), 16));
                    gridView.setLayoutParams(params3);
                    gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                            dismissWithButtonClick(i);
                        }
                    });

                    containerView.addView(gridView);
                }

                if (!ITEMS.isEmpty()) {
                    for (int a = 0; a < ITEMS.size(); a++) {
                        bottomsheetItems.add(new BottomSheetItem(ITEMS.get(a), !ICONS.isEmpty() ? ICONS.get(a) : null));
                    }
                }

                adapter.notifyDataSetChanged();
            }
        }

        WindowManager.LayoutParams params4 = window.getAttributes();
        params4.width = ViewGroup.LayoutParams.MATCH_PARENT;
        params4.gravity = Gravity.TOP | Gravity.START;
        params4.dimAmount = 0;
        params4.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        if (!focusable) {
            params4.flags |= WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM;
        }
        params4.height = ViewGroup.LayoutParams.MATCH_PARENT;
        window.setAttributes(params4);
    }

    @Override
    public void show() {
        super.show();
        if (focusable) {
            try {
                getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        dismissed = false;
        cancelSheetAnimation();
        containerView.measure(View.MeasureSpec.makeMeasureSpec(displaySize.x, View.MeasureSpec.AT_MOST), View.MeasureSpec.makeMeasureSpec(displaySize.y, View.MeasureSpec.AT_MOST));

        backDrawable.setAlpha(0);

        if (Build.VERSION.SDK_INT >= 18) {
            layoutCount = 2;
            containerView.setTranslationY(containerView.getMeasuredHeight());
            handler.postDelayed(startAnimationRunnable = new Runnable() {
                @Override
                public void run() {
                    if (startAnimationRunnable != this) {
                        return;
                    }

                    startAnimationRunnable = null;
                    startShowAnimation();
                }
            }, 150);
        } else {
            startShowAnimation();
        }

        if (bottomSheetCallback != null) {
            bottomSheetCallback.onShown();
        }

        if (onShowListener != null) {
            onShowListener.onShow(this);
        }
    }

    @Override
    public void dismiss() {
        dismissWithButtonClick(-1);
    }

    private void dismissWithButtonClick(final int viewId) {
        if (dismissed) {
            return;
        }

        dismissed = true;
        cancelSheetAnimation();

        AnimatorSet animatorSet = new AnimatorSet();
        if (floatingActionButton != null && fabBehavior == FAB_SLIDE_UP) {
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(containerView, "translationY", containerView.getMeasuredHeight() + Utils.dp(getContext(), 10)),
                    ObjectAnimator.ofInt(backDrawable, "alpha", 0),
                    ObjectAnimator.ofFloat(floatingActionButton, "translationY", 0)
            );
        } else if (floatingActionButton == null || fabBehavior != FAB_SLIDE_UP) {
            animatorSet.playTogether(
                    ObjectAnimator.ofFloat(containerView, "translationY", containerView.getMeasuredHeight() + Utils.dp(getContext(), 10)),
                    ObjectAnimator.ofInt(backDrawable, "alpha", 0)
            );
        }
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                    currentSheetAnimation = null;
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                    currentSheetAnimation = null;

                    if (viewId != -1) {
                        if (onClickListener != null) {
                            onClickListener.onClick(BottomSheet.this, viewId);
                        }
                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                BottomSheet.super.dismiss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }
        });

        if (useFastDismiss) {
            int height = containerView.getMeasuredHeight();
            animatorSet.setDuration(Math.max(60, (int) (180 * (height - containerView.getTranslationY()) / (float) height)));
            useFastDismiss = false;
        } else {
            animatorSet.setDuration(180);
        }

        animatorSet.setInterpolator(new AccelerateInterpolator());
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                    currentSheetAnimation = null;
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                dismissInternal();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                if (floatingActionButton != null && fabBehavior == FAB_SHOW_HIDE) {
                    floatingActionButton.show();
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                super.onAnimationCancel(animation);
                if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                    currentSheetAnimation = null;
                }
            }
        });

        animatorSet.start();
        currentSheetAnimation = animatorSet;

        if (bottomSheetCallback != null) {
            bottomSheetCallback.onDismissed();
        }

        if (onDismissListener != null) {
            onDismissListener.onDismiss(this);
        }
    }

    private BottomSheet(Context context, boolean needFocus) {
        super(context, R.style.TransparentDialog);


      /*  if (Build.VERSION.SDK_INT >= 21) {
            getWindow().addFlags(
                    WindowManager.LayoutParams.FLAG_LAYOUT_INSET_DECOR |
                            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN |
                            WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS
            );
        }
*/
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getWindow().getAttributes().layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }

        ViewConfiguration vc = ViewConfiguration.get(context);
        touchSlop = vc.getScaledTouchSlop();

        Rect padding = new Rect();
        shadowDrawable = ContextCompat.getDrawable(context, R.drawable.sheet_shadow);
        shadowDrawable.setColorFilter(new PorterDuffColorFilter(0xFFFFFFFF, PorterDuff.Mode.MULTIPLY)); // todo ADDED
        shadowDrawable.getPadding(padding);
        backgroundPaddingLeft = padding.left;
        backgroundPaddingTop = padding.top;

        container = new BottomSheet.ContainerView(getContext()) {
            @Override
            public boolean drawChild(Canvas canvas, View child, long drawingTime) {
                try {
                    return allowDrawContent && super.drawChild(canvas, child, drawingTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        };

        container.setBackgroundDrawable(backDrawable);
        focusable = needFocus;

        if (Build.VERSION.SDK_INT >= 21) {
            //container.setFitsSystemWindows(true);
            container.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @SuppressLint("NewApi")
                @Override
                public WindowInsets onApplyWindowInsets(View view, WindowInsets insets) {
                    lastInsets = insets;
                    view.requestLayout();
                    return insets;
                }
            });
            //container.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }

        backDrawable.setAlpha(0);
    }

    private class ContainerView extends FrameLayout implements NestedScrollingParent {

        private int startedTrackingX;
        private int startedTrackingY;
        private int startedTrackingPointerId;

        private boolean maybeStartTracking = false;
        private boolean startedTracking = false;

        private AnimatorSet currentAnimation = null;
        private VelocityTracker velocityTracker = null;
        private NestedScrollingParentHelper nestedScrollingParentHelper;

        public ContainerView(Context context) {
            super(context);
            nestedScrollingParentHelper = new NestedScrollingParentHelper(this);
        }

        @Override
        public boolean onStartNestedScroll(@NonNull View child, @NonNull View target, int axes) {
            return !dismissed && axes == ViewCompat.SCROLL_AXIS_VERTICAL && !canDismissWithSwipe();
        }

        @Override
        public void onNestedScrollAccepted(@NonNull View child, @NonNull View target, int axes) {
            nestedScrollingParentHelper.onNestedScrollAccepted(child, target, axes);
            if (dismissed) {
                return;
            }
            cancelCurrentAnimation();
        }

        @Override
        public void onStopNestedScroll(@NonNull View target) {
            nestedScrollingParentHelper.onStopNestedScroll(target);
            if (dismissed) {
                return;
            }
            checkDismiss(0, 0);
        }

        @Override
        public void onNestedScroll(@NonNull View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int dyUnconsumed) {
            if (dismissed) {
                return;
            }

            cancelCurrentAnimation();

            if (dyUnconsumed != 0) {
                float currentTranslation = containerView.getTranslationY();
                currentTranslation -= dyUnconsumed;

                if (currentTranslation < 0) {
                    currentTranslation = 0;
                }

                containerView.setTranslationY(currentTranslation);
            }
        }

        @Override
        public void onNestedPreScroll(@NonNull View target, int dx, int dy, @NonNull int[] consumed) {
            if (dismissed) {
                return;
            }

            cancelCurrentAnimation();
            float currentTranslation = containerView.getTranslationY();

            if (currentTranslation > 0 && dy > 0) {
                currentTranslation -= dy;
                consumed[1] = dy;

                if (currentTranslation < 0) {
                    currentTranslation = 0;
                    consumed[1] += currentTranslation;
                }

                containerView.setTranslationY(currentTranslation);
            }
        }

        @Override
        public boolean onNestedFling(@NonNull View target, float velocityX, float velocityY, boolean consumed) {
            return false;
        }

        @Override
        public boolean onNestedPreFling(@NonNull View target, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public int getNestedScrollAxes() {
            return nestedScrollingParentHelper.getNestedScrollAxes();
        }

        private void checkDismiss(float velX, float velY) {
            float translationY = containerView.getTranslationY();
            boolean backAnimation = translationY < getPixelsInCM(0.8f, false) && (velY < 3500 || Math.abs(velY) < Math.abs(velX)) || velY < 0 && Math.abs(velY) >= 3500;

            if (!backAnimation) {
                boolean allowOld = allowCustomAnimation;
                allowCustomAnimation = false;
                useFastDismiss = true;
                dismiss();
                allowCustomAnimation = allowOld;
            } else {
                currentAnimation = new AnimatorSet();
                currentAnimation.playTogether(ObjectAnimator.ofFloat(containerView, "translationY", 0));
                currentAnimation.setDuration((int) (150 * (translationY / getPixelsInCM(0.8F, false))));
                currentAnimation.setInterpolator(new DecelerateInterpolator());
                currentAnimation.addListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                        if (currentAnimation != null && currentAnimation.equals(animation)) {
                            currentAnimation = null;
                        }
                    }
                });
                currentAnimation.start();
            }
        }

        private void cancelCurrentAnimation() {
            if (currentAnimation != null) {
                currentAnimation.cancel();
                currentAnimation = null;
            }
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            if (dismissed) {
                return false;
            }
            if (onContainerTouchEvent(ev)) {
                return true;
            }
            if (canDismissWithTouchOutside() && ev != null && (ev.getAction() == MotionEvent.ACTION_DOWN || ev.getAction() == MotionEvent.ACTION_MOVE) && !startedTracking && !maybeStartTracking) {
                startedTrackingX = (int) ev.getX();
                startedTrackingY = (int) ev.getY();
                if (startedTrackingY < containerView.getTop() || startedTrackingX < containerView.getLeft() || startedTrackingX > containerView.getRight()) {
                    dismiss();
                    return true;
                }
                startedTrackingPointerId = ev.getPointerId(0);
                maybeStartTracking = true;
                cancelCurrentAnimation();
                if (velocityTracker != null) {
                    velocityTracker.clear();
                }
            } else if (ev != null && ev.getAction() == MotionEvent.ACTION_MOVE && ev.getPointerId(0) == startedTrackingPointerId) {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                float dx = Math.abs((int) (ev.getX() - startedTrackingX));
                float dy = (int) ev.getY() - startedTrackingY;
                velocityTracker.addMovement(ev);
                if (maybeStartTracking && !startedTracking && (dy > 0 && dy / 3.0f > Math.abs(dx) && Math.abs(dy) >= touchSlop)) {
                    startedTrackingY = (int) ev.getY();
                    maybeStartTracking = false;
                    startedTracking = true;
                    requestDisallowInterceptTouchEvent(true);
                } else if (startedTracking) {
                    float translationY = containerView.getTranslationY();
                    translationY += dy;
                    if (translationY < 0) {
                        translationY = 0;
                    }
                    containerView.setTranslationY(translationY);
                    startedTrackingY = (int) ev.getY();
                }
            } else if (ev == null || ev != null && ev.getPointerId(0) == startedTrackingPointerId && (ev.getAction() == MotionEvent.ACTION_CANCEL || ev.getAction() == MotionEvent.ACTION_UP || ev.getAction() == MotionEvent.ACTION_POINTER_UP)) {
                if (velocityTracker == null) {
                    velocityTracker = VelocityTracker.obtain();
                }
                velocityTracker.computeCurrentVelocity(1000);
                float translationY = containerView.getTranslationY();
                if (startedTracking || translationY != 0) {
                    checkDismiss(velocityTracker.getXVelocity(), velocityTracker.getYVelocity());
                    startedTracking = false;
                } else {
                    maybeStartTracking = false;
                    startedTracking = false;
                }
                if (velocityTracker != null) {
                    velocityTracker.recycle();
                    velocityTracker = null;
                }
                startedTrackingPointerId = -1;
            }
            return startedTracking || !canDismissWithSwipe();
        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            int width = MeasureSpec.getSize(widthMeasureSpec);
            int height = MeasureSpec.getSize(heightMeasureSpec);
            if (lastInsets != null && Build.VERSION.SDK_INT >= 21) {
                //height -= lastInsets.getSystemWindowInsetBottom();
            }

            setMeasuredDimension(width, height);
            if (lastInsets != null && Build.VERSION.SDK_INT >= 21) {
                width -= lastInsets.getSystemWindowInsetRight() + lastInsets.getSystemWindowInsetLeft();
            }
            boolean isPortrait = width < height;

            if (containerView != null) {
                if (!fullWidth) {
                    int widthSpec;
                    widthSpec = MeasureSpec.makeMeasureSpec(isPortrait ? width + backgroundPaddingLeft * 2 : (int) Math.max(width * 0.8f, Math.min(Utils.dp(getContext(), 480), width)) + backgroundPaddingLeft * 2, MeasureSpec.EXACTLY);

                    /*if (Utils.isTablet(getContext())) {
                        widthSpec = MeasureSpec.makeMeasureSpec((int) (Math.min(displaySize.x, displaySize.y) * 0.8f) + backgroundPaddingLeft * 2, MeasureSpec.EXACTLY);
                    } else {
                        widthSpec = MeasureSpec.makeMeasureSpec(isPortrait ? width + backgroundPaddingLeft * 2 : (int) Math.max(width * 0.8f, Math.min(Utils.dp(getContext(), 480), width)) + backgroundPaddingLeft * 2, MeasureSpec.EXACTLY);
                    }*/

                    containerView.measure(widthSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
                } else {
                    containerView.measure(MeasureSpec.makeMeasureSpec(width + backgroundPaddingLeft * 2, MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST));
                }
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == GONE || child == containerView) {
                    continue;
                }
                if (!onCustomMeasure(child, width, height)) {
                    measureChildWithMargins(child, MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), 0, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY), 0);
                }
            }
        }

        @Override
        protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
            layoutCount--;
            if (containerView != null) {
                if (lastInsets != null && Build.VERSION.SDK_INT >= 21) {
                    left += lastInsets.getSystemWindowInsetLeft();
                    right -= lastInsets.getSystemWindowInsetRight();
                }
                int t = (bottom - top) - containerView.getMeasuredHeight();
                int l = ((right - left) - containerView.getMeasuredWidth()) / 2;
                if (lastInsets != null && Build.VERSION.SDK_INT >= 21) {
                    l += lastInsets.getSystemWindowInsetLeft();
                }
                containerView.layout(l, t, l + containerView.getMeasuredWidth(), t + containerView.getMeasuredHeight());
            }

            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() == GONE || child == containerView) {
                    continue;
                }
                if (!onCustomLayout(child, left, top, right, bottom)) {
                    final LayoutParams lp = (LayoutParams) child.getLayoutParams();

                    final int width = child.getMeasuredWidth();
                    final int height = child.getMeasuredHeight();

                    int childLeft;
                    int childTop;

                    int gravity = lp.gravity;
                    if (gravity == -1) {
                        gravity = Gravity.TOP | Gravity.LEFT;
                    }

                    final int absoluteGravity = gravity & Gravity.HORIZONTAL_GRAVITY_MASK;
                    final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

                    switch (absoluteGravity & Gravity.HORIZONTAL_GRAVITY_MASK) {
                        case Gravity.CENTER_HORIZONTAL:
                            childLeft = (right - left - width) / 2 + lp.leftMargin - lp.rightMargin;
                            break;
                        case Gravity.RIGHT:
                            childLeft = right - width - lp.rightMargin;
                            break;
                        case Gravity.LEFT:
                        default:
                            childLeft = lp.leftMargin;
                    }

                    switch (verticalGravity) {
                        case Gravity.TOP:
                            childTop = lp.topMargin;
                            break;
                        case Gravity.CENTER_VERTICAL:
                            childTop = (bottom - top - height) / 2 + lp.topMargin - lp.bottomMargin;
                            break;
                        case Gravity.BOTTOM:
                            childTop = (bottom - top) - height - lp.bottomMargin;
                            break;
                        default:
                            childTop = lp.topMargin;
                    }
                    if (lastInsets != null && Build.VERSION.SDK_INT >= 21) {
                        childLeft += lastInsets.getSystemWindowInsetLeft();
                    }
                    child.layout(childLeft, childTop, childLeft + width, childTop + height);
                }
            }
            if (layoutCount == 0 && startAnimationRunnable != null) {
                handler.removeCallbacks(startAnimationRunnable);
                startAnimationRunnable.run();
                startAnimationRunnable = null;
            }
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            if (canDismissWithSwipe()) {
                return onTouchEvent(event);
            }

            return super.onInterceptTouchEvent(event);
        }

        @Override
        public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            if (maybeStartTracking && !startedTracking) {
                onTouchEvent(null);
            }

            super.requestDisallowInterceptTouchEvent(disallowIntercept);
        }

        @Override
        public boolean hasOverlappingRendering() {
            return false;
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            onContainerDraw(canvas);
        }
    }

    private boolean onContainerTouchEvent(MotionEvent event) {
        return false;
    }

    private boolean canDismissWithSwipe() {
        return false; // todo: true
    }

    private boolean canDismissWithTouchOutside() {
        return true;
    }

    private void cancelSheetAnimation() {
        if (currentSheetAnimation != null) {
            currentSheetAnimation.cancel();
            currentSheetAnimation = null;
        }
    }

    private void startShowAnimation() {
        if (dismissed) {
            return;
        }
        containerView.setVisibility(View.VISIBLE);
        if (!onCustomOpenAnimation()) {
            if (Build.VERSION.SDK_INT >= 20 && useHardwareLayer) {
                container.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
            containerView.setTranslationY(containerView.getMeasuredHeight());
            AnimatorSet animatorSet = new AnimatorSet();

            if (floatingActionButton != null && fabBehavior == FAB_SLIDE_UP) {
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(floatingActionButton, "translationY", -(containerView.getMeasuredHeight())),
                        ObjectAnimator.ofFloat(containerView, "translationY", 0),
                        ObjectAnimator.ofInt(backDrawable, "alpha", dimmingValue));
            } else if (floatingActionButton == null || fabBehavior != FAB_SLIDE_UP) {
                animatorSet.playTogether(
                        ObjectAnimator.ofFloat(containerView, "translationY", 0),
                        ObjectAnimator.ofInt(backDrawable, "alpha", dimmingValue));
            }

            animatorSet.setDuration(200);
            animatorSet.setStartDelay(20);
            animatorSet.setInterpolator(new DecelerateInterpolator());
            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);

                    if (floatingActionButton != null && fabBehavior == FAB_SHOW_HIDE) {
                        floatingActionButton.hide();
                    }
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                        currentSheetAnimation = null;
                        if (useHardwareLayer) {
                            container.setLayerType(View.LAYER_TYPE_NONE, null);
                        }
                    }
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    if (currentSheetAnimation != null && currentSheetAnimation.equals(animation)) {
                        currentSheetAnimation = null;
                    }
                }
            });
            animatorSet.start();
            currentSheetAnimation = animatorSet;
        }
    }

    private void dismissInternal() {
        super.dismiss();
    }

    private float getPixelsInCM(float cm, boolean isX) {
        return (cm / 2.54F) * (isX ? metrics.xdpi : metrics.ydpi);
    }

    protected void onContainerTranslationYChanged(float translationY) {
    }

    public void setAllowDrawContent(boolean value) {
        if (allowDrawContent != value) {
            allowDrawContent = value;
            container.setBackgroundDrawable(allowDrawContent ? backDrawable : null);
            container.invalidate();
        }
    }

    protected boolean onCustomMeasure(View view, int width, int height) {
        return false;
    }

    protected boolean onCustomLayout(View view, int left, int top, int right, int bottom) {
        return false;
    }

    public void onContainerDraw(Canvas canvas) {
    }

    protected boolean onCustomOpenAnimation() {
        return false;
    }

//--------------------------------------------------------------------------------------------------

    public static class Builder {

        private Context context;
        private BottomSheet bottomSheet;

        public Builder(@NonNull Context context) {
            this.context = context;
            bottomSheet = new BottomSheet(context, false);
        }

        public Builder(@NonNull Context context, boolean focus) {
            this.context = context;
            bottomSheet = new BottomSheet(context, focus);
        }

        public Builder(@NonNull Context context, @BoolRes int focus) {
            this.context = context;
            bottomSheet = new BottomSheet(context, context.getResources().getBoolean(focus));
        }

        public Builder setTitle(@StringRes int titleId) {
            bottomSheet.titleText = context.getText(titleId);
            return this;
        }

        public Builder setTitle(@Nullable CharSequence title) {
            bottomSheet.titleText = title;
            return this;
        }

        public Builder setItems(@ArrayRes int itemsId, final OnClickListener listener) {
            bottomSheet.ITEMS.addAll(Arrays.asList(context.getResources().getTextArray(itemsId)));
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@StringRes int[] items, final OnClickListener listener) {
            for (int i : items) {
                bottomSheet.ITEMS.add(context.getResources().getString(i));
            }
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(CharSequence[] items, final OnClickListener listener) {
            bottomSheet.ITEMS.addAll(Arrays.asList(items));
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@ArrayRes int itemsId, int[] icons, final OnClickListener listener) {
            bottomSheet.ITEMS.addAll(Arrays.asList(context.getResources().getTextArray(itemsId)));
            for (int i : icons) {
                bottomSheet.ICONS.add(ContextCompat.getDrawable(context, i));
            }
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@ArrayRes int itemsId, Drawable[] icons, final OnClickListener listener) {
            Collections.addAll(bottomSheet.ITEMS, context.getResources().getTextArray(itemsId));
            Collections.addAll(bottomSheet.ICONS, icons);
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@StringRes int[] items, int[] icons, final OnClickListener listener) {
            for (int i : items) {
                bottomSheet.ITEMS.add(context.getResources().getString(i));
            }
            for (int j : icons) {
                bottomSheet.ICONS.add(ContextCompat.getDrawable(context, j));
            }
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@StringRes int[] items, Drawable[] icons, final OnClickListener listener) {
            for (int i : items) {
                bottomSheet.ITEMS.add(context.getResources().getString(i));
            }
            Collections.addAll(bottomSheet.ICONS, icons);
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@NonNull CharSequence[] items, int[] icons, final OnClickListener listener) {
            bottomSheet.ITEMS.addAll(Arrays.asList(items));
            for (int i : icons) {
                bottomSheet.ICONS.add(ContextCompat.getDrawable(context, i));
            }
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setItems(@NonNull CharSequence[] items, Drawable[] icons, final OnClickListener listener) {
            bottomSheet.ITEMS.addAll(Arrays.asList(items));
            Collections.addAll(bottomSheet.ICONS, icons);
            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setMenu(@MenuRes int menuResId, final OnClickListener listener) {
            BottomSheetMenu menu = new BottomSheetMenu(context);
            new MenuInflater(context).inflate(menuResId, menu);

            for (int i = 0; i < menu.size(); i++) {
                bottomSheet.ITEMS.add(menu.getItem(i).getTitle());
                bottomSheet.ICONS.add(menu.getItem(i).getIcon());
            }

            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setMenu(Menu menu, final OnClickListener listener) {
            for (int i = 0; i < menu.size(); i++) {
                bottomSheet.ITEMS.add(menu.getItem(i).getTitle());
                bottomSheet.ICONS.add(menu.getItem(i).getIcon());
            }

            bottomSheet.onClickListener = listener;
            return this;
        }

        public Builder setView(@LayoutRes int layoutResId) {
            bottomSheet.customView = LayoutInflater.from(context).inflate(layoutResId, null);
            return this;
        }

        public Builder setView(View view) {
            bottomSheet.customView = view;
            return this;
        }

//----- Styles -------------------------------------------------------------------------------------

        public Builder setContentType(@Type int type) {
            bottomSheet.contentType = type;
            return this;
        }

        public Builder setDarkTheme(boolean theme) {
            bottomSheet.darkTheme = theme;
            return this;
        }

        public Builder setDarkTheme(@BoolRes int theme) {
            bottomSheet.darkTheme = context.getResources().getBoolean(theme);
            return this;
        }

        public Builder setFullWidth(boolean fullWidth) {
            bottomSheet.fullWidth = fullWidth;
            return this;
        }

        public Builder setFullWidth(@BoolRes int fullWidth) {
            bottomSheet.fullWidth = context.getResources().getBoolean(fullWidth);
            return this;
        }

        public Builder setCellHeight(int cellHeight) {
            bottomSheet.cellHeight = cellHeight;
            return this;
        }

        public Builder setDividers(boolean dividers) {
            bottomSheet.dividers = dividers;
            return this;
        }

        public Builder setDividers(@BoolRes int dividers) {
            bottomSheet.dividers = context.getResources().getBoolean(dividers);
            return this;
        }

        public Builder setWindowDimming(@IntRange(from = 0, to = 255) int windowDimming) {
            bottomSheet.dimmingValue = windowDimming;
            return this;
        }

        public Builder setTitleMultiline(boolean titleMultiline) {
            bottomSheet.titleTextMultiline = titleMultiline;
            return this;
        }

        public Builder setTitleMultiline(@BoolRes int titleMultiline) {
            bottomSheet.titleTextMultiline = context.getResources().getBoolean(titleMultiline);
            return this;
        }

        public Builder setFabBehavior(FloatingActionButton fab) {
            bottomSheet.floatingActionButton = fab;
            return this;
        }

        public Builder setFabBehavior(FloatingActionButton fab, @FabBehavior int fabBehavior) {
            bottomSheet.floatingActionButton = fab;
            bottomSheet.fabBehavior = fabBehavior;
            return this;
        }

//----- Colors -------------------------------------------------------------------------------------

        public Builder setBackgroundColor(@ColorInt int backgroundColor) {
            bottomSheet.backgroundColor = backgroundColor;
            return this;
        }

        public Builder setBackgroundColorRes(@ColorRes int backgroundColorRes) {
            bottomSheet.backgroundColor = ContextCompat.getColor(context, backgroundColorRes);
            return this;
        }

        public Builder setTitleTextColor(@ColorInt int titleTextColor) {
            bottomSheet.titleTextColor = titleTextColor;
            return this;
        }

        public Builder setTitleTextColorRes(@ColorRes int titleTextColorRes) {
            bottomSheet.titleTextColor = ContextCompat.getColor(context, titleTextColorRes);
            return this;
        }

        public Builder setItemTextColor(@ColorInt int itemTextColor) {
            bottomSheet.itemTextColor = itemTextColor;
            return this;
        }

        public Builder setItemTextColorRes(@ColorRes int itemTextColorRes) {
            bottomSheet.itemTextColor = ContextCompat.getColor(context, itemTextColorRes);
            return this;
        }

        public Builder setIconColor(@ColorInt int iconColor) {
            bottomSheet.iconColor = iconColor;
            return this;
        }

        public Builder setIconColorRes(@ColorInt int iconColorRes) {
            bottomSheet.iconColor = ContextCompat.getColor(context, iconColorRes);
            return this;
        }

        public Builder setIconTintMode(PorterDuff.Mode iconTintMode) {
            bottomSheet.iconTintMode = iconTintMode;
            return this;
        }

        public Builder setItemSelector(int selector) {
            bottomSheet.itemSelector = selector;
            return this;
        }

//----- Interfaces ---------------------------------------------------------------------------------

        public Builder setOnShowListener(DialogInterface.OnShowListener listener) {
            bottomSheet.onShowListener = listener;
            return this;
        }

        public Builder setOnDismissListener(DialogInterface.OnDismissListener listener) {
            bottomSheet.onDismissListener = listener;
            return this;
        }

        public Builder setCallback(BottomSheetCallback callback) {
            bottomSheet.bottomSheetCallback = callback;
            return this;
        }

//----- Build --------------------------------------------------------------------------------------

        public BottomSheet show() {
            bottomSheet.show();
            return bottomSheet;
        }

        public BottomSheet dismiss() {
            bottomSheet.dismiss();
            return bottomSheet;
        }

        public BottomSheet create() {
            return bottomSheet;
        }

//----- Getters ------------------------------------------------------------------------------------

        public TextView getTitleTextView() {
            return bottomSheet.titleTextView;
        }

        public ListView getListView() {
            return bottomSheet.listView;
        }

        public GridView getGridView() {
            return bottomSheet.gridView;
        }

        public View getView() {
            return bottomSheet.customView;
        }

//----- Deprecated ---------------------------------------------------------------------------------

        @Deprecated
        public Builder setCustomView(View view) {
            bottomSheet.customView = view;
            return this;
        }

        @Deprecated
        public Builder setCustomView(@LayoutRes int layoutResId) {
            bottomSheet.customView = LayoutInflater.from(context).inflate(layoutResId, null);
            return this;
        }
    }

//--------------------------------------------------------------------------------------------------

    private class BottomSheetAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return ITEMS != null ? ITEMS.size() : 0;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            int type = getItemViewType(position);
            BottomSheetItem item = bottomsheetItems.get(position);

            if (type == 0) {
                if (contentType == LIST) {
                    if (view == null) {
                        view = new BottomSheetCell(getContext());
                    }

                    BottomSheetCell cell = (BottomSheetCell) view;
                    cell.setIcon(item.icon, iconColor, iconTintMode);
                    cell.setText(item.text, itemTextColor);
                    cell.setHeight(cellHeight);
                    if (position != bottomsheetItems.size() - 1) {
                        cell.setDivider(dividers);
                        cell.setDividerColor(darkTheme);
                    }
                } else {
                    if (view == null) {
                        view = new BottomSheetGrid(getContext());
                    }

                    BottomSheetGrid cell = (BottomSheetGrid) view;
                    cell.setIcon(item.icon, iconColor, iconTintMode);
                    cell.setText(item.text, itemTextColor);
                }
            }

            return view;
        }

        @Override
        public int getItemViewType(int i) {
            return 0;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }
    }
}