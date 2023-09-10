package ml.docilealligator.infinityforreddit.markdown;

import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

import androidx.annotation.NonNull;

public class SuperscriptSpan extends MetricAffectingSpan {
    private static final float SCRIPT_DEF_TEXT_SIZE_RATIO = .75F;
    public final boolean isBracketed;

    public SuperscriptSpan() {
        this.isBracketed = false;
    }

    public SuperscriptSpan(boolean isBracketed) {
        this.isBracketed = isBracketed;
    }

    @Override
    public void updateDrawState(TextPaint tp) {
        apply(tp);
    }

    @Override
    public void updateMeasureState(@NonNull TextPaint tp) {
        apply(tp);
    }

    private void apply(TextPaint paint) {
        paint.setTextSize(paint.getTextSize() * SCRIPT_DEF_TEXT_SIZE_RATIO);
        paint.baselineShift += (int) (paint.ascent() / 2);
    }
}
