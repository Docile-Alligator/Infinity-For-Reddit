package ml.docilealligator.infinityforreddit;

import SubscribedUserDatabase.SubscribedUserData;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import java.util.List;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import pl.droidsonroids.gif.GifImageView;

public class FollowedUsersRecyclerViewAdapter extends
    RecyclerView.Adapter<RecyclerView.ViewHolder> {

  private final Context mContext;
  private final RequestManager glide;
  private List<SubscribedUserData> mSubscribedUserData;

  FollowedUsersRecyclerViewAdapter(Context context) {
    mContext = context;
    glide = Glide.with(context.getApplicationContext());
  }

  @NonNull
  @Override
  public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    return new UserViewHolder(LayoutInflater.from(viewGroup.getContext())
        .inflate(R.layout.item_subscribed_thing, viewGroup, false));
  }

  @Override
  public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder viewHolder, int i) {
    viewHolder.itemView.setOnClickListener(view -> {
      Intent intent = new Intent(mContext, ViewUserDetailActivity.class);
      intent.putExtra(ViewUserDetailActivity.EXTRA_USER_NAME_KEY,
          mSubscribedUserData.get(viewHolder.getAdapterPosition()).getName());
      mContext.startActivity(intent);
    });
    if (!mSubscribedUserData.get(i).getIconUrl().equals("")) {
      glide.load(mSubscribedUserData.get(i).getIconUrl())
          .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
          .error(glide.load(R.drawable.subreddit_default_icon)
              .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
          .into(((UserViewHolder) viewHolder).iconGifImageView);
    } else {
      glide.load(R.drawable.subreddit_default_icon)
          .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
          .into(((UserViewHolder) viewHolder).iconGifImageView);
    }
    ((UserViewHolder) viewHolder).subredditNameTextView
        .setText(mSubscribedUserData.get(i).getName());
  }

  @Override
  public int getItemCount() {
    if (mSubscribedUserData != null) {
      return mSubscribedUserData.size();
    }
    return 0;
  }

  @Override
  public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
    glide.clear(((UserViewHolder) holder).iconGifImageView);
  }

  void setSubscribedUsers(List<SubscribedUserData> subscribedUsers) {
    mSubscribedUserData = subscribedUsers;
    notifyDataSetChanged();
  }


  private class UserViewHolder extends RecyclerView.ViewHolder {

    private final GifImageView iconGifImageView;
    private final TextView subredditNameTextView;

    public UserViewHolder(View itemView) {
      super(itemView);
      iconGifImageView = itemView
          .findViewById(R.id.subreddit_icon_gif_image_view_item_subscribed_subreddit);
      subredditNameTextView = itemView
          .findViewById(R.id.subreddit_name_text_view_item_subscribed_subreddit);
    }
  }
}
