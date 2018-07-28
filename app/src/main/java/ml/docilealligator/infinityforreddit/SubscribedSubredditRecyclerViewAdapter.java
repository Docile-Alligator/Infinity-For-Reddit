package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

class SubscribedSubredditRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context mContext;
    ArrayList<SubredditData> mSubredditData;
    RequestManager glide;

    SubscribedSubredditRecyclerViewAdapter(Context context, ArrayList<SubredditData> subredditData) {
        mContext = context;
        mSubredditData = subredditData;
        glide = Glide.with(context);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new SubredditViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_subreddit, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if(!mSubredditData.get(i).getIconUrl().equals("")) {
            glide.load(mSubredditData.get(i).getIconUrl()).into(((SubredditViewHolder) viewHolder).iconCircleImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon).into(((SubredditViewHolder) viewHolder).iconCircleImageView);
        }
        ((SubredditViewHolder) viewHolder).subredditNameTextView.setText(mSubredditData.get(i).getName());
    }

    @Override
    public int getItemCount() {
        return mSubredditData.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        glide.clear(((SubredditViewHolder) holder).iconCircleImageView);
    }

    private class SubredditViewHolder extends RecyclerView.ViewHolder {
        private CircleImageView iconCircleImageView;
        private TextView subredditNameTextView;

        public SubredditViewHolder(View itemView) {
            super(itemView);
            iconCircleImageView = itemView.findViewById(R.id.subreddit_icon_circle_image_view_item_subscribed_subreddit);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
        }
    }
}
