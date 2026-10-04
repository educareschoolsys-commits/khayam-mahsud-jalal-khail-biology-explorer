package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.SchoolTopBar
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DocumentPreviewScreen
import com.example.ui.screens.ExcelScreen
import com.example.ui.screens.FeeScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.ResultScreen
import com.example.ui.screens.SavedDocumentsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.StudentScreen
import com.example.ui.screens.TimetableScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.PreviewType
import com.example.viewmodel.SchoolViewModel
import com.example.viewmodel.Screen

class MainActivity : ComponentActivity() {
    private val viewModel: SchoolViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun MainApp(viewModel: SchoolViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val profile by viewModel.schoolProfile.collectAsStateWithLifecycle()
    val gradingRules by viewModel.gradingRules.collectAsStateWithLifecycle()
    val students by viewModel.allStudents.collectAsStateWithLifecycle()
    val feeRecords by viewModel.allFeeRecords.collectAsStateWithLifecycle()
    val results by viewModel.allResults.collectAsStateWithLifecycle()
    val timetableEntries by viewModel.allTimetableEntries.collectAsStateWithLifecycle()
    val photos by viewModel.allPhotos.collectAsStateWithLifecycle()
    val savedDocuments by viewModel.allSavedDocuments.collectAsStateWithLifecycle()
    val stats by viewModel.dashboardStats.collectAsStateWithLifecycle()
    val previewState by viewModel.previewState.collectAsStateWithLifecycle()
    val excelImportRows by viewModel.excelImportRows.collectAsStateWithLifecycle()
    val isImportingExcel by viewModel.isImportingExcel.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    // Filters
    val studentQuery by viewModel.studentSearchQuery.collectAsStateWithLifecycle()
    val studentClass by viewModel.studentClassFilter.collectAsStateWithLifecycle()
    val feeMonth by viewModel.feeMonthFilter.collectAsStateWithLifecycle()
    val feeStatus by viewModel.feeStatusFilter.collectAsStateWithLifecycle()
    val resultClass by viewModel.resultClassFilter.collectAsStateWithLifecycle()
    val resultExam by viewModel.resultExamFilter.collectAsStateWithLifecycle()
    val timetableClass by viewModel.timetableClassFilter.collectAsStateWithLifecycle()
    val photoCategory by viewModel.photoCategoryFilter.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Android back handler
    BackHandler(enabled = currentScreen != Screen.DASHBOARD) {
        viewModel.navigateBack()
    }

    val topBarTitle = when (currentScreen) {
        Screen.DASHBOARD -> "اقراۃ روضۃ العلم پبلک سکول"
        Screen.STUDENTS -> "Student Management / طلباء"
        Screen.FEES -> "Fee Management / فیس شعبہ"
        Screen.TIMETABLE -> "Timetable Management / اوقات کار"
        Screen.RESULTS -> "Result System / امتحانی نتائج"
        Screen.PHOTOS -> "School Photos / تصاویر و لوگو"
        Screen.EXCEL_UPLOAD -> "Excel Import / ایکسل درآمد"
        Screen.SAVED_DOCUMENTS -> "Saved Documents / دستاویزات"
        Screen.PREVIEW -> "Document Preview / پیش نظارہ"
        Screen.SETTINGS -> "School Settings / سیٹنگز"
    }

    val topBarSubtitle = when (currentScreen) {
        Screen.DASHBOARD -> profile?.tagline ?: "معیاری دینی و عصری تعلیم کا عظیم گہوارہ"
        else -> profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول"
    }

    Scaffold(
        topBar = {
            SchoolTopBar(
                title = topBarTitle,
                subtitle = topBarSubtitle,
                canNavigateBack = currentScreen != Screen.DASHBOARD,
                onNavigateBack = { viewModel.navigateBack() },
                profile = profile
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                Screen.DASHBOARD -> {
                    DashboardScreen(
                        profile = profile,
                        stats = stats,
                        onNavigate = { viewModel.navigateTo(it) }
                    )
                }
                Screen.STUDENTS -> {
                    StudentScreen(
                        students = students,
                        searchQuery = studentQuery,
                        onSearchQueryChange = { viewModel.studentSearchQuery.value = it },
                        selectedClass = studentClass,
                        onClassFilterChange = { viewModel.studentClassFilter.value = it },
                        onSaveStudent = { viewModel.saveStudent(it) },
                        onDeleteStudent = { viewModel.deleteStudent(it) },
                        onViewFeesForStudent = { stu ->
                            viewModel.feeMonthFilter.value = "All"
                            viewModel.navigateTo(Screen.FEES)
                        },
                        onViewResultForStudent = { stu ->
                            viewModel.resultClassFilter.value = stu.className
                            val res = results.find { it.studentId == stu.id }
                            if (res != null) {
                                viewModel.previewStudentResult(res)
                            } else {
                                viewModel.navigateTo(Screen.RESULTS)
                            }
                        }
                    )
                }
                Screen.FEES -> {
                    FeeScreen(
                        feeRecords = feeRecords,
                        students = students,
                        selectedMonth = feeMonth,
                        onMonthChange = { viewModel.feeMonthFilter.value = it },
                        selectedStatus = feeStatus,
                        onStatusChange = { viewModel.feeStatusFilter.value = it },
                        onSaveFeeRecord = { sId, sName, fName, cls, sec, roll, month, mFee, aFee, oFee, disc, paid, date, id ->
                            viewModel.saveFeeRecord(sId, sName, fName, cls, sec, roll, month, mFee, aFee, oFee, disc, paid, date, id)
                        },
                        onDeleteFeeRecord = { viewModel.deleteFeeRecord(it) },
                        onPreviewReceipt = { viewModel.previewFeeReceipt(it) }
                    )
                }
                Screen.TIMETABLE -> {
                    TimetableScreen(
                        timetableEntries = timetableEntries,
                        selectedClass = timetableClass,
                        onClassChange = { viewModel.timetableClassFilter.value = it },
                        onSaveEntry = { viewModel.saveTimetableEntry(it) },
                        onDeleteEntry = { viewModel.deleteTimetableEntry(it) },
                        onClearClass = { viewModel.clearTimetableForClass(it) },
                        onPreviewTimetable = { viewModel.previewTimetable(it) },
                        onSavePhoto = { t, c, u -> viewModel.saveSchoolPhoto(t, c, u) }
                    )
                }
                Screen.RESULTS -> {
                    ResultScreen(
                        results = results,
                        students = students,
                        gradingRules = gradingRules,
                        selectedClass = resultClass,
                        onClassChange = { viewModel.resultClassFilter.value = it },
                        selectedExam = resultExam,
                        onExamChange = { viewModel.resultExamFilter.value = it },
                        onSaveResult = { sId, sName, fName, cls, sec, roll, exam, date, subs, id ->
                            viewModel.saveResultRecord(sId, sName, fName, cls, sec, roll, exam, date, subs, id)
                        },
                        onDeleteResult = { viewModel.deleteResult(it) },
                        onPreviewStudentResult = { viewModel.previewStudentResult(it) },
                        onPreviewClassResult = { cls, exam -> viewModel.previewClassResult(cls, exam) }
                    )
                }
                Screen.PHOTOS -> {
                    PhotosScreen(
                        photos = photos,
                        selectedCategory = photoCategory,
                        onCategoryChange = { viewModel.photoCategoryFilter.value = it },
                        onSavePhoto = { t, c, u -> viewModel.saveSchoolPhoto(t, c, u) },
                        onDeletePhoto = { viewModel.deletePhoto(it) }
                    )
                }
                Screen.EXCEL_UPLOAD -> {
                    ExcelScreen(
                        importRows = excelImportRows,
                        isImporting = isImportingExcel,
                        onUploadExcelUri = { ctx, uri -> viewModel.loadExcelFromUri(ctx, uri) },
                        onLoadSampleTemplate = { ctx -> viewModel.loadSampleTemplateForImport(ctx) },
                        onUpdateRow = { idx, row -> viewModel.updateImportRow(idx, row) },
                        onRemoveRow = { idx -> viewModel.removeImportRow(idx) },
                        onClearRows = { viewModel.clearImportRows() },
                        onConfirmImport = { viewModel.confirmExcelImport() }
                    )
                }
                Screen.SAVED_DOCUMENTS -> {
                    SavedDocumentsScreen(
                        documents = savedDocuments,
                        onDeleteDocument = { viewModel.deleteSavedDocument(it) },
                        onUpdateDocument = {
                            // Update title
                        }
                    )
                }
                Screen.PREVIEW -> {
                    DocumentPreviewScreen(
                        profile = profile,
                        previewState = previewState,
                        students = students,
                        onEdit = {
                            when (previewState.type) {
                                PreviewType.STUDENT_RESULT, PreviewType.CLASS_RESULT -> viewModel.navigateTo(Screen.RESULTS)
                                PreviewType.FEE_RECEIPT -> viewModel.navigateTo(Screen.FEES)
                                PreviewType.TIMETABLE -> viewModel.navigateTo(Screen.TIMETABLE)
                            }
                        },
                        onSaveToMobile = { ctx -> viewModel.exportCurrentPreviewToPdf(ctx) },
                        onExportPdf = { ctx -> viewModel.exportCurrentPreviewToPdf(ctx) },
                        onExportDocx = { ctx -> viewModel.exportCurrentPreviewToDocx(ctx) },
                        onPrint = { ctx -> viewModel.printCurrentPreview(ctx) },
                        onShare = { ctx, format -> viewModel.shareCurrentPreview(ctx, format) }
                    )
                }
                Screen.SETTINGS -> {
                    SettingsScreen(
                        profile = profile,
                        gradingRules = gradingRules,
                        onSaveProfile = { viewModel.updateSchoolProfile(it) },
                        onSaveGradingRule = { viewModel.updateGradingRule(it) },
                        onDeleteGradingRule = { viewModel.deleteGradingRule(it) },
                        onAddGradingRule = { viewModel.addGradingRule(it) }
                    )
                }
            }
        }
    }
}
