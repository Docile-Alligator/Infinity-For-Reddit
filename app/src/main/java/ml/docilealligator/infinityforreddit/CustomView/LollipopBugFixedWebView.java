package ml.docilealligator.infinityforreddit.CustomView;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.util.AttributeSet;
import android.webkit.WebView;

public class LollipopBugFixedWebView extends WebView{
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
        if (Build.VERSION.SDK_INT == 21 || Build.VERSION.SDK_INT == 22) // Android Lollipop 5.0 & 5.1
            return context.createConfigurationContext(new Configuration());
        return context;
    }
}
