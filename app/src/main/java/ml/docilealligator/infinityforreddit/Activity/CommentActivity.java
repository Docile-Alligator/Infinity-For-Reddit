package ml.docilealligator.infinityforreddit.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Spanned;
import android.text.style.SuperscriptSpan;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import org.commonmark.ext.gfm.tables.TableBlock;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.recycler.MarkwonAdapter;
import io.noties.markwon.recycler.table.TableEntry;
import io.noties.markwon.recycler.table.TableEntryPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import io.noties.markwon.urlprocessor.UrlProcessorRelativeToAbsolute;
import ml.docilealligator.infinityforreddit.AsyncTask.GetCurrentAccountAsyncTask;
import ml.docilealligator.infinityforreddit.CommentData;
import ml.docilealligator.infinityforreddit.Event.SwitchAccountEvent;
import ml.docilealligator.infinityforreddit.Infinity;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditDataRoomDatabase;
import ml.docilealligator.infinityforreddit.SendComment;
import ml.docilealligator.infinityforreddit.Utils.Utils;
import retrofit2.Retrofit;

public class CommentActivity extends BaseActivity {

    public static final String EXTRA_COMMENT_PARENT_TEXT_KEY = "ECPTK";
    public static final String EXTRA_COMMENT_PARENT_BODY_KEY = "ECPBK";
    public static final String EXTRA_PARENT_FULLNAME_KEY = "EPFK";
    public static final String EXTRA_PARENT_DEPTH_KEY = "EPDK";
    public static final String EXTRA_PARENT_POSITION_KEY = "EPPK";
    public static final String EXTRA_IS_REPLYING_KEY = "EIRK";
    public static final String RETURN_EXTRA_COMMENT_DATA_KEY = "RECDK";
    public static final int WRITE_COMMENT_REQUEST_CODE = 1;

    private static final String NULL_ACCESS_TOKEN_STATE = "NATS";
    private static final String ACCESS_TOKEN_STATE = "ATS";

    @BindView(R.id.coordinator_layout_comment_activity)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.appbar_layout_comment_activity)
    AppBarLayout appBarLayout;
    @BindView(R.id.toolbar_comment_activity)
    Toolbar toolbar;
    @BindView(R.id.comment_parent_markwon_view_comment_activity)
    TextView commentParentMarkwonView;
    @BindView(R.id.content_markdown_view_comment_activity)
    RecyclerView contentMarkdownRecyclerView;
    @BindView(R.id.comment_edit_text_comment_activity)
    EditText commentEditText;
    @Inject
    @Named("oauth")
    Retrofit mOauthRetrofit;
    @Inject
    RedditDataRoomDatabase mRedditDataRoomDatabase;
    @Inject
    @Named("default")
    SharedPreferences mSharedPreferences;
    private boolean mNullAccessToken = false;
    private String mAccessToken;
    private String parentFullname;
    private int parentDepth;
    private int parentPosition;
    private boolean isSubmitting = false;
    private boolean isReplying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((Infinity) getApplication()).getAppComponent().inject(this);

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_comment);

        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && isChangeStatusBarIconColor()) {
            addOnOffsetChangedListener(appBarLayout);
        }

        if (savedInstanceState == null) {
            getCurrentAccount();
        } else {
            mNullAccessToken = savedInstanceState.getBoolean(NULL_ACCESS_TOKEN_STATE);
            mAccessToken = savedInstanceState.getString(ACCESS_TOKEN_STATE);
            if (!mNullAccessToken && mAccessToken == null) {
                getCurrentAccount();
            }
        }

        Intent intent = getIntent();
        Markwon markwon = Markwon.builder(this)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(CommentActivity.this, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            startActivity(intent);
                        });
                    }
                })
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .usePlugin(SimpleExtPlugin.create(plugin ->
                                plugin.addExtension(1, '^', (configuration, props) -> {
                                    return new SuperscriptSpan();
                                })
                        )
                )
                .build();
        markwon.setMarkdown(commentParentMarkwonView, intent.getStringExtra(EXTRA_COMMENT_PARENT_TEXT_KEY));
        String parentBody = intent.getStringExtra(EXTRA_COMMENT_PARENT_BODY_KEY);
        if (parentBody != null && !parentBody.equals("")) {
            contentMarkdownRecyclerView.setVisibility(View.VISIBLE);
            contentMarkdownRecyclerView.setNestedScrollingEnabled(false);
            int markdownColor = Utils.getAttributeColor(this, R.attr.secondaryTextColor);
            Markwon postBodyMarkwon = Markwon.builder(this)
                    .usePlugin(new AbstractMarkwonPlugin() {
                        @Override
                        public void beforeSetText(@NonNull TextView textView, @NonNull Spanned markdown) {
                            textView.setTextColor(markdownColor);
                        }

                        @Override
                        public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                            builder.linkResolver((view, link) -> {
                                Intent intent = new Intent(CommentActivity.this, LinkResolverActivity.class);
                                Uri uri = Uri.parse(link);
                                if (uri.getScheme() == null && uri.getHost() == null) {
                                    intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                                } else {
                                    intent.setData(uri);
                                }
                                startActivity(intent);
                            }).urlProcessor(new UrlProcessorRelativeToAbsolute("https://www.reddit.com"));
                        }
                    })
                    .usePlugin(StrikethroughPlugin.create())
                    .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                    .usePlugin(SimpleExtPlugin.create(plugin ->
                                    plugin.addExtension(1, '^', (configuration, props) -> {
                                        return new SuperscriptSpan();
                                    })
                            )
                    )
                    .usePlugin(TableEntryPlugin.create(this))
                    .build();
            MarkwonAdapter markwonAdapter = MarkwonAdapter.builder(R.layout.adapter_default_entry, R.id.text)
                    .include(TableBlock.class, TableEntry.create(builder -> builder
                            .tableLayout(R.layout.adapter_table_block, R.id.table_layout)
                            .textLayoutIsRoot(R.layout.view_table_entry_cell)))
                    .build();
            contentMarkdownRecyclerView.setLayoutManager(new LinearLayoutManager(this));
            contentMarkdownRecyclerView.setAdapter(markwonAdapter);
            markwonAdapter.setMarkdown(postBodyMarkwon, parentBody);
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

        commentEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    @Override
    public SharedPreferences getSharedPreferences() {
        return mSharedPreferences;
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.action_send_comment_activity:
                if (!isSubmitting) {
                    isSubmitting = true;
                    if (commentEditText.getText() == null || commentEditText.getText().toString().equals("")) {
                        Snackbar.make(coordinatorLayout, R.string.comment_content_required, Snackbar.LENGTH_SHORT).show();
                        return true;
                    }

                    item.setEnabled(false);
                    item.getIcon().setAlpha(130);
                    Snackbar sendingSnackbar = Snackbar.make(coordinatorLayout, R.string.sending_comment, Snackbar.LENGTH_INDEFINITE);
                    sendingSnackbar.show();

                    SendComment.sendComment(commentEditText.getText().toString(), parentFullname, parentDepth,
                            getResources().getConfiguration().locale, mOauthRetrofit,
                            mAccessToken,
                            new SendComment.SendCommentListener() {
                                @Override
                                public void sendCommentSuccess(CommentData commentData) {
                                    isSubmitting = false;
                                    item.setEnabled(true);
                                    item.getIcon().setAlpha(255);
                                    Toast.makeText(CommentActivity.this, R.string.send_comment_success, Toast.LENGTH_SHORT).show();
                                    Intent returnIntent = new Intent();
                                    returnIntent.putExtra(RETURN_EXTRA_COMMENT_DATA_KEY, commentData);
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
                                    item.setEnabled(true);
                                    item.getIcon().setAlpha(255);

                                    if (errorMessage == null) {
                                        Snackbar.make(coordinatorLayout, R.string.send_comment_failed, Snackbar.LENGTH_SHORT).show();
                                    } else {
                                        Snackbar.make(coordinatorLayout, errorMessage, Snackbar.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
                return true;
        }

        return false;
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(NULL_ACCESS_TOKEN_STATE, mNullAccessToken);
        outState.putString(ACCESS_TOKEN_STATE, mAccessToken);
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
}
