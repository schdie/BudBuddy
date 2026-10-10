package com.benegedeniz.budsdynamiceq

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.benegedeniz.budsdynamiceq.di.ServiceLocator
import com.benegedeniz.budsdynamiceq.ui.main.MainScreen
import com.benegedeniz.budsdynamiceq.ui.setup.SetupScreen
import com.benegedeniz.budsdynamiceq.ui.theme.BudsDynamicEQTheme
import com.benegedeniz.budsdynamiceq.util.PermissionManager

val LocalGlobalNavBarBottom = compositionLocalOf { 0.dp }

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            BudsDynamicEQTheme(darkTheme = isSystemInDarkTheme()) {
                var permissionsGranted by remember {
                    mutableStateOf(PermissionManager.hasRequiredPermissions(this@MainActivity))
                }

                LaunchedEffect(permissionsGranted) {
                    if (permissionsGranted) {
                        ServiceLocator.provideBudsController(this@MainActivity).startAutoConnect()
                    }
                }

                val navBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

                CompositionLocalProvider(LocalGlobalNavBarBottom provides navBarBottom) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (permissionsGranted) {
                            MainScreen()
                        } else {
                            SetupScreen(onPermissionsGranted = { permissionsGranted = true })
                        }
                    }
                }
            }
        }
    }
}