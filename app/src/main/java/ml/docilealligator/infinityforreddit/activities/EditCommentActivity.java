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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.MediaMetadata;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditCommentBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.RichTextJSONConverter;
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
    public static final String EXTRA_MEDIA_METADATA_LIST = "EMML";
    public static final String EXTRA_POSITION = "EP";
    public static final String RETURN_EXTRA_EDITED_COMMENT_CONTENT = "REECC";
    public static final String RETURN_EXTRA_EDITED_COMMENT_POSITION = "REECP";

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
    private String mCommentContent;
    private boolean isSubmitting = false;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private ActivityEditCommentBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        binding = ActivityEditCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(binding.appbarLayoutEditCommentActivity);
        }
        setSupportActionBar(binding.toolbarEditCommentActivity);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mFullName = getIntent().getStringExtra(EXTRA_FULLNAME);
        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);
        mCommentContent = getIntent().getStringExtra(EXTRA_CONTENT);
        ArrayList<MediaMetadata> mediaMetadataList = getIntent().getParcelableArrayListExtra(EXTRA_MEDIA_METADATA_LIST);

        if (mediaMetadataList != null) {
            StringBuilder sb = new StringBuilder(mCommentContent);
            for (MediaMetadata m : mediaMetadataList) {
                int index = sb.indexOf(m.original.url);
                if (index >= 0) {
                    if (index > 0 && sb.charAt(index - 1) == '(') {
                        sb.replace(index, index + m.original.url.length(), m.id);
                    } else {
                        sb.insert(index + m.original.url.length(), ')')
                                .insert(index, "![](")
                                .replace(index + 4, index + 4 + m.original.url.length(), m.id);
                    }
                    uploadedImages.add(new UploadedImage(m.id, m.id));
                }
            }
            mCommentContent = sb.toString();
        }
        binding.commentEditTextEditCommentActivity.setText(mCommentContent);

        if (savedInstanceState != null) {
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, true, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        EditCommentActivity.this, binding.commentEditTextEditCommentActivity, item);
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

        binding.markdownBottomBarRecyclerViewEditCommentActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, false));
        binding.markdownBottomBarRecyclerViewEditCommentActivity.setAdapter(adapter);

        binding.commentEditTextEditCommentActivity.requestFocus();
        Utils.showKeyboard(this, new Handler(), binding.commentEditTextEditCommentActivity);
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
        binding.coordinatorLayoutEditCommentActivity.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(binding.appbarLayoutEditCommentActivity, null, binding.toolbarEditCommentActivity);
        binding.commentEditTextEditCommentActivity.setTextColor(mCustomThemeWrapper.getCommentColor());

        if (contentTypeface != null) {
            binding.commentEditTextEditCommentActivity.setTypeface(contentTypeface);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Utils.hideKeyboard(this);
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
            intent.putExtra(FullMarkdownActivity.EXTRA_MARKDOWN, binding.commentEditTextEditCommentActivity.getText().toString());
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

            Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.posting, Snackbar.LENGTH_SHORT).show();

            String content = binding.commentEditTextEditCommentActivity.getText().toString();

            Map<String, String> params = new HashMap<>();
            params.put(APIUtils.THING_ID_KEY, mFullName);
            if (!uploadedImages.isEmpty()) {
                try {
                    params.put(APIUtils.RICHTEXT_JSON_KEY, new RichTextJSONConverter().constructRichTextJSON(this, content, uploadedImages));
                    params.put(APIUtils.TEXT_KEY, "");
                } catch (JSONException e) {
                    isSubmitting = false;
                    Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.convert_to_richtext_json_failed, Snackbar.LENGTH_SHORT).show();
                    return;
                }
            } else {
                params.put(APIUtils.TEXT_KEY, content);
            }

            mOauthRetrofit.create(RedditAPI.class)
                    .editPostOrComment(APIUtils.getOAuthHeader(mAccessToken), params)
                    .enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                            isSubmitting = false;
                            if (response.isSuccessful()) {
                                Toast.makeText(EditCommentActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();

                                Intent returnIntent = new Intent();
                                returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT_CONTENT, Utils.modifyMarkdown(content));
                                returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT_POSITION, getIntent().getExtras().getInt(EXTRA_POSITION));
                                setResult(RESULT_OK, returnIntent);

                                finish();
                            } else {
                                Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                            isSubmitting = false;
                            Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
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
                        mAccessToken, binding.commentEditTextEditCommentActivity, binding.coordinatorLayoutEditCommentActivity, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, binding.commentEditTextEditCommentActivity, binding.coordinatorLayoutEditCommentActivity, capturedImageUri, uploadedImages);
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
            if (binding.commentEditTextEditCommentActivity.getText().toString().equals(mCommentContent)) {
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
        int start = Math.max(binding.commentEditTextEditCommentActivity.getSelectionStart(), 0);
        int end = Math.max(binding.commentEditTextEditCommentActivity.getSelectionEnd(), 0);
        int realStart = Math.min(start, end);
        if (realStart > 0 && binding.commentEditTextEditCommentActivity.getText().toString().charAt(realStart - 1) != '\n') {
            binding.commentEditTextEditCommentActivity.getText().replace(realStart, Math.max(start, end),
                    "\n![](" + uploadedImage.imageUrlOrKey + ")\n",
                    0, "\n![]()\n".length() + uploadedImage.imageUrlOrKey.length());
        } else {
            binding.commentEditTextEditCommentActivity.getText().replace(realStart, Math.max(start, end),
                    "![](" + uploadedImage.imageUrlOrKey + ")\n",
                    0, "![]()\n".length() + uploadedImage.imageUrlOrKey.length());
        }
    }
}
