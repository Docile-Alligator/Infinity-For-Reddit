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

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

class SubscribedUserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SubscribedUserData> mSubscribedUserData;
    private RequestManager glide;
    private OnItemClickListener mOnItemClickListener;

    interface OnItemClickListener {
        void onClick();
    }

    SubscribedUserRecyclerViewAdapter(Context context, OnItemClickListener onItemClickListener) {
        glide = Glide.with(context.getApplicationContext());
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UserViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_subreddit, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Need to be implemented

                mOnItemClickListener.onClick();
            }
        });
        if(!mSubscribedUserData.get(i).getIconUrl().equals("")) {
            glide.load(mSubscribedUserData.get(i).getIconUrl()).into(((UserViewHolder) viewHolder).iconCircleImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon).into(((UserViewHolder) viewHolder).iconCircleImageView);
        }
        ((UserViewHolder) viewHolder).subredditNameTextView.setText(mSubscribedUserData.get(i).getName());
    }

    @Override
    public int getItemCount() {
        if(mSubscribedUserData != null) {
            return mSubscribedUserData.size();
        }
        return 0;
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        glide.clear(((UserViewHolder) holder).iconCircleImageView);
    }

    void setSubscribedUsers(List<SubscribedUserData> subscribedUsers){
        mSubscribedUserData = subscribedUsers;
        notifyDataSetChanged();
    }


    private class UserViewHolder extends RecyclerView.ViewHolder {
        private final CircleImageView iconCircleImageView;
        private final TextView subredditNameTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            iconCircleImageView = itemView.findViewById(R.id.subreddit_icon_circle_image_view_item_subscribed_subreddit);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
        }
    }
}
