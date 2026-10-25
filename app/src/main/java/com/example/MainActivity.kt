package com.example

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.example.data.repository.AuthRepository
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.VideoRepository
import com.example.navigation.AppNavigation
import com.example.ui.theme.AadhyaNewsTheme

class MainActivity : ComponentActivity() {

    @androidx.compose.material3.ExperimentalMaterial3Api
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val newsRepository = NewsRepository(this)
        val videoRepository = VideoRepository(this)
        val bookmarkRepository = BookmarkRepository(this)
        val authRepository = AuthRepository(this)

        authRepository.attemptAutoSignIn(lifecycleScope)

        val prefs = getSharedPreferences("aadhyanews_prefs", Context.MODE_PRIVATE)

        setContent {
            val systemDark = isSystemInDarkTheme()
            var isDarkTheme by remember {
                mutableStateOf(prefs.getBoolean("dark_theme", systemDark))
            }

            AadhyaNewsTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    newsRepository = newsRepository,
                    videoRepository = videoRepository,
                    bookmarkRepository = bookmarkRepository,
                    authRepository = authRepository,
                    isDarkTheme = isDarkTheme,
                    onToggleDarkTheme = { enabled ->
                        isDarkTheme = enabled
                        prefs.edit().putBoolean("dark_theme", enabled).apply()
                    }
                )
            }
        }
    }
}
