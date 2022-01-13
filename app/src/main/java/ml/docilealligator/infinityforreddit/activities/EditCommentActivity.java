package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
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

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditCommentActivity extends BaseActivity implements UploadImageEnabledActivity {

    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_FULLNAME = "EF";
    public static final String EXTRA_POSITION = "EP";
    public static final String EXTRA_EDITED_COMMENT_CONTENT = "EECC";
    public static final String EXTRA_EDITED_COMMENT_POSITION = "EECP";

    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int MARKDOWN_PREVIEW_REQUEST_CODE = 300;

    private static final String UPLOADED_IMAGES_STATE = "UIS";

    @BindView(R.id.coordinator_layout_edit_comment_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_edit_comment_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_edit_comment_activity)
    Toolbar toolbar;
    @BindView(R.id.comment_edit_text_edit_comment_activity)
    EditText contentEditText;
    @BindView(R.id.markdown_bottom_bar_recycler_view_edit_comment_activity)
    RecyclerView markdownBottomBarRecyclerView;
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
    private String mCommentContent;
    private boolean isSubmitting = false;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_edit_comment);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullName = getIntent().getStringExtra(EXTRA_FULLNAME);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mCommentContent = getIntent().getStringExtra(EXTRA_CONTENT);
        contentEditText.setText(mCommentContent);

        if (savedInstanceState != null) {
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        EditCommentActivity.this, contentEditText, item);
            }

            @Override
            public void onUploadImage() {
                Utils.hideKeyboard(EditCommentActivity.this);
                UploadedImagesBottomSheetFragment fragment = new UploadedImagesBottomSheetFragment();
                Bundle arguments = new Bundle();
                arguments.putParcelableArrayList(UploadedImagesBottomSheetFragment.EXTRA_UPLOADED_IMAGES,
                        uploadedImages);
                fragment.setArguments(arguments);
                fragment.show(getSupportFragmentManager(), fragment.getTag());
            }
        });

        markdownBottomBarRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        markdownBottomBarRecyclerView.setAdapter(adapter);

        contentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        contentEditText.setTextColor(mCustomThemeWrapper.getCommentColor());

        if (contentTypeface != null) {
            contentEditText.setTypeface(contentTypeface);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(contentEditText.getWindowToken(), 0);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_comment_activity, menu);
        applyMenuItemTheme(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_preview_edit_comment_activity) {
            Intent intent = new Intent(this, FullMarkdownActivity.class);
            intent.putExtra(FullMarkdownActivity.EXTRA_COMMENT_MARKDOWN, contentEditText.getText().toString());
            intent.putExtra(FullMarkdownActivity.EXTRA_SUBMIT_POST, true);
            startActivityForResult(intent, MARKDOWN_PREVIEW_REQUEST_CODE);
        } else if (item.getItemId() == R.id.action_send_edit_comment_activity) {
            editComment();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return false;
    }

    private void editComment() {
        if (!isSubmitting) {
            isSubmitting = true;

            Snackbar.make(coordinatorLayout, R.string.posting, Snackbar.LENGTH_SHORT).show();

            String content = contentEditText.getText().toString();

            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.THING_ID_KEY, mFullName);
            params.put(APIUtils.TEXT_KEY, content);

            mOauthRetrofit.create(RedditAPI.class)
                    .editPostOrComment(APIUtils.getOAuthHeader(mAccessToken), params)
                    .enqueue(new Callback<String>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            isSubmitting = false;
                            if (response.isSuccessful()) {
                                Toast.makeText(EditCommentActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(EXTRA_EDITED_COMMENT_CONTENT, Utils.modifyMarkdown(content));
                                returnIntent.putExtra(EXTRA_EDITED_COMMENT_POSITION, getIntent().getExtras().getInt(EXTRA_POSITION));
                                setResult(RESULT_OK, returnIntent);

                                finish();
                            } else {
                                Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            isSubmitting = false;
                            Snackbar.make(coordinatorLayout, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                        }
                    });

        }
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_REQUEST_CODE) {
                if (data == null) {
                    Toast.makeText(EditCommentActivity.this, R.string.error_getting_image, Toast.LENGTH_LONG).show();
                    return;
                }
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, contentEditText, coordinatorLayout, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, contentEditText, coordinatorLayout, capturedImageUri, uploadedImages);
            } else if (requestCode == MARKDOWN_PREVIEW_REQUEST_CODE) {
                editComment();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelableArrayList(UPLOADED_IMAGES_STATE, uploadedImages);
    }

    @Override
    public void onBackPressed() {
        if (isSubmitting) {
            promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_edit_comment_detail);
        } else {
            if (contentEditText.getText().toString().equals(mCommentContent)) {
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
        int start = Math.max(contentEditText.getSelectionStart(), 0);
        int end = Math.max(contentEditText.getSelectionEnd(), 0);
        contentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                "[" + uploadedImage.imageName + "](" + uploadedImage.imageUrl + ")",
                0, "[]()".length() + uploadedImage.imageName.length() + uploadedImage.imageUrl.length());
    }
}
