package ml.docilealligator.infinityforreddit.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.core.MarkwonTheme;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.inlineparser.HtmlInlineProcessor;
import io.noties.markwon.inlineparser.MarkwonInlineParserPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import ml.docilealligator.infinityforreddit.NetworkState;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPrivateMessagesActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.message.FetchMessage;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.message.ReadMessage;
import retrofit2.Retrofit;

public class MessageRecyclerViewAdapter extends PagedListAdapter<Message, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_DATA = 0;
    private static final int VIEW_TYPE_ERROR = 1;
    private static final int VIEW_TYPE_LOADING = 2;
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK = new DiffUtil.ItemCallback<Message>() {
        @Override
        public boolean areItemsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getId().equals(t1.getId());
        }

        @Override
        public boolean areContentsTheSame(@NonNull Message message, @NonNull Message t1) {
            return message.getBody().equals(t1.getBody());
        }
    };
    private Context mContext;
    private Retrofit mOauthRetrofit;
    private Markwon mMarkwon;
    private String mAccessToken;
    private int mMessageType;
    private NetworkState networkState;
    private RetryLoadingMoreCallback mRetryLoadingMoreCallback;
    private int mColorAccent;
    private int mMessageBackgroundColor;
    private int mUsernameColor;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mUnreadMessageBackgroundColor;
    private int mColorPrimaryLightTheme;
    private int mButtonTextColor;
    private boolean markAllMessagesAsRead = false;

    public MessageRecyclerViewAdapter(Context context, Retrofit oauthRetrofit,
                                      CustomThemeWrapper customThemeWrapper,
                                      String accessToken, String where,
                                      RetryLoadingMoreCallback retryLoadingMoreCallback) {
        super(DIFF_CALLBACK);
        mContext = context;
        mOauthRetrofit = oauthRetrofit;
        mRetryLoadingMoreCallback = retryLoadingMoreCallback;

        mColorAccent = customThemeWrapper.getColorAccent();
        mMessageBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
        mUsernameColor = customThemeWrapper.getUsername();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        int spoilerBackgroundColor = mSecondaryTextColor | 0xFF000000;
        mUnreadMessageBackgroundColor = customThemeWrapper.getUnreadMessageBackgroundColor();
        mColorPrimaryLightTheme = customThemeWrapper.getColorPrimaryLightTheme();
        mButtonTextColor = customThemeWrapper.getButtonTextColor();

        mMarkwon = Markwon.builder(mContext)
                .usePlugin(MarkwonInlineParserPlugin.create(plugin -> {
                    plugin.excludeInlineProcessor(HtmlInlineProcessor.class);
                }))
                .usePlugin(new AbstractMarkwonPlugin() {
                    @NonNull
                    @Override
                    public String processMarkdown(@NonNull String markdown) {
                        StringBuilder markdownStringBuilder = new StringBuilder(markdown);
                        Pattern spoilerPattern = Pattern.compile(">![\\S\\s]+?!<");
                        Matcher matcher = spoilerPattern.matcher(markdownStringBuilder);
                        ArrayList<Integer> matched = new ArrayList<>();
                        while (matcher.find()) {
                            matched.add(matcher.start());
                        }
                        for (int i = matched.size() - 1; i >= 0; i--) {
                            markdownStringBuilder.replace(matched.get(i), matched.get(i) + 1, "&gt;");
                        }
                        return super.processMarkdown(markdownStringBuilder.toString());
                    }

                    @Override
                    public void afterSetText(@NonNull TextView textView) {
                        textView.setHighlightColor(Color.TRANSPARENT);
                        SpannableStringBuilder markdownStringBuilder = new SpannableStringBuilder(textView.getText());
                        Pattern spoilerPattern = Pattern.compile(">![\\S\\s]+?!<");
                        Matcher matcher = spoilerPattern.matcher(markdownStringBuilder);
                        int start = 0;
                        boolean find = false;
                        while (matcher.find(start)) {
                            if (markdownStringBuilder.length() < 4
                                    || matcher.start() < 0
                                    || matcher.end() > markdownStringBuilder.length()) {
                                break;
                            }
                            find = true;
                            markdownStringBuilder.delete(matcher.end() - 2, matcher.end());
                            markdownStringBuilder.delete(matcher.start(), matcher.start() + 2);
                            int matcherStart = matcher.start();
                            int matcherEnd = matcher.end();
                            ClickableSpan clickableSpan = new ClickableSpan() {
                                private boolean isShowing = false;
                                @Override
                                public void updateDrawState(@NonNull TextPaint ds) {
                                    if (isShowing) {
                                        super.updateDrawState(ds);
                                        ds.setColor(mSecondaryTextColor);
                                    } else {
                                        ds.bgColor = spoilerBackgroundColor;
                                        ds.setColor(mSecondaryTextColor);
                                    }
                                    ds.setUnderlineText(false);
                                }

                                @Override
                                public void onClick(@NonNull View view) {
                                    isShowing = !isShowing;
                                    view.invalidate();
                                }
                            };
                            markdownStringBuilder.setSpan(clickableSpan, matcherStart, matcherEnd - 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            start = matcherEnd - 4;
                        }
                        if (find) {
                            textView.setText(markdownStringBuilder);
                        }
                    }

                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            intent.setData(uri);
                            mContext.startActivity(intent);
                        });
                    }

                    @Override
                    public void configureTheme(@NonNull MarkwonTheme.Builder builder) {
                        builder.linkColor(customThemeWrapper.getLinkColor());
                    }
                })
                .usePlugin(StrikethroughPlugin.create())
                .usePlugin(LinkifyPlugin.create(Linkify.WEB_URLS))
                .build();
        mAccessToken = accessToken;
        if (where.equals(FetchMessage.WHERE_MESSAGES)) {
            mMessageType = FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE;
        } else {
            mMessageType = FetchMessage.MESSAGE_TYPE_INBOX;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_DATA) {
            return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
        } else if (viewType == VIEW_TYPE_ERROR) {
            return new ErrorViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_error, parent, false));
        } else {
            return new LoadingViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_footer_loading, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            Message message = getItem(holder.getBindingAdapterPosition());
            if (message != null) {
                ArrayList<Message> replies = message.getReplies();
                Message displayedMessage;
                if (replies != null && !replies.isEmpty() && replies.get(replies.size() - 1) != null) {
                    displayedMessage = replies.get(replies.size() - 1);
                } else {
                    displayedMessage = message;
                }
                if (message.isNew()) {
                    if (markAllMessagesAsRead) {
                        message.setNew(false);
                    } else {
                        ((DataViewHolder) holder).itemView.setBackgroundColor(
                                mUnreadMessageBackgroundColor);
                    }
                }

                if (message.wasComment()) {
                    ((DataViewHolder) holder).titleTextView.setText(message.getTitle());
                } else {
                    ((DataViewHolder) holder).titleTextView.setVisibility(View.GONE);
                }

                ((DataViewHolder) holder).authorTextView.setText(displayedMessage.getAuthor());
                String subject = displayedMessage.getSubject().substring(0, 1).toUpperCase() + displayedMessage.getSubject().substring(1);
                ((DataViewHolder) holder).subjectTextView.setText(subject);
                mMarkwon.setMarkdown(((DataViewHolder) holder).contentCustomMarkwonView, displayedMessage.getBody());

                ((DataViewHolder) holder).itemView.setOnClickListener(view -> {
                    if (mMessageType == FetchMessage.MESSAGE_TYPE_INBOX
                            && message.getContext() != null && !message.getContext().equals("")) {
                        Uri uri = Uri.parse(message.getContext());
                        Intent intent = new Intent(mContext, LinkResolverActivity.class);
                        intent.setData(uri);
                        mContext.startActivity(intent);
                    } else if (mMessageType == FetchMessage.MESSAGE_TYPE_PRIVATE_MESSAGE) {
                        Intent intent = new Intent(mContext, ViewPrivateMessagesActivity.class);
                        intent.putExtra(ViewPrivateMessagesActivity.EXTRA_PRIVATE_MESSAGE, message);
                        intent.putExtra(ViewPrivateMessagesActivity.EXTRA_MESSAGE_POSITION, holder.getBindingAdapterPosition());
                        mContext.startActivity(intent);
                    }

                    if (displayedMessage.isNew()) {
                        ((DataViewHolder) holder).itemView.setBackgroundColor(mMessageBackgroundColor);
                        message.setNew(false);

                        ReadMessage.readMessage(mOauthRetrofit, mAccessToken, message.getFullname(),
                                new ReadMessage.ReadMessageListener() {
                                    @Override
                                    public void readSuccess() {
                                    }

                                    @Override
                                    public void readFailed() {
                                        message.setNew(true);
                                        ((DataViewHolder) holder).itemView.setBackgroundColor(mUnreadMessageBackgroundColor);
                                    }
                                });
                    }
                });

                ((DataViewHolder) holder).authorTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, message.getAuthor());
                    mContext.startActivity(intent);
                });

                ((DataViewHolder) holder).contentCustomMarkwonView.setOnClickListener(view -> {
                    if (((DataViewHolder) holder).contentCustomMarkwonView.getSelectionStart() == -1 && ((DataViewHolder) holder).contentCustomMarkwonView.getSelectionEnd() == -1) {
                        ((DataViewHolder) holder).itemView.performClick();
                    }
                });
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        // Reached at the end
        if (hasExtraRow() && position == getItemCount() - 1) {
            if (networkState.getStatus() == NetworkState.Status.LOADING) {
                return VIEW_TYPE_LOADING;
            } else {
                return VIEW_TYPE_ERROR;
            }
        } else {
            return VIEW_TYPE_DATA;
        }
    }

    @Override
    public int getItemCount() {
        if (hasExtraRow()) {
            return super.getItemCount() + 1;
        }
        return super.getItemCount();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof DataViewHolder) {
            ((DataViewHolder) holder).itemView.setBackgroundColor(mMessageBackgroundColor);
            ((DataViewHolder) holder).titleTextView.setVisibility(View.VISIBLE);
        }
    }

    private boolean hasExtraRow() {
        return networkState != null && networkState.getStatus() != NetworkState.Status.SUCCESS;
    }

    public void setNetworkState(NetworkState newNetworkState) {
        NetworkState previousState = this.networkState;
        boolean previousExtraRow = hasExtraRow();
        this.networkState = newNetworkState;
        boolean newExtraRow = hasExtraRow();
        if (previousExtraRow != newExtraRow) {
            if (previousExtraRow) {
                notifyItemRemoved(super.getItemCount());
            } else {
                notifyItemInserted(super.getItemCount());
            }
        } else if (newExtraRow && !previousState.equals(newNetworkState)) {
            notifyItemChanged(getItemCount() - 1);
        }
    }

    public void updateMessageReply(Message newReply, int position) {
        if (position >= 0 && position < super.getItemCount()) {
            Message message = getItem(position);
            if (message != null) {
                message.addReply(newReply);
                notifyItemChanged(position);
            }
        }
    }

    public void setMarkAllMessagesAsRead(boolean markAllMessagesAsRead) {
        this.markAllMessagesAsRead = markAllMessagesAsRead;
    }

    public interface RetryLoadingMoreCallback {
        void retryLoadingMore();
    }

    class DataViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.author_text_view_item_message)
        TextView authorTextView;
        @BindView(R.id.subject_text_view_item_message)
        TextView subjectTextView;
        @BindView(R.id.title_text_view_item_message)
        TextView titleTextView;
        @BindView(R.id.content_custom_markwon_view_item_message)
        TextView contentCustomMarkwonView;

        DataViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setBackgroundColor(mMessageBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            subjectTextView.setTextColor(mPrimaryTextColor);
            titleTextView.setTextColor(mPrimaryTextColor);
            contentCustomMarkwonView.setTextColor(mSecondaryTextColor);

            contentCustomMarkwonView.setMovementMethod(LinkMovementMethod.getInstance());
        }
    }

    class ErrorViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.error_text_view_item_footer_error)
        TextView errorTextView;
        @BindView(R.id.retry_button_item_footer_error)
        Button retryButton;

        ErrorViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            errorTextView.setText(R.string.load_comments_failed);
            errorTextView.setTextColor(mSecondaryTextColor);
            retryButton.setOnClickListener(view -> mRetryLoadingMoreCallback.retryLoadingMore());
            retryButton.setBackgroundTintList(ColorStateList.valueOf(mColorPrimaryLightTheme));
            retryButton.setTextColor(mButtonTextColor);
        }
    }

    class LoadingViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.progress_bar_item_footer_loading)
        ProgressBar progressBar;

        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            progressBar.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
        }
    }
}
