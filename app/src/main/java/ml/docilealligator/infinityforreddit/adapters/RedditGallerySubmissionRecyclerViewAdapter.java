package ml.docilealligator.infinityforreddit.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RedditGalleryPayload;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.customviews.AspectRatioGifImageView;

public class RedditGallerySubmissionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 1;
    private static final int VIEW_TYPE_ADD_IMAGE = 2;

    private List<RedditGalleryImageInfo> redditGalleryImageInfoList;
    private CustomThemeWrapper customThemeWrapper;
    private ItemClickListener itemClickListener;
    private RequestManager glide;

    public RedditGallerySubmissionRecyclerViewAdapter(Context context, CustomThemeWrapper customThemeWrapper,
                                                      ItemClickListener itemClickListener) {
        glide = Glide.with(context);
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
                    .listener(new RequestListener<Drawable>() {

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            ((ImageViewHolder) holder).progressBar.setVisibility(View.GONE);
                            ((ImageViewHolder) holder).closeImageView.setVisibility(View.VISIBLE);
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            ((ImageViewHolder) holder).progressBar.setVisibility(View.GONE);
                            ((ImageViewHolder) holder).closeImageView.setVisibility(View.VISIBLE);
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
        return redditGalleryImageInfoList == null ? 1 : (redditGalleryImageInfoList.size() >= 9 ? 9 : redditGalleryImageInfoList.size() + 1);
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

    public List<RedditGalleryImageInfo> getRedditGalleryImageInfoList() {
        return redditGalleryImageInfoList;
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

            closeImageView.setOnClickListener(view -> {
                redditGalleryImageInfoList.remove(getBindingAdapterPosition());
                notifyItemRemoved(getBindingAdapterPosition());
            });
        }
    }

    class AddImageViewHolder extends RecyclerView.ViewHolder {

        public AddImageViewHolder(@NonNull View itemView) {
            super(itemView);

            itemView.setBackgroundTintList(ColorStateList.valueOf(customThemeWrapper.getColorPrimaryLightTheme()));
            ((FloatingActionButton) itemView).setImageTintList(ColorStateList.valueOf(customThemeWrapper.getFABIconColor()));

            itemView.setOnClickListener(view -> itemClickListener.onAddImageClicked());
        }
    }

    public static class RedditGalleryImageInfo {
        public String imageUrlString;
        public RedditGalleryPayload.Item payload;

        public RedditGalleryImageInfo(String imageUrlString) {
            this.imageUrlString = imageUrlString;
        }
    }

    public interface ItemClickListener {
        void onAddImageClicked();
    }
}
