package com.revzion.siitglobe

import androidx.compose.runtime.*
import com.revzion.siitglobe.data.api.AuthApi
import com.revzion.siitglobe.data.api.SiitApi
import com.revzion.siitglobe.data.api.createHttpClient
import com.revzion.siitglobe.data.repository.AuthRepository
import com.revzion.siitglobe.data.repository.SiitRepository
import com.revzion.siitglobe.data.storage.TokenStorage
import com.revzion.siitglobe.presentation.auth.LoginScreen
import com.revzion.siitglobe.presentation.main.MainScreen
import com.revzion.siitglobe.presentation.theme.SiitTheme
import com.revzion.siitglobe.presentation.theme.ThemeMode
import com.revzion.siitglobe.viewmodel.AuthViewModel
import com.revzion.siitglobe.viewmodel.FormsViewModel
import com.revzion.siitglobe.viewmodel.SiitViewModel

@Composable
fun App() {
    val storage = remember { TokenStorage() }
    val httpClient = remember { createHttpClient() }
    val authApi = remember { AuthApi(httpClient) }
    val siitApi = remember { SiitApi(httpClient) }
    val authRepository = remember { AuthRepository(authApi, storage) }
    val siitRepository = remember { SiitRepository(siitApi, storage) }

    var themeMode by remember { mutableStateOf(ThemeMode.SYSTEM) }
    var isLoggedIn by remember { mutableStateOf(authRepository.isLoggedIn()) }
    var loggedInUserName by remember { mutableStateOf("Admin") }

    SiitTheme(themeMode = themeMode) {
        if (isLoggedIn) {
            val siitViewModel = remember { SiitViewModel(siitRepository) }
            val formsViewModel = remember { FormsViewModel() }
            MainScreen(
                userName = loggedInUserName,
                siitViewModel = siitViewModel,
                formsViewModel = formsViewModel,
                onLogout = {
                    isLoggedIn = false
                    loggedInUserName = "Admin"
                }
            )
        } else {
            val viewModel = remember { AuthViewModel(authRepository) }
            val state by viewModel.state.collectAsState()

            LaunchedEffect(state.isSuccess) {
                if (state.isSuccess) {
                    loggedInUserName = state.user?.name ?: "Admin"
                    isLoggedIn = true
                }
            }

            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    loggedInUserName = state.user?.name ?: "Admin"
                    isLoggedIn = true
                },
                currentTheme = themeMode,
                onThemeChange = { themeMode = it }
            )
        }
    }
}
