package ml.docilealligator.infinityforreddit.adapters;

import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.BlurTransformation;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.databinding.ItemGalleryImageInPostFeedBinding;
import ml.docilealligator.infinityforreddit.post.Post;

public class PostGalleryTypeImageRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RequestManager glide;
    private Typeface typeface;
    private SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInisdeDownsampleStrategy;
    private int mColorAccent;
    private int mPrimaryTextColor;
    private float mScale;
    private ArrayList<Post.Gallery> galleryImages;
    private boolean blurImage;
    private float ratio;
    private OnItemClickListener onItemClickListener;

    public PostGalleryTypeImageRecyclerViewAdapter(RequestManager glide, Typeface typeface,
                                                   SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInisdeDownsampleStrategy,
                                                   int mColorAccent, int mPrimaryTextColor, float scale,
                                                   OnItemClickListener onItemClickListener) {
        this.glide = glide;
        this.typeface = typeface;
        this.saveMemoryCenterInisdeDownsampleStrategy = saveMemoryCenterInisdeDownsampleStrategy;
        this.mColorAccent = mColorAccent;
        this.mPrimaryTextColor = mPrimaryTextColor;
        this.mScale = scale;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ImageViewHolder(ItemGalleryImageInPostFeedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ImageViewHolder) {
            if (ratio < 0) {
                int height = (int) (400 * mScale);
                ((ImageViewHolder) holder).binding.imageViewItemGalleryImageInPostFeed.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ((ImageViewHolder) holder).binding.imageViewItemGalleryImageInPostFeed.getLayoutParams().height = height;
            } else {
                ((ImageViewHolder) holder).binding.imageViewItemGalleryImageInPostFeed.setRatio(ratio);
            }
            ((ImageViewHolder) holder).binding.errorTextViewItemGalleryImageInPostFeed.setVisibility(View.GONE);
            ((ImageViewHolder) holder).binding.progressBarItemGalleryImageInPostFeed.setVisibility(View.VISIBLE);
            loadImage((ImageViewHolder) holder);
        }
    }

    @Override
    public int getItemCount() {
        return galleryImages == null ? 0 : galleryImages.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    private void loadImage(ImageViewHolder holder) {
        holder.binding.imageViewItemGalleryImageInPostFeed.setRatio(ratio);
        RequestBuilder<Drawable> imageRequestBuilder = glide.load(galleryImages.get(holder.getBindingAdapterPosition()).url).listener(new RequestListener<>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                holder.binding.progressBarItemGalleryImageInPostFeed.setVisibility(View.GONE);
                holder.binding.errorTextViewItemGalleryImageInPostFeed.setVisibility(View.VISIBLE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.binding.errorTextViewItemGalleryImageInPostFeed.setVisibility(View.GONE);
                holder.binding.progressBarItemGalleryImageInPostFeed.setVisibility(View.GONE);
                return false;
            }
        });
        if (blurImage) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                    .into(holder.binding.imageViewItemGalleryImageInPostFeed);
        } else {
            imageRequestBuilder.centerInside().downsample(saveMemoryCenterInisdeDownsampleStrategy).into(holder.binding.imageViewItemGalleryImageInPostFeed);
        }
    }

    public void setGalleryImages(ArrayList<Post.Gallery> galleryImages) {
        this.galleryImages = galleryImages;
        notifyDataSetChanged();
    }

    public void setBlurImage(boolean blurImage) {
        this.blurImage = blurImage;
    }

    public void setRatio(float ratio) {
        this.ratio = ratio;
    }

    class ImageViewHolder extends RecyclerView.ViewHolder {

        ItemGalleryImageInPostFeedBinding binding;

        public ImageViewHolder(ItemGalleryImageInPostFeedBinding binding) {
            super(binding.getRoot());

            this.binding = binding;

            if (typeface != null) {
                binding.errorTextViewItemGalleryImageInPostFeed.setTypeface(typeface);
            }
            binding.progressBarItemGalleryImageInPostFeed.setIndeterminateTintList(ColorStateList.valueOf(mColorAccent));
            binding.errorTextViewItemGalleryImageInPostFeed.setTextColor(mPrimaryTextColor);

            binding.imageViewItemGalleryImageInPostFeed.setOnClickListener(view -> {
                onItemClickListener.onClick(getBindingAdapterPosition());
            });

            binding.errorTextViewItemGalleryImageInPostFeed.setOnClickListener(view -> {
                binding.progressBarItemGalleryImageInPostFeed.setVisibility(View.VISIBLE);
                binding.errorTextViewItemGalleryImageInPostFeed.setVisibility(View.GONE);
                loadImage(this);
            });
        }
    }

    public interface OnItemClickListener {
        void onClick(int galleryItemIndex);
    }
}
