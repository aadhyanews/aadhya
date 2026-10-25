package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.model.District
import com.example.data.model.VideoNews
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val newsRepository: NewsRepository,
    private val videoRepository: VideoRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow("latest")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedDistrict = MutableStateFlow("")
    val selectedDistrict: StateFlow<String> = _selectedDistrict.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val categories: StateFlow<List<Category>> = newsRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val districts: StateFlow<List<District>> = newsRepository.observeDistricts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val breakingNews: StateFlow<List<Article>> = newsRepository.observeBreakingNews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val featuredNews: StateFlow<List<Article>> = newsRepository.observeFeaturedNews()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val videos: StateFlow<List<VideoNews>> = videoRepository.observeVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _bookmarkedIds = MutableStateFlow<Set<String>>(emptySet())
    val bookmarkedIds: StateFlow<Set<String>> = _bookmarkedIds.asStateFlow()

    init {
        loadArticles()
        observeBookmarks()
    }

    private fun loadArticles() {
        viewModelScope.launch {
            combine(selectedCategory, selectedDistrict) { cat, dist ->
                Pair(cat, dist)
            }.collect { (cat, dist) ->
                newsRepository.observeArticles(cat, dist).collect { list ->
                    _articles.value = list
                    _isRefreshing.value = false
                }
            }
        }
    }

    private fun observeBookmarks() {
        viewModelScope.launch {
            bookmarkRepository.observeBookmarks().collect { list ->
                _bookmarkedIds.value = list.map { it.articleId }.toSet()
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }

    fun selectDistrict(districtId: String) {
        _selectedDistrict.value = if (_selectedDistrict.value == districtId) "" else districtId
    }

    fun refresh() {
        _isRefreshing.value = true
        loadArticles()
    }

    fun toggleBookmark(article: Article) {
        viewModelScope.launch {
            bookmarkRepository.toggleBookmark(article)
        }
    }
}
