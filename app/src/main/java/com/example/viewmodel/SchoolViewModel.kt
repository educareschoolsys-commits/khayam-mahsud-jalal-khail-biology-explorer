package com.example.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.db.SchoolDatabase
import com.example.data.model.ClassResultStats
import com.example.data.model.DashboardStats
import com.example.data.model.FeeRecord
import com.example.data.model.GradingRule
import com.example.data.model.ResultRecord
import com.example.data.model.SavedDocument
import com.example.data.model.SchoolPhoto
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.SubjectScore
import com.example.data.model.TimetableEntry
import com.example.data.repository.SchoolRepository
import com.example.export.DocxGenerator
import com.example.export.DocumentPrinter
import com.example.export.DocumentSharer
import com.example.export.ExcelManager
import com.example.export.ExcelRow
import com.example.export.PdfGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class Screen {
    DASHBOARD,
    STUDENTS,
    FEES,
    TIMETABLE,
    RESULTS,
    PHOTOS,
    EXCEL_UPLOAD,
    SAVED_DOCUMENTS,
    PREVIEW,
    SETTINGS
}

enum class PreviewType {
    STUDENT_RESULT,
    CLASS_RESULT,
    FEE_RECEIPT,
    TIMETABLE
}

data class PreviewState(
    val type: PreviewType = PreviewType.STUDENT_RESULT,
    val resultRecord: ResultRecord? = null,
    val subjects: List<SubjectScore> = emptyList(),
    val classStats: ClassResultStats = ClassResultStats(),
    val feeRecord: FeeRecord? = null,
    val timetableEntries: List<TimetableEntry> = emptyList(),
    val selectedClass: String = "",
    val generatedPdfFile: File? = null,
    val generatedDocxFile: File? = null
)

class SchoolViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: SchoolRepository

    init {
        val db = SchoolDatabase.getInstance(application)
        repository = SchoolRepository(db.schoolDao())
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
        }
    }

    // Navigation State
    private val _currentScreen = MutableStateFlow(Screen.DASHBOARD)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Navigation History for back navigation
    private val screenStack = mutableListOf<Screen>()

    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            screenStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (screenStack.isNotEmpty()) {
            _currentScreen.value = screenStack.removeAt(screenStack.size - 1)
            return true
        }
        return false
    }

    // Repository Flows
    val schoolProfile: StateFlow<SchoolProfile?> = repository.schoolProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SchoolProfile())

    val gradingRules: StateFlow<List<GradingRule>> = repository.gradingRules
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allStudents: StateFlow<List<Student>> = repository.allStudents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClasses: StateFlow<List<String>> = repository.allClasses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allFeeRecords: StateFlow<List<FeeRecord>> = repository.allFeeRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allResults: StateFlow<List<ResultRecord>> = repository.allResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTimetableEntries: StateFlow<List<TimetableEntry>> = repository.allTimetableEntries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allPhotos: StateFlow<List<SchoolPhoto>> = repository.allPhotos
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSavedDocuments: StateFlow<List<SavedDocument>> = repository.allSavedDocuments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dashboardStats: StateFlow<DashboardStats> = repository.dashboardStats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardStats())

    // UI Search & Filter States
    val studentSearchQuery = MutableStateFlow("")
    val studentClassFilter = MutableStateFlow("All")

    val feeMonthFilter = MutableStateFlow("All")
    val feeStatusFilter = MutableStateFlow("All")

    val resultClassFilter = MutableStateFlow("Class 5")
    val resultExamFilter = MutableStateFlow("First Term Examination 2026")

    val timetableClassFilter = MutableStateFlow("Class 5")

    val photoCategoryFilter = MutableStateFlow("All")

    val savedDocFilter = MutableStateFlow("All")

    // Preview & Export State
    private val _previewState = MutableStateFlow(PreviewState())
    val previewState: StateFlow<PreviewState> = _previewState.asStateFlow()

    // Excel Upload State
    private val _excelImportRows = MutableStateFlow<List<ExcelRow>>(emptyList())
    val excelImportRows: StateFlow<List<ExcelRow>> = _excelImportRows.asStateFlow()

    private val _isImportingExcel = MutableStateFlow(false)
    val isImportingExcel: StateFlow<Boolean> = _isImportingExcel.asStateFlow()

    // Status Message / Toast feedback
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // School Settings Actions
    fun updateSchoolProfile(profile: SchoolProfile) {
        viewModelScope.launch {
            repository.saveSchoolProfile(profile)
            _userMessage.value = "School profile updated successfully / سکول معلومات محفوظ ہو گئیں"
        }
    }

    fun updateGradingRule(rule: GradingRule) {
        viewModelScope.launch {
            repository.updateGradingRule(rule)
            _userMessage.value = "Grading rule updated / گریڈنگ کا اصول تبدیل ہو گیا"
            recalculateAllResults()
        }
    }

    fun addGradingRule(rule: GradingRule) {
        viewModelScope.launch {
            repository.saveGradingRule(rule)
            _userMessage.value = "New grading rule added"
            recalculateAllResults()
        }
    }

    fun deleteGradingRule(rule: GradingRule) {
        viewModelScope.launch {
            repository.deleteGradingRule(rule)
            _userMessage.value = "Grading rule removed"
            recalculateAllResults()
        }
    }

    // Student Management Actions
    fun saveStudent(student: Student) {
        viewModelScope.launch {
            if (student.id == 0L) {
                val newId = repository.saveStudent(student)
                // If photo was assigned, also save to photos
                if (!student.photoUri.isNullOrBlank()) {
                    val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                    repository.savePhoto(
                        SchoolPhoto(
                            title = "${student.name} (${student.className})",
                            category = "Student",
                            imageUri = student.photoUri,
                            dateAdded = date,
                            associatedId = newId
                        )
                    )
                }
                _userMessage.value = "Student '${student.name}' added successfully"
            } else {
                repository.updateStudent(student)
                _userMessage.value = "Student '${student.name}' updated"
            }
        }
    }

    fun deleteStudent(student: Student) {
        viewModelScope.launch {
            repository.deleteStudent(student)
            _userMessage.value = "Student removed"
        }
    }

    // Fee Management Actions
    fun saveFeeRecord(
        studentId: Long,
        studentName: String,
        fatherName: String,
        className: String,
        section: String,
        rollNumber: String,
        month: String,
        monthlyFee: Double,
        admissionFee: Double,
        otherCharges: Double,
        discount: Double,
        paidAmount: Double,
        paymentDate: String,
        id: Long = 0L
    ) {
        val total = monthlyFee + admissionFee + otherCharges - discount
        val remaining = (total - paidAmount).coerceAtLeast(0.0)
        val status = when {
            paidAmount >= total -> "Paid"
            paidAmount > 0 -> "Partially Paid"
            else -> "Unpaid"
        }

        val record = FeeRecord(
            id = id,
            studentId = studentId,
            studentName = studentName,
            fatherName = fatherName,
            className = className,
            section = section,
            rollNumber = rollNumber,
            month = month,
            monthlyFee = monthlyFee,
            admissionFee = admissionFee,
            otherCharges = otherCharges,
            discount = discount,
            totalFee = total,
            paidAmount = paidAmount,
            remainingAmount = remaining,
            paymentDate = paymentDate,
            paymentStatus = status
        )

        viewModelScope.launch {
            if (id == 0L) {
                repository.saveFeeRecord(record)
                _userMessage.value = "Fee record created for $studentName"
            } else {
                repository.updateFeeRecord(record)
                _userMessage.value = "Fee record updated"
            }
        }
    }

    fun deleteFeeRecord(record: FeeRecord) {
        viewModelScope.launch {
            repository.deleteFeeRecord(record)
            _userMessage.value = "Fee record deleted"
        }
    }

    // Results Actions
    fun saveResultRecord(
        studentId: Long,
        studentName: String,
        fatherName: String,
        className: String,
        section: String,
        rollNumber: String,
        examTitle: String,
        examDate: String,
        subjects: List<SubjectScore>,
        id: Long = 0L
    ) {
        viewModelScope.launch {
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()
            val passPct = profile.passingPercentage
            val minSubjectMarks = profile.minSubjectPassingMarks

            val scoredSubjects = subjects.map {
                val pct = if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100.0 else 0.0
                val grade = repository.calculateGrade(pct, rules)
                val pass = if (it.obtainedMarks >= minSubjectMarks) "Pass" else "Fail"
                it.copy(percentage = pct, grade = grade, passStatus = pass)
            }

            val totMarks = scoredSubjects.sumOf { it.totalMarks }
            val obtMarks = scoredSubjects.sumOf { it.obtainedMarks }
            val pct = if (totMarks > 0) (obtMarks / totMarks) * 100.0 else 0.0
            val grade = repository.calculateGrade(pct, rules)
            val anyFail = scoredSubjects.any { it.passStatus == "Fail" }
            val overallPass = if (!anyFail && pct >= passPct) "Pass" else "Fail"

            val record = ResultRecord(
                id = id,
                studentId = studentId,
                studentName = studentName,
                fatherName = fatherName,
                className = className,
                section = section,
                rollNumber = rollNumber,
                examTitle = examTitle,
                examDate = examDate,
                totalMarks = totMarks,
                obtainedMarks = obtMarks,
                percentage = pct,
                grade = grade,
                position = "",
                passStatus = overallPass,
                subjectsJson = repository.serializeSubjects(scoredSubjects)
            )

            if (id == 0L) {
                repository.saveResult(record)
            } else {
                repository.updateResult(record)
            }
            _userMessage.value = "Marks saved & calculated automatically"
            recalculatePositionsForClass(className, examTitle)
        }
    }

    fun deleteResult(record: ResultRecord) {
        viewModelScope.launch {
            repository.deleteResult(record)
            _userMessage.value = "Result removed"
            recalculatePositionsForClass(record.className, record.examTitle)
        }
    }

    private fun recalculatePositionsForClass(className: String, examTitle: String) {
        viewModelScope.launch {
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()
            val allRes = repository.allResults.stateIn(viewModelScope).value
                .filter { it.className == className && it.examTitle == examTitle }

            val (ranked, _) = repository.recalculateClassResults(
                allRes, rules, profile.passingPercentage, profile.minSubjectPassingMarks
            )
            for (r in ranked) {
                repository.updateResult(r)
            }
        }
    }

    private fun recalculateAllResults() {
        viewModelScope.launch {
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()
            val allRes = repository.allResults.stateIn(viewModelScope).value

            val grouped = allRes.groupBy { Pair(it.className, it.examTitle) }
            for ((_, list) in grouped) {
                val (ranked, _) = repository.recalculateClassResults(
                    list, rules, profile.passingPercentage, profile.minSubjectPassingMarks
                )
                for (r in ranked) {
                    repository.updateResult(r)
                }
            }
        }
    }

    // Timetable Actions
    fun saveTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            if (entry.id == 0L) {
                repository.saveTimetableEntry(entry)
                _userMessage.value = "Timetable entry added"
            } else {
                repository.updateTimetableEntry(entry)
                _userMessage.value = "Timetable entry updated"
            }
        }
    }

    fun deleteTimetableEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            repository.deleteTimetableEntry(entry)
            _userMessage.value = "Timetable entry removed"
        }
    }

    fun clearTimetableForClass(className: String) {
        viewModelScope.launch {
            repository.clearTimetableForClass(className)
            _userMessage.value = "Timetable cleared for $className"
        }
    }

    // Photos Actions
    fun saveSchoolPhoto(title: String, category: String, uri: String) {
        viewModelScope.launch {
            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            repository.savePhoto(
                SchoolPhoto(
                    title = title,
                    category = category,
                    imageUri = uri,
                    dateAdded = date
                )
            )
            _userMessage.value = "Photo saved to $category"
        }
    }

    fun deletePhoto(photo: SchoolPhoto) {
        viewModelScope.launch {
            repository.deletePhoto(photo)
            _userMessage.value = "Photo deleted"
        }
    }

    // Document Preview & Generation Hub
    fun previewStudentResult(result: ResultRecord) {
        viewModelScope.launch {
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()
            val allRes = repository.allResults.stateIn(viewModelScope).value
                .filter { it.className == result.className && it.examTitle == result.examTitle }

            val (ranked, stats) = repository.recalculateClassResults(
                allRes, rules, profile.passingPercentage, profile.minSubjectPassingMarks
            )
            val updatedRecord = ranked.find { it.id == result.id } ?: result
            val subs = repository.parseSubjects(updatedRecord.subjectsJson)

            _previewState.value = PreviewState(
                type = PreviewType.STUDENT_RESULT,
                resultRecord = updatedRecord,
                subjects = subs,
                classStats = stats,
                selectedClass = result.className
            )
            navigateTo(Screen.PREVIEW)
        }
    }

    fun previewClassResult(className: String, examTitle: String) {
        viewModelScope.launch {
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()
            val allRes = repository.allResults.stateIn(viewModelScope).value
                .filter { it.className == className && it.examTitle == examTitle }

            val (ranked, stats) = repository.recalculateClassResults(
                allRes, rules, profile.passingPercentage, profile.minSubjectPassingMarks
            )

            _previewState.value = PreviewState(
                type = PreviewType.CLASS_RESULT,
                classStats = stats,
                selectedClass = className
            )
            navigateTo(Screen.PREVIEW)
        }
    }

    fun previewFeeReceipt(fee: FeeRecord) {
        _previewState.value = PreviewState(
            type = PreviewType.FEE_RECEIPT,
            feeRecord = fee,
            selectedClass = fee.className
        )
        navigateTo(Screen.PREVIEW)
    }

    fun previewTimetable(className: String) {
        val entries = allTimetableEntries.value.filter { it.className == className }
        _previewState.value = PreviewState(
            type = PreviewType.TIMETABLE,
            timetableEntries = entries,
            selectedClass = className
        )
        navigateTo(Screen.PREVIEW)
    }

    // Export & Sharing actions from Preview
    fun exportCurrentPreviewToPdf(context: android.content.Context) {
        viewModelScope.launch {
            val profile = repository.getSchoolProfileSync()
            val state = _previewState.value
            val studentList = allStudents.value

            val file: File? = when (state.type) {
                PreviewType.STUDENT_RESULT -> {
                    state.resultRecord?.let { res ->
                        val stu = studentList.find { it.id == res.studentId }
                        PdfGenerator.generateStudentResultPdf(
                            context = context,
                            profile = profile,
                            result = res,
                            subjects = state.subjects,
                            stats = state.classStats,
                            studentPhotoUri = stu?.photoUri
                        )
                    }
                }
                PreviewType.CLASS_RESULT -> {
                    val results = allResults.value.filter { it.className == state.selectedClass }
                    PdfGenerator.generateClassResultMasterPdf(
                        context = context,
                        profile = profile,
                        className = state.selectedClass,
                        examTitle = results.firstOrNull()?.examTitle ?: "Examination",
                        results = results,
                        stats = state.classStats
                    )
                }
                PreviewType.FEE_RECEIPT -> {
                    state.feeRecord?.let { fee ->
                        val stu = studentList.find { it.id == fee.studentId }
                        PdfGenerator.generateFeeReceiptPdf(
                            context = context,
                            profile = profile,
                            fee = fee,
                            studentPhotoUri = stu?.photoUri
                        )
                    }
                }
                PreviewType.TIMETABLE -> {
                    PdfGenerator.generateTimetablePdf(
                        context = context,
                        profile = profile,
                        className = state.selectedClass,
                        entries = state.timetableEntries
                    )
                }
            }

            if (file != null) {
                _previewState.value = state.copy(generatedPdfFile = file)
                saveDocumentRecord(
                    title = file.nameWithoutExtension,
                    docType = state.type.name,
                    format = "PDF",
                    filePath = file.absolutePath,
                    size = "${file.length() / 1024} KB"
                )
                _userMessage.value = "PDF generated and saved: ${file.name}"
            } else {
                _userMessage.value = "Error generating PDF"
            }
        }
    }

    fun exportCurrentPreviewToDocx(context: android.content.Context) {
        viewModelScope.launch {
            val profile = repository.getSchoolProfileSync()
            val state = _previewState.value

            val file: File? = when (state.type) {
                PreviewType.STUDENT_RESULT -> {
                    state.resultRecord?.let { res ->
                        DocxGenerator.generateStudentResultDocx(
                            context = context,
                            profile = profile,
                            result = res,
                            subjects = state.subjects,
                            stats = state.classStats
                        )
                    }
                }
                PreviewType.FEE_RECEIPT -> {
                    state.feeRecord?.let { fee ->
                        DocxGenerator.generateFeeReceiptDocx(
                            context = context,
                            profile = profile,
                            fee = fee
                        )
                    }
                }
                PreviewType.TIMETABLE -> {
                    DocxGenerator.generateTimetableDocx(
                        context = context,
                        profile = profile,
                        className = state.selectedClass,
                        entries = state.timetableEntries
                    )
                }
                else -> null
            }

            if (file != null) {
                _previewState.value = state.copy(generatedDocxFile = file)
                saveDocumentRecord(
                    title = file.nameWithoutExtension,
                    docType = state.type.name,
                    format = "DOCX",
                    filePath = file.absolutePath,
                    size = "${file.length() / 1024} KB"
                )
                _userMessage.value = "Word document created: ${file.name}"
            } else {
                _userMessage.value = "Word document export completed"
            }
        }
    }

    fun printCurrentPreview(context: android.content.Context) {
        viewModelScope.launch {
            var pdf = _previewState.value.generatedPdfFile
            if (pdf == null || !pdf.exists()) {
                exportCurrentPreviewToPdf(context)
                pdf = _previewState.value.generatedPdfFile
            }
            if (pdf != null && pdf.exists()) {
                DocumentPrinter.printPdf(context, pdf, "Print ${pdf.nameWithoutExtension}")
            } else {
                _userMessage.value = "Please generate PDF first before printing"
            }
        }
    }

    fun shareCurrentPreview(context: android.content.Context, format: String = "PDF") {
        viewModelScope.launch {
            if (format == "PDF") {
                var pdf = _previewState.value.generatedPdfFile
                if (pdf == null || !pdf.exists()) {
                    exportCurrentPreviewToPdf(context)
                    pdf = _previewState.value.generatedPdfFile
                }
                if (pdf != null && pdf.exists()) {
                    DocumentSharer.shareFile(context, pdf, "application/pdf")
                }
            } else {
                var docx = _previewState.value.generatedDocxFile
                if (docx == null || !docx.exists()) {
                    exportCurrentPreviewToDocx(context)
                    docx = _previewState.value.generatedDocxFile
                }
                if (docx != null && docx.exists()) {
                    DocumentSharer.shareFile(
                        context,
                        docx,
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                    )
                }
            }
        }
    }

    private suspend fun saveDocumentRecord(title: String, docType: String, format: String, filePath: String, size: String) {
        val date = SimpleDateFormat("dd-MM-yyyy hh:mm a", Locale.getDefault()).format(Date())
        repository.saveDocument(
            SavedDocument(
                title = title,
                docType = docType,
                fileFormat = format,
                filePath = filePath,
                fileSize = size,
                dateCreated = date
            )
        )
    }

    fun deleteSavedDocument(doc: SavedDocument) {
        viewModelScope.launch {
            try {
                val f = File(doc.filePath)
                if (f.exists()) f.delete()
            } catch (_: Exception) {}
            repository.deleteDocument(doc)
            _userMessage.value = "Document deleted"
        }
    }

    // Excel Upload & Processing
    fun loadExcelFromUri(context: android.content.Context, uri: Uri) {
        viewModelScope.launch {
            _isImportingExcel.value = true
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
                if (inputStream != null) {
                    val rows = ExcelManager.parseStream(inputStream)
                    _excelImportRows.value = rows
                    if (rows.isEmpty()) {
                        _userMessage.value = "No valid data rows found in selected spreadsheet"
                    } else {
                        _userMessage.value = "Loaded ${rows.size} rows for preview. Review and confirm import."
                    }
                }
            } catch (e: Exception) {
                _userMessage.value = "Failed to read Excel file: ${e.message}"
            } finally {
                _isImportingExcel.value = false
            }
        }
    }

    fun loadSampleTemplateForImport(context: android.content.Context) {
        viewModelScope.launch {
            _isImportingExcel.value = true
            val sampleFile = ExcelManager.createSampleTemplateCsv(context)
            val rows = ExcelManager.parseCsv(sampleFile.inputStream())
            _excelImportRows.value = rows
            _isImportingExcel.value = false
            _userMessage.value = "Sample spreadsheet template loaded with ${rows.size} student records."
        }
    }

    fun updateImportRow(index: Int, updated: ExcelRow) {
        val current = _excelImportRows.value.toMutableList()
        if (index in current.indices) {
            current[index] = updated
            _excelImportRows.value = current
        }
    }

    fun removeImportRow(index: Int) {
        val current = _excelImportRows.value.toMutableList()
        if (index in current.indices) {
            current.removeAt(index)
            _excelImportRows.value = current
        }
    }

    fun clearImportRows() {
        _excelImportRows.value = emptyList()
    }

    fun confirmExcelImport() {
        viewModelScope.launch {
            val rows = _excelImportRows.value
            if (rows.isEmpty()) return@launch

            val date = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val rules = repository.gradingRules.stateIn(viewModelScope).value
            val profile = repository.getSchoolProfileSync()

            // Group rows by student (name + roll + class)
            val groupedByStudent = rows.groupBy { Triple(it.studentName, it.className, it.rollNumber) }

            for ((triple, studentRows) in groupedByStudent) {
                val first = studentRows.first()
                // 1. Insert or update student
                val student = Student(
                    name = first.studentName,
                    fatherName = first.fatherName,
                    className = first.className,
                    section = first.section,
                    rollNumber = first.rollNumber,
                    admissionNumber = "ADM-${first.rollNumber}",
                    contactNumber = "",
                    address = ""
                )
                val studentId = repository.saveStudent(student)

                // 2. Insert Fee record
                val feeRecord = FeeRecord(
                    studentId = studentId,
                    studentName = first.studentName,
                    fatherName = first.fatherName,
                    className = first.className,
                    section = first.section,
                    rollNumber = first.rollNumber,
                    month = "October 2026",
                    monthlyFee = first.monthlyFee,
                    admissionFee = 0.0,
                    otherCharges = 0.0,
                    discount = 0.0,
                    totalFee = first.monthlyFee,
                    paidAmount = first.paidFee,
                    remainingAmount = (first.monthlyFee - first.paidFee).coerceAtLeast(0.0),
                    paymentDate = date,
                    paymentStatus = if (first.paidFee >= first.monthlyFee) "Paid" else if (first.paidFee > 0) "Partially Paid" else "Unpaid"
                )
                repository.saveFeeRecord(feeRecord)

                // 3. Insert Result record with subjects
                val subjects = studentRows.map {
                    SubjectScore(
                        subjectName = it.subject,
                        totalMarks = it.totalMarks,
                        obtainedMarks = it.obtainedMarks,
                        percentage = if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100.0 else 0.0,
                        grade = repository.calculateGrade(if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100.0 else 0.0, rules),
                        passStatus = if (it.obtainedMarks >= profile.minSubjectPassingMarks) "Pass" else "Fail"
                    )
                }

                val totMarks = subjects.sumOf { it.totalMarks }
                val obtMarks = subjects.sumOf { it.obtainedMarks }
                val pct = if (totMarks > 0) (obtMarks / totMarks) * 100.0 else 0.0
                val grade = repository.calculateGrade(pct, rules)
                val anyFail = subjects.any { it.passStatus == "Fail" }
                val overallPass = if (!anyFail && pct >= profile.passingPercentage) "Pass" else "Fail"

                val result = ResultRecord(
                    studentId = studentId,
                    studentName = first.studentName,
                    fatherName = first.fatherName,
                    className = first.className,
                    section = first.section,
                    rollNumber = first.rollNumber,
                    examTitle = "Excel Imported Examination 2026",
                    examDate = date,
                    totalMarks = totMarks,
                    obtainedMarks = obtMarks,
                    percentage = pct,
                    grade = grade,
                    position = "",
                    passStatus = overallPass,
                    subjectsJson = repository.serializeSubjects(subjects)
                )
                repository.saveResult(result)
            }

            recalculateAllResults()
            _excelImportRows.value = emptyList()
            _userMessage.value = "Import successful! ${groupedByStudent.size} student profiles, fees, and calculated results created."
            navigateTo(Screen.DASHBOARD)
        }
    }
}
