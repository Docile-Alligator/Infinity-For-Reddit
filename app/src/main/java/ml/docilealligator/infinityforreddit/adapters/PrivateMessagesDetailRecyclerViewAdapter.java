package ml.docilealligator.infinityforreddit.adapters;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.text.Spanned;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.Locale;

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
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.activities.ViewPrivateMessagesActivity;
import ml.docilealligator.infinityforreddit.activities.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.markdown.SuperscriptInlineProcessor;
import ml.docilealligator.infinityforreddit.message.Message;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PrivateMessagesDetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MESSAGE_SENT = 0;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 1;
    private Message mMessage;
    private ViewPrivateMessagesActivity mViewPrivateMessagesActivity;
    private RequestManager mGlide;
    private Locale mLocale;
    private String mAccountName;
    private Markwon mMarkwon;
    private boolean mShowElapsedTime;
    private String mTimeFormatPattern;
    private int mSecondaryTextColor;
    private int mReceivedMessageTextColor;
    private int mSentMessageTextColor;
    private int mReceivedMessageBackgroundColor;
    private int mSentMessageBackgroundColor;

    public PrivateMessagesDetailRecyclerViewAdapter(ViewPrivateMessagesActivity viewPrivateMessagesActivity,
                                                    SharedPreferences sharedPreferences, Locale locale,
                                                    Message message, String accountName,
                                                    CustomThemeWrapper customThemeWrapper) {
        mMessage = message;
        mViewPrivateMessagesActivity = viewPrivateMessagesActivity;
        mGlide = Glide.with(viewPrivateMessagesActivity);
        mLocale = locale;
        mAccountName = accountName;
        mMarkwon = Markwon.builder(viewPrivateMessagesActivity)
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
                        if (mViewPrivateMessagesActivity.contentTypeface != null) {
                            textView.setTypeface(mViewPrivateMessagesActivity.contentTypeface);
                        }
                    }

                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(viewPrivateMessagesActivity, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            intent.setData(uri);
                            viewPrivateMessagesActivity.startActivity(intent);
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
        mShowElapsedTime = sharedPreferences.getBoolean(SharedPreferencesUtils.SHOW_ELAPSED_TIME_KEY, false);
        mTimeFormatPattern = sharedPreferences.getString(SharedPreferencesUtils.TIME_FORMAT_KEY, SharedPreferencesUtils.TIME_FORMAT_DEFAULT_VALUE);
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mReceivedMessageTextColor = customThemeWrapper.getReceivedMessageTextColor();
        mSentMessageTextColor = customThemeWrapper.getSentMessageTextColor();
        mReceivedMessageBackgroundColor = customThemeWrapper.getReceivedMessageBackgroundColor();
        mSentMessageBackgroundColor = customThemeWrapper.getSentMessageBackgroundColor();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return mMessage.getAuthor().equals(mAccountName) ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
        } else {
            return mMessage.getReplies().get(position - 1).getAuthor().equals(mAccountName) ? VIEW_TYPE_MESSAGE_SENT : VIEW_TYPE_MESSAGE_RECEIVED;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            return new SentMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_private_message_sent, parent, false));
        } else {
            return new ReceivedMessageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_private_message_received, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message;
        if (holder.getBindingAdapterPosition() == 0) {
            message = mMessage;
        } else {
            message = mMessage.getReplies().get(holder.getBindingAdapterPosition() - 1);
        }
        if (message != null) {
            if (holder instanceof MessageViewHolder) {
                mMarkwon.setMarkdown(((MessageViewHolder) holder).messageTextView, message.getBody());

                ((MessageViewHolder) holder).messageTextView.setOnClickListener(view -> ((MessageViewHolder) holder).itemView.performClick());
                if (mShowElapsedTime) {
                    ((MessageViewHolder) holder).timeTextView.setText(Utils.getElapsedTime(mViewPrivateMessagesActivity, message.getTimeUTC()));
                } else {
                    ((MessageViewHolder) holder).timeTextView.setText(Utils.getFormattedTime(mLocale, message.getTimeUTC(), mTimeFormatPattern));
                }

                ((MessageViewHolder) holder).messageTextView.setOnClickListener(view -> {
                    if (((MessageViewHolder) holder).timeTextView.getVisibility() != View.VISIBLE) {
                        ((MessageViewHolder) holder).timeTextView.setVisibility(View.VISIBLE);
                        mViewPrivateMessagesActivity.delayTransition();
                    } else {
                        ((MessageViewHolder) holder).timeTextView.setVisibility(View.GONE);
                        mViewPrivateMessagesActivity.delayTransition();
                    }
                });
            }

            if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).messageTextView.setBackground(Utils.getTintedDrawable(mViewPrivateMessagesActivity,
                        R.drawable.private_message_ballon, mSentMessageBackgroundColor));
            } else if (holder instanceof ReceivedMessageViewHolder) {
                mViewPrivateMessagesActivity.fetchUserAvatar(message.getAuthor(), userAvatarUrl -> {
                    if (userAvatarUrl == null || userAvatarUrl.equals("")) {
                        mGlide.load(R.drawable.subreddit_default_icon)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .into(((ReceivedMessageViewHolder) holder).userAvatarImageView);
                    } else {
                        mGlide.load(userAvatarUrl)
                                .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                                .error(mGlide.load(R.drawable.subreddit_default_icon)
                                        .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                                .into(((ReceivedMessageViewHolder) holder).userAvatarImageView);
                    }
                });

                ((ReceivedMessageViewHolder) holder).userAvatarImageView.setOnClickListener(view -> {
                    Intent intent = new Intent(mViewPrivateMessagesActivity, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, message.getAuthor());
                    mViewPrivateMessagesActivity.startActivity(intent);
                });

                ((ReceivedMessageViewHolder) holder).messageTextView.setBackground(
                        Utils.getTintedDrawable(mViewPrivateMessagesActivity,
                                R.drawable.private_message_ballon, mReceivedMessageBackgroundColor));
            }
        }
    }

    @Override
    public int getItemCount() {
        if (mMessage == null) {
            return 0;
        } else if (mMessage.getReplies() == null) {
            return 1;
        } else {
            return 1 + mMessage.getReplies().size();
        }
    }

    public void setMessage(Message message) {
        mMessage = message;
        notifyDataSetChanged();
    }

    public void addReply(Message reply) {
        int currentSize = getItemCount();

        if (mMessage != null) {
            mMessage.addReply(reply);
        } else {
            mMessage = reply;
        }

        notifyItemInserted(currentSize);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).messageTextView.setBackground(null);
            ((MessageViewHolder) holder).timeTextView.setVisibility(View.GONE);
        }
        if (holder instanceof ReceivedMessageViewHolder) {
            mGlide.clear(((ReceivedMessageViewHolder) holder).userAvatarImageView);
        }
    }

    class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView messageTextView;
        TextView timeTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        void setBaseView(TextView messageTextView, TextView timeTextView) {
            this.messageTextView = messageTextView;
            this.timeTextView = timeTextView;

            messageTextView.setTextColor(Color.WHITE);
            timeTextView.setTextColor(mSecondaryTextColor);
        }
    }

    class SentMessageViewHolder extends MessageViewHolder {
        @BindView(R.id.message_text_view_item_private_message_sent)
        TextView messageTextView;
        @BindView(R.id.time_text_view_item_private_message_sent)
        TextView timeTextView;

        SentMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(messageTextView, timeTextView);

            messageTextView.setTextColor(mSentMessageTextColor);

        }
    }

    class ReceivedMessageViewHolder extends MessageViewHolder {
        @BindView(R.id.avatar_image_view_item_private_message_received)
        ImageView userAvatarImageView;
        @BindView(R.id.message_text_view_item_private_message_received)
        TextView messageTextView;
        @BindView(R.id.time_text_view_item_private_message_received)
        TextView timeTextView;

        ReceivedMessageViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            setBaseView(messageTextView, timeTextView);

            messageTextView.setTextColor(mReceivedMessageTextColor);
        }
    }
}
