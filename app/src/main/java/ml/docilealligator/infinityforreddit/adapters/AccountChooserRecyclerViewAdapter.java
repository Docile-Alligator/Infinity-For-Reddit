package ml.docilealligator.infinityforreddit.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import ml.docilealligator.infinityforreddit.R;
import ml.docilealligator.infinityforreddit.account.Account;
import ml.docilealligator.infinityforreddit.activities.BaseActivity;
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper;
import pl.droidsonroids.gif.GifImageView;

public class AccountChooserRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private BaseActivity baseActivity;
    private ArrayList<Account> accounts;
    private RequestManager glide;
    private int primaryTextColor;
    private ItemClickListener itemClickListener;

    public AccountChooserRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                             RequestManager glide, ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new AccountViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_drawer_account, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof AccountViewHolder) {
            glide.load(accounts.get(position).getProfileImageUrl())
                    .error(glide.load(R.drawable.subreddit_default_icon))
                    .apply(RequestOptions.bitmapTransform(new RoundedCornersTransformation(128, 0)))
                    .into(((AccountViewHolder) holder).profileImageGifImageView);
            ((AccountViewHolder) holder).usernameTextView.setText(accounts.get(position).getAccountName());
            holder.itemView.setOnClickListener(view ->
                    itemClickListener.onClick(accounts.get(position)));
        }
    }

    @Override
    public int getItemCount() {
        return accounts == null ? 0 : accounts.size();
    }

    public void changeAccountsDataset(List<Account> accounts) {
        this.accounts = (ArrayList<Account>) accounts;
        notifyDataSetChanged();
    }

    class AccountViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.profile_image_item_account)
        GifImageView profileImageGifImageView;
        @BindView(R.id.username_text_view_item_account)
        TextView usernameTextView;

        AccountViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                usernameTextView.setTypeface(baseActivity.typeface);
            }
            usernameTextView.setTextColor(primaryTextColor);
        }
    }

    public interface ItemClickListener {
        void onClick(Account account);
    }
}
