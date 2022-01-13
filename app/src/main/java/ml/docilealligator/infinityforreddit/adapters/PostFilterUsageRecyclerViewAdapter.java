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
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;

public class PostFilterUsageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PostFilterUsage> postFilterUsages;
    private OnItemClickListener onItemClickListener;
    private BaseActivity activity;

    public interface OnItemClickListener {
        void onClick(PostFilterUsage postFilterUsage);
    }

    public PostFilterUsageRecyclerViewAdapter(BaseActivity activity,
                                              OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new PostFilterUsageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_filter_usage, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PostFilterUsage postFilterUsage = postFilterUsages.get(position);
        switch (postFilterUsage.usage) {
            case PostFilterUsage.HOME_TYPE:
                ((PostFilterUsageViewHolder) holder).usageTextView.setText(R.string.post_filter_usage_home);
                break;
            case PostFilterUsage.SUBREDDIT_TYPE:
                if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(R.string.post_filter_usage_subreddit_all);
                } else {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(activity.getString(R.string.post_filter_usage_subreddit, postFilterUsage.nameOfUsage));
                }
                break;
            case PostFilterUsage.USER_TYPE:
                if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(R.string.post_filter_usage_user_all);
                } else {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(activity.getString(R.string.post_filter_usage_user, postFilterUsage.nameOfUsage));
                }
                break;
            case PostFilterUsage.MULTIREDDIT_TYPE:
                if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(R.string.post_filter_usage_multireddit_all);
                } else {
                    ((PostFilterUsageViewHolder) holder).usageTextView.setText(activity.getString(R.string.post_filter_usage_multireddit, postFilterUsage.nameOfUsage));
                }
                break;
            case PostFilterUsage.SEARCH_TYPE:
                ((PostFilterUsageViewHolder) holder).usageTextView.setText(R.string.post_filter_usage_search);
        }
    }

    @Override
    public int getItemCount() {
        return postFilterUsages == null ? 0 : postFilterUsages.size();
    }

    public void setPostFilterUsages(List<PostFilterUsage> postFilterUsages) {
        this.postFilterUsages = postFilterUsages;
        notifyDataSetChanged();
    }

    private class PostFilterUsageViewHolder extends RecyclerView.ViewHolder {
        TextView usageTextView;

        public PostFilterUsageViewHolder(@NonNull View itemView) {
            super(itemView);
            usageTextView = (TextView) itemView;

            if (activity.typeface != null) {
                usageTextView.setTypeface(activity.typeface);
            }

            usageTextView.setOnClickListener(view -> {
                onItemClickListener.onClick(postFilterUsages.get(getBindingAdapterPosition()));
            });
        }
    }
}
