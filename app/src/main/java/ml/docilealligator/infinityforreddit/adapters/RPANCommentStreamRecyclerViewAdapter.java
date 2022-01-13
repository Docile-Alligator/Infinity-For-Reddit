package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.RPANComment;
import ml.docilealligator.infinityforreddit.activities.RPANActivity;

public class RPANCommentStreamRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private RPANActivity activity;
    private RequestManager glide;
    private ArrayList<RPANComment> rpanComments;

    public RPANCommentStreamRecyclerViewAdapter(RPANActivity activity) {
        this.activity = activity;
        glide = Glide.with(activity);
        rpanComments = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new RPANCommentViewHolder(LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_rpan_comment, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof RPANCommentViewHolder) {
            ((RPANCommentViewHolder) holder).authorTextView.setText(rpanComments.get(position).author);
            ((RPANCommentViewHolder) holder).contentTextView.setText(rpanComments.get(position).content);
            glide.load(rpanComments.get(position).authorIconImage)
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0)))
                    .error(glide.load(R.drawable.subreddit_default_icon)
                            .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(72, 0))))
                    .into(((RPANCommentViewHolder) holder).iconImageView);
        }
    }

    @Override
    public int getItemCount() {
        return rpanComments == null ? 0 : rpanComments.size();
    }

    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder instanceof RPANCommentViewHolder) {
            glide.clear(((RPANCommentViewHolder) holder).iconImageView);
        }
    }

    public void addRPANComment(RPANComment rpanComment) {
        rpanComments.add(rpanComment);
        notifyItemInserted(rpanComments.size() - 1);
    }

    class RPANCommentViewHolder extends RecyclerView.ViewHolder {
        ImageView iconImageView;
        TextView authorTextView;
        TextView contentTextView;

        public RPANCommentViewHolder(@NonNull View itemView) {
            super(itemView);
            iconImageView = itemView.findViewById(R.id.icon_image_view_item_rpan_comment);
            authorTextView = itemView.findViewById(R.id.author_text_view_item_rpan_comment);
            contentTextView = itemView.findViewById(R.id.content_text_view_item_rpan_comment);

            if (activity.typeface != null) {
                authorTextView.setTypeface(activity.typeface);
                contentTextView.setTypeface(activity.typeface);
            }
        }
    }
}
