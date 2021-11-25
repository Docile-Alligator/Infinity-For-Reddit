package ml.docilealligator.infinityforreddit.markdown;

import android.text.Layout;
import android.text.Spannable;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;

import ml.docilealligator.infinityforreddit.customviews.SpoilerOnClickTextView;

public class SpoilerSpan extends ClickableSpan {
    final int textColor;
    final int backgroundColor;
    private boolean isShowing = false;

    public SpoilerSpan(@NonNull int textColor, @NonNull int backgroundColor) {
        this.textColor = textColor;
        this.backgroundColor = backgroundColor;
    }

    @Override
    public void onClick(@NonNull View widget) {
        if (!(widget instanceof TextView)) {
            return;
        }

        final TextView textView = (TextView) widget;
        final Spannable spannable = (Spannable) textView.getText();

        final int end = spannable.getSpanEnd(this);

        if (end < 0) {
            return;
        }

        final Layout layout = textView.getLayout();
        if (layout == null) {
            return;
        }

        if (widget instanceof SpoilerOnClickTextView) {
            ((SpoilerOnClickTextView) textView).setSpoilerOnClick(true);
        }
        isShowing = !isShowing;
        widget.invalidate();
    }

    @Override
    public void updateDrawState(@NonNull TextPaint ds) {
        if (isShowing) {
            ds.bgColor = backgroundColor & 0x0D000000; //Slightly darker background color for revealed spoiler
            super.updateDrawState(ds);
        } else {
            ds.bgColor = backgroundColor;
        }
        ds.setColor(textColor);
        ds.setUnderlineText(false);
    }
}