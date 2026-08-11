package ml.docilealligator.infinityforreddit

import android.content.Context
import android.graphics.Rect
import android.view.View
import androidx.annotation.DimenRes
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemDecoration

class PostGalleryGridLayoutItemDecoration(
    itemOffset: Int,
    var spanCount: Int
) : ItemDecoration() {
    private val mHalfOffset: Int = itemOffset / 2

    internal constructor(context: Context, @DimenRes itemOffsetId: Int, spanCount: Int) : this(
        context.resources.getDimensionPixelSize(itemOffsetId), spanCount
    )

    @OptIn(markerClass = [UnstableApi::class])
    override fun getItemOffsets(
        outRect: Rect, view: View, parent: RecyclerView,
        state: RecyclerView.State
    ) {
        super.getItemOffsets(outRect, view, parent, state)
        if (parent.layoutManager is GridLayoutManager) {
            val layoutParams = view.layoutParams as GridLayoutManager.LayoutParams
            val spanIndex = layoutParams.spanIndex
            val rowIndex = parent.getChildAdapterPosition(view) / spanCount
            val topMargin = if (rowIndex == 0) 0 else mHalfOffset

            if (spanCount == 2) {
                if (spanIndex == 0) {
                    outRect.set(mHalfOffset, topMargin, 0, 0)
                } else {
                    outRect.set(mHalfOffset, topMargin, 0, 0)
                }
            } else if (spanCount == 3) {
                when (spanIndex) {
                    0 -> {
                        outRect.set(mHalfOffset, topMargin, 0, 0)
                    }
                    1 -> {
                        outRect.set(mHalfOffset, topMargin, 0, 0)
                    }
                    else -> {
                        outRect.set(mHalfOffset, topMargin, 0, 0)
                    }
                }
            }
        }
    }
}