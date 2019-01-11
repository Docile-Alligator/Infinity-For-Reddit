package ml.docilealligator.infinityforreddit;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.felipecsl.gifimageview.library.GifImageView;

import java.util.List;

import SubscribedUserDatabase.SubscribedUserData;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

public class SubscribedUserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<SubscribedUserData> mSubscribedUserData;
    private Context mContext;
    private RequestManager glide;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onClick();
    }

    SubscribedUserRecyclerViewAdapter(Context context, OnItemClickListener onItemClickListener) {
        mContext = context;
        glide = Glide.with(context.getApplicationContext());
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new UserViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subscribed_subreddit, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
        viewHolder.itemView.setOnClickListener(view -> {
            Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
            intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY, mSubscribedUserData.get(viewHolder.getAdapterPosition()).getName());
            mContext.startActivity(intent);
            mOnItemClickListener.onClick();
        });
        if(!mSubscribedUserData.get(i).getIconUrl().equals("")) {
            glide.load(mSubscribedUserData.get(i).getIconUrl())
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            if(resource instanceof Animatable) {
                                //This is a gif
                                ((Animatable) resource).start();
                                ((SubscribedUserRecyclerViewAdapter.UserViewHolder) viewHolder).iconGifImageView.startAnimation();
                            }
                            return false;
                        }
                    }).into(((UserViewHolder) viewHolder).iconGifImageView);
        } else {
            glide.load(R.drawable.subreddit_default_icon)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .into(((UserViewHolder) viewHolder).iconGifImageView);
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
        glide.clear(((UserViewHolder) holder).iconGifImageView);
    }

    void setSubscribedUsers(List<SubscribedUserData> subscribedUsers){
        mSubscribedUserData = subscribedUsers;
        notifyDataSetChanged();
    }


    private class UserViewHolder extends RecyclerView.ViewHolder {
        private final GifImageView iconGifImageView;
        private final TextView subredditNameTextView;

        public UserViewHolder(View itemView) {
            super(itemView);
            iconGifImageView = itemView.findViewById(R.id.subreddit_icon_gif_image_view_item_subscribed_subreddit);
            subredditNameTextView = itemView.findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
        }
    }
}
