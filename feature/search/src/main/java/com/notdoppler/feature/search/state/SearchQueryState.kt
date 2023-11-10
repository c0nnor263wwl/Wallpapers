package com.notdoppler.feature.search.state

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.notdoppler.core.domain.enums.PagingKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


@Stable
class SearchQueryState {
    private val _query: MutableStateFlow<String> = MutableStateFlow("")
    val query: StateFlow<String> = _query

    var pagingKey: PagingKey by mutableStateOf(PagingKey.POPULAR)
        private set
    var isSearching by mutableStateOf(false)


    fun updateQuery(data: String) {
        _query.value = data
    }

    fun updateTabOrder(data: PagingKey) {
        pagingKey = data
    }
}