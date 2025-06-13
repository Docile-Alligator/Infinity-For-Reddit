package ml.docilealligator.infinityforreddit.utils

import android.graphics.Color
import androidx.core.graphics.ColorUtils

fun getMaterialSwitchTrackColorFromThumbColor(thumbColor: Int): Int {
    val blendedColor = if (ColorUtils.calculateLuminance(thumbColor) < 0.5) Color.WHITE else Color.BLACK
    val originalAlpha = Color.alpha(thumbColor)
    val opaqueThumbColor = ColorUtils.setAlphaComponent(thumbColor, 255)
    val opaqueNewColor = ColorUtils.blendARGB(opaqueThumbColor, blendedColor, 0.6f)

    return ColorUtils.setAlphaComponent(opaqueNewColor, originalAlpha)
}