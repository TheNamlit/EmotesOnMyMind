package com.thenamlit.emotesonmymind.core.util.pagination


// https://www.youtube.com/watch?v=D6Eus3f6U9I
interface Pagination<Key, Item> {
    suspend fun loadNextItems()
    fun reset()
}
