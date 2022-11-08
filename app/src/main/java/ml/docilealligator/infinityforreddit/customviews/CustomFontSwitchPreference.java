package ml.docilealligator.infinityforreddit.customviews;

import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceViewHolder;
import androidx.preference.SwitchPreference;

import com.google.android.material.materialswitch.MaterialSwitch;

import ml.docilealligator.infinityforreddit.CustomFontReceiver;
import ml.docilealligator.infinityforreddit.CustomThemeWrapperReceiver;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class CustomFontSwitchPreference extends SwitchPreference implements CustomFontReceiver, CustomThemeWrapperReceiver {
    private CustomThemeWrapper customThemeWrapper;
    private Typeface typeface;
    private MaterialSwitch materialSwitch;

    public CustomFontSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(R.layout.preference_switch);

    }

    public CustomFontSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    public CustomFontSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    public CustomFontSwitchPreference(Context context) {
        super(context);
        setWidgetLayoutResource(R.layout.preference_switch);
    }

    @Override
    public void setChecked(boolean checked) {
        super.setChecked(checked);
        if (materialSwitch != null) {
            materialSwitch.setChecked(checked);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View iconImageView = holder.findViewById(android.R.id.icon);
        View titleTextView = holder.findViewById(android.R.id.title);
        View summaryTextView = holder.findViewById(android.R.id.summary);
        materialSwitch = (MaterialSwitch) holder.findViewById(R.id.material_switch_switch_preference);
        materialSwitch.setChecked(isChecked());
        materialSwitch.setOnClickListener(view -> {
            onClick();
        });

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
