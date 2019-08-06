package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.libRG.CustomTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import SubredditDatabase.SubredditRoomDatabase;
import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostTextActivity extends AppCompatActivity implements FlairBottomSheetFragment.FlairSelectionCallback {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;

    @BindView(R.id.coordinator_layout_post_text_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.toolbar_post_text_activity) Toolbar toolbar;
    @BindView(R.id.subreddit_icon_gif_image_view_search_activity) GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_search_activity) TextView subreditNameTextView;
    @BindView(R.id.rules_button_post_text_activity) Button rulesButton;
    @BindView(R.id.flair_custom_text_view_post_text_activity) CustomTextView flairTextView;
    @BindView(R.id.spoiler_custom_text_view_post_text_activity) CustomTextView spoilerTextView;
    @BindView(R.id.nsfw_custom_text_view_post_text_activity) CustomTextView nsfwTextView;
    @BindView(R.id.post_title_edit_text_post_text_activity) EditText titleEditText;
    @BindView(R.id.post_text_content_edit_text_post_text_activity) EditText contentEditText;

    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;

    private String flair = null;
    private boolean isSpoiler = false;
    private boolean isNSFW = false;

    private Menu mMemu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_text);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        if(savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
            isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
            flair = savedInstanceState.getString(FLAIR_STATE);
            isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
            isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);

            if(subredditName != null) {
                subreditNameTextView.setText(subredditName);
                flairTextView.setVisibility(View.VISIBLE);
                if(!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if(isPosting) {
                mPostingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);
                mPostingSnackbar.show();
            }

            if(flair != null) {
                flairTextView.setText(flair);
                flairTextView.setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
            }
            if(isSpoiler) {
                spoilerTextView.setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
            }
            if(isNSFW) {
                nsfwTextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
            }
        } else {
            isPosting = false;

            if(getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);
                subreditNameTextView.setText(subredditName);
                flairTextView.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(iconGifImageView);
            }
        }

        iconGifImageView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        subreditNameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        rulesButton.setOnClickListener(view -> {
            if(subredditName == null) {
                Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(this, RulesActivity.class);
                if(subredditIsUser) {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                startActivity(intent);
            }
        });

        flairTextView.setOnClickListener(view -> {
            if(flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                if(subredditIsUser) {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
                } else {
                    bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                }
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                flairTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                flairTextView.setText(getString(R.string.flair));
                flair = null;
            }
        });

        spoilerTextView.setOnClickListener(view -> {
            if(!isSpoiler) {
                spoilerTextView.setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
                isSpoiler = true;
            } else {
                spoilerTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                isSpoiler = false;
            }
        });

        nsfwTextView.setOnClickListener(view -> {
            if(!isNSFW) {
                nsfwTextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                isNSFW = true;
            } else {
                nsfwTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                isNSFW = false;
            }
        });
    }

    private void displaySubredditIcon() {
        if(iconUrl != null && !iconUrl.equals("")) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(iconGifImageView);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(iconGifImageView);
        }
    }

    private void loadSubredditIcon() {
        new LoadSubredditIconAsyncTask(SubredditRoomDatabase.getDatabase(this).subredditDao(),
                subredditName, mRetrofit, iconImageUrl -> {
            iconUrl = iconImageUrl;
            displaySubredditIcon();
            loadSubredditIconSuccessful = true;
        }).execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_text_activity, menu);
        mMemu = menu;
        if(isPosting) {
            mMemu.findItem(R.id.action_send_post_image_activity).setEnabled(false);
            mMemu.findItem(R.id.action_send_post_image_activity).getIcon().setAlpha(130);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_post_text_activity:
                if(!subredditSelected) {
                    Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                mPostingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);
                mPostingSnackbar.show();

                String subredditName;
                if(subredditIsUser) {
                    subredditName = "u_" + subreditNameTextView.getText().toString();
                } else {
                    subredditName = subreditNameTextView.getText().toString();
                }

                Intent intent = new Intent(this, PostMediaService.class);
                intent.putExtra(PostMediaService.EXTRA_SUBREDDIT_NAME, subredditName);
                intent.putExtra(PostMediaService.EXTRA_TITLE, titleEditText.getText().toString());
                intent.putExtra(PostMediaService.EXTRA_CONTENT, contentEditText.getText().toString());
                intent.putExtra(PostMediaService.EXTRA_KIND, RedditUtils.KIND_SELF);
                intent.putExtra(PostMediaService.EXTRA_FLAIR, flair);
                intent.putExtra(PostMediaService.EXTRA_IS_SPOILER, isSpoiler);
                intent.putExtra(PostMediaService.EXTRA_IS_NSFW, isNSFW);
                intent.putExtra(PostMediaService.EXTRA_POST_TYPE, PostMediaService.EXTRA_POST_TYPE_IMAGE);

                return true;
        }

        return false;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(SUBREDDIT_ICON_STATE, iconUrl);
        outState.putBoolean(SUBREDDIT_SELECTED_STATE, subredditSelected);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putString(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                subreditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
                subreditNameTextView.setText(subredditName);
                displaySubredditIcon();

                flairTextView.setVisibility(View.VISIBLE);
                flairTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
                flairTextView.setText(getString(R.string.flair));
                flair = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Override
    public void flairSelected(String flair) {
        this.flair = flair;
        flairTextView.setText(flair);
        flairTextView.setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
        flairSelectionBottomSheetFragment.dismiss();
    }

    @Subscribe
    public void onSubmitTextPostEvent(SubmitTextOrLinkPostEvent submitTextOrLinkPostEvent) {
        isPosting = false;
        if(submitTextOrLinkPostEvent.postSuccess) {
            Intent intent = new Intent(PostTextActivity.this, ViewPostDetailActivity.class);
            intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, submitTextOrLinkPostEvent.post);
            startActivity(intent);
            finish();
        } else {
            mPostingSnackbar.dismiss();
            mMemu.findItem(R.id.action_send_post_text_activity).setEnabled(true);
            mMemu.findItem(R.id.action_send_post_text_activity).getIcon().setAlpha(255);
            if(submitTextOrLinkPostEvent.errorMessage == null) {
                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(coordinatorLayout, submitTextOrLinkPostEvent.errorMessage, Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
