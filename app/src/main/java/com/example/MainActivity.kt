package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.dialogs.AddDrawDialog
import com.example.ui.dialogs.AddSourceDialog
import com.example.ui.dialogs.ImportReportDialog
import com.example.ui.dialogs.ManualImportDialog
import com.example.ui.screens.CombinationsScreen
import com.example.ui.screens.DrawsScreen
import com.example.ui.screens.NapScreen
import com.example.ui.screens.SourcesScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.screens.TrackingScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.LotoViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: LotoViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                val snackbarMessage by viewModel.snackbarMessage.collectAsStateWithLifecycle()

                LaunchedEffect(snackbarMessage) {
                    snackbarMessage?.let {
                        snackbarHostState.showSnackbar(it)
                        viewModel.clearSnackbar()
                    }
                }

                val sources by viewModel.sources.collectAsStateWithLifecycle()
                val selectedSourceId by viewModel.selectedSourceId.collectAsStateWithLifecycle()
                val draws by viewModel.draws.collectAsStateWithLifecycle()
                val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
                val numberStats by viewModel.numberStats.collectAsStateWithLifecycle()
                val topPairs by viewModel.topPairs.collectAsStateWithLifecycle()
                val topTrios by viewModel.topTrios.collectAsStateWithLifecycle()
                val chiSquareResult by viewModel.chiSquareResult.collectAsStateWithLifecycle()
                val generatedCombinations by viewModel.generatedCombinations.collectAsStateWithLifecycle()
                val weightRecent by viewModel.weightRecent.collectAsStateWithLifecycle()
                val weightGlobal by viewModel.weightGlobal.collectAsStateWithLifecycle()
                val weightGap by viewModel.weightGap.collectAsStateWithLifecycle()
                val selectedNumberForCurve by viewModel.selectedNumberForCurve.collectAsStateWithLifecycle()
                val allTrackedCombinations by viewModel.allTrackedCombinations.collectAsStateWithLifecycle()
                val trackingSummary by viewModel.trackingSummary.collectAsStateWithLifecycle()

                val dailyNapPackage by viewModel.dailyNapPackage.collectAsStateWithLifecycle()
                val showManualImport by viewModel.showManualImportDialog.collectAsStateWithLifecycle()
                val showAddDraw by viewModel.showAddDrawDialog.collectAsStateWithLifecycle()
                val showAddSource by viewModel.showAddSourceDialog.collectAsStateWithLifecycle()
                val importReport by viewModel.importReport.collectAsStateWithLifecycle()
                val editingSource by viewModel.editingSource.collectAsStateWithLifecycle()

                var currentTab by remember { mutableIntStateOf(0) } // 0: Résultats, 1: NAP 1-2-3, 2: Statistiques, 3: Suivi NAP, 4: Sources
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                val currentSource = sources.find { it.id == selectedSourceId }

                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        ModalDrawerSheet(
                            modifier = Modifier.width(320.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .padding(horizontal = 14.dp, vertical = 16.dp)
                            ) {
                                // En-tête du tiroir
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(bottom = 14.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .background(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = RoundedCornerShape(12.dp)
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Default.Casino,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Loto-Proba",
                                            fontSize = 19.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "🗄️ Tiroir de navigation & Outils",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = "Pays actif : ${currentSource?.name ?: "Niger"}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Text(
                                    text = "TIROIRS PRINCIPAUX",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = null) },
                                    label = { Text("Résultats & Dernier Tirage", fontWeight = FontWeight.SemiBold) },
                                    badge = {
                                        Text(
                                            "${draws.size}",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    },
                                    selected = currentTab == 0,
                                    onClick = {
                                        currentTab = 0
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Star, contentDescription = null) },
                                    label = { Text("Pronostics NAP (N1, N2, N3)", fontWeight = FontWeight.SemiBold) },
                                    badge = { Text("PERM 5", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary) },
                                    selected = currentTab == 1,
                                    onClick = {
                                        currentTab = 1
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Analytics, contentDescription = null) },
                                    label = { Text("Statistiques & Fréquences", fontWeight = FontWeight.SemiBold) },
                                    badge = { Text("0..90", fontSize = 10.sp) },
                                    selected = currentTab == 2,
                                    onClick = {
                                        currentTab = 2
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.FactCheck, contentDescription = null) },
                                    label = { Text("Suivi des Gains & Bilan", fontWeight = FontWeight.SemiBold) },
                                    selected = currentTab == 3,
                                    onClick = {
                                        currentTab = 3
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Public, contentDescription = null) },
                                    label = { Text("Gestion Pays & Liens URL", fontWeight = FontWeight.SemiBold) },
                                    badge = { Text("Sources", fontSize = 10.sp) },
                                    selected = currentTab == 4,
                                    onClick = {
                                        currentTab = 4
                                        coroutineScope.launch { drawerState.close() }
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                                Text(
                                    text = "ACTIONS RAPIDES & BASE ROOM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                                    label = { Text("Importer (Texte / CSV)") },
                                    selected = false,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        viewModel.showManualImportDialog.value = true
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Download, contentDescription = null) },
                                    label = { Text("Exporter la base Room (CSV)") },
                                    selected = false,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        val csv = viewModel.exportDrawsCsv()
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("Loto-Proba Export", csv))
                                        Toast.makeText(context, "${draws.size} tirages copiés en CSV !", Toast.LENGTH_LONG).show()
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                                    label = { Text("Ajouter un tirage manuellement") },
                                    selected = false,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        viewModel.showAddDrawDialog.value = true
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )

                                NavigationDrawerItem(
                                    icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                                    label = { Text("Actualiser depuis le site") },
                                    selected = false,
                                    onClick = {
                                        coroutineScope.launch { drawerState.close() }
                                        viewModel.refreshCurrentSourceWeb()
                                    },
                                    modifier = Modifier.padding(vertical = 2.dp)
                                )
                            }
                        }
                    }
                ) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarHostState) },
                        topBar = {
                            TopAppBar(
                                navigationIcon = {
                                    IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                        Icon(
                                            Icons.Default.Menu,
                                            contentDescription = "Ouvrir le tiroir de navigation",
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                },
                                title = {
                                    Column {
                                        Text(
                                            text = "Loto-Proba",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 17.5.sp
                                        )
                                        Text(
                                            text = when(currentTab) {
                                                0 -> "Tiroir : Résultats & Tirages"
                                                1 -> "Tiroir : Pronostics NAP 1-2-3"
                                                2 -> "Tiroir : Statistiques & Fréquences"
                                                3 -> "Tiroir : Suivi des Gains"
                                                else -> "Tiroir : Gestion des Pays"
                                            },
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                actions = {
                                    if (isRefreshing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    } else {
                                        IconButton(onClick = { viewModel.refreshCurrentSourceWeb() }) {
                                            Icon(Icons.Default.Refresh, contentDescription = "Actualiser les tirages", tint = MaterialTheme.colorScheme.primary)
                                        }
                                    }

                                    IconButton(onClick = { currentTab = 4 }) {
                                        Icon(
                                            Icons.Default.Public,
                                            contentDescription = "Gérer les sources / pays",
                                            tint = if (currentTab == 4) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar {
                            NavigationBarItem(
                                selected = currentTab == 0,
                                onClick = { currentTab = 0 },
                                icon = { Icon(Icons.Default.FormatListNumbered, contentDescription = "Résultats") },
                                label = { Text("Résultats", fontSize = 11.sp, fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) }
                            )
                            NavigationBarItem(
                                selected = currentTab == 1,
                                onClick = { currentTab = 1 },
                                icon = { Icon(Icons.Default.Star, contentDescription = "Pronostics NAP") },
                                label = { Text("NAP 1-2-3", fontSize = 11.sp, fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) }
                            )
                            NavigationBarItem(
                                selected = currentTab == 2,
                                onClick = { currentTab = 2 },
                                icon = { Icon(Icons.Default.Analytics, contentDescription = "Statistiques") },
                                label = { Text("Statistiques", fontSize = 11.sp, fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) }
                            )
                            NavigationBarItem(
                                selected = currentTab == 3,
                                onClick = { currentTab = 3 },
                                icon = { Icon(Icons.Default.FactCheck, contentDescription = "Suivi NAP") },
                                label = { Text("Suivi NAP", fontSize = 11.sp, fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal) }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            0 -> DrawsScreen(
                                sources = sources,
                                selectedSourceId = selectedSourceId,
                                draws = draws,
                                isRefreshing = isRefreshing,
                                onSelectSource = { viewModel.selectSource(it) },
                                onRefreshWeb = { viewModel.refreshCurrentSourceWeb() },
                                onOpenManualImport = { viewModel.showManualImportDialog.value = true },
                                onOpenAddDraw = { viewModel.showAddDrawDialog.value = true },
                                onOpenAddSource = {
                                    viewModel.editingSource.value = null
                                    viewModel.showAddSourceDialog.value = true
                                },
                                onEditCurrentSource = { source ->
                                    viewModel.editingSource.value = source
                                    viewModel.showAddSourceDialog.value = true
                                },
                                onExportCsv = { viewModel.exportDrawsCsv() },
                                onDeleteDraw = { viewModel.deleteDraw(it) }
                            )
                            1 -> NapScreen(
                                napPackage = dailyNapPackage,
                                onSaveForTracking = { viewModel.saveTodayNapPropositions() }
                            )
                            2 -> StatsScreen(
                                stats = numberStats,
                                draws = draws,
                                topPairs = topPairs,
                                topTrios = topTrios,
                                chiSquareResult = chiSquareResult,
                                selectedNumberForCurve = selectedNumberForCurve,
                                onSelectNumberForCurve = { viewModel.selectNumberForCurve(it) }
                            )
                            3 -> TrackingScreen(
                                trackedCombinations = allTrackedCombinations,
                                summary = trackingSummary
                            )
                            4 -> SourcesScreen(
                                sources = sources,
                                selectedSourceId = selectedSourceId,
                                onSelectSource = { viewModel.selectSource(it) },
                                onAddSource = {
                                    viewModel.editingSource.value = null
                                    viewModel.showAddSourceDialog.value = true
                                },
                                onEditSource = { source ->
                                    viewModel.editingSource.value = source
                                    viewModel.showAddSourceDialog.value = true
                                },
                                onDeleteSource = { viewModel.deleteSource(it) },
                                onRefreshSource = { viewModel.refreshCurrentSourceWeb() },
                                onResetSampleData = { viewModel.resetSampleData() }
                            )
                        }
                    }

                    // Dialogs
                    if (showManualImport) {
                        ManualImportDialog(
                            onDismiss = { viewModel.showManualImportDialog.value = false },
                            onImport = { text -> viewModel.importManualText(text) }
                        )
                    }

                    if (showAddDraw) {
                        AddDrawDialog(
                            onDismiss = { viewModel.showAddDrawDialog.value = false },
                            onConfirm = { date, n1, n2, n3, n4, n5, name ->
                                viewModel.addSingleDraw(date, n1, n2, n3, n4, n5, name)
                            }
                        )
                    }

                    if (showAddSource) {
                        AddSourceDialog(
                            sourceToEdit = editingSource,
                            onDismiss = {
                                viewModel.showAddSourceDialog.value = false
                                viewModel.editingSource.value = null
                            },
                            onSave = { name, country, url, preset ->
                                viewModel.saveSource(name, country, url, preset)
                            }
                        )
                    }

                    importReport?.let { report ->
                        ImportReportDialog(
                            report = report,
                            onDismiss = { viewModel.clearImportReport() }
                        )
                    }
                }
            }
        }
    }
}
}
