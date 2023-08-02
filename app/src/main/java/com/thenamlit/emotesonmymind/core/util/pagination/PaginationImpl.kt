package com.thenamlit.emotesonmymind.core.util.pagination

import com.thenamlit.emotesonmymind.core.util.Resource
import com.thenamlit.emotesonmymind.core.util.SimpleResource
import com.thenamlit.emotesonmymind.core.util.UiText


// https://www.youtube.com/watch?v=D6Eus3f6U9I
class PaginationImpl<Key, Item>(
    private val initialKey: Key,
    private inline val onLoadUpdated: (Boolean) -> Unit,
    private inline val onRequest: suspend (nextKey: Key) -> Resource<List<Item>>,
    private inline val getNextKey: suspend (List<Item>) -> Key,
    private inline val onError: suspend (SimpleResource) -> Unit,
    private inline val onSuccess: suspend (items: List<Item>, newKey: Key) -> Unit,
) : Pagination<Key, Item> {
    private var currentKey = initialKey
    private var currentlyMakingRequest = false

    override suspend fun loadNextItems() {
        if (currentlyMakingRequest) {
            return
        }

        currentlyMakingRequest = true

        onLoadUpdated(true)
        val requestResult = onRequest(currentKey)
        currentlyMakingRequest = false

        when (requestResult) {
            is Resource.Success -> {
                requestResult.data?.let { items: List<Item> ->
                    currentKey = getNextKey(items)
                    onSuccess(items, currentKey)
                } ?: kotlin.run {
                    onError(
                        Resource.Error(
                            uiText = UiText.DynamicString(value = "loadNextItems | Items undefined"),
                            logging = "loadNextItems | Items undefined"
                        )
                    )
                }
            }

            is Resource.Error -> {
                onError(
                    Resource.Error(
                        uiText = requestResult.uiText,
                        logging = requestResult.logging
                    )
                )
            }
        }

        onLoadUpdated(false)
    }

    override fun reset() {
        currentKey = initialKey
    }
}
