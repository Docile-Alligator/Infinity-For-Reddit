package ml.docilealligator.infinityforreddit.Activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MultiReddit.CreateMultiReddit;
import ml.docilealligator.infinityforreddit.MultiReddit.MultiRedditJSONModel;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import retrofit2.Retrofit;

public class CreateMultiRedditActivity extends BaseActivity {

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";
    private static final String ACCOUNT_NAME_STATE = "ANS";
    @BindView(R.id.coordinator_layout_create_multi_reddit_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_create_multi_reddit_activity)
    Toolbar toolbar;
    @BindView(R.id.multi_reddit_name_edit_text_create_multi_reddit_activity)
    EditText nameEditText;
    @BindView(R.id.description_edit_text_create_multi_reddit_activity)
    EditText descriptionEditText;
    @BindView(R.id.visibility_wrapper_linear_layout_create_multi_reddit_activity)
    LinearLayout visibilityLinearLayout;
    @BindView(R.id.visibility_switch_create_multi_reddit_activity)
    Switch visibilitySwitch;
    @BindView(R.id.select_subreddit_text_view_create_multi_reddit_activity)
    TextView selectSubredditTextView;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    SharedPreferences mSharedPreferences;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String mAccountName;
    private ArrayList<String> subredditsName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_multi_reddit);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getCurrentAccountAndBindView();
    }

    private void getCurrentAccountAndBindView() {
        new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
            if (account == null) {
                mNullAccessToken = true;
            } else {
                mAccessToken = account.getAccessToken();
                mAccountName = account.getUsername();
            }
            bindView();
        }).execute();
    }

    private void bindView() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.create_multi_reddit_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_create_create_multi_reddit_activity:
                if (mAccountName == null || mAccessToken == null) {
                    Snackbar.make(coordinatorLayout, R.string.something_went_wrong, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                if (nameEditText.getText() == null || nameEditText.getText().toString().equals("")) {
                    Snackbar.make(coordinatorLayout, R.string.no_multi_reddit_name, Snackbar.LENGTH_SHORT).show();
                    return true;
                }
                subredditsName = new ArrayList<>();
                /*subredditsName.add("funny");
                subredditsName.add("Infinity_For_Reddit");*/
                String jsonModel = new MultiRedditJSONModel(nameEditText.getText().toString(), descriptionEditText.getText().toString(),
                        visibilitySwitch.isChecked(), subredditsName).createJSONModel();
                CreateMultiReddit.createMultiReddit(mOauthRetrofit, mRedditDataRoomDatabase, mAccessToken,
                        "/user/" + mAccountName + "/m/" + nameEditText.getText().toString(),
                        jsonModel, new CreateMultiReddit.CreateMultiRedditListener() {
                            @Override
                            public void success() {
                                finish();
                            }

                            @Override
                            public void failed(int errorCode) {
                                if (errorCode == 409) {
                                    Snackbar.make(coordinatorLayout, R.string.duplicate_multi_reddit, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(coordinatorLayout, R.string.create_multi_reddit_failed, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });
        }
        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
        outState.putString(ACCOUNT_NAME_STATE, mAccountName);
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
    }
}
