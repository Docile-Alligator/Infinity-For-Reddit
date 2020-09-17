package ml.docilealligator.infinityforreddit.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ml.docilealligator.infinityforreddit.CustomTheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.R;

public class MarkdownBottomBarRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int BOLD = 0;
    public static final int ITALIC = 1;
    public static final int LINK = 2;
    public static final int STRIKE_THROUGH = 3;
    public static final int HEADER = 4;
    public static final int ORDERED_LIST = 5;
    public static final int UNORDERED_LIST = 6;
    public static final int SPOILER = 7;

    private static final int ITEM_COUNT = 8;

    private CustomThemeWrapper customThemeWrapper;
    private ItemClickListener itemClickListener;

    public interface ItemClickListener {
        void onClick(int item);
    }

    public MarkdownBottomBarRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper,
                                                ItemClickListener itemClickListener) {
        this.customThemeWrapper = customThemeWrapper;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MarkdownBottomBarItemViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_markdown_bottom_bar, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof MarkdownBottomBarItemViewHolder) {
            switch (position) {
                case BOLD:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_bold_black_24dp);
                    break;
                case ITALIC:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_italic_black_24dp);
                    break;
                case LINK:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_link_round_black_24dp);
                    break;
                case STRIKE_THROUGH:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_strikethrough_black_24dp);
                    break;
                case HEADER:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_header_hashtag_black_24dp);
                    break;
                case ORDERED_LIST:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_ordered_list_black_24dp);
                    break;
                case UNORDERED_LIST:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_unordered_list_black_24dp);
                    break;
                case SPOILER:
                    ((MarkdownBottomBarItemViewHolder) holder).imageView.setImageResource(R.drawable.ic_spoiler_black_24dp);
                    break;
            }
        }
    }

    @Override
    public int getItemCount() {
        return ITEM_COUNT;
    }

    class MarkdownBottomBarItemViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public MarkdownBottomBarItemViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = (ImageView) itemView;
            itemView.setOnClickListener(view -> itemClickListener.onClick(getAdapterPosition()));

            imageView.setColorFilter(customThemeWrapper.getPrimaryIconColor(), android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
