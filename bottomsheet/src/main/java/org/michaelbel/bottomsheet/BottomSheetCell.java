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

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Date: 17 FEB 2018
 * Time: 00:30 MSK
 *
 * @author Michael Bel
 */

@SuppressWarnings("all")
public class BottomSheetCell extends FrameLayout {

    private Paint paint;
    private int cellHeight;
    private boolean divider;

    private TextView textView;
    private ImageView iconView;

    public BottomSheetCell(Context context) {
        super(context);

        if (paint == null) {
            paint = new Paint();
            paint.setStrokeWidth(1);
            paint.setColor(0x1F000000);
        }

        cellHeight = Utils.dp(context, 48);

        iconView = new ImageView(context);
        iconView.setScaleType(ImageView.ScaleType.CENTER);

        LayoutParams params1 = new LayoutParams(Utils.dp(context, 24), Utils.dp(context, 24));
        params1.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params1.leftMargin = Utils.dp(context, 16);
        params1.rightMargin = Utils.dp(context, 16);

        iconView.setLayoutParams(params1);
        addView(iconView);

        textView = new TextView(context);
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);

        LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        params2.leftMargin = Utils.dp(context, 16);
        params2.rightMargin = Utils.dp(context, 16);

        textView.setLayoutParams(params2);
        addView(textView);
    }

    public BottomSheetCell setIcon(@DrawableRes int resId, @ColorInt int color, PorterDuff.Mode iconTintMode) {
        if (resId != 0) {
            Drawable res = ContextCompat.getDrawable(getContext(), resId);
            if (res != null) {
                res.clearColorFilter();
                res.mutate().setColorFilter(color, iconTintMode);
            }

            iconView.setImageDrawable(res);

            LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            params2.leftMargin = Utils.dp(getContext(), 72);
            params2.rightMargin = Utils.dp(getContext(), 16);
            textView.setLayoutParams(params2);
        } else {
            LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            params2.leftMargin = Utils.dp(getContext(), 16);
            params2.rightMargin = Utils.dp(getContext(), 16);
            textView.setLayoutParams(params2);
        }

        return this;
    }

    public BottomSheetCell setIcon(Drawable resId, @ColorInt int color, PorterDuff.Mode iconTintMode) {
        if (resId != null) {
            Drawable res = resId;
            if (res != null) {
                res.clearColorFilter();
                res.mutate().setColorFilter(color, iconTintMode);
            }

            iconView.setImageDrawable(res);

            LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            params2.leftMargin = Utils.dp(getContext(), 72);
            params2.rightMargin = Utils.dp(getContext(), 16);
            textView.setLayoutParams(params2);
        } else {
            LayoutParams params2 = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params2.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
            params2.leftMargin = Utils.dp(getContext(), 16);
            params2.rightMargin = Utils.dp(getContext(), 16);
            textView.setLayoutParams(params2);
        }

        return this;
    }

    public BottomSheetCell setText(@NonNull CharSequence text, @ColorInt int color) {
        textView.setText(text);
        textView.setTextColor(color);
        return this;
    }

    public BottomSheetCell setHeight(int height) {
        cellHeight = height;
        return this;
    }

    public BottomSheetCell setDivider(boolean divider) {
        this.divider = divider;
        setWillNotDraw(!divider);
        return this;
    }

    public BottomSheetCell setDividerColor(boolean theme) {
        paint.setColor(!theme ? 0x1F000000 : 0x1FFFFFFF);
        setWillNotDraw(false);
        return this;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.makeMeasureSpec(widthMeasureSpec, MeasureSpec.EXACTLY);
        int height = cellHeight + (divider ? 1 : 0);
        setMeasuredDimension(width, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (divider) {
            canvas.drawLine(getPaddingLeft(), getHeight() - 1, getWidth() - getPaddingRight(), getHeight() - 1, paint);
        }
    }
}