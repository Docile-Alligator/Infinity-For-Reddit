package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditGalleryPayload;
import ml.docilealligator.infinityforreddit.activities.PostGalleryActivity;
import ml.docilealligator.infinityforreddit.bottomsheetfragments.SetRedditGalleryItemCaptionAndUrlBottomSheetFragment;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;

public class RedditGallerySubmissionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD_IMAGE = 2;

    private PostGalleryActivity activity;
    private ArrayList<RedditGalleryImageInfo> redditGalleryImageInfoList;
    private CustomThemeWrapper customThemeWrapper;
    private ItemClickListener itemClickListener;
    private RequestManager glide;

    public RedditGallerySubmissionRecyclerViewAdapter(PostGalleryActivity activity, CustomThemeWrapper customThemeWrapper,
                                                      ItemClickListener itemClickListener) {
        this.activity = activity;
        glide = Glide.with(activity);
        this.customThemeWrapper = customThemeWrapper;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (redditGalleryImageInfoList == null || position >= redditGalleryImageInfoList.size()) {
            return VIEW_TYPE_ADD_IMAGE;
        }

        return VIEW_TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ADD_IMAGE) {
            return new AddImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reddit_gallery_submission_add_image, parent, false));
        }
        return new ImageViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reddit_gallery_submission_image, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            glide.load(redditGalleryImageInfoList.get(position).imageUrlString)
                    .apply(new RequestOptions().transform(new CenterCrop(), new RoundedCorners(48)))
                    .listener(new RequestListener<>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((ImageViewHolder) holder).progressBar.setVisibility(View.GONE);
                            ((ImageViewHolder) holder).closeImageView.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            return false;
                        }
                    })
                    .into(((ImageViewHolder) holder).imageView);

            if (redditGalleryImageInfoList.get(position).payload != null) {
                ((ImageViewHolder) holder).progressBar.setVisibility(View.GONE);
                ((ImageViewHolder) holder).closeImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return redditGalleryImageInfoList == null ? 1 : (redditGalleryImageInfoList.size() >= 20 ? 20 : redditGalleryImageInfoList.size() + 1);
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof ImageViewHolder) {
            glide.clear(((ImageViewHolder) holder).imageView);
            ((ImageViewHolder) holder).progressBar.setVisibility(View.VISIBLE);
            ((ImageViewHolder) holder).closeImageView.setVisibility(View.GONE);
        }
    }

    public ArrayList<RedditGalleryImageInfo> getRedditGalleryImageInfoList() {
        return redditGalleryImageInfoList;
    }

    public void setRedditGalleryImageInfoList(ArrayList<RedditGalleryImageInfo> redditGalleryImageInfoList) {
        this.redditGalleryImageInfoList = redditGalleryImageInfoList;
        notifyDataSetChanged();
    }

    public void addImage(String imageUrl) {
        if (redditGalleryImageInfoList == null) {
            redditGalleryImageInfoList = new ArrayList<>();
        }
        redditGalleryImageInfoList.add(new RedditGalleryImageInfo(imageUrl));
        notifyItemInserted(redditGalleryImageInfoList.size() - 1);
    }

    public void setImageAsUploaded(String mediaId) {
        redditGalleryImageInfoList.get(redditGalleryImageInfoList.size() - 1).payload = new RedditGalleryPayload.Item("", "", mediaId);
        notifyItemChanged(redditGalleryImageInfoList.size() - 1);
    }

    public void removeFailedToUploadImage() {
        redditGalleryImageInfoList.remove(redditGalleryImageInfoList.size() - 1);
        notifyItemRemoved(redditGalleryImageInfoList.size());
    }

    public void setCaptionAndUrl(int position, String caption, String url) {
        if (redditGalleryImageInfoList.size() > position && position >= 0) {
            redditGalleryImageInfoList.get(position).payload.setCaption(caption);
            redditGalleryImageInfoList.get(position).payload.setOutboundUrl(url);
        }
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.aspect_ratio_gif_image_view_item_reddit_gallery_submission_image)
        AspectRatioGifImageView imageView;
        @BindView(R.id.progress_bar_item_reddit_gallery_submission_image)
        ProgressBar progressBar;
        @BindView(R.id.close_image_view_item_reddit_gallery_submission_image)
        ImageView closeImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);

            imageView.setRatio(1);

            imageView.setOnClickListener(view -> {
                RedditGalleryPayload.Item payload = redditGalleryImageInfoList.get(getBindingAdapterPosition()).payload;
                if (payload != null) {
                    SetRedditGalleryItemCaptionAndUrlBottomSheetFragment fragment = new SetRedditGalleryItemCaptionAndUrlBottomSheetFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(SetRedditGalleryItemCaptionAndUrlBottomSheetFragment.EXTRA_POSITION, getBindingAdapterPosition());
                    bundle.putString(SetRedditGalleryItemCaptionAndUrlBottomSheetFragment.EXTRA_CAPTION, payload.getCaption());
                    bundle.putString(SetRedditGalleryItemCaptionAndUrlBottomSheetFragment.EXTRA_URL, payload.getOutboundUrl());
                    fragment.setArguments(bundle);
                    fragment.show(activity.getSupportFragmentManager(), fragment.getTag());
                }
            });

            closeImageView.setOnClickListener(view -> {
                redditGalleryImageInfoList.remove(getBindingAdapterPosition());
                notifyItemRemoved(getBindingAdapterPosition());
            });
        }
    }

    class AddImageViewHolder extends RecyclerView.ViewHolder {

        public AddImageViewHolder(@NonNull View itemView) {
            super(itemView);

            FloatingActionButton fab = itemView.findViewById(R.id.fab_item_gallery_submission_add_image);
            fab.setBackgroundTintList(ColorStateList.valueOf(customThemeWrapper.getColorPrimaryLightTheme()));
            fab.setImageTintList(ColorStateList.valueOf(customThemeWrapper.getFABIconColor()));

            itemView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    itemView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    int width = itemView.getMeasuredWidth();
                    ViewGroup.LayoutParams params = itemView.getLayoutParams();
                    params.height = width;
                    itemView.setLayoutParams(params);
                }
            });

            fab.setOnClickListener(view -> itemClickListener.onAddImageClicked());
            itemView.setOnClickListener(view -> fab.performClick());
        }
    }

    public static class RedditGalleryImageInfo implements Parcelable {
        public String imageUrlString;
        public RedditGalleryPayload.Item payload;

        public RedditGalleryImageInfo(String imageUrlString) {
            this.imageUrlString = imageUrlString;
        }

        protected RedditGalleryImageInfo(Parcel in) {
            imageUrlString = in.readString();
            payload = in.readParcelable(RedditGalleryPayload.Item.class.getClassLoader());
        }

        public static final Creator<RedditGalleryImageInfo> CREATOR = new Creator<RedditGalleryImageInfo>() {
            @Override
            public RedditGalleryImageInfo createFromParcel(Parcel in) {
                return new RedditGalleryImageInfo(in);
            }

            @Override
            public RedditGalleryImageInfo[] newArray(int size) {
                return new RedditGalleryImageInfo[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(imageUrlString);
            parcel.writeParcelable(payload, i);
        }
    }

    public interface ItemClickListener {
        void onAddImageClicked();
    }
}
