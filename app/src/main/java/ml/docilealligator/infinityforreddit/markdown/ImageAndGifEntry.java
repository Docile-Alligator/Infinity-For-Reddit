package ml.docilealligator.infinityforreddit.markdown;

import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;

import io.noties.markwon.Markwon;
import io.noties.markwon.recycler.MarkwonAdapter;
import jp.wasabeef.glide.transformations.BlurTransformation;
import ml.docilealligator.infinityforreddit.SaveMemoryCenterInisdeDownsampleStrategy;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.databinding.MarkdownImageAndGifBlockBinding;
import ml.docilealligator.infinityforreddit.post.Post;
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils;

public class ImageAndGifEntry extends MarkwonAdapter.Entry<ImageAndGifBlock, ImageAndGifEntry.Holder> {
    private BaseActivity baseActivity;
    private RequestManager glide;
    private SaveMemoryCenterInisdeDownsampleStrategy saveMemoryCenterInsideDownsampleStrategy;
    private OnItemClickListener onItemClickListener;

    public ImageAndGifEntry(BaseActivity baseActivity, SharedPreferences sharedPreferences,
                            RequestManager glide, OnItemClickListener onItemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        this.saveMemoryCenterInsideDownsampleStrategy = new SaveMemoryCenterInisdeDownsampleStrategy(
                Integer.parseInt(sharedPreferences.getString(SharedPreferencesUtils.POST_FEED_MAX_RESOLUTION, "5000000")));
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public Holder createHolder(@NonNull LayoutInflater inflater, @NonNull ViewGroup parent) {
        return new Holder(MarkdownImageAndGifBlockBinding.inflate(inflater, parent, false));
    }

    @Override
    public void bindHolder(@NonNull Markwon markwon, @NonNull Holder holder, @NonNull ImageAndGifBlock node) {
        holder.imageAndGifBlock = node;

        holder.binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);

        RequestBuilder<Drawable> imageRequestBuilder = glide.load(node.mediaMetadata.original.url).listener(holder.requestListener);
        boolean blurImage = false;
        if (blurImage) {
            imageRequestBuilder.apply(RequestOptions.bitmapTransform(new BlurTransformation(50, 10)))
                    .into(holder.binding.imageViewMarkdownImageAndGifBlock);
        } else {
            imageRequestBuilder.centerInside().downsample(saveMemoryCenterInsideDownsampleStrategy).into(holder.binding.imageViewMarkdownImageAndGifBlock);
        }
    }

    @Override
    public void onViewRecycled(@NonNull Holder holder) {
        super.onViewRecycled(holder);
        glide.clear(holder.binding.imageViewMarkdownImageAndGifBlock);
        holder.binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
        holder.binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setVisibility(View.GONE);
    }

    public class Holder extends MarkwonAdapter.Holder {
        MarkdownImageAndGifBlockBinding binding;
        RequestListener<Drawable> requestListener;
        ImageAndGifBlock imageAndGifBlock;

        public Holder(@NonNull MarkdownImageAndGifBlockBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            requestListener = new RequestListener<>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
                    binding.loadImageErrorTextViewMarkdownImageAndGifBlock.setVisibility(View.VISIBLE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    binding.progressBarMarkdownImageAndGifBlock.setVisibility(View.GONE);
                    return false;
                }
            };
            binding.getRoot().setOnClickListener(view -> {
                if (imageAndGifBlock != null) {
                    onItemClickListener.onItemClick(imageAndGifBlock.mediaMetadata);
                }
            });
        }

        /*private final RequestListener<Drawable> requestListener = new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                binding.progressBar.setVisibility(View.GONE);
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                binding.progressBar.setVisibility(View.GONE);
                return false;
            }
        };*/

        /*public Holder(@NonNull AdapterGifEntryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.progressBar.setIndeterminateTintList(ColorStateList.valueOf(customThemeWrapper.getColorAccent()));
            binding.giphyWatermark.setTextColor(customThemeWrapper.getCommentColor());

            binding.gifLink.setTextColor(customThemeWrapper.getLinkColor());

        }*/

        /*void bindImage(ImageMetadata image) {
            binding.gifLink.setVisibility(View.GONE);
            binding.iv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.giphyWatermark.setVisibility(View.GONE);

            ViewGroup.LayoutParams params = binding.iv.getLayoutParams();
            if (image.x > image.y) {
                params.height = dpToPx(160);
                params.width = params.height * image.x / image.y;
            } else {
                params.width = dpToPx(160);
                params.height = params.width * image.y / image.x;
            }
            binding.iv.setLayoutParams(params);

            // todo: check if waitForLayout is necessary here since we explicitly set width/height in LP
            Target<Drawable> target = new DrawableImageViewTarget(binding.iv)
                    .waitForLayout();
            glide.load(image.getUrl())
                    .addListener(requestListener)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(target);
        }

        void bindGif(GiphyGifMetadata gif) {
            if (!canLoadGif()) {
                // video autoplay is disabled, don't load gif
                binding.gifLink.setVisibility(View.VISIBLE);
                binding.iv.setVisibility(View.GONE);
                binding.progressBar.setVisibility(View.GONE);
                binding.giphyWatermark.setVisibility(View.GONE);

                binding.gifLink.setOnClickListener(v -> {
                    onClickListener.accept(Uri.parse(gif.getGifUrl()));
                });
                return;
            }
            binding.gifLink.setVisibility(View.GONE);
            binding.iv.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.giphyWatermark.setVisibility(View.VISIBLE);

            ViewGroup.LayoutParams params = binding.iv.getLayoutParams();
            if (gif.x > gif.y) {
                params.height = dpToPx(160);
                params.width = params.height * gif.x / gif.y;
            } else {
                params.width = dpToPx(160);
                params.height = params.width * gif.y / gif.x;
            }
            binding.iv.setLayoutParams(params);

            // todo: check if waitForLayout is necessary here since we explicitly set width/height in LP
            Target<Drawable> target = new DrawableImageViewTarget(binding.iv)
                    .waitForLayout();
            glide.load(gif.getGifUrl())
                    .addListener(requestListener)
                    .error(R.drawable.ic_error_outline_black_24dp)
                    .into(target);
        }

        void recycle() {
            glide.clear(binding.iv);
        }

        @SuppressWarnings("SameParameterValue")
        private int dpToPx(int dp) {
            float density = itemView.getContext().getResources().getDisplayMetrics().density;
            return (int) (dp * density);
        }

        private boolean canLoadGif() {
            // ideally this would be injected, but it is a bit unpleasant to do
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(itemView.getContext());
            String dataSavingMode = sharedPreferences.getString(SharedPreferencesUtils.DATA_SAVING_MODE, SharedPreferencesUtils.DATA_SAVING_MODE_OFF);
            Log.i("GifEntry", "datasaving=" + dataSavingMode);
            if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ALWAYS)) {
                return false;
            } else if (dataSavingMode.equals(SharedPreferencesUtils.DATA_SAVING_MODE_ONLY_ON_CELLULAR_DATA)) {
                int networkType = Utils.getConnectedNetwork(itemView.getContext());
                return networkType != Utils.NETWORK_TYPE_CELLULAR;
            } else {
                return true;
            }
        }*/
    }

    public interface OnItemClickListener {
        void onItemClick(Post.MediaMetadata mediaMetadata);
    }
}
