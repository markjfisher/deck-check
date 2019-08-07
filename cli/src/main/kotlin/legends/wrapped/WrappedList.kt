package legends.wrapped

class WrappedList(private val entries: List<String>): Iterable<String> {
    override fun iterator(): Iterator<String> {
        return entries.sorted().iterator()
    }

    fun containsOnly(vararg entry: String) = entry.toList().size == entries.size && entry.toList().containsAll(entries)
    fun contains(vararg entry: String) = entries.containsAll(entry.toList())
    fun subsetOf(vararg entry: String) = entry.toList().containsAll(entries)
    fun size() = entries.size
}