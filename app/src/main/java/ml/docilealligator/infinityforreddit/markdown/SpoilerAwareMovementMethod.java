package ml.docilealligator.infinityforreddit.markdown;

import android.graphics.RectF;
import android.text.Layout;
import android.text.Spannable;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import me.saket.bettermovementmethod.BetterLinkMovementMethod;

/**
 * Extension of {@link BetterLinkMovementMethod} that handles {@link SpoilerSpan}s
 */
public class SpoilerAwareMovementMethod extends BetterLinkMovementMethod {
    private final RectF touchedLineBounds = new RectF();

    @Override
    protected ClickableSpan findClickableSpanUnderTouch(TextView textView, Spannable text, MotionEvent event) {
        // A copy of super method. Logic for selecting correct clickable span was moved to selectClickableSpan

        // So we need to find the location in text where touch was made, regardless of whether the TextView
        // has scrollable text. That is, not the entire text is currently visible.
        int touchX = (int) event.getX();
        int touchY = (int) event.getY();

        // Ignore padding.
        touchX -= textView.getTotalPaddingLeft();
        touchY -= textView.getTotalPaddingTop();

        // Account for scrollable text.
        touchX += textView.getScrollX();
        touchY += textView.getScrollY();

        final Layout layout = textView.getLayout();
        final int touchedLine = layout.getLineForVertical(touchY);
        final int touchOffset = layout.getOffsetForHorizontal(touchedLine, touchX);

        touchedLineBounds.left = layout.getLineLeft(touchedLine);
        touchedLineBounds.top = layout.getLineTop(touchedLine);
        touchedLineBounds.right = layout.getLineWidth(touchedLine) + touchedLineBounds.left;
        touchedLineBounds.bottom = layout.getLineBottom(touchedLine);

        if (touchedLineBounds.contains(touchX, touchY)) {
            // Find a ClickableSpan that lies under the touched area.
            final Object[] spans = text.getSpans(touchOffset, touchOffset, ClickableSpan.class);
            // BEGIN Infinity changed
            return selectClickableSpan(spans);
            // END Infinity changed
        } else {
            // Touch lies outside the line's horizontal bounds where no spans should exist.
            return null;
        }
    }

    /**
     * Select a span according to priorities:
     * 1. Hidden spoiler
     * 2. Non-spoiler span (i.e. link)
     * 3. Shown spoiler
     */
    @Nullable
    private ClickableSpan selectClickableSpan(@NonNull Object[] spans) {
        SpoilerSpan spoilerSpan = null;
        ClickableSpan nonSpoilerSpan = null;
        for (int i = spans.length - 1; i >= 0; i--) {
            final Object span = spans[i];
            if (span instanceof SpoilerSpan) {
                spoilerSpan = (SpoilerSpan) span;
            } else if (span instanceof ClickableSpan) {
                nonSpoilerSpan = (ClickableSpan) span;
            }
        }

        if (spoilerSpan != null && !spoilerSpan.isShowing()) {
            return spoilerSpan;
        } else if (nonSpoilerSpan != null){
            return nonSpoilerSpan;
        } else {
            return spoilerSpan;
        }
    }

    @Override
    protected void dispatchUrlLongClick(TextView textView, ClickableSpan clickableSpan) {
        if (clickableSpan instanceof SpoilerSpan) {
            ((SpoilerSpan) clickableSpan).onLongClick(textView);
            return;
        }
        super.dispatchUrlLongClick(textView, clickableSpan);
    }

    @Override
    protected void highlightUrl(TextView textView, ClickableSpan clickableSpan, Spannable text) {
        if (clickableSpan instanceof SpoilerSpan) {
            return;
        }
        super.highlightUrl(textView, clickableSpan, text);
    }
}
