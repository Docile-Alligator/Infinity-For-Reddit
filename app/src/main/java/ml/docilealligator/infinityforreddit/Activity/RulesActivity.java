package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Adapter.RulesRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditAPI;
import ml.docilealligator.infinityforreddit.Rule;
import ml.docilealligator.infinityforreddit.Utils.JSONUtils;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RulesActivity extends BaseActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    @BindView(R.id.coordinator_layout_rules_activity)
    CoordinatorLayout coordinatorLayout;
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
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    private String mSubredditName;
    private RulesRecyclerViewAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_rules);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();

            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(appBarLayout);
            }

            if (isImmersiveInterface()) {
                window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                adjustToolbar(toolbar);

                int navBarHeight = getNavBarHeight();
                if (navBarHeight > 0) {
                    recyclerView.setPadding(0, 0, 0, navBarHeight);
                }
            }
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mSubredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new RulesRecyclerViewAdapter(this, mCustomThemeWrapper);
        recyclerView.setAdapter(mAdapter);

        fetchRules();
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndToolbarTheme(appBarLayout, toolbar);
        errorTextView.setTextColor(mCustomThemeWrapper.getSecondaryTextColor());
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
                    new ParseRulesAsyncTask(response.body(), new ParseRulesAsyncTask.ParseRulesAsyncTaskListener() {
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

    private static class ParseRulesAsyncTask extends AsyncTask<Void, ArrayList<Rule>, ArrayList<Rule>> {
        private String response;
        private ParseRulesAsyncTaskListener parseRulesAsyncTaskListener;

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
                        description = Utils.modifyMarkdown(rulesArray.getJSONObject(i).getString(JSONUtils.DESCRIPTION_KEY));
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
