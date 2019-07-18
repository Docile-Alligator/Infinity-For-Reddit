package ml.docilealligator.infinityforreddit;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RulesActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    @BindView(R.id.progress_bar_rules_activity) ProgressBar progressBar;
    @BindView(R.id.recycler_view_rules_activity) RecyclerView recyclerView;
    @BindView(R.id.error_text_view_rules_activity) TextView errorTextView;

    private String mSubredditName;

    private RulesRecyclerViewAdapter mAdapter;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rules);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

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
                if(response.isSuccessful()) {
                    new ParseRulesAsyncTask(response.body(), new ParseRulesAsyncTask.ParseRulesAsyncTaskListener() {
                        @Override
                        public void parseSuccessful(ArrayList<Rule> rules) {
                            progressBar.setVisibility(View.GONE);
                            if(rules == null || rules.size() == 0) {
                                errorTextView.setVisibility(View.VISIBLE);
                                errorTextView.setText(R.string.no_rule);
                                errorTextView.setOnClickListener(view -> {});
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
        if(item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    private static class ParseRulesAsyncTask extends AsyncTask<Void, ArrayList<Rule>, ArrayList<Rule>> {
        private String response;
        private ParseRulesAsyncTaskListener parseRulesAsyncTaskListener;

        interface ParseRulesAsyncTaskListener {
            void parseSuccessful(ArrayList<Rule> rules);
            void parseFailed();
        }

        ParseRulesAsyncTask(String response, ParseRulesAsyncTaskListener parseRulesAsyncTaskListener) {
            this.response = response;
            this.parseRulesAsyncTaskListener = parseRulesAsyncTaskListener;
        }

        @Override
        protected ArrayList<Rule> doInBackground(Void... voids) {
            try {
                JSONArray rulesArray = new JSONObject(response).getJSONArray(JSONUtils.RULES_KEY);
                ArrayList<Rule> rules = new ArrayList<>();
                for(int i = 0; i < rulesArray.length(); i++) {
                    String shortName = rulesArray.getJSONObject(i).getString(JSONUtils.SHORT_NAME_KEY);
                    String descriptionHtml = null;
                    if(rulesArray.getJSONObject(i).has(JSONUtils.DESCRIPTION_HTML_KEY)) {
                        descriptionHtml = rulesArray.getJSONObject(i).getString(JSONUtils.DESCRIPTION_HTML_KEY);
                    }
                    rules.add(new Rule(shortName, descriptionHtml));
                }
                return rules;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Rule> rules) {
            if(rules != null) {
                parseRulesAsyncTaskListener.parseSuccessful(rules);
            } else {
                parseRulesAsyncTaskListener.parseFailed();
            }
        }
    }
}
