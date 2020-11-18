package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.UserFlair;
import ml.docilealligator.infinityforreddit.utils.Utils;

public class UserFlairRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private CustomThemeWrapper customThemeWrapper;
    private ArrayList<UserFlair> userFlairs;
    private ItemClickListener itemClickListener;

    public UserFlairRecyclerViewAdapter(CustomThemeWrapper customThemeWrapper, ArrayList<UserFlair> userFlairs,
                                        ItemClickListener itemClickListener) {
        this.customThemeWrapper = customThemeWrapper;
        this.userFlairs = userFlairs;
        this.itemClickListener = itemClickListener;
    }

    public interface ItemClickListener {
        void onClick(UserFlair userFlair, boolean editUserFlair);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new UserFlairViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user_flair, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof UserFlairViewHolder) {
            UserFlair userFlair = userFlairs.get(holder.getAdapterPosition());
            if (userFlair.getHtmlText() == null || userFlair.getHtmlText().equals("")) {
                ((UserFlairViewHolder) holder).userFlairHtmlTextView.setText(userFlair.getText());
            } else {
                Utils.setHTMLWithImageToTextView(((UserFlairViewHolder) holder).userFlairHtmlTextView, userFlair.getHtmlText(), true);
            }
            if (userFlair.isEditable()) {
                ((UserFlairViewHolder) holder).editUserFlairImageView.setVisibility(View.VISIBLE);
            } else {
                ((UserFlairViewHolder) holder).editUserFlairImageView.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return userFlairs == null ? 0 : userFlairs.size();
    }

    class UserFlairViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_flair_html_text_view_item_user_flair)
        TextView userFlairHtmlTextView;
        @BindView(R.id.edit_user_flair_image_view_item_user_flair)
        ImageView editUserFlairImageView;

        public UserFlairViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            userFlairHtmlTextView.setTextColor(customThemeWrapper.getPrimaryTextColor());
            editUserFlairImageView.setColorFilter(customThemeWrapper.getPrimaryTextColor(), android.graphics.PorterDuff.Mode.SRC_IN);

            itemView.setOnClickListener(view -> {
                itemClickListener.onClick(userFlairs.get(getAdapterPosition()), false);
            });

            editUserFlairImageView.setOnClickListener(view -> {
                itemClickListener.onClick(userFlairs.get(getAdapterPosition()), true);
            });
        }
    }
}
