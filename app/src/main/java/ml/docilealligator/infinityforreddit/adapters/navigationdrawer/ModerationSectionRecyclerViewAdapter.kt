package ml.docilealligator.infinityforreddit.adapters.navigationdrawer

import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.activities.BaseActivity
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuGroupTitleBinding
import ml.docilealligator.infinityforreddit.databinding.ItemNavDrawerMenuItemBinding
import ml.docilealligator.infinityforreddit.utils.SharedPreferencesUtils

class ModerationSectionRecyclerViewAdapter(
    private val baseActivity: BaseActivity,
    private val customThemeWrapper: CustomThemeWrapper,
    private val navigationDrawerSharedPreferences: SharedPreferences,
    private val itemClickListener: NavigationDrawerRecyclerViewMergedAdapter.ItemClickListener
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val VIEW_TYPE_MENU_GROUP_TITLE: Int = 1
    private val VIEW_TYPE_MENU_ITEM: Int = 2
    private val MODERATION_SECTION_ITEMS: Int = 1

    private val resources: Resources = baseActivity.getResources()
    private val primaryTextColor: Int = customThemeWrapper.primaryTextColor
    private val secondaryTextColor: Int = customThemeWrapper.secondaryTextColor
    private val primaryIconColor: Int = customThemeWrapper.primaryIconColor
    private var collapsePreferencesSection = navigationDrawerSharedPreferences.getBoolean(
        SharedPreferencesUtils.COLLAPSE_PREFERENCES_SECTION,
        false
    )

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) VIEW_TYPE_MENU_GROUP_TITLE else VIEW_TYPE_MENU_ITEM
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == VIEW_TYPE_MENU_GROUP_TITLE) {
            return MenuGroupTitleViewHolder(
                ItemNavDrawerMenuGroupTitleBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false)
            )
        } else {
            return MenuItemViewHolder(
                ItemNavDrawerMenuItemBinding
                    .inflate(LayoutInflater.from(parent.getContext()), parent, false)
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is MenuGroupTitleViewHolder) {
            holder.binding.titleTextViewItemNavDrawerMenuGroupTitle.setText(R.string.label_moderation)
            if (collapsePreferencesSection) {
                holder.binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(
                    R.drawable.ic_baseline_arrow_drop_up_24dp
                )
            } else {
                holder.binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setImageResource(
                    R.drawable.ic_baseline_arrow_drop_down_24dp
                )
            }

            holder.itemView.setOnClickListener { _ ->
                if (collapsePreferencesSection) {
                    collapsePreferencesSection = !collapsePreferencesSection
                    notifyItemRangeInserted(
                        holder.getBindingAdapterPosition() + 1,
                        MODERATION_SECTION_ITEMS
                    )
                } else {
                    collapsePreferencesSection = !collapsePreferencesSection
                    notifyItemRangeRemoved(
                        holder.getBindingAdapterPosition() + 1,
                        MODERATION_SECTION_ITEMS
                    )
                }
                notifyItemChanged(holder.getBindingAdapterPosition())
            }
        } else if (holder is MenuItemViewHolder) {
            var stringId = 0
            var drawableId = 0

            when (position) {
                1 -> {
                    stringId = R.string.mod_mail
                    drawableId = R.drawable.ic_mod_24dp
                }
            }

            if (stringId != 0) {
                holder.binding.textViewItemNavDrawerMenuItem.setText(stringId)
                holder.binding.imageViewItemNavDrawerMenuItem.setImageDrawable(
                    ContextCompat.getDrawable(
                        baseActivity,
                        drawableId
                    )
                )
                val finalStringId = stringId
                holder.itemView.setOnClickListener { _ ->
                    itemClickListener.onMenuClick(
                        finalStringId
                    )
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return if (collapsePreferencesSection) 1 else MODERATION_SECTION_ITEMS + 1
    }

    inner class MenuGroupTitleViewHolder internal constructor(var binding: ItemNavDrawerMenuGroupTitleBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {

        init {
            if (baseActivity.typeface != null) {
                binding.titleTextViewItemNavDrawerMenuGroupTitle.setTypeface(baseActivity.typeface)
            }
            binding.titleTextViewItemNavDrawerMenuGroupTitle.setTextColor(secondaryTextColor)
            binding.collapseIndicatorImageViewItemNavDrawerMenuGroupTitle.setColorFilter(
                secondaryTextColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }

    inner class MenuItemViewHolder internal constructor(var binding: ItemNavDrawerMenuItemBinding) :
        RecyclerView.ViewHolder(binding.getRoot()) {

        init {
            if (baseActivity.typeface != null) {
                binding.textViewItemNavDrawerMenuItem.setTypeface(baseActivity.typeface)
            }
            binding.textViewItemNavDrawerMenuItem.setTextColor(primaryTextColor)
            binding.imageViewItemNavDrawerMenuItem.setColorFilter(
                primaryIconColor,
                PorterDuff.Mode.SRC_IN
            )
        }
    }
}