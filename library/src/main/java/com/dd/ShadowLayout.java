package com.dd;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.dd.shadow.layout.R;

public class ShadowLayout extends FrameLayout {

    private int mShadowColor;
    private float mShadowRadius;
    private float mCornerRadius;
    private float mDx;
    private float mDy;
    private boolean trimLeft;
    private boolean trimTop;
    private boolean trimRight;
    private boolean trimBottom;

    private boolean mInvalidateShadowOnSizeChanged = true;
    private boolean mForceInvalidateShadow = false;

    public ShadowLayout(Context context) {
        super(context);
        initView(context, null, 0);
    }

    public ShadowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs, 0);
    }

    public ShadowLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShadowLayout(final Context context, final AttributeSet attrs, final int defStyleAttr,
                        final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initView(context, attrs, defStyleRes);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return 0;
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return 0;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0 && (getBackground() == null || mInvalidateShadowOnSizeChanged || mForceInvalidateShadow)) {
            mForceInvalidateShadow = false;
            setBackgroundCompat(w, h);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mForceInvalidateShadow) {
            mForceInvalidateShadow = false;
            setBackgroundCompat(right - left, bottom - top);
        }
    }

    public void setInvalidateShadowOnSizeChanged(boolean invalidateShadowOnSizeChanged) {
        mInvalidateShadowOnSizeChanged = invalidateShadowOnSizeChanged;
    }

    public void invalidateShadow() {
        mForceInvalidateShadow = true;
        requestLayout();
        invalidate();
    }

    private void initView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyledRes) {
        initAttributes(context, attrs, defStyledRes);
        adjustAttributes();

        final int xPadding = (int) (mShadowRadius + Math.abs(mDx));
        final int yPadding = (int) (mShadowRadius + Math.abs(mDy));
        setPadding(trimLeft ? 0 : xPadding,
                trimTop ? 0 : yPadding,
                trimRight ? 0 : xPadding,
                trimBottom ? 0 : yPadding);
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    private void setBackgroundCompat(int w, int h) {
        // Cause paint.setShadowLayer() can not be previewed.
        if (isInEditMode()) {
            setBackgroundColor(mShadowColor);
            return;
        }

        Bitmap bitmap = createShadowBitmap(w, h, mCornerRadius, mShadowRadius, mDx, mDy, mShadowColor, Color.TRANSPARENT);
        BitmapDrawable drawable = new BitmapDrawable(getResources(), bitmap);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }


    private void initAttributes(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleRes) {
        if (attrs == null) {
            return;
        }
        final TypedArray attr = getTypedArray(context, attrs, R.styleable.ShadowLayout, defStyleRes);

        try {
            mCornerRadius = attr.getDimension(R.styleable.ShadowLayout_sl_cornerRadius, getResources().getDimension(R.dimen.default_corner_radius));
            mShadowRadius = attr.getDimension(R.styleable.ShadowLayout_sl_shadowRadius, getResources().getDimension(R.dimen.default_shadow_radius));
            mDx = attr.getDimension(R.styleable.ShadowLayout_sl_dx, 0);
            mDy = attr.getDimension(R.styleable.ShadowLayout_sl_dy, 0);
            trimLeft = attr.getBoolean(R.styleable.ShadowLayout_sl_trim_left, false);
            trimTop = attr.getBoolean(R.styleable.ShadowLayout_sl_trim_top, false);
            trimRight = attr.getBoolean(R.styleable.ShadowLayout_sl_trim_right, false);
            trimBottom = attr.getBoolean(R.styleable.ShadowLayout_sl_trim_bottom, false);
            mShadowColor = attr.getColor(R.styleable.ShadowLayout_sl_shadowColor, getResources().getColor(R.color.default_shadow_color));
        } finally {
            attr.recycle();
        }
    }

    @NonNull
    private TypedArray getTypedArray(@NonNull Context context, @NonNull AttributeSet attributeSet,
                                     @NonNull int[] attr, int defStyleRes) {
        return context.obtainStyledAttributes(attributeSet, attr, 0, defStyleRes);
    }

    /**
     * Cause {@link Paint#setShadowLayer(float, float, float, int)} will set alpha of the shadow as
     * the paint's alpha if the shadow color is opaque, or the alpha from the shadow color if not.
     */
    private void adjustAttributes() {
        if (Color.alpha(mShadowColor) >= 255) {
            int red = Color.red(mShadowColor);
            int green = Color.green(mShadowColor);
            int blue = Color.blue(mShadowColor);
            mShadowColor = Color.argb(254, red, green, blue);
        }
    }

    @NonNull
    private Bitmap createShadowBitmap(int shadowWidth, int shadowHeight, float cornerRadius, float shadowRadius,
                                      float dx, float dy, int shadowColor, int fillColor) {

        Bitmap output = Bitmap.createBitmap(shadowWidth, shadowHeight, Bitmap.Config.ARGB_4444);
        Canvas canvas = new Canvas(output);

        RectF shadowRect = new RectF(
                trimLeft ? 0 : shadowRadius,
                trimTop ? 0 : shadowRadius,
                trimRight ? shadowWidth : shadowWidth - shadowRadius,
                trimBottom ? shadowHeight : shadowHeight - shadowRadius);

//        if (dy > 0) {
            shadowRect.top += dy;
            shadowRect.bottom -= dy;
//        } else if (dy < 0) {
//            shadowRect.top += Math.abs(dy);
//            shadowRect.bottom -= Math.abs(dy);
//        }

//        if (dx > 0) {
            shadowRect.left += dx;
            shadowRect.right -= dx;
//        } else if (dx < 0) {
//            shadowRect.left += Math.abs(dx);
//            shadowRect.right -= Math.abs(dx);
//        }

        Paint shadowPaint = new Paint();
        shadowPaint.setAntiAlias(true);
        shadowPaint.setColor(fillColor);
        shadowPaint.setStyle(Paint.Style.FILL);

        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        if (!isInEditMode()) {
            shadowPaint.setShadowLayer(shadowRadius, dx, dy, shadowColor);
        }

        canvas.drawRoundRect(shadowRect, cornerRadius, cornerRadius, shadowPaint);

        return output;
    }
}
