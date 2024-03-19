package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.customviews.slidr.Slidr;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditPostBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditPostActivity extends BaseActivity implements UploadImageEnabledActivity {

    public static final String EXTRA_TITLE = "ET";
    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_FULLNAME = "EF";

    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int MARKDOWN_PREVIEW_REQUEST_CODE = 300;

    private static final String UPLOADED_IMAGES_STATE = "UIS";

    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    @Named("upload_media")
    Retrofit mUploadMediaRetrofit;
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
    private String mFullName;
    private String mAccessToken;
    private String mPostContent;
    private boolean isSubmitting = false;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private ActivityEditPostBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);
        binding = ActivityEditPostBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (mSharedPreferences.getBoolean(SharedPreferencesUtils.SWIPE_RIGHT_TO_GO_BACK, true)) {
            Slidr.attach(this);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutEditPostActivity);
        }

        setSupportActionBar(binding.toolbarEditPostActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullName = getIntent().getStringExtra(EXTRA_FULLNAME);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        binding.postTitleTextViewEditPostActivity.setText(getIntent().getStringExtra(EXTRA_TITLE));
        mPostContent = getIntent().getStringExtra(EXTRA_CONTENT);
        binding.postContentEditTextEditPostActivity.setText(mPostContent);

        if (savedInstanceState != null) {
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        EditPostActivity.this, binding.postContentEditTextEditPostActivity, item);
            }

            @Override
            public void onUploadImage() {
                Utils.hideKeyboard(EditPostActivity.this);
                UploadedImagesBottomSheetFragment fragment = new UploadedImagesBottomSheetFragment();
                Bundle arguments = new Bundle();
                arguments.putParcelableArrayList(UploadedImagesBottomSheetFragment.EXTRA_UPLOADED_IMAGES,
                        uploadedImages);
                fragment.setArguments(arguments);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });

        binding.markdownBottomBarRecyclerViewEditPostActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManagerBugFixed.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewEditPostActivity.setAdapter(adapter);

        binding.postContentEditTextEditPostActivity.requestFocus();
        Utils.showKeyboard(this, new Handler(), binding.postContentEditTextEditPostActivity);
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
        binding.coordinatorLayoutEditPostActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutEditPostActivity, null, binding.toolbarEditPostActivity);
        binding.postTitleTextViewEditPostActivity.setTextColor(mCustomThemeWrapper.getPostTitleColor());
        binding.dividerEditPostActivity.setBackgroundColor(mCustomThemeWrapper.getPostTitleColor());
        binding.postContentEditTextEditPostActivity.setTextColor(mCustomThemeWrapper.getPostContentColor());

        if (titleTypeface != null) {
            binding.postTitleTextViewEditPostActivity.setTypeface(titleTypeface);
        }
        if (contentTypeface != null) {
            binding.postContentEditTextEditPostActivity.setTypeface(contentTypeface);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_post_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_preview_edit_post_activity) {
            Intent intent = new Intent(this, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_MARKDOWN, binding.postContentEditTextEditPostActivity.getText().toString());
            intent.putExtra(FullMarkdownActivity.EXTRA_SUBMIT_POST, true);
            startActivityForResult(intent, MARKDOWN_PREVIEW_REQUEST_CODE);
        } else if (item.getItemId() == R.id.action_send_edit_post_activity) {
            editPost();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void editPost() {
        if (!isSubmitting) {
            isSubmitting = true;

            Snackbar.make(binding.coordinatorLayoutEditPostActivity, R.string.posting, Snackbar.LENGTH_SHORT).show();

            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.THING_ID_KEY, mFullName);
            params.put(APIUtils.TEXT_KEY, binding.postContentEditTextEditPostActivity.getText().toString());

            mOauthRetrofit.create(RedditAPI.class)
                    .editPostOrComment(APIUtils.getOAuthHeader(mAccessToken), params)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            isSubmitting = false;
                            Toast.makeText(EditPostActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();
                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);
                            finish();
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            isSubmitting = false;
                            Snackbar.make(binding.coordinatorLayoutEditPostActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                        }
                    });

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(EditPostActivity.this, R.string.error_getting_image, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, binding.postContentEditTextEditPostActivity, binding.coordinatorLayoutEditPostActivity, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, binding.postContentEditTextEditPostActivity, binding.coordinatorLayoutEditPostActivity, capturedImageUri, uploadedImages);
            } else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                editPost();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(UPLOADED_IMAGES_STATE, uploadedImages);
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
    public void onBackPressed() {
        if (isSubmitting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_edit_post_detail);
        } else {
            if (binding.postContentEditTextEditPostActivity.getText().toString().equals(mPostContent)) {
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
            capturedImageUri = FileProvider.getUriForFile(this, getPackageName() + ".provider",
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
        int start = Math.max(binding.postContentEditTextEditPostActivity.getSelectionStart(), 0);
        int end = Math.max(binding.postContentEditTextEditPostActivity.getSelectionEnd(), 0);
        binding.postContentEditTextEditPostActivity.getText().replace(Math.min(start, end), Math.max(start, end),
                "[" + uploadedImage.imageName + "](" + uploadedImage.imageUrlOrKey + ")",
                0, "[]()".length() + uploadedImage.imageName.length() + uploadedImage.imageUrlOrKey.length());
    }
}
