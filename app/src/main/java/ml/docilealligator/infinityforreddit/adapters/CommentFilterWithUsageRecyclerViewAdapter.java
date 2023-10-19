package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilter;
import ml.docilealligator.infinityforreddit.commentfilter.CommentFilterWithUsage;
import ml.docilealligator.infinityforreddit.customviews.LinearLayoutManagerBugFixed;
import ml.docilealligator.infinityforreddit.databinding.ItemCommentFilterWithUsageBinding;

public class CommentFilterWithUsageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private BaseActivity activity;
    private final OnItemClickListener onItemClickListener;
    private List<CommentFilterWithUsage> commentFilterWithUsageList;
    private RecyclerView.RecycledViewPool recycledViewPool;

    public interface OnItemClickListener {
        void onItemClick(CommentFilter commentFilter);
    }

    public CommentFilterWithUsageRecyclerViewAdapter(BaseActivity activity, OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.recycledViewPool = new RecyclerView.RecycledViewPool();
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CommentFilterViewHolder(ItemCommentFilterWithUsageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CommentFilterViewHolder) {
            ((CommentFilterViewHolder) holder).binding.commentFilterNameTextViewItemCommentFilter.setText(commentFilterWithUsageList.get(position).commentFilter.name);
            ((CommentFilterViewHolder) holder).adapter.setCommentFilterUsageList(commentFilterWithUsageList.get(position).commentFilterUsageList);
        }
    }

    @Override
    public int getItemCount() {
        return commentFilterWithUsageList == null ? 0 : commentFilterWithUsageList.size();
    }

    public void setCommentFilterWithUsageList(List<CommentFilterWithUsage> commentFilterWithUsageList) {
        this.commentFilterWithUsageList = commentFilterWithUsageList;
        notifyDataSetChanged();
    }

    private class CommentFilterViewHolder extends RecyclerView.ViewHolder {
        ItemCommentFilterWithUsageBinding binding;
        CommentFilterUsageEmbeddedRecyclerViewAdapter adapter;

        public CommentFilterViewHolder(@NonNull ItemCommentFilterWithUsageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            binding.commentFilterNameTextViewItemCommentFilter.setTextColor(activity.customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                binding.commentFilterNameTextViewItemCommentFilter.setTypeface(activity.typeface);
            }

            binding.getRoot().setOnClickListener(view -> {
                onItemClickListener.onItemClick(commentFilterWithUsageList.get(getBindingAdapterPosition()).commentFilter);
            });

            binding.commentFilterUsageRecyclerViewItemCommentFilter.setRecycledViewPool(recycledViewPool);
            binding.commentFilterUsageRecyclerViewItemCommentFilter.setLayoutManager(new LinearLayoutManagerBugFixed(activity));
            adapter = new CommentFilterUsageEmbeddedRecyclerViewAdapter(activity);
            binding.commentFilterUsageRecyclerViewItemCommentFilter.setAdapter(adapter);
        }
    }
}
