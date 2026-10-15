package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.HeaderBar
import com.example.ui.components.SidebarNav
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.ElectionViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                PelsusGmimApp()
            }
        }
    }
}

@Composable
fun PelsusGmimApp(
    viewModel: ElectionViewModel = viewModel()
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val snackbarState by viewModel.snackbar.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle back button: if not on Home, back goes to Home
    BackHandler(enabled = currentScreen != AppScreen.BERANDA) {
        viewModel.navigateTo(AppScreen.BERANDA)
    }

    // Trigger snackbar messages
    LaunchedEffect(snackbarState) {
        snackbarState?.let {
            snackbarHostState.showSnackbar(
                message = it.message,
                duration = SnackbarDuration.Short
            )
            viewModel.clearSnackbar()
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = NavyPrimary,
                drawerContentColor = Color.White,
                modifier = Modifier.width(280.dp)
            ) {
                SidebarNav(
                    viewModel = viewModel,
                    onNavigate = { screen ->
                        viewModel.navigateTo(screen)
                        coroutineScope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                HeaderBar(
                    viewModel = viewModel,
                    onMenuToggle = {
                        coroutineScope.launch {
                            if (drawerState.isClosed) drawerState.open() else drawerState.close()
                        }
                    }
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = NavyDark,
                    contentColor = Color.White
                ) {
                    val bottomItems = listOf(
                        Triple(AppScreen.BERANDA, "Beranda", Icons.Default.Dashboard),
                        Triple(AppScreen.PROGRES_PEMILIHAN, "Progres", Icons.Default.PieChart),
                        Triple(AppScreen.DAFTAR_HADIR, "Hadir", Icons.Default.HowToReg),
                        Triple(AppScreen.INPUT_HASIL, "Input Suara", Icons.Default.EditNote),
                        Triple(AppScreen.IMPOR_EXCEL, "Impor Excel", Icons.Default.UploadFile)
                    )

                    bottomItems.forEach { (screen, label, icon) ->
                        val isSelected = currentScreen == screen
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { viewModel.navigateTo(screen) },
                            icon = {
                                Icon(
                                    imageVector = icon,
                                    contentDescription = label,
                                    tint = if (isSelected) DiakenGold else Color(0xFF94A3B8)
                                )
                            },
                            label = {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.White else Color(0xFF94A3B8)
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = NavyMedium
                            )
                        )
                    }
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    Snackbar(
                        snackbarData = data,
                        containerColor = if (snackbarState?.isError == true) DangerRed else NavyDark,
                        contentColor = Color.White
                    )
                }
            },
            contentWindowInsets = WindowInsets.safeDrawing,
            modifier = Modifier.fillMaxSize()
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(BgLight)
            ) {
                when (currentScreen) {
                    AppScreen.BERANDA -> DashboardScreen(viewModel = viewModel)
                    AppScreen.PROGRES_PEMILIHAN, AppScreen.STATUS_PILIH -> ProgressScreen(viewModel = viewModel)
                    AppScreen.DAFTAR_HADIR -> AttendanceScreen(viewModel = viewModel)
                    AppScreen.INPUT_HASIL -> VoteInputScreen(viewModel = viewModel)
                    AppScreen.HASIL_PEMILIHAN -> VoteResultsScreen(viewModel = viewModel)
                    AppScreen.IMPOR_EXCEL -> ExcelImportScreen(viewModel = viewModel)
                    AppScreen.MASTER_DATA, AppScreen.PANITIA, AppScreen.LOKASI_PEMILIHAN -> MasterDataScreen(viewModel = viewModel)
                    AppScreen.LAPORAN_BAP -> ReportsScreen(viewModel = viewModel)
                    AppScreen.PEMILIHAN_BIPRA, AppScreen.PEMILIHAN_BPMJ, AppScreen.PEMILIHAN_LANSIA -> UnderDevScreen(screen = currentScreen, viewModel = viewModel)
                    AppScreen.PENGATURAN -> SettingsScreen(viewModel = viewModel)
                }
            }
        }
    }
}
