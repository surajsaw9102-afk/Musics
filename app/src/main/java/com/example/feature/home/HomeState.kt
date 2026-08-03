package com.example.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.database.entities.SongEntity
import com.example.feature.recommendation.UserSignalTracker
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeState : ViewModel() {

    private val _homeFeed = MutableStateFlow(HomeFeedData(isLoading = true))
    val homeFeed: StateFlow<HomeFeedData> = _homeFeed.asStateFlow()

    private var currentCategoryFilter = "All"

    init {
        observeTasteProfile()
    }

    private fun observeTasteProfile() {
        viewModelScope.launch {
            UserSignalTracker.tasteProfile.collect { profile ->
                val feed = HomeFeedGenerator.generateFeed(profile, currentCategoryFilter)
                _homeFeed.value = feed
            }
        }
    }

    fun setCategoryFilter(category: String) {
        currentCategoryFilter = category
        val profile = UserSignalTracker.tasteProfile.value
        _homeFeed.value = HomeFeedGenerator.generateFeed(profile, currentCategoryFilter)
    }

    fun refreshFeed() {
        _homeFeed.value = _homeFeed.value.copy(isLoading = true)
        viewModelScope.launch {
            kotlinx.coroutines.delay(200) // Smooth pull-to-refresh feel
            val profile = UserSignalTracker.tasteProfile.value
            _homeFeed.value = HomeFeedGenerator.generateFeed(profile, currentCategoryFilter)
        }
    }

    fun onSongPlayed(song: SongEntity) {
        UserSignalTracker.recordSongPlay(song)
    }

    fun onSongLiked(song: SongEntity, isLiked: Boolean) {
        UserSignalTracker.recordSongLike(song, isLiked)
    }

    fun resetToFreshUser() {
        UserSignalTracker.resetProfileForNewUser()
    }
}
