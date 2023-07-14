package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class CustomFontPreferenceCategory extends PreferenceCategory implements CustomFontReceiver, CustomThemeWrapperReceiver {
    private CustomThemeWrapper customThemeWrapper;
    private Typeface typeface;

    public CustomFontPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomFontPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomFontPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFontPreferenceCategory(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View titleTextView = holder.findViewById(android.R.id.title);

        if (customThemeWrapper != null) {
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTextColor(customThemeWrapper.getColorAccent());
            }
        }

        if (typeface != null) {
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTypeface(typeface);
            }
        }
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }

    @Override
    public void setCustomThemeWrapper(CustomThemeWrapper customThemeWrapper) {
        this.customThemeWrapper = customThemeWrapper;
    }
}
