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
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.postfilter.PostFilter;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class PostFilterRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_POST_FILTER = 2;

    private BaseActivity activity;
    private CustomThemeWrapper customThemeWrapper;
    private final OnItemClickListener onItemClickListener;
    private List<PostFilter> postFilterList;

    public interface OnItemClickListener {
        void onItemClick(PostFilter postFilter);
    }

    public PostFilterRecyclerViewAdapter(BaseActivity activity, CustomThemeWrapper customThemeWrapper,
                                         OnItemClickListener onItemClickListener) {
        this.activity = activity;
        this.customThemeWrapper = customThemeWrapper;
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        }
        return VIEW_TYPE_POST_FILTER;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_fragment_header, parent, false));
        } else {
            return new PostFilterViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post_filter, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof PostFilterViewHolder) {
            ((PostFilterViewHolder) holder).textView.setText(postFilterList.get(position - 1).name);
        }
    }

    @Override
    public int getItemCount() {
        return postFilterList == null ? 1 : 1 + postFilterList.size();
    }

    public void setPostFilterList(List<PostFilter> postFilterList) {
        this.postFilterList = postFilterList;
        notifyDataSetChanged();
    }

    private class PostFilterViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public PostFilterViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = (TextView) itemView;

            textView.setTextColor(customThemeWrapper.getPrimaryTextColor());

            if (activity.typeface != null) {
                textView.setTypeface(activity.typeface);
            }

            itemView.setOnClickListener(view -> {
                onItemClickListener.onItemClick(postFilterList.get(getBindingAdapterPosition() - 1));
            });
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            TextView infoTextView = itemView.findViewById(R.id.info_text_view_item_filter_fragment_header);
            infoTextView.setTextColor(customThemeWrapper.getSecondaryTextColor());
            infoTextView.setCompoundDrawablesWithIntrinsicBounds(Utils.getTintedDrawable(activity, R.drawable.ic_info_preference_24dp, activity.customThemeWrapper.getPrimaryIconColor()), null, null, null);
        }
    }
}
