package com.happyfamliy.qbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.happyfamliy.qbot.ui.chat.ChatScreen
import com.happyfamliy.qbot.ui.chat.ChatViewModel
import com.happyfamliy.qbot.ui.memory.MemoryScreen
import com.happyfamliy.qbot.ui.memory.MemoryViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                MainApp()
            }
        }
    }
}

@Composable
fun MainApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.padding(16.dp))
                NavigationDrawerItem(
                    label = { Text("Chat") },
                    selected = false, // Could be improved with current backstack entry
                    onClick = {
                        navController.navigate("chat") {
                            popUpTo("chat") { inclusive = true }
                        }
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Chat, contentDescription = null) }
                )
                NavigationDrawerItem(
                    label = { Text("Memory") },
                    selected = false,
                    onClick = {
                        navController.navigate("memory")
                        scope.launch { drawerState.close() }
                    },
                    icon = { Icon(Icons.Default.Memory, contentDescription = null) }
                )
            }
        }
    ) {
        NavHost(navController = navController, startDestination = "chat") {
            composable("chat") {
                val chatViewModel: ChatViewModel = hiltViewModel()
                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("QBot Chat") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        ChatScreen(viewModel = chatViewModel)
                    }
                }
            }
            composable("memory") {
                val memoryViewModel: MemoryViewModel = hiltViewModel()
                Scaffold(
                    topBar = {
                        @OptIn(ExperimentalMaterial3Api::class)
                        TopAppBar(
                            title = { Text("Memory Dashboard") },
                            navigationIcon = {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu")
                                }
                            }
                        )
                    }
                ) { padding ->
                    Surface(modifier = Modifier.padding(padding)) {
                        MemoryScreen(viewModel = memoryViewModel)
                    }
                }
            }
        }
    }
}
