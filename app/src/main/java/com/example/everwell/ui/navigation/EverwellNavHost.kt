
package com.example.everwell.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.everwell.ui.organ.HeartScreen

private data class NavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
)

private val navItems = listOf(
    NavItem("today", "今天", Icons.Filled.Home),
    NavItem("explore", "身体探索", Icons.Filled.Favorite),
    NavItem("profile", "我的", Icons.Filled.Person)
)

@Composable
fun EverwellNavHost() {

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        containerColor = Color(0xFFF7F7F9),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(76.dp),
                    color = Color(0xFF6750A4),
                    shape = RoundedCornerShape(38.dp),
                    shadowElevation = 10.dp
                ) {

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        navItems.forEach { item ->

                            val selected = currentRoute == item.route

                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        color = if (selected)
                                            Color(0xFF806BC0)
                                        else
                                            Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        navController.navigate(item.route) {
                                            popUpTo(
                                                navController.graph
                                                    .findStartDestination().id
                                            ) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                    tint = if (selected)
                                        Color.White
                                    else
                                        Color(0xFFD8CBEF),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->

        NavHost(
            navController = navController,
            startDestination = "today",
            modifier = Modifier.padding(innerPadding)
        ) {

            composable("today") {
                PlaceholderScreen("今天")
            }

            composable("explore") {
                HeartScreen()
            }

            composable("profile") {
                PlaceholderScreen("我的")
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(title: String) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F7F9)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            color = Color(0xFF6750A4)
        )
    }
}
