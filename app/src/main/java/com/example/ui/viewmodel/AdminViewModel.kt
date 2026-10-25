package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppSettings
import com.example.data.model.Article
import com.example.data.model.Category
import com.example.data.model.District
import com.example.data.model.VideoNews
import com.example.data.repository.AuthRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AdminViewModel(
    private val newsRepository: NewsRepository,
    private val videoRepository: VideoRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    val currentUser = authRepository.currentUser
    val isUserAdmin = authRepository.isUserAdmin()

    val categories: StateFlow<List<Category>> = newsRepository.observeCategories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val districts: StateFlow<List<District>> = newsRepository.observeDistricts()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val appSettings: StateFlow<AppSettings> = newsRepository.observeAppSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    val pendingArticles: StateFlow<List<Article>> = newsRepository.observePendingArticles()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reporters: StateFlow<List<com.example.data.model.ReporterUser>> = newsRepository.observeReporters()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _allArticles = MutableStateFlow<List<Article>>(emptyList())
    val allArticles: StateFlow<List<Article>> = _allArticles.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    init {
        loadAllArticles()
    }

    private fun loadAllArticles() {
        viewModelScope.launch {
            newsRepository.observeArticles().collect {
                _allArticles.value = it
            }
        }
    }

    fun approveArticle(articleId: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = newsRepository.approveArticle(articleId)
            _isSubmitting.value = false
            if (result.isSuccess) {
                _statusMessage.value = "వార్త ఆమోదించబడింది మరియు లైవ్ ఫీడ్‌లో ప్రచురించబడింది!"
                onDone()
            } else {
                _statusMessage.value = "ఆమోదం విఫలమైంది: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun rejectArticle(articleId: String, reason: String, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = newsRepository.rejectArticle(articleId, reason)
            _isSubmitting.value = false
            if (result.isSuccess) {
                _statusMessage.value = "వార్త తిరస్కరించబడింది."
                onDone()
            } else {
                _statusMessage.value = "తిరస్కరణ విఫలమైంది: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun toggleReporterAccess(reporterId: String, active: Boolean) {
        viewModelScope.launch {
            val result = newsRepository.toggleReporterStatus(reporterId, active)
            if (result.isSuccess) {
                _statusMessage.value = if (active) "రిపోర్టర్ యాక్సెస్ ప్రారంభించబడింది" else "రిపోర్టర్ యాక్సెస్ నిలిపివేయబడింది"
            } else {
                _statusMessage.value = "స్టేటస్ మార్పు విఫలమైంది: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun saveArticle(article: Article, isNew: Boolean, onDone: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = if (isNew) {
                newsRepository.createArticle(article)
            } else {
                newsRepository.updateArticle(article)
            }
            _isSubmitting.value = false
            if (result.isSuccess) {
                _statusMessage.value = "వార్త విజయవంతంగా భద్రపరచబడింది!"
                onDone()
            } else {
                _statusMessage.value = "లోపం: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun deleteArticle(articleId: String) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = newsRepository.deleteArticle(articleId)
            _isSubmitting.value = false
            if (result.isSuccess) {
                _statusMessage.value = "వార్త తొలగించబడింది"
                loadAllArticles()
            } else {
                _statusMessage.value = "తొలగించడం విఫలమైంది: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun updateLiveStreamSettings(url: String, isEnabled: Boolean) {
        viewModelScope.launch {
            val current = appSettings.value
            val updated = current.copy(liveStreamUrl = url, liveStreamEnabled = isEnabled)
            newsRepository.updateAppSettings(updated)
            _statusMessage.value = "లైవ్ టీవీ సెట్టింగ్స్ నవీకరించబడ్డాయి"
        }
    }

    fun createVideo(video: VideoNews, onDone: () -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            val result = videoRepository.createVideo(video)
            _isSubmitting.value = false
            if (result.isSuccess) {
                _statusMessage.value = "వీడియో వార్త విజయవంతంగా జోడించబడింది"
                onDone()
            } else {
                _statusMessage.value = "లోపం: ${result.exceptionOrNull()?.message}"
            }
        }
    }

    fun seedInitialDatabase() {
        viewModelScope.launch {
            _isSubmitting.value = true
            val res = newsRepository.seedInitialDataIfEmpty()
            _isSubmitting.value = false
            if (res.isSuccess) {
                _statusMessage.value = "వర్గాలు, జిల్లాలు మరియు ప్రారంభ డేటా విజయవంతంగా చేర్చబడ్డాయి!"
            } else {
                _statusMessage.value = "డేటా సేవ్ లోపం: ${res.exceptionOrNull()?.message}"
            }
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
