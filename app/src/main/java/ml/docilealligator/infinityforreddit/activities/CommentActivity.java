package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.MarkwonPlugin;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.recycler.MarkwonAdapter;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.AnyAccountAccessTokenAuthenticator;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.SendComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityCommentBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.MarkdownUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;

public class CommentActivity extends BaseActivity implements UploadImageEnabledActivity, AccountChooserBottomSheetFragment.AccountChooserListener {

    public static final String EXTRA_COMMENT_PARENT_TITLE_KEY = "ECPTK";
    public static final String EXTRA_COMMENT_PARENT_BODY_KEY = "ECPBK";
    public static final String EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY = "ECPBMK";
    public static final String EXTRA_PARENT_FULLNAME_KEY = "EPFK";
    public static final String EXTRA_PARENT_DEPTH_KEY = "EPDK";
    public static final String EXTRA_PARENT_POSITION_KEY = "EPPK";
    public static final String EXTRA_IS_REPLYING_KEY = "EIRK";
    public static final String RETURN_EXTRA_COMMENT_DATA_KEY = "RECDK";
    public static final int WRITE_COMMENT_REQUEST_CODE = 1;
    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int MARKDOWN_PREVIEW_REQUEST_CODE = 300;

    private static final String SELECTED_ACCOUNT_STATE = "SAS";
    private static final String UPLOADED_IMAGES_STATE = "UIS";

    @Inject
    @Named("no_oauth")
    Retrofit mRetrofit;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
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
    private RequestManager mGlide;
    private Account selectedAccount;
    private String mAccessToken;
    private String parentFullname;
    private int parentDepth;
    private int parentPosition;
    private boolean isSubmitting = false;
    private boolean isReplying;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private Menu mMenu;

    /**
     * Post or comment body text color
     */
    @ColorInt
    private int parentTextColor;
    @ColorInt
    private int parentSpoilerBackgroundColor;
    private ActivityCommentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        Intent intent = getIntent();
        isReplying = intent.getExtras().getBoolean(EXTRA_IS_REPLYING_KEY);
        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.commentAppbarLayout);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        if (mAccessToken == null) {
            finish();
            return;
        }

        String parentTitle = intent.getStringExtra(EXTRA_COMMENT_PARENT_TITLE_KEY);
        if (!TextUtils.isEmpty(parentTitle)) {
            binding.commentParentTitleTextView.setVisibility(View.VISIBLE);
            binding.commentParentTitleTextView.setText(parentTitle);
            binding.commentParentTitleTextView.setOnLongClickListener(view -> {
                Utils.hideKeyboard(CommentActivity.this);
                CopyTextBottomSheetFragment.show(getSupportFragmentManager(),
                        parentTitle, null);
                return true;
            });
        }

        String parentBodyMarkdown = intent.getStringExtra(EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY);
        String parentBody = intent.getStringExtra(EXTRA_COMMENT_PARENT_BODY_KEY);
        if (parentBodyMarkdown != null && !parentBodyMarkdown.equals("")) {
            binding.commentContentMarkdownView.setVisibility(View.VISIBLE);
            binding.commentContentMarkdownView.setNestedScrollingEnabled(false);
            int linkColor = mCustomThemeWrapper.getLinkColor();
            MarkwonPlugin miscPlugin = new AbstractMarkwonPlugin() {
                @Override
                public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                    if (contentTypeface != null) {
                        textView.setTypeface(contentTypeface);
                    }
                    textView.setTextColor(parentTextColor);
                    textView.setOnLongClickListener(view -> {
                        Utils.hideKeyboard(CommentActivity.this);
                        CopyTextBottomSheetFragment.show(getSupportFragmentManager(),
                                parentBody, parentBodyMarkdown);
                        return true;
                    });
                }

                @Override
                public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                    builder.linkResolver((view, link) -> {
                        Intent intent = new Intent(CommentActivity.this, LinkResolverActivity.class);
                        Uri uri = Uri.parse(link);
                        intent.setData(uri);
                        startActivity(intent);
                    });
                }

                @Override
                public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                    builder.linkColor(linkColor);
                }
            };
            Markwon postBodyMarkwon = MarkdownUtils.createFullRedditMarkwon(this,
                    miscPlugin, parentTextColor, parentSpoilerBackgroundColor, null);
            MarkwonAdapter markwonAdapter = MarkdownUtils.createTablesAdapter();
            binding.commentContentMarkdownView.setLayoutManager(new LinearLayoutManagerBugFixed(this));
            binding.commentContentMarkdownView.setAdapter(markwonAdapter);
            markwonAdapter.setMarkdown(postBodyMarkwon, parentBodyMarkdown);
            // noinspection NotifyDataSetChanged
            markwonAdapter.notifyDataSetChanged();
        }
        parentFullname = intent.getStringExtra(EXTRA_PARENT_FULLNAME_KEY);
        parentDepth = intent.getExtras().getInt(EXTRA_PARENT_DEPTH_KEY);
        parentPosition = intent.getExtras().getInt(EXTRA_PARENT_POSITION_KEY);
        if (isReplying) {
            binding.commentToolbar.setTitle(getString(R.string.comment_activity_label_is_replying));
        }

        setSupportActionBar(binding.commentToolbar);

        mGlide = Glide.with(this);

        if (savedInstanceState != null) {
            selectedAccount = savedInstanceState.getParcelable(SELECTED_ACCOUNT_STATE);
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);

            if (selectedAccount != null) {
                mGlide.load(selectedAccount.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(binding.commentAccountIconGifImageView);

                binding.commentAccountNameTextView.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }
        } else {
            loadCurrentAccount();
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        CommentActivity.this, binding.commentCommentEditText, item);
            }

            @Override
            public void onUploadImage() {
                Utils.hideKeyboard(CommentActivity.this);
                UploadedImagesBottomSheetFragment fragment = new UploadedImagesBottomSheetFragment();
                Bundle arguments = new Bundle();
                arguments.putParcelableArrayList(UploadedImagesBottomSheetFragment.EXTRA_UPLOADED_IMAGES,
                        uploadedImages);
                fragment.setArguments(arguments);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });

        binding.commentMarkdownBottomBarRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManagerBugFixed.HORIZONTAL, false));
        binding.commentMarkdownBottomBarRecyclerView.setAdapter(adapter);

        binding.commentAccountLinearLayout.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.commentCommentEditText.requestFocus();
        Utils.showKeyboard(this, new Handler(), binding.commentCommentEditText);
    }

    private void loadCurrentAccount() {
        Handler handler = new Handler();
        mExecutor.execute(() -> {
            Account account = mRedditDataRoomDatabase.accountDao().getCurrentAccount();
            selectedAccount = account;
            handler.post(() -> {
                if (!isFinishing() && !isDestroyed() && account != null) {
                    mGlide.load(account.getProfileImageUrl())
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                            .error(mGlide.load(R.drawable.subreddit_default_icon)
                                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                            .into(binding.commentAccountIconGifImageView);

                    binding.commentAccountNameTextView.setText(account.getAccountName());
                }
            });
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_ACCOUNT_STATE, selectedAccount);
        outState.putParcelableArrayList(UPLOADED_IMAGES_STATE, uploadedImages);
    }

    @Override
    public SharedPreferences getDefaultSharedPreferences() {
        return mSharedPreferences;
    }

    @Override
    protected CustomThemeWrapper getCustomThemeWrapper() {
        return mCustomThemeWrapper;
    }

    @Override
    protected void applyCustomTheme() {
        binding.commentCoordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.commentAppbarLayout, null, binding.commentToolbar);
        binding.commentParentTitleTextView.setTextColor(customThemeWrapper.getPostTitleColor());
        binding.commentDivider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        binding.commentCommentEditText.setTextColor(mCustomThemeWrapper.getCommentColor());
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.commentCommentEditText.setHintTextColor(secondaryTextColor);
        if (isReplying) {
            parentTextColor = mCustomThemeWrapper.getCommentColor();
        } else {
            parentTextColor = mCustomThemeWrapper.getPostContentColor();
        }
        parentSpoilerBackgroundColor = parentTextColor | 0xFF000000;
        binding.commentAccountNameTextView.setTextColor(mCustomThemeWrapper.getPrimaryTextColor());

        if (typeface != null) {
            binding.commentCommentEditText.setTypeface(typeface);
        }
        if (titleTypeface != null) {
            binding.commentParentTitleTextView.setTypeface(titleTypeface);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comment_activity, menu);
        mMenu = menu;
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_preview_comment_activity) {
            Intent intent = new Intent(this, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_COMMENT_MARKDOWN, binding.commentCommentEditText.getText().toString());
            intent.putExtra(FullMarkdownActivity.EXTRA_SUBMIT_POST, true);
            startActivityForResult(intent, MARKDOWN_PREVIEW_REQUEST_CODE);
        } else if (itemId == R.id.action_send_comment_activity) {
            sendComment(item);
            return true;
        }

        return false;
    }

    public void sendComment(@Nullable MenuItem item) {
        if (!isSubmitting) {
            isSubmitting = true;
            if (binding.commentCommentEditText.getText() == null || binding.commentCommentEditText.getText().toString().equals("")) {
                isSubmitting = false;
                Snackbar.make(binding.commentCoordinatorLayout, R.string.comment_content_required, Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (item != null) {
                item.setEnabled(false);
                item.getIcon().setAlpha(130);
            }
            Snackbar sendingSnackbar = Snackbar.make(binding.commentCoordinatorLayout, R.string.sending_comment, Snackbar.LENGTH_INDEFINITE);
            sendingSnackbar.show();

            Retrofit newAuthenticatorOauthRetrofit = mOauthRetrofit.newBuilder().client(new OkHttpClient.Builder().authenticator(new AnyAccountAccessTokenAuthenticator(mRetrofit, mRedditDataRoomDatabase, selectedAccount, mCurrentAccountSharedPreferences))
                    .connectTimeout(30, TimeUnit.SECONDS)
                    .readTimeout(30, TimeUnit.SECONDS)
                    .writeTimeout(30, TimeUnit.SECONDS)
                    .connectionPool(new ConnectionPool(0, 1, TimeUnit.NANOSECONDS))
                    .build())
                    .build();
            SendComment.sendComment(mExecutor, new Handler(), binding.commentCommentEditText.getText().toString(),
                    parentFullname, parentDepth, newAuthenticatorOauthRetrofit, selectedAccount,
                    new SendComment.SendCommentListener() {
                        @Override
                        public void sendCommentSuccess(Comment comment) {
                            isSubmitting = false;
                            if (item != null) {
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                            }
                            Toast.makeText(CommentActivity.this, R.string.send_comment_success, Toast.LENGTH_SHORT).show();
                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_EXTRA_COMMENT_DATA_KEY, comment);
                            returnIntent.putExtra(EXTRA_PARENT_FULLNAME_KEY, parentFullname);
                            if (isReplying) {
                                returnIntent.putExtra(EXTRA_PARENT_POSITION_KEY, parentPosition);
                            }
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }

                        @Override
                        public void sendCommentFailed(@Nullable String errorMessage) {
                            isSubmitting = false;
                            sendingSnackbar.dismiss();
                            if (item != null) {
                                item.setEnabled(true);
                                item.getIcon().setAlpha(255);
                            }

                            if (errorMessage == null || !errorMessage.equals("")) {
                                Snackbar.make(binding.commentCoordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(binding.commentCoordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(CommentActivity.this, R.string.error_getting_image, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, binding.commentCommentEditText, binding.commentCoordinatorLayout, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, binding.commentCommentEditText, binding.commentCoordinatorLayout, capturedImageUri, uploadedImages);
            } else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                sendComment(mMenu == null ? null : mMenu.findItem(R.id.action_send_comment_activity));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (isSubmitting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_edit_comment_detail);
        } else {
            if (binding.commentCommentEditText.getText().toString().equals("")) {
                finish();
            } else {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            }
        }
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

    @Override
    public void uploadImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,
                getResources().getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
    }

    @Override
    public void captureImage() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            capturedImageUri = FileProvider.getUriForFile(this, "ml.docilealligator.infinityforreddit.provider",
                    File.createTempFile("captured_image", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
            startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
        } catch (IOException ex) {
            Toast.makeText(this, R.string.error_creating_temp_file, Toast.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, R.string.no_camera_available, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void insertImageUrl(UploadedImage uploadedImage) {
        int start = Math.max(binding.commentCommentEditText.getSelectionStart(), 0);
        int end = Math.max(binding.commentCommentEditText.getSelectionEnd(), 0);
        binding.commentCommentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                "[" + uploadedImage.imageName + "](" + uploadedImage.imageUrl + ")",
                0, "[]()".length() + uploadedImage.imageName.length() + uploadedImage.imageUrl.length());
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.commentAccountIconGifImageView);

            binding.commentAccountNameTextView.setText(selectedAccount.getAccountName());
        }
    }
}
