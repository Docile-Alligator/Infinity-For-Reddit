package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.databinding.ItemPostFilterUsageEmbeddedBinding;
import ml.docilealligator.infinityforreddit.postfilter.PostFilterUsage;

public class PostFilterUsageEmbeddedRecyclerViewAdapter extends RecyclerView.Adapter<PostFilterUsageEmbeddedRecyclerViewAdapter.EntryViewHolder> {

    private BaseActivity baseActivity;
    private List<PostFilterUsage> postFilterUsageList;

    public PostFilterUsageEmbeddedRecyclerViewAdapter(BaseActivity baseActivity) {
        this.baseActivity = baseActivity;
    }

    @NonNull
    @Override
    public EntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new EntryViewHolder(ItemPostFilterUsageEmbeddedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull EntryViewHolder holder, int position) {
        if (postFilterUsageList == null || postFilterUsageList.isEmpty()) {
            holder.textView.setText(R.string.click_to_apply_post_filter);
        } else if (holder.getBindingAdapterPosition() > 4) {
            holder.textView.setText(baseActivity.getString(R.string.post_filter_usage_embedded_more_count, postFilterUsageList.size() - 5));
        } else {
            PostFilterUsage postFilterUsage = postFilterUsageList.get(holder.getBindingAdapterPosition());
            switch (postFilterUsage.usage) {
                case PostFilterUsage.HOME_TYPE:
                    holder.textView.setText(R.string.post_filter_usage_home);
                    break;
                case PostFilterUsage.SUBREDDIT_TYPE:
                    if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                        holder.textView.setText(R.string.post_filter_usage_embedded_subreddit_all);
                    } else {
                        holder.textView.setText("r/" + postFilterUsage.nameOfUsage);
                    }
                    break;
                case PostFilterUsage.USER_TYPE:
                    if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                        holder.textView.setText(R.string.post_filter_usage_embedded_user_all);
                    } else {
                        holder.textView.setText("u/" + postFilterUsage.nameOfUsage);
                    }
                    break;
                case PostFilterUsage.SEARCH_TYPE:
                    holder.textView.setText(R.string.post_filter_usage_search);
                    break;
                case PostFilterUsage.MULTIREDDIT_TYPE:
                    if (postFilterUsage.nameOfUsage.equals(PostFilterUsage.NO_USAGE)) {
                        holder.textView.setText(R.string.post_filter_usage_embedded_multireddit_all);
                    } else {
                        holder.textView.setText(postFilterUsage.nameOfUsage);
                    }
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return postFilterUsageList == null || postFilterUsageList.isEmpty() ? 1 : (postFilterUsageList.size() > 5 ? 6 : postFilterUsageList.size());
    }

    public void setPostFilterUsageList(List<PostFilterUsage> postFilterUsageList) {
        this.postFilterUsageList = postFilterUsageList;
        notifyDataSetChanged();
    }

    class EntryViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public EntryViewHolder(@NonNull ItemPostFilterUsageEmbeddedBinding binding) {
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
