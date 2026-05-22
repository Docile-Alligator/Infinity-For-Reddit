package ml.docilealligator.infinityforreddit

class AutoRemovalLinkedHashMap<K, V>(
    var maxSize: Int
): LinkedHashMap<K, V>() {
    override fun removeEldestEntry(eldest: Map.Entry<K?, V?>?): Boolean {
        return size > maxSize
    }
}