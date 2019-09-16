package ml.docilealligator.infinityforreddit;

import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO;
import static androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.libRG.CustomTextView;
import javax.inject.Inject;
import javax.inject.Named;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import pl.droidsonroids.gif.GifImageView;
import retrofit2.Retrofit;

public class PostTextActivity extends AppCompatActivity implements
    FlairBottomSheetFragment.FlairSelectionCallback {

  static final String EXTRA_SUBREDDIT_NAME = "ESN";
  static final String EXTRA_CONTENT = "EC";

  private static final String SUBREDDIT_NAME_STATE = "SNS";
  private static final String SUBREDDIT_ICON_STATE = "SIS";
  private static final String SUBREDDIT_SELECTED_STATE = "SSS";
  private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
  private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
  private static final String IS_POSTING_STATE = "IPS";
  private static final String FLAIR_STATE = "FS";
  private static final String IS_SPOILER_STATE = "ISS";
  private static final String IS_NSFW_STATE = "INS";
  private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
  private static final String ACCESS_TOKEN_STATE = "ATS";

  private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;

  @BindView(R.id.coordinator_layout_post_text_activity)
  CoordinatorLayout coordinatorLayout;
  @BindView(R.id.toolbar_post_text_activity)
  Toolbar toolbar;
  @BindView(R.id.subreddit_icon_gif_image_view_search_activity)
  GifImageView iconGifImageView;
  @BindView(R.id.subreddit_name_text_view_search_activity)
  TextView subredditNameTextView;
  @BindView(R.id.rules_button_post_text_activity)
  Button rulesButton;
  @BindView(R.id.flair_custom_text_view_post_text_activity)
  CustomTextView flairTextView;
  @BindView(R.id.spoiler_custom_text_view_post_text_activity)
  CustomTextView spoilerTextView;
  @BindView(R.id.nsfw_custom_text_view_post_text_activity)
  CustomTextView nsfwTextView;
  @BindView(R.id.post_title_edit_text_post_text_activity)
  EditText titleEditText;
  @BindView(R.id.post_text_content_edit_text_post_text_activity)
  EditText contentEditText;
  @Inject
  @Named("no_oauth")
  Retrofit mRetrofit;
  @Inject
  @Named("oauth")
  Retrofit mOauthRetrofit;
  @Inject
  RedditDataRoomDatabase mRedditDataRoomDatabase;
  @Inject
  SharedPreferences mSharedPreferences;
  private boolean mNullAccessToken = false;
  private String mAccessToken;
  private String iconUrl;
  private String subredditName;
  private boolean subredditSelected = false;
  private boolean subredditIsUser;
  private boolean loadSubredditIconSuccessful = true;
  private boolean isPosting;
  private Flair flair;
  private boolean isSpoiler = false;
  private boolean isNSFW = false;
  private Menu mMemu;
  private RequestManager mGlide;
  private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
  private Snackbar mPostingSnackbar;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_post_text);

    ButterKnife.bind(this);

    EventBus.getDefault().register(this);

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
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    mGlide = Glide.with(this);

    mPostingSnackbar = Snackbar
        .make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_INDEFINITE);

    if (savedInstanceState != null) {
      mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
      mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);

      if (!mNullAccessToken && mAccessToken == null) {
        getCurrentAccount();
      }

      subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
      iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
      subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
      subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
      loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
      isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
      flair = savedInstanceState.getParcelable(FLAIR_STATE);
      isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
      isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);

      if (subredditName != null) {
        subredditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
        subredditNameTextView.setText(subredditName);
        flairTextView.setVisibility(View.VISIBLE);
        if (!loadSubredditIconSuccessful) {
          loadSubredditIcon();
        }
      }
      displaySubredditIcon();

      if (isPosting) {
        mPostingSnackbar.show();
      }

      if (flair != null) {
        flairTextView.setText(flair.getText());
        flairTextView
            .setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
      }
      if (isSpoiler) {
        spoilerTextView
            .setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
      }
      if (isNSFW) {
        nsfwTextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
      }
    } else {
      getCurrentAccount();

      isPosting = false;

      if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
        loadSubredditIconSuccessful = false;
        subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
        subredditSelected = true;
        subredditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
        subredditNameTextView.setText(subredditName);
        flairTextView.setVisibility(View.VISIBLE);
        loadSubredditIcon();
      } else {
        mGlide.load(R.drawable.subreddit_default_icon)
            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
            .into(iconGifImageView);
      }

      String text = getIntent().getStringExtra(EXTRA_CONTENT);
      if (text != null) {
        contentEditText.setText(text);
      }
    }

    iconGifImageView.setOnClickListener(view -> {
      Intent intent = new Intent(this, SubredditSelectionActivity.class);
      startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
    });

    subredditNameTextView.setOnClickListener(view -> {
      Intent intent = new Intent(this, SubredditSelectionActivity.class);
      startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
    });

    rulesButton.setOnClickListener(view -> {
      if (subredditName == null) {
        Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
      } else {
        Intent intent = new Intent(this, RulesActivity.class);
        if (subredditIsUser) {
          intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
        } else {
          intent.putExtra(RulesActivity.EXTRA_SUBREDDIT_NAME, subredditName);
        }
        startActivity(intent);
      }
    });

    flairTextView.setOnClickListener(view -> {
      if (flair == null) {
        flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
        Bundle bundle = new Bundle();
        bundle.putString(FlairBottomSheetFragment.EXTRA_ACCESS_TOKEN, mAccessToken);
        if (subredditIsUser) {
          bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, "u_" + subredditName);
        } else {
          bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
        }
        flairSelectionBottomSheetFragment.setArguments(bundle);
        flairSelectionBottomSheetFragment
            .show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
      } else {
        flairTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        flairTextView.setText(getString(R.string.flair));
        flair = null;
      }
    });

    spoilerTextView.setOnClickListener(view -> {
      if (!isSpoiler) {
        spoilerTextView
            .setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
        isSpoiler = true;
      } else {
        spoilerTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        isSpoiler = false;
      }
    });

    nsfwTextView.setOnClickListener(view -> {
      if (!isNSFW) {
        nsfwTextView.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        isNSFW = true;
      } else {
        nsfwTextView.setBackgroundColor(getResources().getColor(android.R.color.transparent));
        isNSFW = false;
      }
    });
  }

  private void getCurrentAccount() {
    new GetCurrentAccountAsyncTask(mRedditDataRoomDatabase.accountDao(), account -> {
      if (account == null) {
        mNullAccessToken = true;
      } else {
        mAccessToken = account.getAccessToken();
      }
    }).execute();
  }

  private void displaySubredditIcon() {
    if (iconUrl != null && !iconUrl.equals("")) {
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
    new LoadSubredditIconAsyncTask(mRedditDataRoomDatabase.subredditDao(),
        subredditName, mRetrofit, iconImageUrl -> {
      iconUrl = iconImageUrl;
      displaySubredditIcon();
      loadSubredditIconSuccessful = true;
    }).execute();
  }

  private void promptAlertDialog(int titleResId, int messageResId) {
    new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
        .setTitle(titleResId)
        .setMessage(messageResId)
        .setPositiveButton(R.string.yes, (dialogInterface, i)
            -> finish())
        .setNegativeButton(R.string.no, null)
        .show();
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.post_text_activity, menu);
    mMemu = menu;
    if (isPosting) {
      mMemu.findItem(R.id.action_send_post_text_activity).setEnabled(false);
      mMemu.findItem(R.id.action_send_post_text_activity).getIcon().setAlpha(130);
    }
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        if (isPosting) {
          promptAlertDialog(R.string.exit_when_submit_post, R.string.exit_when_submit_post_detail);
          return true;
        } else {
          if (!titleEditText.getText().toString().equals("") || !contentEditText.getText()
              .toString().equals("")) {
            promptAlertDialog(R.string.discard_post, R.string.discard_post_detail);
            return true;
          }
        }
        finish();
        return true;
      case R.id.action_send_post_text_activity:
        if (!subredditSelected) {
          Snackbar.make(coordinatorLayout, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT)
              .show();
          return true;
        }

        if (titleEditText.getText() == null || titleEditText.getText().toString().equals("")) {
          Snackbar.make(coordinatorLayout, R.string.title_required, Snackbar.LENGTH_SHORT).show();
          return true;
        }

        isPosting = true;

        item.setEnabled(false);
        item.getIcon().setAlpha(130);

        mPostingSnackbar.show();

        String subredditName;
        if (subredditIsUser) {
          subredditName = "u_" + subredditNameTextView.getText().toString();
        } else {
          subredditName = subredditNameTextView.getText().toString();
        }

        Intent intent = new Intent(this, SubmitPostService.class);
        intent.putExtra(SubmitPostService.EXTRA_ACCESS_TOKEN, mAccessToken);
        intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
        intent.putExtra(SubmitPostService.EXTRA_TITLE, titleEditText.getText().toString());
        intent.putExtra(SubmitPostService.EXTRA_CONTENT, contentEditText.getText().toString());
        intent.putExtra(SubmitPostService.EXTRA_KIND, RedditUtils.KIND_SELF);
        intent.putExtra(SubmitPostService.EXTRA_FLAIR, flair);
        intent.putExtra(SubmitPostService.EXTRA_IS_SPOILER, isSpoiler);
        intent.putExtra(SubmitPostService.EXTRA_IS_NSFW, isNSFW);
        intent
            .putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TEXT_OR_LINK);
        startService(intent);

        return true;
    }

    return false;
  }

  @Override
  public void onBackPressed() {
    if (isPosting) {
      promptAlertDialog(R.string.exit_when_submit_post, R.string.exit_when_submit_post_detail);
    } else {
      if (!titleEditText.getText().toString().equals("") || !contentEditText.getText().toString()
          .equals("")) {
        promptAlertDialog(R.string.discard_post, R.string.discard_post_detail);
      } else {
        finish();
      }
    }
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
    outState.putParcelable(FLAIR_STATE, flair);
    outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
    outState.putBoolean(IS_NSFW_STATE, isNSFW);
    outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
    outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
      if (resultCode == RESULT_OK) {
        subredditName = data.getExtras()
            .getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
        iconUrl = data.getExtras()
            .getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
        subredditSelected = true;
        subredditIsUser = data.getExtras()
            .getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

        subredditNameTextView.setTextColor(getResources().getColor(R.color.primaryTextColor));
        subredditNameTextView.setText(subredditName);
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
  public void flairSelected(Flair flair) {
    this.flair = flair;
    flairTextView.setText(flair.getText());
    flairTextView.setBackgroundColor(getResources().getColor(R.color.backgroundColorPrimaryDark));
  }

  @Subscribe
  public void onAccountSwitchEvent(SwitchAccountEvent event) {
    finish();
  }

  @Subscribe
  public void onSubmitTextPostEvent(SubmitTextOrLinkPostEvent submitTextOrLinkPostEvent) {
    isPosting = false;
    mPostingSnackbar.dismiss();
    if (submitTextOrLinkPostEvent.postSuccess) {
      Intent intent = new Intent(PostTextActivity.this, ViewPostDetailActivity.class);
      intent.putExtra(ViewPostDetailActivity.EXTRA_POST_DATA, submitTextOrLinkPostEvent.post);
      startActivity(intent);
      finish();
    } else {
      mMemu.findItem(R.id.action_send_post_text_activity).setEnabled(true);
      mMemu.findItem(R.id.action_send_post_text_activity).getIcon().setAlpha(255);
      if (submitTextOrLinkPostEvent.errorMessage == null) {
        Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
      } else {
        Snackbar.make(coordinatorLayout,
            submitTextOrLinkPostEvent.errorMessage.substring(0, 1).toUpperCase()
                + submitTextOrLinkPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT)
            .show();
      }
    }
  }
}
