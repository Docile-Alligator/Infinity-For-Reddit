package ml.docilealligator.infinityforreddit.customviews.preference

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import ml.docilealligator.infinityforreddit.CustomFontReceiver
import ml.docilealligator.infinityforreddit.R
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapper
import ml.docilealligator.infinityforreddit.customtheme.CustomThemeWrapperReceiver
import ml.docilealligator.infinityforreddit.utils.Utils

class CustomFontPreferenceWithBackground @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = androidx.preference.R.attr.preferenceStyle,
    defStyleRes: Int = 0
) : Preference(context, attrs, defStyleAttr, defStyleRes), CustomFontReceiver,
    CustomThemeWrapperReceiver {
    private var customThemeWrapper: CustomThemeWrapper? = null
    private var typeface: Typeface? = null
    private var top = false
    private var bottom = false

    init {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.CustomFontPreference, 0, 0
        ).let { a ->
            try {
                top = a.getBoolean(R.styleable.CustomFontPreference_top, false)
                bottom = a.getBoolean(R.styleable.CustomFontPreference_bottom, false)
            } finally {
                a.recycle()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val margin16 = Utils.convertDpToPixel(
            16f,
            context
        ).toInt()
        val margin2 = Utils.convertDpToPixel(
            2f,
            context
        ).toInt()


        if (top) {
            if (bottom) {
                holder.itemView.background = AppCompatResources.getDrawable(
                    context,
                    R.drawable.preference_background_top_and_bottom
                )
            } else {
                holder.itemView.background =
                    AppCompatResources.getDrawable(context, R.drawable.preference_background_top)
            }
            setMargins(holder.itemView, margin16, margin16, margin16, -1)
        } else if (bottom) {
            holder.itemView.background =
                AppCompatResources.getDrawable(context, R.drawable.preference_background_bottom)
            setMargins(holder.itemView, margin16, margin2, margin16, -1)
        } else {
            holder.itemView.background =
                AppCompatResources.getDrawable(context, R.drawable.preference_background_middle)
            setMargins(holder.itemView, margin16, margin2, margin16, -1)
        }

        val iconImageView = holder.findViewById(android.R.id.icon)
        val titleTextView = holder.findViewById(android.R.id.title)
        val summaryTextView = holder.findViewById(android.R.id.summary)

        customThemeWrapper?.let {
            holder.itemView.backgroundTintList = ColorStateList.valueOf(it.filledCardViewBackgroundColor)
            if (iconImageView is ImageView) {
                if (isEnabled) {
                    iconImageView.setColorFilter(
                        it.primaryIconColor,
                        PorterDuff.Mode.SRC_IN
                    )
                } else {
                    iconImageView.setColorFilter(
                        it.secondaryTextColor,
                        PorterDuff.Mode.SRC_IN
                    )
                }
            }
            if (titleTextView is TextView) {
                titleTextView.setTextColor(it.primaryTextColor)
            }
            if (summaryTextView is TextView) {
                summaryTextView.setTextColor(it.secondaryTextColor)
            }
        }

        if (typeface != null) {
            if (titleTextView is TextView) {
                titleTextView.setTypeface(typeface)
            }
            if (summaryTextView is TextView) {
                summaryTextView.setTypeface(typeface)
            }
        }
    }

    override fun setCustomFont(
        typeface: Typeface?,
        titleTypeface: Typeface?,
        contentTypeface: Typeface?
    ) {
        this.typeface = typeface
    }

    override fun setCustomThemeWrapper(customThemeWrapper: CustomThemeWrapper) {
        this.customThemeWrapper = customThemeWrapper
    }

    fun setTop(top: Boolean) {
        this.top = top
    }

    companion object {
        fun <T : View?> setMargins(view: T, left: Int, top: Int, right: Int, bottom: Int) {
            val lp = view!!.layoutParams
            if (lp is MarginLayoutParams) {
                val marginParams = lp

                if (top >= 0) {
                    marginParams.topMargin = top
                }
                if (bottom >= 0) {
                    marginParams.bottomMargin = bottom
                }
                if (left >= 0) {
                    marginParams.marginStart = left
                }
                if (right >= 0) {
                    marginParams.marginEnd = right
                }

                view.layoutParams = marginParams
            }
        }
    }
}