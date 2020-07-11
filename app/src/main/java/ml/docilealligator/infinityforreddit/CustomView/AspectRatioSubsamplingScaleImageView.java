package ml.docilealligator.infinityforreddit.CustomView;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;

public class AspectRatioSubsamplingScaleImageView extends SubsamplingScaleImageView {
    private float ratio;

    public AspectRatioSubsamplingScaleImageView(Context context) {
        super(context);
        ratio = 1.0F;
    }

    public AspectRatioSubsamplingScaleImageView(Context context, AttributeSet attr) {
        super(context, attr);
        this.ratio = 1.0F;
        this.init(context, attr);
    }

    public final float getRatio() {
        return this.ratio;
    }

    public final void setRatio(float var1) {
        this.ratio = var1;
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, com.santalu.aspectratioimageview.R.styleable.AspectRatioImageView);
            this.ratio = a.getFloat(com.santalu.aspectratioimageview.R.styleable.AspectRatioImageView_ari_ratio, 1.0F);
            a.recycle();
        }

    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
