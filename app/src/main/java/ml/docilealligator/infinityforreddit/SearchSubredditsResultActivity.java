package ml.docilealligator.infinityforreddit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.appbar.AppBarLayout;
import javax.inject.Inject;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class SearchSubredditsResultActivity extends AppCompatActivity {

  static final String EXTRA_QUERY = "EQ";
  static final String EXTRA_RETURN_SUBREDDIT_NAME = "ERSN";
  static final String EXTRA_RETURN_SUBREDDIT_ICON_URL = "ERSIURL";

  private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
  private static final String ACCESS_TOKEN_STATE = "ATS";
  private static final String ACCOUNT_NAME_STATE = "ANS";
  private static final String FRAGMENT_OUT_STATE = "FOS";

  @BindView(R.id.appbar_layout_search_subreddits_result_activity)
  AppBarLayout appBarLayout;
  @BindView(R.id.toolbar_search_subreddits_result_activity)
  Toolbar toolbar;
  Fragment mFragment;
  @Inject
  RedditDataRoomDatabase mRedditDataRoomDatabase;
  @Inject
  SharedPreferences mSharedPreferences;
  private boolean mNullAccessToken = false;
  private String mAccessToken;
  private String mAccountName;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_search_subreddits_result);

    ButterKnife.bind(this);

    ((Infinity) getApplication()).getAppComponent().inject(this);

    EventBus.getDefault().register(this);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      Resources resources = getResources();

      if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
          || resources.getBoolean(R.bool.isTablet)) {
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        boolean lightNavBar = false;
        if ((resources.getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK)
            != Configuration.UI_MODE_NIGHT_YES) {
          lightNavBar = true;
        }
        boolean finalLightNavBar = lightNavBar;

        View decorView = window.getDecorView();
        appBarLayout.addOnOffsetChangedListener(new AppBarStateChangeListener() {
          @Override
          void onStateChanged(AppBarLayout appBarLayout, State state) {
            if (state == State.COLLAPSED) {
              if (finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
              }
            } else if (state == State.EXPANDED) {
              if (finalLightNavBar) {
                decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
              }
            }
          }
        });

        int statusBarResourceId = getResources()
            .getIdentifier("status_bar_height", "dimen", "android");
        if (statusBarResourceId > 0) {
          ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) toolbar
              .getLayoutParams();
          params.topMargin = getResources().getDimensionPixelSize(statusBarResourceId);
          toolbar.setLayoutParams(params);
        }
      }
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
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    String query = getIntent().getExtras().getString(EXTRA_QUERY);

    if (savedInstanceState == null) {
      getCurrentAccountAndInitializeFragment(query);
    } else {
      mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
      mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
      mAccountName = savedInstanceState.getString(ACCOUNT_NAME_STATE);
      if (!mNullAccessToken && mAccessToken == null) {
        getCurrentAccountAndInitializeFragment(query);
      } else {
        mFragment = getSupportFragmentManager().getFragment(savedInstanceState, FRAGMENT_OUT_STATE);
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.frame_layout_search_subreddits_result_activity, mFragment).commit();
      }
    }
  }

  private void getCurrentAccountAndInitializeFragment(String query) {
    new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
      if (account == null) {
        mNullAccessToken = true;
      } else {
        mAccessToken = account.getAccessToken();
        mAccountName = account.getUsername();
      }

      mFragment = new SubredditListingFragment();
      Bundle bundle = new Bundle();
      bundle.putString(SubredditListingFragment.EXTRA_QUERY, query);
      bundle.putBoolean(SubredditListingFragment.EXTRA_IS_POSTING, true);
      bundle.putString(SubredditListingFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
      bundle.putString(SubredditListingFragment.EXTRA_ACCOUNT_NAME, mAccountName);
      mFragment.setArguments(bundle);
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.frame_layout_search_subreddits_result_activity, mFragment).commit();
    }).execute();
  }

  void getSelectedSubreddit(String name, String iconUrl) {
    Intent returnIntent = new Intent();
    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_NAME, name);
    returnIntent.putExtra(EXTRA_RETURN_SUBREDDIT_ICON_URL, iconUrl);
    setResult(Activity.RESULT_OK, returnIntent);
    finish();
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return false;
  }

  @Override
  protected void onSaveInstanceState(@NonNull Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mFragment != null) {
      getSupportFragmentManager().putFragment(outState, FRAGMENT_OUT_STATE, mFragment);
    }
    outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
    outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
    outState.putString(ACCOUNT_NAME_STATE, mAccountName);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Subscribe
  public void onAccountSwitchEvent(SwitchAccountEvent event) {
    finish();
  }
}
