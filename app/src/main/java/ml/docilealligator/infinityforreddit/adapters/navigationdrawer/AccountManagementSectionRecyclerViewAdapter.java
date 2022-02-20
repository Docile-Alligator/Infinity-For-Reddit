package ml.docilealligator.infinityforreddit.adapters.navigationdrawer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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

public class AccountManagementSectionRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ACCOUNT = 1;
    private static final int VIEW_TYPE_MENU_ITEM = 2;

    private BaseActivity baseActivity;
    private ArrayList<Account> accounts;
    private RequestManager glide;
    private int primaryTextColor;
    private int primaryIconColor;
    private boolean isLoggedIn;
    private NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener;

    public AccountManagementSectionRecyclerViewAdapter(BaseActivity baseActivity, CustomThemeWrapper customThemeWrapper,
                                                       RequestManager glide, boolean isLoggedIn,
                                                       NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener itemClickListener) {
        this.baseActivity = baseActivity;
        this.glide = glide;
        primaryTextColor = customThemeWrapper.getPrimaryTextColor();
        primaryIconColor = customThemeWrapper.getPrimaryIconColor();
        this.isLoggedIn = isLoggedIn;
        this.itemClickListener = itemClickListener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position >= accounts.size()) {
            return VIEW_TYPE_MENU_ITEM;
        } else {
            return VIEW_TYPE_ACCOUNT;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ACCOUNT) {
            return new AccountViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_nav_drawer_account, parent, false));
        }

        return new MenuItemViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_nav_drawer_menu_item, parent, false));
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
                    itemClickListener.onAccountClick(accounts.get(position).getAccountName()));
        } else if (holder instanceof MenuItemViewHolder) {
            int stringId = 0;
            int drawableId = 0;

            if (isLoggedIn) {
                int offset = accounts == null ? 0 : accounts.size();
                if (position == offset) {
                    stringId = R.string.add_account;
                    drawableId = R.drawable.ic_outline_add_circle_outline_24dp;
                } else if (position == offset + 1) {
                    stringId = R.string.anonymous_account;
                    drawableId = R.drawable.ic_anonymous_24dp;
                } else if (position == offset + 2) {
                    stringId = R.string.log_out;
                    drawableId = R.drawable.ic_log_out_24dp;
                }
            } else {
                stringId = R.string.add_account;
                drawableId = R.drawable.ic_outline_add_circle_outline_24dp;
            }

            if (stringId != 0) {
                ((MenuItemViewHolder) holder).menuTextView.setText(stringId);
                ((MenuItemViewHolder) holder).imageView.setImageDrawable(ContextCompat.getDrawable(baseActivity, drawableId));
            }
            int finalStringId = stringId;
            holder.itemView.setOnClickListener(view -> itemClickListener.onMenuClick(finalStringId));
        }
    }

    @Override
    public int getItemCount() {
        if (isLoggedIn) {
            if (accounts != null && !accounts.isEmpty()) {
                return 3 + accounts.size();
            } else {
                return 3;
            }
        } else {
            if (accounts != null && !accounts.isEmpty()) {
                return 1 + accounts.size();
            } else {
                return 1;
            }
        }
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

    class MenuItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.image_view_item_nav_drawer_menu_item)
        ImageView imageView;
        @BindView(R.id.text_view_item_nav_drawer_menu_item)
        TextView menuTextView;

        MenuItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            if (baseActivity.typeface != null) {
                menuTextView.setTypeface(baseActivity.typeface);
            }
            menuTextView.setTextColor(primaryTextColor);
            imageView.setColorFilter(primaryIconColor, android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }
}
