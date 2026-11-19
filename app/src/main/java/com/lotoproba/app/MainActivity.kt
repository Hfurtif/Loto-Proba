package com.lotoproba.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.lotoproba.app.data.model.LotoDraw
import com.lotoproba.app.data.model.LotoSource
import com.lotoproba.app.ui.LotoViewModel
import com.lotoproba.app.ui.components.AppIcons
import com.lotoproba.app.ui.dialogs.AddDrawDialog
import com.lotoproba.app.ui.dialogs.AddSourceDialog
import com.lotoproba.app.ui.dialogs.ImportReportDialog
import com.lotoproba.app.ui.dialogs.ManualImportDialog
import com.lotoproba.app.ui.screens.AiScreen
import com.lotoproba.app.ui.screens.CombinationsScreen
import com.lotoproba.app.ui.screens.DrawsScreen
import com.lotoproba.app.ui.screens.NapScreen
import com.lotoproba.app.ui.screens.SourcesScreen
import com.lotoproba.app.ui.screens.StatsScreen
import com.lotoproba.app.ui.screens.TrackingScreen
import com.lotoproba.app.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

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
                val numberFrequencies01To90 by viewModel.numberFrequencies01To90.collectAsStateWithLifecycle()
                val topPairs by viewModel.topPairs.collectAsStateWithLifecycle()
                val topTrios by viewModel.topTrios.collectAsStateWithLifecycle()
                val pairsAndTriosAnalysis by viewModel.pairsAndTriosAnalysis.collectAsStateWithLifecycle()
                val pairTrioLimit by viewModel.pairTrioLimit.collectAsStateWithLifecycle()
                val gapAndColdAnalysis by viewModel.gapAndColdAnalysis.collectAsStateWithLifecycle()
                val recentWindowSize by viewModel.recentWindowSize.collectAsStateWithLifecycle()
                val chiSquareResult by viewModel.chiSquareResult.collectAsStateWithLifecycle()
                val generatedCombinations by viewModel.generatedCombinations.collectAsStateWithLifecycle()
                val weightRecent by viewModel.weightRecent.collectAsStateWithLifecycle()
                val weightGlobal by viewModel.weightGlobal.collectAsStateWithLifecycle()
                val weightGap by viewModel.weightGap.collectAsStateWithLifecycle()
                val selectedNumberForCurve by viewModel.selectedNumberForCurve.collectAsStateWithLifecycle()
                val allTrackedCombinations by viewModel.allTrackedCombinations.collectAsStateWithLifecycle()
                val trackingSummary by viewModel.trackingSummary.collectAsStateWithLifecycle()

                val dailyNapPackage by viewModel.dailyNapPackage.collectAsStateWithLifecycle()
                val userProposedNumbers by viewModel.userProposedNumbers.collectAsStateWithLifecycle()
                val userNumbersAnalysis by viewModel.userNumbersAnalysis.collectAsStateWithLifecycle()
                val userNumbersAiReport by viewModel.userNumbersAiReport.collectAsStateWithLifecycle()
                val isAnalyzingUserNumbersWithAi by viewModel.isAnalyzingUserNumbersWithAi.collectAsStateWithLifecycle()
                val showManualImport by viewModel.showManualImportDialog.collectAsStateWithLifecycle()
                val showAddDraw by viewModel.showAddDrawDialog.collectAsStateWithLifecycle()
                val showAddSource by viewModel.showAddSourceDialog.collectAsStateWithLifecycle()
                val importReport by viewModel.importReport.collectAsStateWithLifecycle()
                val editingSource by viewModel.editingSource.collectAsStateWithLifecycle()

                val customApiKey by viewModel.customApiKey.collectAsStateWithLifecycle()
                val isApiKeyConfigured by viewModel.isApiKeyConfigured.collectAsStateWithLifecycle()
                val isAiGenerating by viewModel.isAiGenerating.collectAsStateWithLifecycle()
                val isAiEvaluating by viewModel.isAiEvaluating.collectAsStateWithLifecycle()
                val aiEvaluations by viewModel.aiEvaluations.collectAsStateWithLifecycle()
                val latestAiPrediction by viewModel.latestAiPrediction.collectAsStateWithLifecycle()

                // 0: Résultats, 1: Synthèse NAP, 2: Statistiques & χ², 3: Suivi statistique, 4: Sources, 5: Analyste IA, 6: Combinaisons pondérées
                var currentTab by rememberSaveable { mutableIntStateOf(0) }

                // État indépendant du tiroir gauche (Navigation uniquement)
                val leftDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                // État indépendant du panneau droit (Outils rapides & Base Room uniquement)
                var isRightPanelOpen by rememberSaveable { mutableStateOf(false) }

                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current
                val currentSource = sources.find { it.id == selectedSourceId }

                // Exclusion mutuelle : un seul panneau ouvert à la fois
                LaunchedEffect(leftDrawerState.isOpen) {
                    if (leftDrawerState.isOpen && isRightPanelOpen) {
                        isRightPanelOpen = false
                    }
                }

                val openLeftNavigationDrawer: () -> Unit = {
                    isRightPanelOpen = false
                    coroutineScope.launch { leftDrawerState.open() }
                }

                val openRightToolsPanel: () -> Unit = {
                    coroutineScope.launch {
                        if (leftDrawerState.isOpen) {
                            leftDrawerState.close()
                        }
                        isRightPanelOpen = true
                    }
                }

                // Bouton Retour ferme le tiroir/panneau ouvert avant de quitter l'écran
                BackHandler(enabled = leftDrawerState.isOpen || isRightPanelOpen) {
                    if (leftDrawerState.isOpen) {
                        coroutineScope.launch { leftDrawerState.close() }
                    } else if (isRightPanelOpen) {
                        isRightPanelOpen = false
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    // TIROIR GAUCHE M3 : Navigation entre les écrans UNIQUEMENT
                    // gesturesEnabled = leftDrawerState.isOpen empêche toute ouverture involontaire lors du défilement des listes.
                    ModalNavigationDrawer(
                        drawerState = leftDrawerState,
                        gesturesEnabled = leftDrawerState.isOpen,
                        drawerContent = {
                            LeftNavigationDrawerContent(
                                currentTab = currentTab,
                                currentSourceName = currentSource?.name ?: "Niger Loto (CSV intégré)",
                                drawsCount = draws.size,
                                trackedCount = allTrackedCombinations.size,
                                sourcesCount = sources.size,
                                onSelectTab = { tab ->
                                    currentTab = tab
                                    coroutineScope.launch { leftDrawerState.close() }
                                }
                            )
                        }
                    ) {
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            snackbarHost = { SnackbarHost(snackbarHostState) },
                            topBar = {
                                TopAppBar(
                                    navigationIcon = {
                                        IconButton(
                                            onClick = openLeftNavigationDrawer,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("open_left_drawer_button")
                                        ) {
                                            Icon(
                                                Icons.Default.Menu,
                                                contentDescription = "Ouvrir le menu de navigation",
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    title = {
                                        Column {
                                            Text(
                                                text = "LotoProba",
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 17.5.sp
                                            )
                                            Text(
                                                text = when (currentTab) {
                                                    0 -> "Résultats & Tirages"
                                                    1 -> "Synthèse NAP 1-2-3"
                                                    2 -> "Statistiques & χ²"
                                                    3 -> "Suivi statistique"
                                                    4 -> "Sources & CSV local"
                                                    5 -> "Analyste IA"
                                                    else -> "Combinaisons pondérées"
                                                },
                                                fontSize = 11.5.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    actions = {
                                        if (isRefreshing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .padding(horizontal = 12.dp)
                                                    .size(20.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        } else {
                                            IconButton(
                                                onClick = { viewModel.refreshCurrentSourceWeb() },
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .testTag("topbar_refresh_button")
                                            ) {
                                                Icon(
                                                    Icons.Default.Refresh,
                                                    contentDescription = "Actualiser la source active",
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = { currentTab = 5 },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("topbar_ai_button")
                                        ) {
                                            Icon(
                                                AppIcons.AutoAwesome,
                                                contentDescription = "Analyste IA",
                                                tint = if (currentTab == 5) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
                                            )
                                        }

                                        IconButton(
                                            onClick = openRightToolsPanel,
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("open_right_panel_button")
                                        ) {
                                            Icon(
                                                AppIcons.Tune,
                                                contentDescription = "Ouvrir le panneau d'outils et base Room",
                                                tint = if (isRightPanelOpen) {
                                                    MaterialTheme.colorScheme.primary
                                                } else {
                                                    MaterialTheme.colorScheme.onSurface
                                                }
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
                                        icon = {
                                            Icon(
                                                AppIcons.FormatListNumbered,
                                                contentDescription = "Résultats"
                                            )
                                        },
                                        label = {
                                            Text(
                                                "Résultats",
                                                fontSize = 11.sp,
                                                fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 1,
                                        onClick = { currentTab = 1 },
                                        icon = {
                                            Icon(
                                                Icons.Default.Star,
                                                contentDescription = "NAP 1-2-3"
                                            )
                                        },
                                        label = {
                                            Text(
                                                "NAP 1-2-3",
                                                fontSize = 11.sp,
                                                fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 2,
                                        onClick = { currentTab = 2 },
                                        icon = {
                                            Icon(
                                                AppIcons.Analytics,
                                                contentDescription = "Statistiques"
                                            )
                                        },
                                        label = {
                                            Text(
                                                "Statistiques",
                                                fontSize = 11.sp,
                                                fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                    NavigationBarItem(
                                        selected = currentTab == 3,
                                        onClick = { currentTab = 3 },
                                        icon = {
                                            Icon(
                                                AppIcons.FactCheck,
                                                contentDescription = "Suivi"
                                            )
                                        },
                                        label = {
                                            Text(
                                                "Suivi",
                                                fontSize = 11.sp,
                                                fontWeight = if (currentTab == 3) FontWeight.Bold else FontWeight.Normal,
                                                maxLines = 1
                                            )
                                        }
                                    )
                                }
                            }
                        ) { innerPadding ->
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(innerPadding),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .widthIn(max = 840.dp)
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
                                            onSaveForTracking = { viewModel.saveTodayNapPropositions() },
                                            userProposedNumbers = userProposedNumbers,
                                            userNumbersAnalysis = userNumbersAnalysis,
                                            userNumbersAiReport = userNumbersAiReport,
                                            isAnalyzingUserNumbersWithAi = isAnalyzingUserNumbersWithAi,
                                            onSubmitUserNumbers = { viewModel.submitUserNumbersInput(it) },
                                            onRemoveUserNumber = { viewModel.removeUserProposedNumber(it) },
                                            onClearUserNumbers = { viewModel.clearUserProposedNumbers() },
                                            onRequestAiAnalysisForUserNumbers = { viewModel.requestAiAnalysisForUserNumbers() },
                                            onSaveUserNumbersForTracking = { viewModel.saveUserProposedNumbersForTracking() }
                                        )

                                        2 -> StatsScreen(
                                            stats = numberStats,
                                            draws = draws,
                                            topPairs = topPairs,
                                            topTrios = topTrios,
                                            chiSquareResult = chiSquareResult,
                                            selectedNumberForCurve = selectedNumberForCurve,
                                            onSelectNumberForCurve = { viewModel.selectNumberForCurve(it) },
                                            frequencies01To90 = numberFrequencies01To90,
                                            onRecalculateFrequencies01To90 = { viewModel.calculateNumberFrequencies01To90() },
                                            pairsAndTriosAnalysis = pairsAndTriosAnalysis,
                                            pairTrioLimit = pairTrioLimit,
                                            onSelectPairTrioLimit = { viewModel.updatePairTrioLimit(it) },
                                            onRecalculatePairsAndTrios = { viewModel.analyzePairsAndTriosFromHistory() },
                                            gapAndColdAnalysis = gapAndColdAnalysis,
                                            recentWindowSize = recentWindowSize,
                                            onSelectRecentWindowSize = { viewModel.updateRecentWindowSize(it) },
                                            onRecalculateGapsAndCold = { viewModel.calculateGapsAndLeastRecentFromHistory() },
                                            onRecalculateChiSquare = { viewModel.runChiSquareEquityTestFromHistory() }
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

                                        5 -> AiScreen(
                                            customApiKey = customApiKey,
                                            isKeyConfigured = isApiKeyConfigured,
                                            isAiGenerating = isAiGenerating,
                                            isAiEvaluating = isAiEvaluating,
                                            latestPrediction = latestAiPrediction,
                                            aiEvaluations = aiEvaluations,
                                            latestDraw = draws.firstOrNull(),
                                            onSaveApiKey = { viewModel.saveCustomApiKey(it) },
                                            onClearApiKey = { viewModel.clearCustomApiKey() },
                                            onGeneratePrediction = { viewModel.generateAiPrediction() },
                                            onEvaluateLatestDraw = { viewModel.evaluateLatestDrawWithAi() },
                                            onDeleteEvaluation = { viewModel.deleteAiEvaluation(it) }
                                        )

                                        6 -> CombinationsScreen(
                                            combinations = generatedCombinations,
                                            weightRecent = weightRecent,
                                            weightGlobal = weightGlobal,
                                            weightGap = weightGap,
                                            onWeightsChanged = { r, g, gap -> viewModel.updateWeights(r, g, gap) },
                                            onSaveForTracking = { viewModel.saveTodayPropositions() }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // PANNEAU DROIT INDÉPENDANT : Outils rapides & Base Room UNIQUEMENT
                    RightToolsOverlayPanel(
                        isOpen = isRightPanelOpen,
                        sources = sources,
                        selectedSourceId = selectedSourceId,
                        latestDraw = draws.firstOrNull(),
                        drawsCount = draws.size,
                        onDismiss = { isRightPanelOpen = false },
                        onSelectSource = { viewModel.selectSource(it) },
                        onRefreshWeb = {
                            isRightPanelOpen = false
                            viewModel.refreshCurrentSourceWeb()
                        },
                        onOpenManualImport = {
                            isRightPanelOpen = false
                            viewModel.showManualImportDialog.value = true
                        },
                        onExportCsv = {
                            isRightPanelOpen = false
                            val csv = viewModel.exportDrawsCsv()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("LotoProba Export", csv))
                            Toast.makeText(context, "${draws.size} tirages copiés en CSV.", Toast.LENGTH_LONG).show()
                        },
                        onOpenAddDraw = {
                            isRightPanelOpen = false
                            viewModel.showAddDrawDialog.value = true
                        },
                        onCopyLatestDraw = { draw ->
                            val text = "${draw.dateString} : ${draw.getNumbersList().joinToString(" - ")}"
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Dernier Tirage", text))
                            Toast.makeText(context, "Dernier tirage copié.", Toast.LENGTH_SHORT).show()
                            isRightPanelOpen = false
                        },
                        onManageSources = {
                            isRightPanelOpen = false
                            currentTab = 4
                        }
                    )

                    // Boîtes de dialogue modales
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

/**
 * Contenu exclusif du TIROIR GAUCHE : Navigation entre les écrans uniquement.
 */
@Composable
private fun LeftNavigationDrawerContent(
    currentTab: Int,
    currentSourceName: String,
    drawsCount: Int,
    trackedCount: Int,
    sourcesCount: Int,
    onSelectTab: (Int) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.84f)
            .widthIn(max = 310.dp)
            .testTag("left_navigation_drawer_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 16.dp)
        ) {
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
                        AppIcons.Casino,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "LotoProba",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Menu de navigation",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Source : $currentSourceName",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(bottom = 10.dp))

            Text(
                text = "ÉCRANS DE L'APPLICATION",
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .semantics { heading() }
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.FormatListNumbered, contentDescription = null) },
                label = { Text("Résultats & Tirages", fontWeight = FontWeight.SemiBold) },
                badge = {
                    Text(
                        "$drawsCount",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                selected = currentTab == 0,
                onClick = { onSelectTab(0) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_draws")
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Star, contentDescription = null) },
                label = { Text("Synthèse NAP (N1, N2, N3)", fontWeight = FontWeight.SemiBold) },
                badge = { Text("PERM 5", fontSize = 10.sp, color = MaterialTheme.colorScheme.secondary) },
                selected = currentTab == 1,
                onClick = { onSelectTab(1) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_nap")
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.Analytics, contentDescription = null) },
                label = { Text("Statistiques & Fréquences", fontWeight = FontWeight.SemiBold) },
                badge = { Text("0..90", fontSize = 10.sp) },
                selected = currentTab == 2,
                onClick = { onSelectTab(2) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_stats")
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.Tune, contentDescription = null) },
                label = { Text("Combinaisons pondérées", fontWeight = FontWeight.SemiBold) },
                badge = { Text("5 grilles", fontSize = 10.sp) },
                selected = currentTab == 6,
                onClick = { onSelectTab(6) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_combinations")
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.FactCheck, contentDescription = null) },
                label = { Text("Suivi statistique & Bilan", fontWeight = FontWeight.SemiBold) },
                badge = { Text("$trackedCount", fontSize = 10.sp) },
                selected = currentTab == 3,
                onClick = { onSelectTab(3) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_tracking")
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.AutoAwesome, contentDescription = null) },
                label = { Text("Analyste IA (Synthèses)", fontWeight = FontWeight.SemiBold) },
                badge = {
                    Text(
                        "Gemini",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                },
                selected = currentTab == 5,
                onClick = { onSelectTab(5) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_ai")
            )

            NavigationDrawerItem(
                icon = { Icon(AppIcons.Public, contentDescription = null) },
                label = { Text("Sources & CSV local", fontWeight = FontWeight.SemiBold) },
                badge = { Text("$sourcesCount", fontSize = 10.sp) },
                selected = currentTab == 4,
                onClick = { onSelectTab(4) },
                modifier = Modifier
                    .padding(vertical = 2.dp)
                    .testTag("drawer_nav_sources")
            )
        }
    }
}

/**
 * Panneau latéral droit séparé (jamais un second ModalNavigationDrawer imbriqué) :
 * dédié exclusivement aux outils rapides, à la source active et à la base Room.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RightToolsOverlayPanel(
    isOpen: Boolean,
    sources: List<LotoSource>,
    selectedSourceId: String,
    latestDraw: LotoDraw?,
    drawsCount: Int,
    onDismiss: () -> Unit,
    onSelectSource: (String) -> Unit,
    onRefreshWeb: () -> Unit,
    onOpenManualImport: () -> Unit,
    onExportCsv: () -> Unit,
    onOpenAddDraw: () -> Unit,
    onCopyLatestDraw: (LotoDraw) -> Unit,
    onManageSources: () -> Unit
) {
    var cumulativeDragX by remember(isOpen) { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrim semi-transparent cliquable pour fermer le panneau droit
        AnimatedVisibility(
            visible = isOpen,
            enter = fadeIn(tween(220)),
            exit = fadeOut(tween(200))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.42f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss
                    )
                    .testTag("right_panel_scrim")
            )
        }

        // Panneau droit glissant depuis la droite
        AnimatedVisibility(
            visible = isOpen,
            modifier = Modifier.align(Alignment.CenterEnd),
            enter = slideInHorizontally(animationSpec = tween(250)) { fullWidth -> fullWidth },
            exit = slideOutHorizontally(animationSpec = tween(210)) { fullWidth -> fullWidth }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.84f)
                    .widthIn(max = 310.dp)
                    .pointerInput(isOpen) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                cumulativeDragX += dragAmount
                                // Glissement vers la droite pour fermer uniquement quand le panneau est ouvert
                                if (cumulativeDragX > 90f) {
                                    cumulativeDragX = 0f
                                    onDismiss()
                                }
                            },
                            onDragEnd = { cumulativeDragX = 0f },
                            onDragCancel = { cumulativeDragX = 0f }
                        )
                    }
                    .testTag("right_tools_panel"),
                shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(
                            WindowInsets.safeDrawing.only(
                                WindowInsetsSides.Vertical + WindowInsetsSides.End
                            )
                        )
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Outils & Base Room",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "$drawsCount tirage(s) stocké(s) localement",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("close_right_panel_button")
                        ) {
                            Icon(
                                Icons.Default.Close,
                                contentDescription = "Fermer le panneau d'outils"
                            )
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    Text(
                        text = "SOURCE ACTIVE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    if (sources.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sources.forEach { source ->
                                FilterChip(
                                    selected = source.id == selectedSourceId,
                                    onClick = { onSelectSource(source.id) },
                                    label = {
                                        Text(
                                            text = source.name,
                                            fontSize = 11.sp,
                                            maxLines = 1
                                        )
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "ACTIONS RAPIDES ROOM",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Refresh, contentDescription = null) },
                        label = { Text("Actualiser la source active") },
                        selected = false,
                        onClick = onRefreshWeb,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("right_panel_refresh_action")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(AppIcons.UploadFile, contentDescription = null) },
                        label = { Text("Importer (Texte / CSV)") },
                        selected = false,
                        onClick = onOpenManualImport,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("right_panel_import_action")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(AppIcons.Download, contentDescription = null) },
                        label = { Text("Exporter la base Room (CSV)") },
                        selected = false,
                        onClick = onExportCsv,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("right_panel_export_action")
                    )

                    NavigationDrawerItem(
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        label = { Text("Ajouter un tirage manuel") },
                        selected = false,
                        onClick = onOpenAddDraw,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("right_panel_add_draw_action")
                    )

                    if (latestDraw != null) {
                        NavigationDrawerItem(
                            icon = { Icon(AppIcons.ContentCopy, contentDescription = null) },
                            label = { Text("Copier le dernier tirage (${latestDraw.dateString})") },
                            selected = false,
                            onClick = { onCopyLatestDraw(latestDraw) },
                            modifier = Modifier
                                .padding(vertical = 2.dp)
                                .testTag("right_panel_copy_latest_action")
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    NavigationDrawerItem(
                        icon = { Icon(AppIcons.Public, contentDescription = null) },
                        label = { Text("Gérer les sources & liens web") },
                        selected = false,
                        onClick = onManageSources,
                        modifier = Modifier
                            .padding(vertical = 2.dp)
                            .testTag("right_panel_manage_sources_action")
                    )
                }
            }
        }
    }
}
