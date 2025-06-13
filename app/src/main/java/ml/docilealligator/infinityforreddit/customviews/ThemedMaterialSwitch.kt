package ml.docilealligator.infinityforreddit.customviews

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import com.google.android.material.materialswitch.MaterialSwitch
import ml.docilealligator.infinityforreddit.Infinity
import ml.docilealligator.infinityforreddit.utils.deriveContrastingColor

class ThemedMaterialSwitch @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = com.google.android.material.R.attr.materialSwitchStyle
): MaterialSwitch(context, attrs, defStyleAttr) {
    init {
        val app = context.applicationContext
        if (app is Infinity) {
            val customThemeWrapper = (context.applicationContext as Infinity).customThemeWrapper
            setThumbTintList(ColorStateList.valueOf(customThemeWrapper.colorAccent))
            val states = arrayOf(
                intArrayOf(R.attr.state_checked)
            )
            val colors = intArrayOf(
                deriveContrastingColor(customThemeWrapper.colorAccent)
            )
            setTrackTintList(ColorStateList(states, colors))
        }
    }
}