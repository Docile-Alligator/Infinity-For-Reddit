package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterUsage;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFilterUsageEmbeddedBinding;

public class CommentFilterUsageEmbeddedRecyclerViewAdapter extends RecyclerView.Adapter<CommentFilterUsageEmbeddedRecyclerViewAdapter.EntryViewHolder> {
    private BaseActivity baseActivity;
    private List<CommentFilterUsage> commentFilterUsageList;

    public CommentFilterUsageEmbeddedRecyclerViewAdapter(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EntryViewHolder(ItemCommentFilterUsageEmbeddedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        if (commentFilterUsageList == null || commentFilterUsageList.isEmpty()) {
            holder.textView.setText(R.string.comment_filter_applied_to_all_subreddits);
        } else if (holder.getBindingAdapterPosition() > 4) {
            holder.textView.setText(baseActivity.getString(R.string.comment_filter_usage_embedded_more_count, commentFilterUsageList.size() - 5));
        } else {
            CommentFilterUsage commentFilterUsage = commentFilterUsageList.get(holder.getBindingAdapterPosition());
            switch (commentFilterUsage.usage) {
                case CommentFilterUsage.SUBREDDIT_TYPE:
                    holder.textView.setText("r/" + commentFilterUsage.nameOfUsage);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return commentFilterUsageList == null || commentFilterUsageList.isEmpty() ? 1 : (commentFilterUsageList.size() > 5 ? 6 : commentFilterUsageList.size());
    }

    public void setCommentFilterUsageList(List<CommentFilterUsage> commentFilterUsageList) {
        this.commentFilterUsageList = commentFilterUsageList;
        notifyDataSetChanged();
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public EntryViewHolder(@NonNull ItemCommentFilterUsageEmbeddedBinding binding) {
            super(binding.getRoot());
            textView = binding.getRoot();

            textView.setTextColor(baseActivity.customThemeWrapper.getSecondaryTextColor());

            if (baseActivity.typeface != null) {
                textView.setTypeface(baseActivity.typeface);
            }

            textView.setOnClickListener(view -> {
                Toast.makeText(baseActivity, textView.getText(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
