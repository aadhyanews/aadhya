package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.model.District
import com.example.data.model.UserBookmark
import com.example.data.model.VideoNews
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CategoriesViewModel(private val newsRepository: NewsRepository) : ViewModel() {
    val categories: StateFlow<List<Category>> = newsRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCategory = MutableStateFlow("telangana")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    init {
        viewModelScope.launch {
            selectedCategory.collect { cat ->
                newsRepository.observeArticles(categoryId = cat).collect {
                    _articles.value = it
                }
            }
        }
    }

    fun selectCategory(categoryId: String) {
        _selectedCategory.value = categoryId
    }
}

class VideosViewModel(private val videoRepository: VideoRepository) : ViewModel() {
    val videos: StateFlow<List<VideoNews>> = videoRepository.observeVideos()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVideo = MutableStateFlow<VideoNews?>(null)
    val selectedVideo: StateFlow<VideoNews?> = _selectedVideo.asStateFlow()

    fun selectVideo(video: VideoNews) {
        _selectedVideo.value = video
    }
}

class BookmarksViewModel(
    private val bookmarkRepository: BookmarkRepository,
    private val newsRepository: NewsRepository
) : ViewModel() {
    val bookmarks: StateFlow<List<UserBookmark>> = bookmarkRepository.observeBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun removeBookmark(articleId: String) {
        viewModelScope.launch {
            bookmarkRepository.removeBookmark(articleId)
        }
    }
}

class ArticleDetailViewModel(
    private val articleId: String,
    private val newsRepository: NewsRepository,
    private val bookmarkRepository: BookmarkRepository
) : ViewModel() {

    private val _article = MutableStateFlow<Article?>(null)
    val article: StateFlow<Article?> = _article.asStateFlow()

    private val _isBookmarked = MutableStateFlow(false)
    val isBookmarked: StateFlow<Boolean> = _isBookmarked.asStateFlow()

    private val _fontSizeScale = MutableStateFlow(1f) // 1f = normal, 1.2f = large, 1.4f = extra large
    val fontSizeScale: StateFlow<Float> = _fontSizeScale.asStateFlow()

    private val _relatedArticles = MutableStateFlow<List<Article>>(emptyList())
    val relatedArticles: StateFlow<List<Article>> = _relatedArticles.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val art = newsRepository.getArticle(articleId)
            _article.value = art
            if (art != null) {
                _isBookmarked.value = bookmarkRepository.isBookmarked(art.id)
                newsRepository.incrementViewCount(art.id)
                // load related
                newsRepository.observeArticles(categoryId = art.categoryId).collect { list ->
                    _relatedArticles.value = list.filter { it.id != art.id }.take(4)
                }
            }
        }
    }

    fun toggleBookmark() {
        val current = _article.value ?: return
        viewModelScope.launch {
            val newStatus = bookmarkRepository.toggleBookmark(current)
            _isBookmarked.value = newStatus
        }
    }

    fun increaseFontSize() {
        if (_fontSizeScale.value < 1.4f) {
            _fontSizeScale.value += 0.15f
        }
    }

    fun decreaseFontSize() {
        if (_fontSizeScale.value > 0.85f) {
            _fontSizeScale.value -= 0.15f
        }
    }
}

class LiveTvViewModel(private val newsRepository: NewsRepository) : ViewModel() {
    val appSettings: StateFlow<AppSettings> = newsRepository.observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())
}
