package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.RemoveCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GradingRule
import com.example.data.model.ResultRecord
import com.example.data.model.Student
import com.example.data.model.SubjectScore
import com.example.ui.components.StatusChip
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResultScreen(
    results: List<ResultRecord>,
    students: List<Student>,
    gradingRules: List<GradingRule>,
    selectedClass: String,
    onClassChange: (String) -> Unit,
    selectedExam: String,
    onExamChange: (String) -> Unit,
    onSaveResult: (
        studentId: Long,
        studentName: String,
        fatherName: String,
        className: String,
        section: String,
        rollNumber: String,
        examTitle: String,
        examDate: String,
        subjects: List<SubjectScore>,
        id: Long
    ) -> Unit,
    onDeleteResult: (ResultRecord) -> Unit,
    onPreviewStudentResult: (ResultRecord) -> Unit,
    onPreviewClassResult: (String, String) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Student Cards, 1: Class Master Tabulation
    var showMarksDialog by remember { mutableStateOf(false) }
    var editingResult by remember { mutableStateOf<ResultRecord?>(null) }
    var resultToDelete by remember { mutableStateOf<ResultRecord?>(null) }

    val distinctClasses = listOf("Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val distinctExams = listOf("First Term Examination 2026", "Mid Term Examination 2026", "Annual Examination 2026")

    val classResults = results.filter { it.className == selectedClass && it.examTitle == selectedExam }

    // Automated Class Statistics
    val totalStudents = classResults.size
    val passedCount = classResults.count { it.passStatus == "Pass" }
    val failedCount = totalStudents - passedCount
    val passRate = if (totalStudents > 0) (passedCount.toDouble() / totalStudents) * 100.0 else 0.0
    val classAvg = if (totalStudents > 0) classResults.map { it.percentage }.average() else 0.0
    val highestMarks = classResults.maxOfOrNull { it.obtainedMarks } ?: 0.0
    val lowestMarks = classResults.minOfOrNull { it.obtainedMarks } ?: 0.0

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingResult = null
                    showMarksDialog = true
                },
                containerColor = SchoolNavy,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_result_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Enter Student Marks")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("results_screen")
        ) {
            // Class Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(distinctClasses) { cls ->
                    FilterChip(
                        selected = selectedClass == cls,
                        onClick = { onClassChange(cls) },
                        label = { Text(cls) }
                    )
                }
            }

            // Exam Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(distinctExams) { ex ->
                    FilterChip(
                        selected = selectedExam == ex,
                        onClick = { onExamChange(ex) },
                        label = { Text(ex) }
                    )
                }
            }

            // Tab Row: Student Cards vs Class Master Result
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White,
                contentColor = SchoolNavy
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Student Results (${classResults.size})", fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Class Master Sheet", fontWeight = FontWeight.Bold) }
                )
            }

            // Class Statistics KPI Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.06f))
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Class Benchmark: $selectedClass",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SchoolNavy
                            )
                        )
                        OutlinedButton(
                            onClick = { onPreviewClassResult(selectedClass, selectedExam) },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("Preview Class Sheet", fontSize = 10.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Total / Passed", fontSize = 10.sp, color = Color.Gray)
                            Text("$totalStudents / $passedCount", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Column {
                            Text("Pass %", fontSize = 10.sp, color = Color.Gray)
                            Text(String.format(Locale.US, "%.1f%%", passRate), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                        }
                        Column {
                            Text("Class Avg", fontSize = 10.sp, color = Color.Gray)
                            Text(String.format(Locale.US, "%.1f%%", classAvg), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                        }
                        Column {
                            Text("Highest / Lowest", fontSize = 10.sp, color = Color.Gray)
                            Text("${highestMarks.toInt()} / ${lowestMarks.toInt()}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolGold)
                        }
                    }
                }
            }

            if (selectedTab == 0) {
                // Single Student Result Cards List
                if (classResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No results found for $selectedClass ($selectedExam).\nTap '+' to enter marks for students.",
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(classResults, key = { it.id }) { record ->
                            val stu = students.find { it.id == record.studentId }
                            ResultStudentCard(
                                record = record,
                                studentPhotoUri = stu?.photoUri,
                                onPreview = { onPreviewStudentResult(record) },
                                onEdit = {
                                    editingResult = record
                                    showMarksDialog = true
                                },
                                onDelete = { resultToDelete = record }
                            )
                        }
                    }
                }
            } else {
                // Class Master Tabulation Sheet View
                ClassMasterTabulationView(
                    results = classResults,
                    onPreviewClass = { onPreviewClassResult(selectedClass, selectedExam) }
                )
            }
        }
    }

    if (showMarksDialog) {
        MarksEntryDialog(
            result = editingResult,
            students = students.filter { it.className == selectedClass },
            allStudents = students,
            gradingRules = gradingRules,
            selectedClass = selectedClass,
            selectedExam = selectedExam,
            onDismiss = { showMarksDialog = false },
            onSave = { sId, sName, fName, cls, sec, roll, exam, date, subs, id ->
                onSaveResult(sId, sName, fName, cls, sec, roll, exam, date, subs, id)
                showMarksDialog = false
            }
        )
    }

    if (resultToDelete != null) {
        AlertDialog(
            onDismissRequest = { resultToDelete = null },
            title = { Text("Delete Result") },
            text = { Text("Are you sure you want to delete the result for ${resultToDelete?.studentName}?") },
            confirmButton = {
                Button(
                    onClick = {
                        resultToDelete?.let { onDeleteResult(it) }
                        resultToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { resultToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun ResultStudentCard(
    record: ResultRecord,
    studentPhotoUri: String?,
    onPreview: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("result_card_${record.rollNumber}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StudentAvatar(photoUri = studentPhotoUri, name = record.studentName)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = record.studentName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SchoolNavy
                            )
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (record.position.isNotBlank()) {
                                Box(
                                    modifier = Modifier
                                        .background(SchoolGold.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Pos: ${record.position}",
                                        color = SchoolGold,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            StatusChip(record.passStatus)
                        }
                    }

                    Text(
                        text = "Roll #${record.rollNumber} • ${record.className} (${record.section})",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Scores Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Obtained Marks", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        "${record.obtainedMarks.toInt()} / ${record.totalMarks.toInt()}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = SchoolNavy
                    )
                }
                Column {
                    Text("Percentage", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        String.format(Locale.US, "%.1f%%", record.percentage),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = StatusPaid
                    )
                }
                Column {
                    Text("Grade", fontSize = 10.sp, color = Color.Gray)
                    Text(
                        record.grade,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = SchoolNavyLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onPreview,
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Result Sheet / رزلٹ کارڈ", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Marks", tint = SchoolNavy)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Result", tint = StatusUnpaid)
                }
            }
        }
    }
}

@Composable
fun ClassMasterTabulationView(
    results: List<ResultRecord>,
    onPreviewClass: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Class Master Tabulation / مکمل نتیجہ چارٹ",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SchoolNavy)
            )
            Button(
                onClick = onPreviewClass,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Export Class Result", fontSize = 12.sp)
            }
        }

        // Table Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = SchoolNavy)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Roll", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                Text("Student Name", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1.5f))
                Text("Marks", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                Text("%", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                Text("Grd", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center)
                Text("Pos", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                Text("Status", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
            }
        }

        // Rows
        results.forEachIndexed { idx, res ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(containerColor = if (idx % 2 == 1) Color(0xFFF8FAFC) else Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(res.rollNumber, fontSize = 11.sp, modifier = Modifier.width(40.dp))
                    Text(res.studentName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.5f))
                    Text("${res.obtainedMarks.toInt()}/${res.totalMarks.toInt()}", fontSize = 11.sp, modifier = Modifier.width(60.dp), textAlign = TextAlign.Center)
                    Text(String.format(Locale.US, "%.0f%%", res.percentage), fontSize = 11.sp, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text(res.grade, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(35.dp), textAlign = TextAlign.Center)
                    Text(res.position, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SchoolGold, modifier = Modifier.width(45.dp), textAlign = TextAlign.Center)
                    Text(
                        res.passStatus,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (res.passStatus == "Pass") StatusPaid else StatusUnpaid,
                        modifier = Modifier.width(45.dp),
                        textAlign = TextAlign.End
                    )
                }
            }
        }
    }
}

data class EditableSubject(
    var name: String,
    var total: String,
    var obtained: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarksEntryDialog(
    result: ResultRecord?,
    students: List<Student>,
    allStudents: List<Student>,
    gradingRules: List<GradingRule>,
    selectedClass: String,
    selectedExam: String,
    onDismiss: () -> Unit,
    onSave: (
        studentId: Long,
        studentName: String,
        fatherName: String,
        className: String,
        section: String,
        rollNumber: String,
        examTitle: String,
        examDate: String,
        subjects: List<SubjectScore>,
        id: Long
    ) -> Unit
) {
    val studentList = if (students.isNotEmpty()) students else allStudents
    var selectedStudent by remember {
        mutableStateOf(
            if (result != null) studentList.find { it.id == result.studentId }
            else studentList.firstOrNull()
        )
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    var examTitle by remember { mutableStateOf(result?.examTitle ?: selectedExam) }
    var examDate by remember {
        mutableStateOf(
            result?.examDate ?: SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        )
    }

    // Editable subjects list
    val subjectsState = remember {
        mutableStateListOf<EditableSubject>().apply {
            if (result != null && result.subjectsJson.isNotBlank()) {
                try {
                    val array = JSONArray(result.subjectsJson)
                    for (i in 0 until array.length()) {
                        val o = array.getJSONObject(i)
                        add(
                            EditableSubject(
                                name = o.optString("subjectName", "Subject"),
                                total = o.optDouble("totalMarks", 100.0).toInt().toString(),
                                obtained = o.optDouble("obtainedMarks", 0.0).toInt().toString()
                            )
                        )
                    }
                } catch (_: Exception) {}
            }
            if (isEmpty()) {
                add(EditableSubject("قرآن و ناظرہ", "100", "90"))
                add(EditableSubject("اردو", "100", "85"))
                add(EditableSubject("English", "100", "80"))
                add(EditableSubject("Mathematics", "100", "88"))
                add(EditableSubject("General Science", "100", "82"))
                add(EditableSubject("Social Studies", "100", "80"))
            }
        }
    }

    // Live Auto Formulas
    val totalMarksSum = subjectsState.sumOf { it.total.toDoubleOrNull() ?: 0.0 }
    val obtainedMarksSum = subjectsState.sumOf { it.obtained.toDoubleOrNull() ?: 0.0 }
    val livePercentage = if (totalMarksSum > 0) (obtainedMarksSum / totalMarksSum) * 100.0 else 0.0

    // Grade calculation
    val sortedRules = gradingRules.sortedByDescending { it.minPercentage }
    var liveGrade = "F"
    for (r in sortedRules) {
        if (livePercentage >= r.minPercentage) {
            liveGrade = r.grade
            break
        }
    }
    val anyFailed = subjectsState.any { (it.obtained.toDoubleOrNull() ?: 0.0) < 33.0 }
    val livePassStatus = if (!anyFailed && livePercentage >= 40.0) "Pass" else "Fail"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (result == null) "Enter Student Marks / اندراج نمبرات" else "Edit Marks / تدوین نمبرات",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Student selector
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.name} (Roll #${it.rollNumber})" } ?: "Select Student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Student *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        studentList.forEach { s ->
                            DropdownMenuItem(
                                text = { Text("${s.name} - Roll #${s.rollNumber} (${s.className})") },
                                onClick = {
                                    selectedStudent = s
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = examTitle,
                    onValueChange = { examTitle = it },
                    label = { Text("Exam Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Live Formula Result Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Total / Obtained", fontSize = 10.sp, color = Color.Gray)
                            Text("${obtainedMarksSum.toInt()} / ${totalMarksSum.toInt()}", fontWeight = FontWeight.Bold, color = SchoolNavy)
                        }
                        Column {
                            Text("Percentage", fontSize = 10.sp, color = Color.Gray)
                            Text(String.format(Locale.US, "%.1f%%", livePercentage), fontWeight = FontWeight.Bold, color = StatusPaid)
                        }
                        Column {
                            Text("Grade", fontSize = 10.sp, color = Color.Gray)
                            Text(liveGrade, fontWeight = FontWeight.ExtraBold, color = SchoolNavyLight)
                        }
                        StatusChip(livePassStatus)
                    }
                }

                // Dynamic Subjects Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Subjects & Marks", fontWeight = FontWeight.Bold, color = SchoolNavy)
                    TextButton(onClick = {
                        subjectsState.add(EditableSubject("Subject ${subjectsState.size + 1}", "100", "0"))
                    }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Subject", fontSize = 11.sp)
                    }
                }

                // Subjects entries
                subjectsState.forEachIndexed { idx, sub ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = sub.name,
                            onValueChange = {
                                subjectsState[idx] = sub.copy(name = it)
                            },
                            label = { Text("Subject") },
                            modifier = Modifier.weight(1.8f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sub.total,
                            onValueChange = {
                                subjectsState[idx] = sub.copy(total = it)
                            },
                            label = { Text("Total") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = sub.obtained,
                            onValueChange = {
                                subjectsState[idx] = sub.copy(obtained = it)
                            },
                            label = { Text("Obt") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (subjectsState.size > 1) {
                                    subjectsState.removeAt(idx)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Remove", tint = StatusUnpaid)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent
                    if (s != null) {
                        val subs = subjectsState.map {
                            val tot = it.total.toDoubleOrNull() ?: 100.0
                            val obt = it.obtained.toDoubleOrNull() ?: 0.0
                            val pct = if (tot > 0) (obt / tot) * 100.0 else 0.0
                            SubjectScore(
                                subjectName = it.name.trim(),
                                totalMarks = tot,
                                obtainedMarks = obt,
                                percentage = pct,
                                passStatus = if (obt >= 33.0) "Pass" else "Fail"
                            )
                        }
                        onSave(
                            s.id,
                            s.name,
                            s.fatherName,
                            s.className,
                            s.section,
                            s.rollNumber,
                            examTitle.trim(),
                            examDate.trim(),
                            subs,
                            result?.id ?: 0L
                        )
                    }
                },
                enabled = selectedStudent != null && subjectsState.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
            ) {
                Text("Save & Calculate")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
