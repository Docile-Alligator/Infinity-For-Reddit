package ml.docilealligator.infinityforreddit.activities;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivitySettingsBinding;
import ml.docilealligator.infinityforreddit.events.RecreateActivityEvent;
import ml.docilealligator.infinityforreddit.settings.AboutPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.AdvancedPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.FontPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.GesturesAndButtonsPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.InterfacePreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.MainPreferenceFragment;
import ml.docilealligator.infinityforreddit.settings.PostPreferenceFragment;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class SettingsActivity extends BaseActivity implements
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_STATE = "TS";

    private ActivitySettingsBinding binding;

    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutSettingsActivity);
            }

            if (isImmersiveInterface()) {
                ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                    @NonNull
                    @Override
                    public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                        Insets allInsets = Utils.getInsets(insets, false);

                        setMargins(binding.toolbarSettingsActivity,
                                allInsets.left,
                                allInsets.top,
                                allInsets.right,
                                BaseActivity.IGNORE_MARGIN);

                        return insets;
                    }
                });
            }
        }

        setSupportActionBar(binding.toolbarSettingsActivity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout_settings_activity, new MainPreferenceFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_STATE));
        }

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.frame_layout_settings_activity);
            if (fragment instanceof AboutPreferenceFragment) {
                setTitle(R.string.settings_about_master_title);
            } else if (fragment instanceof InterfacePreferenceFragment) {
                setTitle(R.string.settings_interface_title);
            } else if (fragment instanceof FontPreferenceFragment) {
                setTitle(R.string.settings_font_title);
            } else if (fragment instanceof GesturesAndButtonsPreferenceFragment) {
                setTitle(R.string.settings_gestures_and_buttons_title);
            } else if (fragment instanceof PostPreferenceFragment) {
                setTitle(R.string.settings_category_post_title);
            } else if (fragment instanceof AdvancedPreferenceFragment) {
                setTitle(R.string.settings_advanced_master_title);
            } else if (fragment instanceof MainPreferenceFragment) {
                setTitle(R.string.settings_activity_label);
            }
        });
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    public SharedPreferences getCurrentAccountSharedPreferences() {
        return mCurrentAccountSharedPreferences;
    }

    @Override
    public CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutSettingsActivity,
                binding.collapsingToolbarLayoutSettingsActivity, binding.toolbarSettingsActivity);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putCharSequence(TITLE_STATE, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, Preference pref) {
        // Instantiate the new Fragment
        final Bundle args = pref.getExtras();
        final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
                getClassLoader(),
                pref.getFragment());
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, 0);

        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left, R.anim.enter_from_left, R.anim.exit_to_right)
                .replace(R.id.frame_layout_settings_activity, fragment)
                .addToBackStack(null)
                .commit();
        setTitle(pref.getTitle());
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void showSnackbar(int stringId, int actionStringId, View.OnClickListener onClickListener) {
        Snackbar.make(binding.getRoot(), stringId, BaseTransientBottomBar.LENGTH_SHORT).setAction(actionStringId, onClickListener).show();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecreateActivityEvent(RecreateActivityEvent recreateActivityEvent) {
        ActivityCompat.recreate(this);
    }
}
