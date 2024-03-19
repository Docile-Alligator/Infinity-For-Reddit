package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.Flair;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.RedditGalleryPayload;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.adapters.RedditGallerySubmissionRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.asynctasks.LoadSubredditIcon;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.AccountChooserBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.FlairBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SelectOrCaptureImageBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityPostGalleryBinding;
import ml.docilealligator.infinityforreddit.events.SubmitGalleryPostEvent;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.services.SubmitPostService;
import ml.docilealligator.infinityforreddit.utils.JSONUtils;
import ml.docilealligator.infinityforreddit.utils.UploadImageUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class PostGalleryActivity extends BaseActivity implements FlairBottomSheetFragment.FlairSelectionCallback,
        AccountChooserBottomSheetFragment.AccountChooserListener {

    static final String EXTRA_SUBREDDIT_NAME = "ESN";

    private static final String SELECTED_ACCOUNT_STATE = "SAS";
    private static final String SUBREDDIT_NAME_STATE = "SNS";
    private static final String SUBREDDIT_ICON_STATE = "SIS";
    private static final String SUBREDDIT_SELECTED_STATE = "SSS";
    private static final String SUBREDDIT_IS_USER_STATE = "SIUS";
    private static final String LOAD_SUBREDDIT_ICON_STATE = "LSIS";
    private static final String IS_POSTING_STATE = "IPS";
    private static final String FLAIR_STATE = "FS";
    private static final String IS_SPOILER_STATE = "ISS";
    private static final String IS_NSFW_STATE = "INS";
    private static final String REDDIT_GALLERY_IMAGE_INFO_STATE = "RGIIS";

    private static final int SUBREDDIT_SELECTION_REQUEST_CODE = 0;
    private static final int PICK_IMAGE_REQUEST_CODE = 1;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 2;

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
    private Account selectedAccount;
    private ArrayList<RedditGallerySubmissionRecyclerViewAdapter.RedditGalleryImageInfo> redditGalleryImageInfoList;
    private String iconUrl;
    private String subredditName;
    private boolean subredditSelected = false;
    private boolean subredditIsUser;
    private boolean loadSubredditIconSuccessful = true;
    private boolean isPosting;
    private int primaryTextColor;
    private int flairBackgroundColor;
    private int flairTextColor;
    private int spoilerBackgroundColor;
    private int spoilerTextColor;
    private int nsfwBackgroundColor;
    private int nsfwTextColor;
    private Flair flair;
    private boolean isSpoiler = false;
    private boolean isNSFW = false;
    private Resources resources;
    private Menu mMemu;
    private RequestManager mGlide;
    private FlairBottomSheetFragment flairSelectionBottomSheetFragment;
    private Snackbar mPostingSnackbar;
    private RedditGallerySubmissionRecyclerViewAdapter adapter;
    private Uri imageUri;
    private boolean isUploading;
    private ActivityPostGalleryBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityPostGalleryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutPostGalleryActivity);
        }

        setSupportActionBar(binding.toolbarPostGalleryActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mGlide = Glide.with(this);

        mPostingSnackbar = Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.posting, Snackbar.LENGTH_INDEFINITE);

        resources = getResources();

        adapter = new RedditGallerySubmissionRecyclerViewAdapter(this, mCustomThemeWrapper, () -> {
            if (!isUploading) {
                SelectOrCaptureImageBottomSheetFragment fragment = new SelectOrCaptureImageBottomSheetFragment();
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            } else {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.please_wait_image_is_uploading, Snackbar.LENGTH_SHORT).show();
            }
        });
        binding.imagesRecyclerViewPostGalleryActivity.setAdapter(adapter);
        Resources resources = getResources();
        int nColumns = resources.getBoolean(R.bool.isTablet) || resources.getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        ((GridLayoutManager) binding.imagesRecyclerViewPostGalleryActivity.getLayoutManager()).setSpanCount(nColumns);
        binding.imagesRecyclerViewPostGalleryActivity.addItemDecoration(new RecyclerView.ItemDecoration() {
            @Override
            public void getItemOffsets(@NonNull Rect outRect, @NonNull View view, @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
                int offset = (int) (Utils.convertDpToPixel(16, PostGalleryActivity.this));
                int halfOffset = offset / 2;
                outRect.set(halfOffset, 0, halfOffset, offset);
            }
        });

        if (savedInstanceState != null) {
            selectedAccount = savedInstanceState.getParcelable(SELECTED_ACCOUNT_STATE);
            subredditName = savedInstanceState.getString(SUBREDDIT_NAME_STATE);
            iconUrl = savedInstanceState.getString(SUBREDDIT_ICON_STATE);
            subredditSelected = savedInstanceState.getBoolean(SUBREDDIT_SELECTED_STATE);
            subredditIsUser = savedInstanceState.getBoolean(SUBREDDIT_IS_USER_STATE);
            loadSubredditIconSuccessful = savedInstanceState.getBoolean(LOAD_SUBREDDIT_ICON_STATE);
            isPosting = savedInstanceState.getBoolean(IS_POSTING_STATE);
            flair = savedInstanceState.getParcelable(FLAIR_STATE);
            isSpoiler = savedInstanceState.getBoolean(IS_SPOILER_STATE);
            isNSFW = savedInstanceState.getBoolean(IS_NSFW_STATE);
            redditGalleryImageInfoList = savedInstanceState.getParcelableArrayList(REDDIT_GALLERY_IMAGE_INFO_STATE);

            if (selectedAccount != null) {
                mGlide.load(selectedAccount.getProfileImageUrl())
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .error(mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                        .into(binding.accountIconGifImageViewPostGalleryActivity);

                binding.accountNameTextViewPostGalleryActivity.setText(selectedAccount.getAccountName());
            } else {
                loadCurrentAccount();
            }

            if (redditGalleryImageInfoList != null && !redditGalleryImageInfoList.isEmpty()) {
                if (redditGalleryImageInfoList.get(redditGalleryImageInfoList.size() - 1).payload == null) {
                    imageUri = Uri.parse(redditGalleryImageInfoList.get(redditGalleryImageInfoList.size() - 1).imageUrlString);
                    uploadImage();
                }
            }
            adapter.setRedditGalleryImageInfoList(redditGalleryImageInfoList);

            if (subredditName != null) {
                binding.subredditNameTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostGalleryActivity.setText(subredditName);
                binding.flairCustomTextViewPostGalleryActivity.setVisibility(View.VISIBLE);
                if (!loadSubredditIconSuccessful) {
                    loadSubredditIcon();
                }
            }
            displaySubredditIcon();

            if (isPosting) {
                mPostingSnackbar.show();
            }

            if (flair != null) {
                binding.flairCustomTextViewPostGalleryActivity.setText(flair.getText());
                binding.flairCustomTextViewPostGalleryActivity.setBackgroundColor(flairBackgroundColor);
                binding.flairCustomTextViewPostGalleryActivity.setBorderColor(flairBackgroundColor);
                binding.flairCustomTextViewPostGalleryActivity.setTextColor(flairTextColor);
            }
            if (isSpoiler) {
                binding.spoilerCustomTextViewPostGalleryActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostGalleryActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostGalleryActivity.setTextColor(spoilerTextColor);
            }
            if (isNSFW) {
                binding.nsfwCustomTextViewPostGalleryActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostGalleryActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostGalleryActivity.setTextColor(nsfwTextColor);
            }
        } else {
            isPosting = false;

            loadCurrentAccount();

            if (getIntent().hasExtra(EXTRA_SUBREDDIT_NAME)) {
                loadSubredditIconSuccessful = false;
                subredditName = getIntent().getStringExtra(EXTRA_SUBREDDIT_NAME);
                subredditSelected = true;
                binding.subredditNameTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostGalleryActivity.setText(subredditName);
                binding.flairCustomTextViewPostGalleryActivity.setVisibility(View.VISIBLE);
                loadSubredditIcon();
            } else {
                mGlide.load(R.drawable.subreddit_default_icon)
                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                        .into(binding.subredditIconGifImageViewPostGalleryActivity);
            }
        }

        binding.accountLinearLayoutPostGalleryActivity.setOnClickListener(view -> {
            AccountChooserBottomSheetFragment fragment = new AccountChooserBottomSheetFragment();
            fragment.show(getSupportFragmentManager(), fragment.getTag());
        });

        binding.subredditRelativeLayoutPostGalleryActivity.setOnClickListener(view -> {
            Intent intent = new Intent(this, SubredditSelectionActivity.class);
            intent.putExtra(SubredditSelectionActivity.EXTRA_SPECIFIED_ACCOUNT, selectedAccount);
            startActivityForResult(intent, SUBREDDIT_SELECTION_REQUEST_CODE);
        });

        binding.rulesButtonPostGalleryActivity.setOnClickListener(view -> {
            if (subredditName == null) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
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

        binding.flairCustomTextViewPostGalleryActivity.setOnClickListener(view -> {
            if (flair == null) {
                flairSelectionBottomSheetFragment = new FlairBottomSheetFragment();
                Bundle bundle = new Bundle();
                bundle.putString(FlairBottomSheetFragment.EXTRA_SUBREDDIT_NAME, subredditName);
                flairSelectionBottomSheetFragment.setArguments(bundle);
                flairSelectionBottomSheetFragment.show(getSupportFragmentManager(), flairSelectionBottomSheetFragment.getTag());
            } else {
                binding.flairCustomTextViewPostGalleryActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostGalleryActivity.setText(getString(R.string.flair));
                flair = null;
            }
        });

        binding.spoilerCustomTextViewPostGalleryActivity.setOnClickListener(view -> {
            if (!isSpoiler) {
                binding.spoilerCustomTextViewPostGalleryActivity.setBackgroundColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostGalleryActivity.setBorderColor(spoilerBackgroundColor);
                binding.spoilerCustomTextViewPostGalleryActivity.setTextColor(spoilerTextColor);
                isSpoiler = true;
            } else {
                binding.spoilerCustomTextViewPostGalleryActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.spoilerCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                isSpoiler = false;
            }
        });

        binding.nsfwCustomTextViewPostGalleryActivity.setOnClickListener(view -> {
            if (!isNSFW) {
                binding.nsfwCustomTextViewPostGalleryActivity.setBackgroundColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostGalleryActivity.setBorderColor(nsfwBackgroundColor);
                binding.nsfwCustomTextViewPostGalleryActivity.setTextColor(nsfwTextColor);
                isNSFW = true;
            } else {
                binding.nsfwCustomTextViewPostGalleryActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.nsfwCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                isNSFW = false;
            }
        });

        binding.receivePostReplyNotificationsLinearLayoutPostGalleryActivity.setOnClickListener(view -> {
            binding.receivePostReplyNotificationsSwitchMaterialPostGalleryActivity.performClick();
        });

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper,
                new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
                    @Override
                    public void onClick(int item) {
                        MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                                PostGalleryActivity.this, binding.postContentEditTextPostGalleryActivity, item);
                    }

                    @Override
                    public void onUploadImage() {

                    }
                });

        binding.markdownBottomBarRecyclerViewPostGalleryActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewPostGalleryActivity.setAdapter(adapter);
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
                            .into(binding.accountIconGifImageViewPostGalleryActivity);

                    binding.accountNameTextViewPostGalleryActivity.setText(account.getAccountName());
                }
            });
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
        binding.coordinatorLayoutPostGalleryActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutPostGalleryActivity, null, binding.toolbarPostGalleryActivity);
        primaryTextColor = mCustomThemeWrapper.getPrimaryTextColor();
        binding.accountNameTextViewPostGalleryActivity.setTextColor(primaryTextColor);
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        binding.subredditNameTextViewPostGalleryActivity.setTextColor(secondaryTextColor);
        binding.rulesButtonPostGalleryActivity.setTextColor(mCustomThemeWrapper.getButtonTextColor());
        binding.rulesButtonPostGalleryActivity.setBackgroundColor(mCustomThemeWrapper.getColorPrimaryLightTheme());
        binding.receivePostReplyNotificationsTextViewPostGalleryActivity.setTextColor(primaryTextColor);
        int dividerColor = mCustomThemeWrapper.getDividerColor();
        binding.divider1PostGalleryActivity.setDividerColor(dividerColor);
        binding.divider2PostGalleryActivity.setDividerColor(dividerColor);
        flairBackgroundColor = mCustomThemeWrapper.getFlairBackgroundColor();
        flairTextColor = mCustomThemeWrapper.getFlairTextColor();
        spoilerBackgroundColor = mCustomThemeWrapper.getSpoilerBackgroundColor();
        spoilerTextColor = mCustomThemeWrapper.getSpoilerTextColor();
        nsfwBackgroundColor = mCustomThemeWrapper.getNsfwBackgroundColor();
        nsfwTextColor = mCustomThemeWrapper.getNsfwTextColor();
        binding.flairCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
        binding.spoilerCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
        binding.nsfwCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostGalleryActivity.setTextColor(primaryTextColor);
        binding.postTitleEditTextPostGalleryActivity.setHintTextColor(secondaryTextColor);
        binding.postContentEditTextPostGalleryActivity.setTextColor(primaryTextColor);
        binding.postContentEditTextPostGalleryActivity.setHintTextColor(secondaryTextColor);
        if (typeface != null) {
            binding.subredditNameTextViewPostGalleryActivity.setTypeface(typeface);
            binding.rulesButtonPostGalleryActivity.setTypeface(typeface);
            binding.receivePostReplyNotificationsTextViewPostGalleryActivity.setTypeface(typeface);
            binding.flairCustomTextViewPostGalleryActivity.setTypeface(typeface);
            binding.spoilerCustomTextViewPostGalleryActivity.setTypeface(typeface);
            binding.nsfwCustomTextViewPostGalleryActivity.setTypeface(typeface);
            binding.postTitleEditTextPostGalleryActivity.setTypeface(typeface);
        }
        if (contentTypeface != null) {
            binding.postContentEditTextPostGalleryActivity.setTypeface(contentTypeface);
        }
    }

    public void selectImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, resources.getString(R.string.select_from_gallery)), PICK_IMAGE_REQUEST_CODE);
    }

    public void captureImage() {
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider",
                    File.createTempFile("temp_img", ".jpg", getExternalFilesDir(Environment.DIRECTORY_PICTURES)));
            pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(pictureIntent, CAPTURE_IMAGE_REQUEST_CODE);
        } catch (IOException ex) {
            Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.error_creating_temp_file, Snackbar.LENGTH_SHORT).show();
        } catch (ActivityNotFoundException e) {
            Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.no_camera_available, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void uploadImage() {
        Handler handler = new Handler();
        isUploading = true;
        mExecutor.execute(() -> {
            try {
                Bitmap resource = Glide.with(PostGalleryActivity.this).asBitmap().load(imageUri).submit().get();
                String response = UploadImageUtils.uploadImage(mOauthRetrofit, mUploadMediaRetrofit, accessToken, resource, true, false);
                String mediaId = new JSONObject(response).getJSONObject(JSONUtils.ASSET_KEY).getString(JSONUtils.ASSET_ID_KEY);
                handler.post(() -> {
                    adapter.setImageAsUploaded(mediaId);
                    isUploading = false;
                });
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
                handler.post(() -> {
                    adapter.removeFailedToUploadImage();
                    Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.get_image_bitmap_failed, Snackbar.LENGTH_LONG).show();
                    isUploading = false;
                });
            } catch (XmlPullParserException | JSONException | IOException e) {
                e.printStackTrace();
                handler.post(() -> {
                    adapter.removeFailedToUploadImage();
                    Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.upload_image_failed, Snackbar.LENGTH_LONG).show();
                    isUploading = false;
                });
            }
        });
    }

    private void displaySubredditIcon() {
        if (iconUrl != null && !iconUrl.isEmpty()) {
            mGlide.load(iconUrl)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.subredditIconGifImageViewPostGalleryActivity);
        } else {
            mGlide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(binding.subredditIconGifImageViewPostGalleryActivity);
        }
    }

    private void loadSubredditIcon() {
        LoadSubredditIcon.loadSubredditIcon(mExecutor, new Handler(), mRedditDataRoomDatabase, subredditName,
                accessToken, accountName, mOauthRetrofit, mRetrofit, iconImageUrl -> {
            iconUrl = iconImageUrl;
            displaySubredditIcon();
            loadSubredditIconSuccessful = true;
        });
    }

    private void promptAlertDialog(int titleResId, int messageResId) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.discard_dialog_button, (dialogInterface, i)
                        -> finish())
                .setNegativeButton(R.string.no, null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_gallery_activity, menu);
        applyMenuItemTheme(menu);
        mMemu = menu;
        if (isPosting) {
            mMemu.findItem(R.id.action_send_post_gallery_activity).setEnabled(false);
            mMemu.findItem(R.id.action_send_post_gallery_activity).getIcon().setAlpha(130);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            if (isPosting) {
                promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
                return true;
            } else {
                redditGalleryImageInfoList = adapter.getRedditGalleryImageInfoList();
                if (!binding.postTitleEditTextPostGalleryActivity.getText().toString().isEmpty()
                        || !binding.postContentEditTextPostGalleryActivity.getText().toString().isEmpty()
                        || redditGalleryImageInfoList != null) {
                    promptAlertDialog(R.string.discard, R.string.discard_detail);
                    return true;
                }
            }
            finish();
            return true;
        } else if (itemId == R.id.action_send_post_gallery_activity) {
            if (!subredditSelected) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.select_a_subreddit, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (binding.postTitleEditTextPostGalleryActivity.getText() == null || binding.postTitleEditTextPostGalleryActivity.getText().toString().isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.title_required, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            redditGalleryImageInfoList = adapter.getRedditGalleryImageInfoList();
            if (redditGalleryImageInfoList == null || redditGalleryImageInfoList.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.select_an_image, Snackbar.LENGTH_SHORT).show();
                return true;
            }

            if (redditGalleryImageInfoList.get(redditGalleryImageInfoList.size() - 1).payload == null) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.please_wait_image_is_uploading, Snackbar.LENGTH_LONG).show();
                return true;
            }

            isPosting = true;

            item.setEnabled(false);
            item.getIcon().setAlpha(130);

            mPostingSnackbar.show();

            String subredditName;
            if (subredditIsUser) {
                subredditName = "u_" + binding.subredditNameTextViewPostGalleryActivity.getText().toString();
            } else {
                subredditName = binding.subredditNameTextViewPostGalleryActivity.getText().toString();
            }

            Intent intent = new Intent(this, SubmitPostService.class);
            intent.putExtra(SubmitPostService.EXTRA_ACCOUNT, selectedAccount);
            intent.putExtra(SubmitPostService.EXTRA_SUBREDDIT_NAME, subredditName);
            intent.putExtra(SubmitPostService.EXTRA_POST_TYPE, SubmitPostService.EXTRA_POST_TYPE_GALLERY);
            ArrayList<RedditGalleryPayload.Item> items = new ArrayList<>();
            for (RedditGallerySubmissionRecyclerViewAdapter.RedditGalleryImageInfo i : redditGalleryImageInfoList) {
                items.add(i.payload);
            }
            RedditGalleryPayload payload = new RedditGalleryPayload(subredditName, subredditIsUser ? "profile" : "subreddit",
                    binding.postTitleEditTextPostGalleryActivity.getText().toString(),
                    binding.postContentEditTextPostGalleryActivity.getText().toString(), isSpoiler, isNSFW,
                    binding.receivePostReplyNotificationsSwitchMaterialPostGalleryActivity.isChecked(), flair, items);
            intent.putExtra(SubmitPostService.EXTRA_REDDIT_GALLERY_PAYLOAD, new Gson().toJson(payload));

            ContextCompat.startForegroundService(this, intent);

            return true;
        }

        return false;
    }

    @Override
    public void onBackPressed() {
        if (isPosting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_submit_post_detail);
        } else {
            redditGalleryImageInfoList = adapter.getRedditGalleryImageInfoList();
            if (!binding.postTitleEditTextPostGalleryActivity.getText().toString().isEmpty()
                    || !binding.postContentEditTextPostGalleryActivity.getText().toString().isEmpty()
                    || redditGalleryImageInfoList != null) {
                promptAlertDialog(R.string.discard, R.string.discard_detail);
            } else {
                finish();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(SELECTED_ACCOUNT_STATE, selectedAccount);
        outState.putString(SUBREDDIT_NAME_STATE, subredditName);
        outState.putString(SUBREDDIT_ICON_STATE, iconUrl);
        outState.putBoolean(SUBREDDIT_SELECTED_STATE, subredditSelected);
        outState.putBoolean(SUBREDDIT_IS_USER_STATE, subredditIsUser);
        outState.putBoolean(LOAD_SUBREDDIT_ICON_STATE, loadSubredditIconSuccessful);
        outState.putBoolean(IS_POSTING_STATE, isPosting);
        outState.putParcelable(FLAIR_STATE, flair);
        outState.putBoolean(IS_SPOILER_STATE, isSpoiler);
        outState.putBoolean(IS_NSFW_STATE, isNSFW);
        redditGalleryImageInfoList = adapter.getRedditGalleryImageInfoList();
        outState.putParcelableArrayList(REDDIT_GALLERY_IMAGE_INFO_STATE, redditGalleryImageInfoList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SUBREDDIT_SELECTION_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                subredditName = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_NAME);
                iconUrl = data.getExtras().getString(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_ICON_URL);
                subredditSelected = true;
                subredditIsUser = data.getExtras().getBoolean(SubredditSelectionActivity.EXTRA_RETURN_SUBREDDIT_IS_USER);

                binding.subredditNameTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                binding.subredditNameTextViewPostGalleryActivity.setText(subredditName);
                displaySubredditIcon();

                binding.flairCustomTextViewPostGalleryActivity.setVisibility(View.VISIBLE);
                binding.flairCustomTextViewPostGalleryActivity.setBackgroundColor(resources.getColor(android.R.color.transparent));
                binding.flairCustomTextViewPostGalleryActivity.setTextColor(primaryTextColor);
                binding.flairCustomTextViewPostGalleryActivity.setText(getString(R.string.flair));
                flair = null;
            }
        } else if (requestCode == PICK_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                if (data == null) {
                    Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.error_getting_image, Snackbar.LENGTH_SHORT).show();
                    return;
                }

                imageUri = data.getData();
                adapter.addImage(imageUri.toString());
                uploadImage();
            }
        } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                adapter.addImage(imageUri.toString());
                uploadImage();
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
        binding.flairCustomTextViewPostGalleryActivity.setText(flair.getText());
        binding.flairCustomTextViewPostGalleryActivity.setBackgroundColor(flairBackgroundColor);
        binding.flairCustomTextViewPostGalleryActivity.setBorderColor(flairBackgroundColor);
        binding.flairCustomTextViewPostGalleryActivity.setTextColor(flairTextColor);
    }

    @Override
    public void onAccountSelected(Account account) {
        if (account != null) {
            selectedAccount = account;

            mGlide.load(selectedAccount.getProfileImageUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(mGlide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(binding.accountIconGifImageViewPostGalleryActivity);

            binding.accountNameTextViewPostGalleryActivity.setText(selectedAccount.getAccountName());
        }
    }

    public void setCaptionAndUrl(int position, String caption, String url) {
        if (adapter != null) {
            adapter.setCaptionAndUrl(position, caption, url);
        }
    }

    @Subscribe
    public void onAccountSwitchEvent(SwitchAccountEvent event) {
        finish();
    }

    @Subscribe
    public void onSubmitGalleryPostEvent(SubmitGalleryPostEvent submitGalleryPostEvent) {
        isPosting = false;
        mPostingSnackbar.dismiss();
        if (submitGalleryPostEvent.postSuccess) {
            Intent intent = new Intent(this, LinkResolverActivity.class);
            intent.setData(Uri.parse(submitGalleryPostEvent.postUrl));
            startActivity(intent);
            finish();
        } else {
            mMemu.findItem(R.id.action_send_post_gallery_activity).setEnabled(true);
            mMemu.findItem(R.id.action_send_post_gallery_activity).getIcon().setAlpha(255);
            if (submitGalleryPostEvent.errorMessage == null || submitGalleryPostEvent.errorMessage.isEmpty()) {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
            } else {
                Snackbar.make(binding.coordinatorLayoutPostGalleryActivity, submitGalleryPostEvent.errorMessage.substring(0, 1).toUpperCase()
                        + submitGalleryPostEvent.errorMessage.substring(1), Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}