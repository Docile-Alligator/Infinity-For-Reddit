package ml.docilealligator.infinityforreddit.customviews.preference;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.ListPreference;
import androidx.preference.PreferenceViewHolder;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapperReceiver;

public class CustomFontListPreference extends ListPreference implements CustomFontReceiver, CustomThemeWrapperReceiver {
    private CustomThemeWrapper customThemeWrapper;
    private Typeface typeface;

    public CustomFontListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public CustomFontListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CustomFontListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomFontListPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View iconImageView = holder.findViewById(android.R.id.icon);
        View titleTextView = holder.findViewById(android.R.id.title);
        View summaryTextView = holder.findViewById(android.R.id.summary);

        if (customThemeWrapper != null) {
            if (iconImageView instanceof ImageView) {
                if (isEnabled()) {
                    ((ImageView) iconImageView).setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                } else {
                    ((ImageView) iconImageView).setColorFilter(customThemeWrapper.getSecondaryTextColor(), android.graphics.PorterDuff.Mode.SRC_IN);
                }
            }
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTextColor(customThemeWrapper.getPrimaryTextColor());
            }
            if (summaryTextView instanceof TextView) {
                ((TextView) summaryTextView).setTextColor(customThemeWrapper.getSecondaryTextColor());
            }
        }

        if (typeface != null) {
            if (titleTextView instanceof TextView) {
                ((TextView) titleTextView).setTypeface(typeface);
            }
            if (summaryTextView instanceof TextView) {
                ((TextView) summaryTextView).setTypeface(typeface);
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
