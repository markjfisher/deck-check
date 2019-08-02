package legends.wrapped

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class WrappedListTest {
    @Test
    fun `contains checks a number of items in a list`() {
        val wrappedList = WrappedList(listOf("a", "b", "c"))
        assertThat(wrappedList.contains("a")).isTrue()
        assertThat(wrappedList.contains("a", "b")).isTrue()
        assertThat(wrappedList.contains("a", "b", "c")).isTrue()
        assertThat(wrappedList.contains("c", "a", "b")).isTrue()

        assertThat(wrappedList.contains("")).isFalse()
        assertThat(wrappedList.contains("x")).isFalse()
        assertThat(wrappedList.contains("a", "x")).isFalse()
    }

    @Test
    fun `containsOnly checks every entry is in the list`() {
        val wrappedList = WrappedList(listOf("a", "b", "c"))

        assertThat(wrappedList.containsOnly("a")).isFalse()
        assertThat(wrappedList.containsOnly("a", "b")).isFalse()

        assertThat(wrappedList.containsOnly("a", "b", "c")).isTrue()
        assertThat(wrappedList.containsOnly("c", "a", "b")).isTrue()

        assertThat(wrappedList.containsOnly("")).isFalse()
        assertThat(wrappedList.containsOnly("x")).isFalse()
        assertThat(wrappedList.containsOnly("a", "x")).isFalse()
    }

    @Test
    fun `subset checks the list is elements are in the given list`() {
        val wrappedList = WrappedList(listOf("a", "b"))

        assertThat(wrappedList.subsetOf("a", "b")).isTrue()
        assertThat(wrappedList.subsetOf("a", "b", "c")).isTrue()
        assertThat(wrappedList.subsetOf("c")).isFalse()
        assertThat(wrappedList.subsetOf("a", "c")).isFalse()
        assertThat(wrappedList.subsetOf("c", "b", "a")).isTrue()
    }
}