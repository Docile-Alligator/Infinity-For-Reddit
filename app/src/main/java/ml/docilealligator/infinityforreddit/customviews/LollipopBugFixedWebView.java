package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.webkit.WebView;

public class LollipopBugFixedWebView extends WebView {
    private boolean isAnonymous;

    public LollipopBugFixedWebView(Context context) {
        super(getFixedContext(context));
    }

    public LollipopBugFixedWebView(Context context, AttributeSet attrs) {
        super(getFixedContext(context), attrs);
    }

    public LollipopBugFixedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(getFixedContext(context), attrs, defStyleAttr);
    }

    // To fix Android Lollipop WebView problem create a new configuration on that Android version only
    private static Context getFixedContext(Context context) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP
                || Build.VERSION.SDK_INT == Build.VERSION_CODES.LOLLIPOP_MR1) // Android Lollipop 5.0 & 5.1
            return context.createConfigurationContext(new Configuration());
        return context;
    }

    public void setAnonymous(boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }

    @Override
    public InputConnection onCreateInputConnection(EditorInfo outAttrs) {
        InputConnection inputConnection = super.onCreateInputConnection(outAttrs);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && isAnonymous) {
            outAttrs.imeOptions |= EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING;
        }
        return inputConnection;
    }
}
