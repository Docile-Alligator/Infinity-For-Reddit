package ml.docilealligator.infinityforreddit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.google.android.material.appbar.AppBarLayout;
import java.util.ArrayList;
import javax.inject.Inject;
import javax.inject.Named;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RulesActivity extends AppCompatActivity {

  static final String EXTRA_SUBREDDIT_NAME = "ESN";

  @BindView(R.id.appbar_layout_rules_activity)
  AppBarLayout appBarLayout;
  @BindView(R.id.toolbar_rules_activity)
  Toolbar toolbar;
  @BindView(R.id.progress_bar_rules_activity)
  ProgressBar progressBar;
  @BindView(R.id.recycler_view_rules_activity)
  RecyclerView recyclerView;
  @BindView(R.id.error_text_view_rules_activity)
  TextView errorTextView;
  @Inject
  @Named("no_oauth")
  Retrofit mRetrofit;
  @Inject
  SharedPreferences mSharedPreferences;
  private String mSubredditName;
  private RulesRecyclerViewAdapter mAdapter;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_rules);

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
        if (finalLightNavBar) {
          decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR);
        }
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

        if (resources.getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT
            || resources.getBoolean(R.bool.isTablet)) {
          int navBarResourceId = resources
              .getIdentifier("navigation_bar_height", "dimen", "android");
          if (navBarResourceId > 0) {
            recyclerView.setPadding(0, 0, 0, resources.getDimensionPixelSize(navBarResourceId));
          }
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

    mSubredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);

    recyclerView.setLayoutManager(new LinearLayoutManager(this));
    mAdapter = new RulesRecyclerViewAdapter(this);
    recyclerView.setAdapter(mAdapter);

    fetchRules();
  }

  private void fetchRules() {
    progressBar.setVisibility(View.VISIBLE);
    errorTextView.setVisibility(View.GONE);

    RedditAPI api = mRetrofit.create(RedditAPI.class);
    Call<String> rulesCall = api.getRules(mSubredditName);
    rulesCall.enqueue(new Callback<String>() {
      @Override
      public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
        if (response.isSuccessful()) {
          new ParseRulesAsyncTask(response.body(),
              new ParseRulesAsyncTask.ParseRulesAsyncTaskListener() {
                @Override
                public void parseSuccessful(ArrayList<Rule> rules) {
                  progressBar.setVisibility(View.GONE);
                  if (rules == null || rules.size() == 0) {
                    errorTextView.setVisibility(View.VISIBLE);
                    errorTextView.setText(R.string.no_rule);
                    errorTextView.setOnClickListener(view -> {
                    });
                  }
                  mAdapter.changeDataset(rules);
                }

                @Override
                public void parseFailed() {
                  displayError();
                }
              }).execute();
        } else {
          displayError();
        }
      }

      @Override
      public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
        displayError();
      }
    });
  }

  private void displayError() {
    progressBar.setVisibility(View.GONE);
    errorTextView.setVisibility(View.VISIBLE);
    errorTextView.setText(R.string.error_loading_rules);
    errorTextView.setOnClickListener(view -> fetchRules());
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
  protected void onDestroy() {
    super.onDestroy();
    EventBus.getDefault().unregister(this);
  }

  @Subscribe
  public void onAccountSwitchEvent(SwitchAccountEvent event) {
    finish();
  }

  private static class ParseRulesAsyncTask extends
      AsyncTask<Void, ArrayList<Rule>, ArrayList<Rule>> {

    private final String response;
    private final ParseRulesAsyncTaskListener parseRulesAsyncTaskListener;

    ParseRulesAsyncTask(String response, ParseRulesAsyncTaskListener parseRulesAsyncTaskListener) {
      this.response = response;
      this.parseRulesAsyncTaskListener = parseRulesAsyncTaskListener;
    }

    @Override
    protected ArrayList<Rule> doInBackground(Void... voids) {
      try {
        JSONArray rulesArray = new JSONObject(response).getJSONArray(JSONUtils.RULES_KEY);
        ArrayList<Rule> rules = new ArrayList<>();
        for (int i = 0; i < rulesArray.length(); i++) {
          String shortName = rulesArray.getJSONObject(i).getString(JSONUtils.SHORT_NAME_KEY);
          String description = null;
          if (rulesArray.getJSONObject(i).has(JSONUtils.DESCRIPTION_KEY)) {
            description = Utils.addSubredditAndUserLink(
                rulesArray.getJSONObject(i).getString(JSONUtils.DESCRIPTION_KEY));
          }
          rules.add(new Rule(shortName, description));
        }
        return rules;
      } catch (JSONException e) {
        e.printStackTrace();
      }
      return null;
    }

    @Override
    protected void onPostExecute(ArrayList<Rule> rules) {
      if (rules != null) {
        parseRulesAsyncTaskListener.parseSuccessful(rules);
      } else {
        parseRulesAsyncTaskListener.parseFailed();
      }
    }

    interface ParseRulesAsyncTaskListener {

      void parseSuccessful(ArrayList<Rule> rules);

      void parseFailed();
    }
  }
}
