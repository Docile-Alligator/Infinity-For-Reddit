package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class SuicidePreventionActivity extends BaseActivity {

    static final String EXTRA_QUERY = "EQ";
    static final String EXTRA_RETURN_QUERY = "ERQ";
    @BindView(R.id.linear_layout_suicide_prevention_activity)
    LinearLayout linearLayout;
    @BindView(R.id.quote_text_view_suicide_prevention_activity)
    TextView quoteTextView;
    @BindView(R.id.linear_layout_check_box_wrapper_suicide_prevention_activity)
    LinearLayout checkBoxWrapperlinearLayout;
    @BindView(R.id.do_not_show_this_again_check_box)
    MaterialCheckBox doNotShowThisAgainCheckBox;
    @BindView(R.id.do_not_show_this_again_text_view)
    TextView doNotShowThisAgainTextView;
    @BindView(R.id.continue_button_suicide_prevention_activity)
    MaterialButton continueButton;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplicationContext()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suicide_prevention);

        ButterKnife.bind(this);

        applyCustomTheme();

        checkBoxWrapperlinearLayout.setOnClickListener(view -> {
            doNotShowThisAgainCheckBox.performClick();
        });

        continueButton.setOnClickListener(view -> {
            if (doNotShowThisAgainCheckBox.isChecked()) {
                mSharedPreferences.edit().putBoolean(SharedPreferencesUtils.SHOW_SUICIDE_PREVENTION_ACTIVITY, false).apply();
            }
            Intent returnIntent = new Intent();
            returnIntent.putExtra(EXTRA_RETURN_QUERY, getIntent().getStringExtra(EXTRA_QUERY));
            setResult(RESULT_OK, returnIntent);
            finish();
        });
    }

    @Override
    protected SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        linearLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        quoteTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        doNotShowThisAgainTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());
        continueButton.setBackgroundTintList(ColorStateList.valueOf(mCustomThemeWrapper.getColorPrimaryLightTheme()));
        continueButton.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        if (typeface != null) {
            quoteTextView.setTypeface(typeface);
            doNotShowThisAgainTextView.setTypeface(typeface);
            continueButton.setTypeface(typeface);
        }
    }
}