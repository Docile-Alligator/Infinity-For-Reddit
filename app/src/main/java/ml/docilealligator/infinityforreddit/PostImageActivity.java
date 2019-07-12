package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostImageActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";
    static final String EXTRA_SUBREDDIT_ICON = "ESI";

    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String IMAGE_URI_STATE = "IUS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;

    @BindView(R.id.coordinator_layout_post_image_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.subreddit_icon_gif_image_view_post_image_activity) GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_image_activity) TextView subreditNameTextView;
    @BindView(R.id.rules_button_post_image_activity) Button rulesButton;
    @BindView(R.id.post_title_edit_text_post_image_activity) EditText titleEditText;
    @BindView(R.id.select_image_constraint_layout_post_image_activity) ConstraintLayout constraintLayout;
    @BindView(R.id.capture_fab_post_image_activity) FloatingActionButton captureFab;
    @BindView(R.id.select_from_library_fab_post_image_activity) FloatingActionButton selectFromLibraryFab;
    @BindView(R.id.select_again_text_view_post_image_activity) TextView selectAgainTextView;
    @BindView(R.id.image_view_post_image_activity) ImageView imageView;

    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private Uri imageUri;

    private RequestManager mGlide;
    private Locale mLocale;

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;

    @Inject
    @Named("auth_info")
    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_image);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

        mGlide = Glide.with(this);
        mLocale = getResources().getConfiguration().locale;

        if(savedInstanceState != null) {
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            if(savedInstanceState.getString(IMAGE_URI_STATE) != null) {
                imageUri = Uri.parse(savedInstanceState.getString(IMAGE_URI_STATE));
                loadImage();
            }

            if(subredditName != null) {
                subreditNameTextView.setText(subredditName);
            }
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

        } else {
            if(getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                subredditName = getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME);
                iconUrl = getIntent().getExtras().getString(EXTRA_SUBREDDIT_ICON);
                subreditNameTextView.setText(subredditName);
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

        captureFab.setOnClickListener(view -> {

        });

        selectFromLibraryFab.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent,getResources().getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
        });

        selectAgainTextView.setOnClickListener(view -> {
            selectAgainTextView.setVisibility(View.GONE);
            mGlide.clear(imageView);
            constraintLayout.setVisibility(View.VISIBLE);
        });
    }

    private void loadImage() {
        constraintLayout.setVisibility(View.GONE);
        imageView.setVisibility(View.VISIBLE);
        selectAgainTextView.setVisibility(View.VISIBLE);
        mGlide.load(imageUri).into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_image_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_post_image_activity:
                if(!subredditSelected) {
                    Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                    return true;
                }

                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar postingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);
                postingSnackbar.show();

                String subredditName;
                if(subredditIsUser) {
                    subredditName = "u_" + subreditNameTextView.getText().toString();
                } else {
                    subredditName = subreditNameTextView.getText().toString();
                }

                /*SubmitPost.submitTextOrLinkPost(mOauthRetrofit, sharedPreferences, mLocale, subredditName,
                        titleEditText.getText().toString(), contentEditText.getText().toString(),
                        false, RedditUtils.KIND_LINK, new SubmitPost.SubmitPostListener() {
                            @Override
                            public void submitSuccessful(Post post) {
                                Intent intent = new Intent(PostLinkActivity.this, ViewPostDetailActivity.class);
                                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void submitFailed(@Nullable String errorMessage) {
                                postingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                                if(errorMessage == null) {
                                    Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                                } else {
                                    Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        });*/
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
        if(imageUri != null) {
            outState.putString(IMAGE_URI_STATE, imageUri.toString());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME_KEY);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL_KEY);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER_KEY);

                subreditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
                subreditNameTextView.setText(subredditName);
                if(!iconUrl.equals("")) {
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
        } else if(requestCode == PICK_IMAGE_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                if(data == null) {
                    Snackbar.make(coordinatorLayout, R.string.error_getting_image, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                imageUri = data.getData();
                loadImage();
            }
        }
    }
}
