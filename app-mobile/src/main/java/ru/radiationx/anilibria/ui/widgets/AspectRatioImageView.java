package ru.radiationx.anilibria.ui.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.ImageView; // <-- вместо AppCompatImageView

import ru.radiationx.anilibria.R;

/**
 * Created by radiationx on 26.08.17.
 */
public class AspectRatioImageView extends ImageView {
    private float aspectRatio = 1.0f;
    private boolean enabledAspectRation = true;

    public AspectRatioImageView(Context context) {
        super(context);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public AspectRatioImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        try (TypedArray typedArray = getContext().obtainStyledAttributes(attrs,
                R.styleable.AspectRatioImageView)) {
            aspectRatio = typedArray.getFloat(R.styleable.AspectRatioImageView_aspectRatio, 1f);
            enabledAspectRation = typedArray.getBoolean(R.styleable.AspectRatioImageView_enabledAspectRatio, true);
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (enabledAspectRation) {
            float width = getMeasuredWidth();
            float height = Math.min(width * aspectRatio, computeMaxHeight());
            setMeasuredDimension((int) width, (int) height);
        }
    }

    private float computeMaxHeight() {
        // если есть логика ограничения, можно оставить. Иначе можно убрать
        // или возвратить просто getMeasuredHeight().
        return Float.MAX_VALUE;
    }

    public void setAspectRatio(float aspectRatio) {
        this.aspectRatio = aspectRatio;
        requestLayout();
    }

    public void setEnabledAspectRation(boolean enabled) {
        enabledAspectRation = enabled;
        //requestLayout();
    }
}
