package ml.docilealligator.infinityforreddit.utils

import android.graphics.Color
import androidx.core.graphics.ColorUtils

fun deriveContrastingColor(originalColor: Int): Int {
    val blendedColor = if (ColorUtils.calculateLuminance(originalColor) < 0.5) Color.WHITE else Color.BLACK
    val originalAlpha = Color.alpha(originalColor)
    val opaqueThumbColor = ColorUtils.setAlphaComponent(originalColor, 255)
    val opaqueNewColor = ColorUtils.blendARGB(opaqueThumbColor, blendedColor, 0.6f)

    return ColorUtils.setAlphaComponent(opaqueNewColor, originalAlpha)
}