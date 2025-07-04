package ml.docilealligator.infinityforreddit.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;

import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.RandomBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.databinding.ActivityFetchRandomSubredditOrPostBinding;
import ml.docilealligator.infinityforreddit.post.FetchPost;
import retrofit2.Retrofit;

public class FetchRandomSubredditOrPostActivity extends BaseActivity {

    public static final String EXTRA_RANDOM_OPTION = "ERO";

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    @Inject
    @Named("current_account")
    SharedPreferences mCurrentAccountSharedPreferences;
    @Inject
    CustomThemeWrapper mCustomThemeWrapper;
    @Inject
    Executor mExecutor;
    private boolean isCanceled = false;
    private ActivityFetchRandomSubredditOrPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplicationContext()).getAppComponent().inject(this);
        super.onCreate(savedInstanceState);

        binding = ActivityFetchRandomSubredditOrPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int option = getIntent().getIntExtra(EXTRA_RANDOM_OPTION, RandomBottomSheetFragment.RANDOM_SUBREDDIT);

        FetchPost.fetchRandomPost(mExecutor, new Handler(), mRetrofit,
                option == RandomBottomSheetFragment.RANDOM_NSFW_SUBREDDIT
                || option == RandomBottomSheetFragment.RANDOM_NSFW_POST, new FetchPost.FetchRandomPostListener() {
            @Override
            public void fetchRandomPostSuccess(String postId, String subredditName) {
                if (!isCanceled) {
                    switch (option) {
                        case RandomBottomSheetFragment.RANDOM_SUBREDDIT:
                        case RandomBottomSheetFragment.RANDOM_NSFW_SUBREDDIT: {
                            Intent intent = new Intent(FetchRandomSubredditOrPostActivity.this, ViewSubredditDetailActivity.class);
                            intent.putExtra(ViewSubredditDetailActivity.EXTRA_SUBREDDIT_NAME_KEY, subredditName);
                            startActivity(intent);
                        }
                        break;
                        case RandomBottomSheetFragment.RANDOM_POST:
                        case RandomBottomSheetFragment.RANDOM_NSFW_POST:
                            Intent intent = new Intent(FetchRandomSubredditOrPostActivity.this, ViewPostDetailActivity.class);
                            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_ID, postId);
                            startActivity(intent);
                            break;
                    }

                    finish();
                }
            }

            @Override
            public void fetchRandomPostFailed() {
                Toast.makeText(FetchRandomSubredditOrPostActivity.this, R.string.fetch_random_thing_failed, Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                isCanceled = true;
                setEnabled(false);
                triggerBackPress();
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
        binding.getRoot().setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
    }
}