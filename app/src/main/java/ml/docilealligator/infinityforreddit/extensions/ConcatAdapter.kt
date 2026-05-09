package ml.docilealligator.infinityforreddit.extensions

import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.RecyclerView

fun ConcatAdapter.getAbsolutePosition(childAdapter: RecyclerView.Adapter<*>, localPosition: Int): Int {
    var previousItemSize = 0
    for (adapter in adapters) {
        if (childAdapter == adapter) {
            return previousItemSize + localPosition
        }

        previousItemSize += adapter.itemCount
    }

    return -1
}