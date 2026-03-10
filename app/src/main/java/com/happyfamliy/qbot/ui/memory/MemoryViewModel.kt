package com.happyfamliy.qbot.ui.memory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.happyfamliy.qbot.data.local.dao.FactDao
import com.happyfamliy.qbot.data.local.entity.FactEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MemoryViewModel @Inject constructor(
    private val factDao: FactDao
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    @OptIn(ExperimentalCoroutinesApi::class)
    val facts: StateFlow<List<FactEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                factDao.getAllFacts()
            } else {
                factDao.searchFacts(query)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteFact(fact: FactEntity) {
        viewModelScope.launch {
            factDao.deleteFact(fact)
        }
    }
}
