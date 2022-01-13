package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;

public class CustomFontPreference extends Preference implements CustomFontReceiver {
    private Typeface typeface;

    public CustomFontPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomFontPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomFontPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFontPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        if (typeface != null) {
            View titleTextView = holder.findViewById(android.R.id.title);
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTypeface(typeface);
            }
            View summaryTextView = holder.findViewById(android.R.id.summary);
            if (summaryTextView instanceof TextView) {
                ((TextView) summaryTextView).setTypeface(typeface);
            }
        }
    }

    @Override
    public void setCustomFont(Typeface typeface, Typeface titleTypeface, Typeface contentTypeface) {
        this.typeface = typeface;
    }
}
