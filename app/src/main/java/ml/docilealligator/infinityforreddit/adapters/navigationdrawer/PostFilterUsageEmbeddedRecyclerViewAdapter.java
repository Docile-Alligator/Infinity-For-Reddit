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
            holder.textView.setText(postFilterUsageList.get(holder.getBindingAdapterPosition()).nameOfUsage);
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
            this.textView = binding.getRoot();
            textView.setOnClickListener(view -> {
                Toast.makeText(baseActivity, textView.getText(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
