package ml.docilealligator.infinityforreddit.videoautoplay

import ml.docilealligator.infinityforreddit.videoautoplay.widget.Container
import kotlin.math.min


class MultiPlayPlayerSelector(
    var simultaneousAutoplayLimit: Int
): PlayerSelector {
    override fun select(
        container: Container,
        items: List<ToroPlayer?>
    ): Collection<ToroPlayer?> {
        if (simultaneousAutoplayLimit < 0) {
            return items
        }

        val result: MutableList<ToroPlayer?> = ArrayList()
        val count = min(items.size, simultaneousAutoplayLimit)
        for (i in 0..<count) {
            result.add(items[i])
        }
        return result
    }

    // Don't care about this cuz we don't need to play the videos in reverse order
    override fun reverse(): PlayerSelector {
        return PlayerSelector.DEFAULT_REVERSE;
    }
}