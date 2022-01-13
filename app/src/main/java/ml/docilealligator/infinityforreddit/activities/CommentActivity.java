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
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.html.HtmlPlugin;
import io.noties.markwon.html.tag.SuperScriptHandler;
import io.noties.markwon.inlineparser.AutolinkInlineProcessor;
import io.noties.markwon.inlineparser.BangInlineProcessor;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UploadImageEnabledActivity;
import ml.docilealligator.infinityforreddit.UploadedImage;
import ml.docilealligator.infinityforreddit.adapters.MarkdownBottomBarRecyclerViewAdapter;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.CopyTextBottomSheetFragment;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.UploadedImagesBottomSheetFragment;
import ml.docilealligator.infinityforreddit.comment.Comment;
import ml.docilealligator.infinityforreddit.comment.SendComment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.events.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.markdown.SpoilerParserPlugin;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;
import retrofit2.Retrofit;

public class CommentActivity extends BaseActivity implements UploadImageEnabledActivity {

    public static final String EXTRA_COMMENT_PARENT_TEXT_KEY = "ECPTK";
    public static final String EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY = "ECPTMK";
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
    private static final String UPLOADED_IMAGES_STATE = "UIS";

    @BindView(R.id.coordinator_layout_comment_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_comment_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_comment_activity)
    Toolbar toolbar;
    @BindView(R.id.comment_parent_markwon_view_comment_activity)
    TextView commentParentMarkwonView;
    @BindView(R.id.divider_comment_activity)
    View divider;
    @BindView(R.id.content_markdown_view_comment_activity)
    RecyclerView contentMarkdownRecyclerView;
    @BindView(R.id.comment_edit_text_comment_activity)
    EditText commentEditText;
    @BindView(R.id.markdown_bottom_bar_recycler_view_comment_activity)
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
    private String mAccessToken;
    private String parentFullname;
    private int parentDepth;
    private int parentPosition;
    private boolean isSubmitting = false;
    private boolean isReplying;
    private int markdownColor;
    private Uri capturedImageUri;
    private ArrayList<UploadedImage> uploadedImages = new ArrayList<>();
    private Menu mMenu;
    private int commentColor;
    private int commentSpoilerBackgroundColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        setImmersiveModeNotApplicable();

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        applyCustomTheme();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        mAccessToken = mCurrentAccountSharedPreferences.getString(SharedPreferencesUtils.ACCESS_TOKEN, null);

        Intent intent = getIntent();
        String parentTextMarkdown = intent.getStringExtra(EXTRA_COMMENT_PARENT_TEXT_MARKDOWN_KEY);
        String parentText = intent.getStringExtra(EXTRA_COMMENT_PARENT_TEXT_KEY);
        CopyTextBottomSheetFragment copyTextBottomSheetFragment = new CopyTextBottomSheetFragment();

        int linkColor = mCustomThemeWrapper.getLinkColor();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                    plugin.excludeInlineProcessor(BangInlineProcessor.class);
                    plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                }))
                .usePlugin(HtmlPlugin.create(plugin -> {
                    plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @NonNull
                    @Override
                    public String processMarkdown(@NonNull String markdown) {
                        return Utils.fixSuperScript(markdown);
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
                })
                .usePlugin(SpoilerParserPlugin.create(commentColor, commentSpoilerBackgroundColor))
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
        if (parentTextMarkdown != null) {
            commentParentMarkwonView.setOnLongClickListener(view -> {
                Utils.hideKeyboard(CommentActivity.this);
                Bundle bundle = new Bundle();
                if (parentText == null) {
                    bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, parentTextMarkdown);
                } else {
                    bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, parentText);
                    bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, parentTextMarkdown);
                }
                copyTextBottomSheetFragment.setArguments(bundle);
                copyTextBottomSheetFragment.show(getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
                return true;
            });
            markwon.setMarkdown(commentParentMarkwonView, parentTextMarkdown);
        }
        String parentBodyMarkdown = intent.getStringExtra(EXTRA_COMMENT_PARENT_BODY_MARKDOWN_KEY);
        String parentBody = intent.getStringExtra(EXTRA_COMMENT_PARENT_BODY_KEY);
        if (parentBodyMarkdown != null && !parentBodyMarkdown.equals("")) {
            contentMarkdownRecyclerView.setVisibility(View.VISIBLE);
            contentMarkdownRecyclerView.setNestedScrollingEnabled(false);
            Markwon postBodyMarkwon = Markwon.builder(this)
                    .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                        plugin.excludeInlineProcessor(AutolinkInlineProcessor.class);
                        plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                        plugin.excludeInlineProcessor(BangInlineProcessor.class);
                        plugin.addInlineProcessor(new SuperscriptInlineProcessor());
                    }))
                    .usePlugin(HtmlPlugin.create(plugin -> {
                        plugin.excludeDefaults(true).addHandler(new SuperScriptHandler());
                    }))
                    .usePlugin(new AbstractMarkwonPlugin() {
                        @NonNull
                        @Override
                        public String processMarkdown(@NonNull String markdown) {
                            return Utils.fixSuperScript(markdown);
                        }

                        @Override
                        public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                            if (contentTypeface != null) {
                                textView.setTypeface(contentTypeface);
                            }
                            textView.setTextColor(markdownColor);
                            textView.setOnLongClickListener(view -> {
                                Utils.hideKeyboard(CommentActivity.this);
                                Bundle bundle = new Bundle();
                                bundle.putString(CopyTextBottomSheetFragment.EXTRA_RAW_TEXT, parentBody);
                                bundle.putString(CopyTextBottomSheetFragment.EXTRA_MARKDOWN, parentBodyMarkdown);
                                copyTextBottomSheetFragment.setArguments(bundle);
                                copyTextBottomSheetFragment.show(getSupportFragmentManager(), copyTextBottomSheetFragment.getTag());
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
                    })
                    .usePlugin(StrikethroughPlugin.create())
                    .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                    .usePlugin(TableEntryPlugin.create(this))
                    .build();
            MarkwonAdapter markwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                    .include(TableBlock.class, TableEntry.create(builder -> builder
                            .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                            .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                    .build();
            contentMarkdownRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this));
            contentMarkdownRecyclerView.setAdapter(markwonAdapter);
            markwonAdapter.setMarkdown(postBodyMarkwon, parentBodyMarkdown);
            markwonAdapter.notifyDataSetChanged();
        }
        parentFullname = intent.getStringExtra(EXTRA_PARENT_FULLNAME_KEY);
        parentDepth = intent.getExtras().getInt(EXTRA_PARENT_DEPTH_KEY);
        parentPosition = intent.getExtras().getInt(EXTRA_PARENT_POSITION_KEY);
        isReplying = intent.getExtras().getBoolean(EXTRA_IS_REPLYING_KEY);
        if (isReplying) {
            toolbar.setTitle(getString(R.string.comment_activity_label_is_replying));
        }

        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            uploadedImages = savedInstanceState.getParcelableArrayList(UPLOADED_IMAGES_STATE);
        }

        MarkdownBottomBarRecyclerViewAdapter adapter = new MarkdownBottomBarRecyclerViewAdapter(
                mCustomThemeWrapper, new MarkdownBottomBarRecyclerViewAdapter.ItemClickListener() {
            @Override
            public void onClick(int item) {
                MarkdownBottomBarRecyclerViewAdapter.bindEditTextWithItemClickListener(
                        CommentActivity.this, commentEditText, item);
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

        markdownBottomBarRecyclerView.setLayoutManager(new LinearLayoutManagerBugFixed(this,
                LinearLayoutManagerBugFixed.HORIZONTAL, false));
        markdownBottomBarRecyclerView.setAdapter(adapter);

        commentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
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
        coordinatorLayout.setBackgroundColor(mCustomThemeWrapper.getBackgroundColor());
        applyAppBarLayoutAndCollapsingToolbarLayoutAndToolbarTheme(appBarLayout, null, toolbar);
        commentColor = mCustomThemeWrapper.getCommentColor();
        commentSpoilerBackgroundColor = commentColor | 0xFF000000;
        commentParentMarkwonView.setTextColor(commentColor);
        divider.setBackgroundColor(mCustomThemeWrapper.getDividerColor());
        commentEditText.setTextColor(mCustomThemeWrapper.getCommentColor());
        int secondaryTextColor = mCustomThemeWrapper.getSecondaryTextColor();
        commentEditText.setHintTextColor(secondaryTextColor);
        markdownColor = secondaryTextColor;

        if (typeface != null) {
            commentParentMarkwonView.setTypeface(typeface);
            commentEditText.setTypeface(typeface);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(commentEditText.getWindowToken(), 0);
        }
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
            intent.putExtra(FullMarkdownActivity.EXTRA_COMMENT_MARKDOWN, commentEditText.getText().toString());
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
            if (commentEditText.getText() == null || commentEditText.getText().toString().equals("")) {
                isSubmitting = false;
                Snackbar.make(coordinatorLayout, R.string.comment_content_required, Snackbar.LENGTH_SHORT).show();
                return;
            }

            if (item != null) {
                item.setEnabled(false);
                item.getIcon().setAlpha(130);
            }
            Snackbar sendingSnackbar = Snackbar.make(coordinatorLayout, R.string.sending_comment, Snackbar.LENGTH_INDEFINITE);
            sendingSnackbar.show();

            SendComment.sendComment(mExecutor, new Handler(), commentEditText.getText().toString(),
                    parentFullname, parentDepth, mOauthRetrofit, mAccessToken,
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
                                Snackbar.make(coordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                            } else {
                                Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
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
                        mAccessToken, commentEditText, coordinatorLayout, data.getData(), uploadedImages);
            } else if (requestCode == CAPTURE_IMAGE_REQUEST_CODE) {
                Utils.uploadImageToReddit(this, mExecutor, mOauthRetrofit, mUploadMediaRetrofit,
                        mAccessToken, commentEditText, coordinatorLayout, capturedImageUri, uploadedImages);
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
            if (commentEditText.getText().toString().equals("")) {
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
        int start = Math.max(commentEditText.getSelectionStart(), 0);
        int end = Math.max(commentEditText.getSelectionEnd(), 0);
        commentEditText.getText().replace(Math.min(start, end), Math.max(start, end),
                "[" + uploadedImage.imageName + "](" + uploadedImage.imageUrl + ")",
                0, "[]()".length() + uploadedImage.imageName.length() + uploadedImage.imageUrl.length());
    }
}
