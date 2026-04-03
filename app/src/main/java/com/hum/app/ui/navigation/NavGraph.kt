package com.hum.app.ui.navigation

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.hum.app.ui.addexpense.AddExpenseSheet
import com.hum.app.ui.family.FamilyScreen
import com.hum.app.ui.familysetup.FamilySetupScreen
import com.hum.app.ui.home.HomeScreen
import com.hum.app.ui.insights.InsightsScreen
import com.hum.app.ui.login.LoginScreen
import com.hum.app.ui.recurring.RecurringScreen
import com.hum.app.ui.theme.Amber40
import com.hum.app.ui.theme.Teal40

object Routes {
    const val LOGIN = "login"
    const val FAMILY_SETUP = "family_setup"
    const val MAIN = "main"
    const val HOME = "home"
    const val RECURRING = "recurring"
    const val INSIGHTS = "insights"
    const val FAMILY = "family"
}

data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Routes.HOME, "Home", Icons.Default.Home),
    BottomNavItem(Routes.RECURRING, "Recurring", Icons.Default.Repeat),
    BottomNavItem(Routes.INSIGHTS, "Insights", Icons.Default.Analytics),
    BottomNavItem(Routes.FAMILY, "Family", Icons.Default.Groups)
)

@Composable
fun HumNavGraph(startRoute: String = Routes.LOGIN) {
    val rootNavController = rememberNavController()

    NavHost(navController = rootNavController, startDestination = startRoute) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onSignedIn = { hasFamilyId ->
                    val dest = if (hasFamilyId) Routes.MAIN else Routes.FAMILY_SETUP
                    rootNavController.navigate(dest) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.FAMILY_SETUP) {
            FamilySetupScreen(
                onFamilyReady = {
                    rootNavController.navigate(Routes.MAIN) {
                        popUpTo(Routes.FAMILY_SETUP) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.MAIN) {
            MainScreen(
                onSignedOut = {
                    rootNavController.navigate(Routes.LOGIN) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
fun MainScreen(onSignedOut: () -> Unit) {
    val navController = rememberNavController()
    var showAddExpense by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            GradientFab(onClick = { showAddExpense = true })
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                bottomNavItems.forEach { item ->
                    NavigationBarItem(
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) },
                        selected = currentRoute == item.route,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.HOME) {
                HomeScreen()
            }
            composable(Routes.RECURRING) {
                RecurringScreen()
            }
            composable(Routes.INSIGHTS) {
                InsightsScreen()
            }
            composable(Routes.FAMILY) {
                FamilyScreen(onLeftFamily = onSignedOut)
            }
        }

        if (showAddExpense) {
            AddExpenseSheet(onDismiss = { showAddExpense = false })
        }
    }
}

@Composable
private fun GradientFab(onClick: () -> Unit) {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.88f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "fab_scale"
    )
    val fabGradient = Brush.linearGradient(
        colors = listOf(Teal40, Color(0xFF00ACC1), Amber40)
    )

    Box(
        modifier = Modifier
            .size(64.dp)
            .scale(scale)
            .shadow(
                elevation = if (pressed) 4.dp else 10.dp,
                shape = CircleShape,
                ambientColor = Teal40.copy(alpha = 0.4f),
                spotColor = Teal40.copy(alpha = 0.5f)
            )
            .background(brush = fabGradient, shape = CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        pressed = true
                        tryAwaitRelease()
                        pressed = false
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Add Expense",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}
