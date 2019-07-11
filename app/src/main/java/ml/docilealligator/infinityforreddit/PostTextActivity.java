package ml.docilealligator.infinityforreddit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostTextActivity extends AppCompatActivity {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;

    @BindView(R.id.coordinator_layout_post_detail_activity) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.subreddit_icon_gif_image_view_post_text_activity) GifImageView iconGifImageView;
    @BindView(R.id.subreddit_name_text_view_post_text_activity) TextView subreditNameTextView;
    @BindView(R.id.rules_button_post_text_activity) Button rulesButton;
    @BindView(R.id.post_title_edit_text_post_text_activity) EditText titleEditText;
    @BindView(R.id.post_text_content_edit_text_post_text_activity) EditText contentEditText;

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
        setContentView(R.layout.activity_post_text);

        ButterKnife.bind(this);

        ((Infinity) getApplication()).getmAppComponent().inject(this);

        ActionBar actionBar = getSupportActionBar();
        Drawable upArrow = getResources().getDrawable(R.drawable.ic_arrow_back_white_24dp);
        actionBar.setHomeAsUpIndicator(upArrow);

        mGlide = Glide.with(this);
        mLocale = getResources().getConfiguration().locale;

        if(getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
            subreditNameTextView.setText(getIntent().getExtras().getString(EXTRA_SUBREDDIT_NAME));
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(iconGifImageView);
        }

        subreditNameTextView.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_text_activity, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_send_post_text_activity:
                item.setEnabled(false);
                item.getIcon().setAlpha(130);
                Snackbar postingSnackbar = Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);
                postingSnackbar.show();

                SubmitPost.submitPostText(mOauthRetrofit, sharedPreferences, mLocale, subreditNameTextView.getText().toString(),
                        titleEditText.getText().toString(), contentEditText.getText().toString(),
                        false, new SubmitPost.SubmitPostListener() {
                            @Override
                            public void submitSuccessful(Post post) {
                                Intent intent = new Intent(PostTextActivity.this, ViewPostDetailActivity.class);
                                intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, post);
                                startActivity(intent);
                                finish();
                            }

                            @Override
                            public void submitFailed() {
                                postingSnackbar.dismiss();
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT);
                            }
                        });
                return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                subreditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
                subreditNameTextView.setText(data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME_KEY));

                String iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL_KEY);
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
        }
    }
}
