package ml.docilealligator.infinityforreddit.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.giphy.sdk.core.models.Media;
import com.giphy.sdk.ui.GPHContentType;
import com.giphy.sdk.ui.Giphy;
import com.giphy.sdk.ui.views.GiphyDialogFragment;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import kotlin.Unit;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.apis.RedditAPI;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.ParseComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ActivityEditCommentBinding;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.RichTextJSONConverter;
import ml.docilealligator.infinityforreddit.repositories.EditCommentActivityRepository;
import ml.docilealligator.infinityforreddit.thing.GiphyGif;
import ml.docilealligator.infinityforreddit.thing.MediaMetadata;
import ml.docilealligator.infinityforreddit.thing.UploadedImage;
import ml.docilealligator.infinityforreddit.utils.APIUtils;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import ml.docilealligator.infinityforreddit.viewmodels.EditCommentActivityViewModel;
import retrofit2.Response;
import retrofit2.Retrofit;

public class EditCommentActivity extends BaseActivity implements UploadImageEnabledActivity,
        GiphyDialogFragment.GifSelectionListener {

    public static final String EXTRA_CONTENT = "EC";
    public static final String EXTRA_FULLNAME = "EF";
    public static final String EXTRA_MEDIA_METADATA_LIST = "EMML";
    public static final String EXTRA_POSITION = "EP";
    public static final String RETURN_EXTRA_EDITED_COMMENT = "REEC";
    public static final String RETURN_EXTRA_EDITED_COMMENT_CONTENT = "REECC";
    public static final String RETURN_EXTRA_EDITED_COMMENT_POSITION = "REECP";

    private static final int PICK_IMAGE_REQUEST_CODE = 100;
    private static final int CAPTURE_IMAGE_REQUEST_CODE = 200;
    private static final int MARKDOWN_PREVIEW_REQUEST_CODE = 300;

    private static final String UPLOADED_IMAGES_STATE = "UIS";
    private static final String GIPHY_GIF_STATE = "GGS";

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
    private String mFullName;
    private String mAccessToken;
    private String mCommentContent;
    private boolean isSubmitting = false;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private GiphyGif giphyGif;
    private ActivityEditCommentBinding binding;
    public EditCommentActivityViewModel editCommentActivityViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicableBelowAndroid16();

        super.onCreate(savedInstanceState);

        binding = ActivityEditCommentBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (isImmersiveInterface()) {
            if (isChangeStatusBarIconColor()) {
                addOnOffsetChangedListener(binding.appbarLayoutEditCommentActivity);
            }

            ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), new OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsetsCompat onApplyWindowInsets(@NonNull View v, @NonNull WindowInsetsCompat insets) {
                    Insets allInsets = Utils.getInsets(insets, true);

                    setMargins(binding.toolbarEditCommentActivity,
                            allInsets.left,
                            allInsets.top,
                            allInsets.right,
                            BaseActivity.IGNORE_MARGIN);

                    binding.linearLayoutEditCommentActivity.setPadding(
                            allInsets.left,
                            0,
                            allInsets.right,
                            allInsets.bottom
                    );

                    return WindowInsetsCompat.CONSUMED;
                }
            });
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
            giphyGif = savedInstanceState.getParcelable(GIPHY_GIF_STATE);
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, true, true, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
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

            @Override
            public void onSelectGiphyGif() {
                GiphyDialogFragment.Companion.newInstance().show(getSupportFragmentManager(), "giphy_dialog");
            }
        });

        binding.markdownBottomBarRecyclerViewEditCommentActivity.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManager.HORIZONTAL, true).setStackFromEndAndReturnCurrentObject());
        binding.markdownBottomBarRecyclerViewEditCommentActivity.setAdapter(adapter);

        binding.commentEditTextEditCommentActivity.requestFocus();
        Utils.showKeyboard(this, new Handler(), binding.commentEditTextEditCommentActivity);

        Giphy.INSTANCE.configure(this, APIUtils.GIPHY_GIF_API_KEY);

        editCommentActivityViewModel = new ViewModelProvider(
                this,
                EditCommentActivityViewModel.Companion.provideFactory(new EditCommentActivityRepository(mRedditDataRoomDatabase.commentDraftDao()))
        ).get(EditCommentActivityViewModel.class);

        if (savedInstanceState == null) {
            editCommentActivityViewModel.getCommentDraft(mFullName).observe(this, commentDraft -> {
                if (commentDraft != null && !commentDraft.getContent().isEmpty()) {
                    binding.commentEditTextEditCommentActivity.setText(commentDraft.getContent());
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (isSubmitting) {
                    promptAlertDialog(R.string.exit_when_submit, R.string.exit_when_edit_comment_detail, false);
                } else {
                    String content = binding.commentEditTextEditCommentActivity.getText().toString();
                    if (content.isEmpty() || content.equals(mCommentContent)) {
                        editCommentActivityViewModel.deleteCommentDraft(mFullName, () -> {
                            setEnabled(false);
                            triggerBackPress();
                            return Unit.INSTANCE;
                        });
                    } else {
                        promptAlertDialog(R.string.save_comment_draft, R.string.save_comment_draft_detail, true);
                    }
                }
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
            triggerBackPress();
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
            if (!uploadedImages.isEmpty() || giphyGif != null) {
                try {
                    params.put(APIUtils.RICHTEXT_JSON_KEY, new RichTextJSONConverter().constructRichTextJSON(this, content, uploadedImages, giphyGif));
                    params.put(APIUtils.TEXT_KEY, "");
                } catch (JSONException e) {
                    isSubmitting = false;
                    Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.convert_to_richtext_json_failed, Snackbar.LENGTH_SHORT).show();
                    return;
                }
            } else {
                params.put(APIUtils.TEXT_KEY, content);
            }

            Handler handler = new Handler(getMainLooper());
            mExecutor.execute(() -> {
                try {
                    Response<String> response = mOauthRetrofit.create(RedditAPI.class)
                            .editPostOrComment(APIUtils.getOAuthHeader(mAccessToken), params).execute();
                    if (response.isSuccessful()) {
                        Comment comment = ParseComment.parseSingleComment(new JSONObject(response.body()), 0);
                        handler.post(() -> {
                            isSubmitting = false;
                            Toast.makeText(EditCommentActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();

                            Intent returnIntent = new Intent();
                            returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT, comment);
                            returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT_POSITION, getIntent().getExtras().getInt(EXTRA_POSITION));
                            setResult(RESULT_OK, returnIntent);

                            editCommentActivityViewModel.deleteCommentDraft(mFullName, () -> {
                                finish();
                                return Unit.INSTANCE;
                            });
                        });
                    } else {
                        handler.post(() -> {
                            isSubmitting = false;
                            Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        isSubmitting = false;
                        Snackbar.make(binding.coordinatorLayoutEditCommentActivity, R.string.post_failed, Snackbar.LENGTH_SHORT).show();
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    handler.post(() -> {
                        isSubmitting = false;
                        Toast.makeText(EditCommentActivity.this, R.string.edit_success, Toast.LENGTH_SHORT).show();

                        Intent returnIntent = new Intent();
                        returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT_CONTENT, Utils.modifyMarkdown(content));
                        returnIntent.putExtra(RETURN_EXTRA_EDITED_COMMENT_POSITION, getIntent().getExtras().getInt(EXTRA_POSITION));
                        setResult(RESULT_OK, returnIntent);

                        editCommentActivityViewModel.deleteCommentDraft(mFullName, () -> {
                            finish();
                            return Unit.INSTANCE;
                        });
                    });
                }
            });
        }
    }

    private void promptAlertDialog(int titleResId, int messageResId, boolean canSaveDraft) {
        new MaterialAlertDialogBuilder(this, R.style.MaterialAlertDialogTheme)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(R.string.yes, (dialogInterface, i)
                        -> {
                    if (canSaveDraft) {
                        editCommentActivityViewModel.saveCommentDraft(mFullName, binding.commentEditTextEditCommentActivity.getText().toString(), () -> {
                            finish();
                            return Unit.INSTANCE;
                        });
                    } else {
                        finish();
                    }
                })
                .setNegativeButton(R.string.no, (dialog, which) -> {
                    if (canSaveDraft) {
                        finish();
                    }
                })
                .setNeutralButton(R.string.cancel, null)
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
        outState.putParcelable(GIPHY_GIF_STATE, giphyGif);
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

    @Override
    public void didSearchTerm(@NonNull String s) {

    }

    @Override
    public void onGifSelected(@NonNull Media media, @Nullable String s, @NonNull GPHContentType gphContentType) {
        this.giphyGif = new GiphyGif(media.getId(), true);

        int start = Math.max(binding.commentEditTextEditCommentActivity.getSelectionStart(), 0);
        int end = Math.max(binding.commentEditTextEditCommentActivity.getSelectionEnd(), 0);
        int realStart = Math.min(start, end);
        if (realStart > 0 && binding.commentEditTextEditCommentActivity.getText().toString().charAt(realStart - 1) != '\n') {
            binding.commentEditTextEditCommentActivity.getText().replace(realStart, Math.max(start, end),
                    "\n![gif](" + giphyGif.id + ")\n",
                    0, "\n![gif]()\n".length() + giphyGif.id.length());
        } else {
            binding.commentEditTextEditCommentActivity.getText().replace(realStart, Math.max(start, end),
                    "![gif](" + giphyGif.id + ")\n",
                    0, "![gif]()\n".length() + giphyGif.id.length());
        }
    }

    @Override
    public void onDismissed(@NonNull GPHContentType gphContentType) {

    }
}
