package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import pl.droidsonroids.gif.GifImageView;

public class AspectRatioGifImageView extends GifImageView {
    private float ratio;

    public AspectRatioGifImageView(Context context) {
        super(context);
        this.ratio = 1.0F;
    }

    public AspectRatioGifImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.ratio = 1.0F;
        this.init(context, attrs);
    }

    public final float getRatio() {
        return this.ratio;
    }

    public final void setRatio(float var1) {
        if (Math.abs(this.ratio - var1) > 0.0001) {
            this.ratio = var1;

            requestLayout();
            invalidate();
        }
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, com.santalu.aspectratioimageview.R.styleable.AspectRatioImageView);
            this.ratio = a.getFloat(com.santalu.aspectratioimageview.R.styleable.AspectRatioImageView_ari_ratio, 1.0F);
            a.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (this.ratio > 0) {
            int width = this.getMeasuredWidth();
            int height = this.getMeasuredHeight();
            if (width != 0 || height != 0) {
                if (width > 0) {
                    height = (int) ((float) width * this.ratio);
                } else {
                    width = (int) ((float) height / this.ratio);
                }

                this.setMeasuredDimension(width, height);
            }
        }
    }
}
