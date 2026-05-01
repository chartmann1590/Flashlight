package com.charles.flashlight.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.charles.flashlight.data.SettingsRepository
import com.charles.flashlight.torch.TorchViewModel
import com.charles.flashlight.ui.AboutScreen
import com.charles.flashlight.ui.HomeScreen
import com.charles.flashlight.ui.SettingsScreen

@Composable
fun FlashlightNavHost(
    navController: NavHostController,
    torchViewModel: TorchViewModel,
    settingsRepository: SettingsRepository,
    monetizationActive: Boolean,
    nativeAdUnitId: String,
    bannerAdUnitId: String,
    onTorchToggleForAds: () -> Unit,
    onOpenWebsite: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenCoffee: () -> Unit,
    onOpenScreenLight: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Routes.HOME,
        modifier = modifier
    ) {
        composable(Routes.HOME) {
            HomeScreen(
                torchViewModel = torchViewModel,
                settingsRepository = settingsRepository,
                monetizationActive = monetizationActive,
                nativeAdUnitId = nativeAdUnitId,
                bannerAdUnitId = bannerAdUnitId,
                onTorchToggleForAds = onTorchToggleForAds,
                onNavigateSettings = { navController.navigate(Routes.SETTINGS) },
                onNavigateAbout = { navController.navigate(Routes.ABOUT) },
                onOpenWebsite = onOpenWebsite,
                onOpenPrivacy = onOpenPrivacy,
                onOpenCoffee = onOpenCoffee,
                onOpenScreenLight = onOpenScreenLight
            )
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(
                repository = settingsRepository,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.ABOUT) {
            AboutScreen(
                monetizationActive = monetizationActive,
                nativeAdUnitId = nativeAdUnitId,
                onBack = { navController.popBackStack() },
                onOpenPrivacy = onOpenPrivacy,
                onOpenWebsite = onOpenWebsite,
                onOpenCoffee = onOpenCoffee
            )
        }
    }
}
