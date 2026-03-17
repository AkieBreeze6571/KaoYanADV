package com.example.kaoyanadventure.ui

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.kaoyanadventure.data.AppContainer
import com.example.kaoyanadventure.data.AppThemeMode
import com.example.kaoyanadventure.ui.screens.AchievementWallScreen
import com.example.kaoyanadventure.ui.screens.DashboardScreen
import com.example.kaoyanadventure.ui.screens.RecordsScreen
import com.example.kaoyanadventure.ui.screens.ReviewScreen
import com.example.kaoyanadventure.ui.screens.SessionDetailScreen
import com.example.kaoyanadventure.ui.screens.SettingsScreen
import com.example.kaoyanadventure.ui.screens.SubjectEditScreen
import com.example.kaoyanadventure.ui.screens.TimerScreen
import com.example.kaoyanadventure.ui.screens.GameTasksScreen
import com.example.kaoyanadventure.ui.screens.GameWalletScreen
import com.example.kaoyanadventure.ui.screens.GameShopScreen

@Composable
fun AppRoot() {
    val ctx = LocalContext.current

    val container = remember(ctx) { AppContainer().apply { init(ctx) } }

    LaunchedEffect(Unit) {
        container.repo.ensureDefaultSubjects()
        container.settings.recordAppOpen(System.currentTimeMillis())
        container.game.tasks.ensureTodayTasks()
    }

    val themeMode by container.settings.themeMode.collectAsState(initial = AppThemeMode.SYSTEM)

    KaoyanTheme(themeMode) {
        Surface(color = MaterialTheme.colorScheme.background) {
            val navController = rememberNavController()
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

            Scaffold(
                modifier = Modifier.systemBarsPadding(),
                bottomBar = {
                    BottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { route ->
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = Routes.DASHBOARD,
                    modifier = Modifier.padding(innerPadding)
                ) {
                    composable(
                        route = Routes.DASHBOARD,
                        enterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeIn()
                        },
                        exitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left) + fadeOut()
                        },
                        popEnterTransition = {
                            slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeIn()
                        },
                        popExitTransition = {
                            slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right) + fadeOut()
                        }
                    ) {
                        DashboardScreen(
                            container = container,
                            onOpenAchievements = { navController.navigate(Routes.ACHIEVEMENTS) }
                        )
                    }

                    composable(Routes.TIMER) {
                        TimerScreen(
                            container = container,
                            onOpenSession = { id -> navController.navigate(Routes.SESSION_DETAIL + "/$id") },
                            onEditSubject = { id -> navController.navigate(Routes.SUBJECT_EDIT + "/$id") }
                        )
                    }

                    composable(Routes.RECORDS) {
                        RecordsScreen(
                            container = container,
                            onOpenSession = { id -> navController.navigate(Routes.SESSION_DETAIL + "/$id") }
                        )
                    }

                    composable(Routes.REVIEW) {
                        ReviewScreen(container)
                    }

                    composable(Routes.GAME_TASKS) {
                        GameTasksScreen(
                            container = container,
                            onOpenWallet = { navController.navigate(Routes.GAME_WALLET) },
                            onOpenShop = { navController.navigate(Routes.GAME_SHOP) }
                        )
                    }

                    composable(Routes.GAME_WALLET) {
                        GameWalletScreen(
                            container = container,
                            onOpenTasks = { navController.navigate(Routes.GAME_TASKS) },
                            onOpenShop = { navController.navigate(Routes.GAME_SHOP) }
                        )
                    }

                    composable(Routes.GAME_SHOP) {
                        GameShopScreen(
                            container = container,
                            onOpenTasks = { navController.navigate(Routes.GAME_TASKS) },
                            onOpenWallet = { navController.navigate(Routes.GAME_WALLET) }
                        )
                    }

                    composable(Routes.SETTINGS) {
                        SettingsScreen(container)
                    }

                    composable(Routes.ACHIEVEMENTS) {
                        AchievementWallScreen(container)
                    }

                    composable(Routes.SESSION_DETAIL + "/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        SessionDetailScreen(container, sessionId = id)
                    }

                    composable(Routes.SUBJECT_EDIT + "/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        SubjectEditScreen(container, subjectId = id)
                    }
                }
            }
        }
    }
}

object Routes {
    const val DASHBOARD = "dashboard"
    const val TIMER = "timer"
    const val RECORDS = "records"
    const val REVIEW = "review"
    const val GAME_TASKS = "game_tasks"
    const val GAME_WALLET = "game_wallet"
    const val GAME_SHOP = "game_shop"
    const val SETTINGS = "settings"
    const val SESSION_DETAIL = "session"
    const val SUBJECT_EDIT = "subject_edit"
    const val ACHIEVEMENTS = "achievements"
}
