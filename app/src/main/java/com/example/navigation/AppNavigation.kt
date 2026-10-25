package com.example.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.data.model.VideoNews
import com.example.data.repository.AuthRepository
import com.example.data.repository.BookmarkRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.UserSessionManager
import com.example.data.repository.VideoRepository
import com.example.ui.components.AadhyaBottomNav
import com.example.ui.components.MandatoryPermissionsPrompt
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.ArticleDetailScreen
import com.example.ui.screens.CategoriesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveTvScreen
import com.example.ui.screens.MoreScreen
import com.example.ui.screens.PhoneLoginScreen
import com.example.ui.screens.ReporterDashboardScreen
import com.example.ui.screens.SavedNewsScreen
import com.example.ui.screens.SearchScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.VideoDetailScreen
import com.example.ui.screens.VideosScreen
import com.example.ui.viewmodel.AdminViewModel
import com.example.ui.viewmodel.ArticleDetailViewModel
import com.example.ui.viewmodel.BookmarksViewModel
import com.example.ui.viewmodel.CategoriesViewModel
import com.example.ui.viewmodel.HomeViewModel
import com.example.ui.viewmodel.LiveTvViewModel
import com.example.ui.viewmodel.VideosViewModel

object NavRoutes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val CATEGORIES = "categories"
    const val VIDEOS = "videos"
    const val BOOKMARKS = "bookmarks"
    const val MORE = "more"
    const val ARTICLE_DETAIL = "article_detail/{articleId}"
    const val LIVE_TV = "live_tv"
    const val SEARCH = "search"
    const val ADMIN_DASHBOARD = "admin_dashboard"
    const val PHONE_LOGIN = "phone_login"
    const val REPORTER_DASHBOARD = "reporter_dashboard"

    fun articleDetail(id: String) = "article_detail/$id"
}

@androidx.annotation.OptIn(androidx.media3.common.util.UnstableApi::class)
@androidx.compose.material3.ExperimentalMaterial3Api
@Composable
fun AppNavigation(
    newsRepository: NewsRepository,
    videoRepository: VideoRepository,
    bookmarkRepository: BookmarkRepository,
    authRepository: AuthRepository,
    isDarkTheme: Boolean,
    onToggleDarkTheme: (Boolean) -> Unit,
    navController: NavHostController = rememberNavController()
) {
    val context = LocalContext.current
    val sessionManager = remember { UserSessionManager(context) }
    val session by sessionManager.session.collectAsStateWithLifecycle()

    var showPermissionsDialog by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavRoutes.SPLASH

    val primaryRoutes = setOf(
        NavRoutes.HOME,
        NavRoutes.CATEGORIES,
        NavRoutes.VIDEOS,
        NavRoutes.BOOKMARKS,
        NavRoutes.MORE
    )

    // ViewModels
    val homeViewModel = remember { HomeViewModel(newsRepository, videoRepository, bookmarkRepository) }
    val categoriesViewModel = remember { CategoriesViewModel(newsRepository) }
    val videosViewModel = remember { VideosViewModel(videoRepository) }
    val bookmarksViewModel = remember { BookmarksViewModel(bookmarkRepository, newsRepository) }
    val liveTvViewModel = remember { LiveTvViewModel(newsRepository) }
    val adminViewModel = remember { AdminViewModel(newsRepository, videoRepository, authRepository) }

    var selectedVideoForDetail by remember { mutableStateOf<VideoNews?>(null) }

    // Mandatory permissions prompt on launch/request
    if (showPermissionsDialog) {
        MandatoryPermissionsPrompt(
            onDismiss = { showPermissionsDialog = false },
            onPermissionsResult = { showPermissionsDialog = false }
        )
    }

    Scaffold(
        bottomBar = {
            if (currentRoute in primaryRoutes) {
                AadhyaBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = NavRoutes.SPLASH
            ) {
                composable(NavRoutes.SPLASH) {
                    SplashScreen(
                        onSplashFinished = {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.SPLASH) { inclusive = true }
                            }
                            // Trigger permissions check after splash
                            showPermissionsDialog = true
                        }
                    )
                }

                composable(NavRoutes.HOME) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        isAdminUser = authRepository.isUserAdmin() || session.isAdmin,
                        onArticleClick = { article ->
                            navController.navigate(NavRoutes.articleDetail(article.id))
                        },
                        onVideoClick = { video ->
                            selectedVideoForDetail = video
                            navController.navigate("video_detail")
                        },
                        onSearchClick = { navController.navigate(NavRoutes.SEARCH) },
                        onLiveTvClick = { navController.navigate(NavRoutes.LIVE_TV) },
                        onVideosClick = { navController.navigate(NavRoutes.VIDEOS) },
                        onAdminClick = {
                            if (session.isAdmin || authRepository.isUserAdmin()) {
                                navController.navigate(NavRoutes.ADMIN_DASHBOARD)
                            } else if (session.isReporter) {
                                navController.navigate(NavRoutes.REPORTER_DASHBOARD)
                            } else {
                                navController.navigate(NavRoutes.PHONE_LOGIN)
                            }
                        }
                    )
                }

                composable(NavRoutes.CATEGORIES) {
                    CategoriesScreen(
                        viewModel = categoriesViewModel,
                        onArticleClick = { article ->
                            navController.navigate(NavRoutes.articleDetail(article.id))
                        }
                    )
                }

                composable(NavRoutes.VIDEOS) {
                    VideosScreen(
                        viewModel = videosViewModel,
                        onVideoClick = { video ->
                            selectedVideoForDetail = video
                            navController.navigate("video_detail")
                        }
                    )
                }

                composable("video_detail") {
                    selectedVideoForDetail?.let { video ->
                        VideoDetailScreen(
                            video = video,
                            onBackClick = { navController.popBackStack() }
                        )
                    }
                }

                composable(NavRoutes.BOOKMARKS) {
                    SavedNewsScreen(
                        viewModel = bookmarksViewModel,
                        onArticleClick = { articleId ->
                            navController.navigate(NavRoutes.articleDetail(articleId))
                        }
                    )
                }

                composable(NavRoutes.MORE) {
                    MoreScreen(
                        authRepository = authRepository,
                        sessionManager = sessionManager,
                        isDarkTheme = isDarkTheme,
                        onToggleDarkTheme = onToggleDarkTheme,
                        onNavigateToAdmin = { navController.navigate(NavRoutes.ADMIN_DASHBOARD) },
                        onNavigateToReporter = { navController.navigate(NavRoutes.REPORTER_DASHBOARD) },
                        onNavigateToPhoneLogin = { navController.navigate(NavRoutes.PHONE_LOGIN) },
                        onRequestPermissions = { showPermissionsDialog = true }
                    )
                }

                composable(NavRoutes.PHONE_LOGIN) {
                    PhoneLoginScreen(
                        sessionManager = sessionManager,
                        newsRepository = newsRepository,
                        onLoginSuccess = { role ->
                            navController.popBackStack()
                            if (role == "admin") {
                                navController.navigate(NavRoutes.ADMIN_DASHBOARD)
                            } else {
                                navController.navigate(NavRoutes.REPORTER_DASHBOARD)
                            }
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.REPORTER_DASHBOARD) {
                    ReporterDashboardScreen(
                        sessionManager = sessionManager,
                        newsRepository = newsRepository,
                        onBackClick = { navController.popBackStack() },
                        onLogoutClick = {
                            sessionManager.logout()
                            navController.popBackStack()
                        }
                    )
                }

                composable(
                    route = NavRoutes.ARTICLE_DETAIL,
                    arguments = listOf(navArgument("articleId") { type = NavType.StringType })
                ) { backStackEntry ->
                    val articleId = backStackEntry.arguments?.getString("articleId") ?: ""
                    val detailViewModel = remember(articleId) {
                        ArticleDetailViewModel(articleId, newsRepository, bookmarkRepository)
                    }
                    ArticleDetailScreen(
                        viewModel = detailViewModel,
                        onBackClick = { navController.popBackStack() },
                        onRelatedArticleClick = { rel ->
                            navController.navigate(NavRoutes.articleDetail(rel.id))
                        }
                    )
                }

                composable(NavRoutes.LIVE_TV) {
                    LiveTvScreen(
                        viewModel = liveTvViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.SEARCH) {
                    SearchScreen(
                        homeViewModel = homeViewModel,
                        onArticleClick = { article ->
                            navController.navigate(NavRoutes.articleDetail(article.id))
                        },
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(NavRoutes.ADMIN_DASHBOARD) {
                    AdminDashboardScreen(
                        viewModel = adminViewModel,
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
