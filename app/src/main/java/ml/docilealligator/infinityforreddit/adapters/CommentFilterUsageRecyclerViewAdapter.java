package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsage;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;

public class CommentFilterUsageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<CommentFilterUsage> commentFilterUsages;
    private final BaseActivity activity;
    private final CustomThemeWrapper customThemeWrapper;
    private final CommentFilterUsageRecyclerViewAdapter.OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onClick(CommentFilterUsage commentFilterUsage);
    }

    public CommentFilterUsageRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                              CommentFilterUsageRecyclerViewAdapter.OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentFilterUsageRecyclerViewAdapter.CommentFilterUsageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_filter_usage, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        CommentFilterUsage commentFilterUsage = commentFilterUsages.get(position);
        if (commentFilterUsage.usage == CommentFilterUsage.SUBREDDIT_TYPE) {
            ((CommentFilterUsageViewHolder) holder).usageTextView.setText(activity.getString(R.string.post_filter_usage_subreddit, commentFilterUsage.nameOfUsage));
        }
    }

    @Override
    public int getItemCount() {
        return commentFilterUsages == null ? 0 : commentFilterUsages.size();
    }

    public void setCommentFilterUsages(List<CommentFilterUsage> commentFilterUsages) {
        this.commentFilterUsages = commentFilterUsages;
        notifyDataSetChanged();
    }

    private class CommentFilterUsageViewHolder extends RecyclerView.ViewHolder {
        TextView usageTextView;

        public CommentFilterUsageViewHolder(@NonNull View itemView) {
            super(itemView);
            usageTextView = (TextView) itemView;

            usageTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                usageTextView.setTypeface(activity.typeface);
            }

            usageTextView.setOnClickListener(view -> {
                onItemClickListener.onClick(commentFilterUsages.get(getBindingAdapterPosition()));
            });
        }
    }
}
