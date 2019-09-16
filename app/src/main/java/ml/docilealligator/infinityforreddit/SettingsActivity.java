package ml.docilealligator.infinityforreddit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import Settings.AboutPreferenceFragment;
import Settings.MainPreferenceFragment;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import javax.inject.Inject;

public class SettingsActivity extends AppCompatActivity implements
    PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

  private static final String TITLE_STATE = "TS";

  @BindView(R.id.toolbar_settings_activity)
  Toolbar toolbar;

  @Inject
  SharedPreferences mSharedPreferences;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.settings_activity);

    ButterKnife.bind(this);

    ((Infinity) getApplication()).getAppComponent().inject(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      Window window = getWindow();
      if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
          != Configuration.UI_MODE_NIGHT_YES) {
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
      }
      window.setNavigationBarColor(ContextCompat.getColor(this, R.color.navBarColor));
    }

    boolean systemDefault = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q;
    int themeType = Integer
        .parseInt(mSharedPreferences.getString(SharedPreferencesUtils.THEME_KEY, "2"));
    switch (themeType) {
      case 0:
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_NO);
        break;
      case 1:
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES);
        break;
      case 2:
        if (systemDefault) {
          AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM);
        } else {
          AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_AUTO_BATTERY);
        }

    }

    setSupportActionBar(toolbar);

    if (savedInstanceState == null) {
      getSupportFragmentManager()
          .beginTransaction()
          .replace(R.id.frame_layout_settings_activity, new MainPreferenceFragment())
          .commit();
    } else {
      setTitle(savedInstanceState.getCharSequence(TITLE_STATE));
    }
    getSupportFragmentManager().addOnBackStackChangedListener(() -> {
      if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
        setTitle(R.string.settings_activity_label);
      } else if (getSupportFragmentManager().findFragmentById(
          R.id.frame_layout_settings_activity) instanceof AboutPreferenceFragment) {
        setTitle(R.string.settings_about_master_title);
      }
    });


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
  public boolean onPreferenceStartFragment(PreferenceFragmentCompat caller, Preference pref) {
    // Instantiate the new Fragment
    final Bundle args = pref.getExtras();
    final Fragment fragment = getSupportFragmentManager().getFragmentFactory().instantiate(
        getClassLoader(),
        pref.getFragment());
    fragment.setArguments(args);
    fragment.setTargetFragment(caller, 0);

    getSupportFragmentManager().beginTransaction()
        .replace(R.id.frame_layout_settings_activity, fragment)
        .addToBackStack(null)
        .commit();
    setTitle(pref.getTitle());
    return true;
  }
}
