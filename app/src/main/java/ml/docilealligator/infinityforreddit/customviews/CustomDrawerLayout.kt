package ml.docilealligator.infinityforreddit.customviews

import android.content.Context
import android.util.AttributeSet
import androidx.customview.widget.ViewDragHelper
import androidx.drawerlayout.widget.DrawerLayout
import java.lang.reflect.Field
import kotlin.jvm.java
import kotlin.math.max

class CustomDrawerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : DrawerLayout(context, attrs) {
    var swipeEdgeSize = 0
        set(value) {
            if (field != value) {
                field = value
                applyLeftDraggerEdgeSize()
            }
        }

    private var leftDraggerField: Field? = null
    private var edgeSizeField: Field? = null

    init {
        try {
            leftDraggerField = DrawerLayout::class.java.getDeclaredField("mLeftDragger")
            leftDraggerField?.isAccessible = true

            edgeSizeField = ViewDragHelper::class.java.getDeclaredField("mEdgeSize")
            edgeSizeField?.isAccessible = true
        } catch (_: Exception) {}
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        super.onLayout(changed, l, t, r, b)
        applyLeftDraggerEdgeSize()
    }

    private fun applyLeftDraggerEdgeSize() {
        try {
            val viewDragHelper = leftDraggerField?.get(this) as? ViewDragHelper ?: return

            val originalEdgeSize = edgeSizeField?.get(viewDragHelper) as? Int ?: return
            edgeSizeField?.setInt(viewDragHelper, max(swipeEdgeSize, originalEdgeSize))
        } catch (_: Exception) {

        }
    }
}