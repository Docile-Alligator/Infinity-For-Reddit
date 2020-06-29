package ml.docilealligator.infinityforreddit.Adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.style.SuperscriptSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.Markwon;
import io.noties.markwon.MarkwonConfiguration;
import io.noties.markwon.ext.strikethrough.StrikethroughPlugin;
import io.noties.markwon.linkify.LinkifyPlugin;
import io.noties.markwon.simple.ext.SimpleExtPlugin;
import ml.docilealligator.infinityforreddit.Activity.LinkResolverActivity;
import ml.docilealligator.infinityforreddit.Activity.ViewUserDetailActivity;
import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.Message;
import ml.docilealligator.infinityforreddit.R;

public class PrivateMessagesDetailRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Message mMessage;
    private Context mContext;
    private Markwon mMarkwon;
    private int mMessageBackgroundColor;
    private int mUsernameColor;
    private int mPrimaryTextColor;
    private int mSecondaryTextColor;
    private int mUnreadMessageBackgroundColor;

    public PrivateMessagesDetailRecyclerViewAdapter(Context context, Message message, CustomThemeWrapper customThemeWrapper) {
        mMessage = message;
        mContext = context;
        mMarkwon = Markwon.builder(mContext)
                .usePlugin(new AbstractMarkwonPlugin() {
                    @Override
                    public void configureConfiguration(@NonNull MarkwonConfiguration.Builder builder) {
                        builder.linkResolver((view, link) -> {
                            Intent intent = new Intent(mContext, LinkResolverActivity.class);
                            Uri uri = Uri.parse(link);
                            if (uri.getScheme() == null && uri.getHost() == null) {
                                intent.setData(LinkResolverActivity.getRedditUriByPath(link));
                            } else {
                                intent.setData(uri);
                            }
                            mContext.startActivity(intent);
                        });
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
                .build();
        mMessageBackgroundColor = customThemeWrapper.getCardViewBackgroundColor();
        mUsernameColor = customThemeWrapper.getUsername();
        mPrimaryTextColor = customThemeWrapper.getPrimaryTextColor();
        mSecondaryTextColor = customThemeWrapper.getSecondaryTextColor();
        mUnreadMessageBackgroundColor = customThemeWrapper.getUnreadMessageBackgroundColor();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new DataViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DataViewHolder) {
            Message message;
            if (holder.getAdapterPosition() == 0) {
                message = mMessage;
            } else {
                message = mMessage.getReplies().get(holder.getAdapterPosition() - 1);
            }

            if (message != null) {
                if (message.isNew()) {
                    ((DataViewHolder) holder).itemView.setBackgroundColor(
                            mUnreadMessageBackgroundColor);
                }

                ((DataViewHolder) holder).authorTextView.setText(message.getAuthor());
                String subject = message.getSubject().substring(0, 1).toUpperCase() + message.getSubject().substring(1);
                ((DataViewHolder) holder).subjectTextView.setText(subject);
                mMarkwon.setMarkdown(((DataViewHolder) holder).contentCustomMarkwonView, message.getBody());

                ((DataViewHolder) holder).authorTextView.setOnClickListener(view -> {
                    Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
                    intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, message.getAuthor());
                    mContext.startActivity(intent);
                });

                ((DataViewHolder) holder).contentCustomMarkwonView.setOnClickListener(view -> ((DataViewHolder) holder).itemView.performClick());
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

            titleTextView.setVisibility(View.GONE);

            itemView.setBackgroundColor(mMessageBackgroundColor);
            authorTextView.setTextColor(mUsernameColor);
            subjectTextView.setTextColor(mPrimaryTextColor);
            titleTextView.setTextColor(mPrimaryTextColor);
            contentCustomMarkwonView.setTextColor(mSecondaryTextColor);
        }
    }
}
