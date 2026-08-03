package com.example.core.state

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.search.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SearchData(
    val query: String = "",
    val filterOptions: SearchFilterOptions = SearchFilterOptions(),
    val searchResults: SearchResultsGroup = SearchResultsGroup(),
    val autocompleteSuggestions: List<String> = emptyList(),
    val recentSearches: List<String> = listOf("Lumina Eclipse", "Synthwave 2026", "Zero Gravity", "Lo-Fi Beats"),
    val trendingSearches: List<String> = SearchRepository.TRENDING_SEARCHES,
    val moodCategories: List<MoodCategory> = SearchRepository.MOOD_CATEGORIES,
    val onlinePlaylists: List<OnlinePlaylist> = SearchRepository.ONLINE_PLAYLISTS,
    val selectedMood: MoodCategory? = null,
    val isSearching: Boolean = false,
    val isVoiceSearching: Boolean = false,
    val showFilterSheet: Boolean = false
)

class SearchState : ViewModel() {
    private val _searchData = MutableStateFlow(SearchData())
    val searchData: StateFlow<SearchData> = _searchData.asStateFlow()

    private var searchDebounceJob: Job? = null

    fun updateQuery(newQuery: String) {
        val currentData = _searchData.value
        _searchData.value = currentData.copy(
            query = newQuery,
            isSearching = newQuery.isNotEmpty()
        )

        searchDebounceJob?.cancel()
        searchDebounceJob = viewModelScope.launch {
            if (newQuery.isNotEmpty()) {
                val autocomplete = SearchRepository.getAutocompleteSuggestions(newQuery)
                _searchData.value = _searchData.value.copy(autocompleteSuggestions = autocomplete)
                delay(150) // Smooth debounced search feel
            }

            val results = SearchRepository.performSearch(newQuery, _searchData.value.filterOptions)
            _searchData.value = _searchData.value.copy(
                searchResults = results,
                isSearching = false
            )
        }
    }

    fun selectCategory(category: SearchCategory) {
        val updatedFilters = _searchData.value.filterOptions.copy(category = category)
        updateFilters(updatedFilters)
    }

    fun setSortBy(sortBy: SortByOption) {
        val updatedFilters = _searchData.value.filterOptions.copy(sortBy = sortBy)
        updateFilters(updatedFilters)
    }

    fun setDurationFilter(duration: String) {
        val updatedFilters = _searchData.value.filterOptions.copy(durationFilter = duration)
        updateFilters(updatedFilters)
    }

    fun setAudioQualityFilter(quality: String) {
        val updatedFilters = _searchData.value.filterOptions.copy(audioQualityFilter = quality)
        updateFilters(updatedFilters)
    }

    private fun updateFilters(newFilters: SearchFilterOptions) {
        _searchData.value = _searchData.value.copy(filterOptions = newFilters)
        val results = SearchRepository.performSearch(_searchData.value.query, newFilters)
        _searchData.value = _searchData.value.copy(searchResults = results)
    }

    fun selectMood(mood: MoodCategory?) {
        _searchData.value = _searchData.value.copy(selectedMood = mood)
        if (mood != null) {
            updateQuery(mood.name)
        } else {
            updateQuery("")
        }
    }

    fun toggleFilterSheet(show: Boolean) {
        _searchData.value = _searchData.value.copy(showFilterSheet = show)
    }

    fun startVoiceSearch() {
        _searchData.value = _searchData.value.copy(isVoiceSearching = true)
    }

    fun stopVoiceSearch() {
        _searchData.value = _searchData.value.copy(isVoiceSearching = false)
    }

    fun applyVoiceQuery(voiceText: String) {
        _searchData.value = _searchData.value.copy(isVoiceSearching = false)
        addRecentSearch(voiceText)
        updateQuery(voiceText)
    }

    fun addRecentSearch(searchQuery: String) {
        val trimmed = searchQuery.trim()
        if (trimmed.isEmpty()) return
        val currentList = _searchData.value.recentSearches.toMutableList()
        currentList.remove(trimmed)
        currentList.add(0, trimmed)
        _searchData.value = _searchData.value.copy(
            recentSearches = currentList.take(8)
        )
    }

    fun removeRecentSearch(searchQuery: String) {
        val currentList = _searchData.value.recentSearches.toMutableList()
        currentList.remove(searchQuery)
        _searchData.value = _searchData.value.copy(recentSearches = currentList)
    }

    fun clearRecentSearches() {
        _searchData.value = _searchData.value.copy(recentSearches = emptyList())
    }
}
