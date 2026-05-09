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

fun ConcatAdapter.getLocalPosition(childAdapter: RecyclerView.Adapter<*>, absolutePosition: Int): Int {
    try {
        val pair = getWrappedAdapterAndPosition(absolutePosition)
        if (childAdapter == pair.first) {
            return pair.second
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }

    return -1
}