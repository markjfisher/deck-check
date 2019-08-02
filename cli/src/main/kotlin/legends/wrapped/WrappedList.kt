package legends.wrapped

class WrappedList(private val entries: List<String>) {
    // fun contains(entry: String) = entries.contains(entry)
    fun containsOnly(vararg entry: String) = entry.toList().size == entries.size && entry.toList().containsAll(entries)
    fun contains(vararg entry: String) = entries.containsAll(entry.toList())
}