package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.TimetableEntry
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusUnpaid

@Composable
fun TimetableScreen(
    timetableEntries: List<TimetableEntry>,
    selectedClass: String,
    onClassChange: (String) -> Unit,
    onSaveEntry: (TimetableEntry) -> Unit,
    onDeleteEntry: (TimetableEntry) -> Unit,
    onClearClass: (String) -> Unit,
    onPreviewTimetable: (String) -> Unit,
    onSavePhoto: (String, String, String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingEntry by remember { mutableStateOf<TimetableEntry?>(null) }
    var entryToDelete by remember { mutableStateOf<TimetableEntry?>(null) }
    var timetablePhotoUri by remember { mutableStateOf<String?>(null) }
    var showPhotoDialog by remember { mutableStateOf(false) }

    val distinctClasses = listOf("Class 5", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")

    val classEntries = timetableEntries.filter { it.className == selectedClass }
    val entriesByDay = days.associateWith { day ->
        classEntries.filter { it.day.equals(day, ignoreCase = true) }.sortedBy { it.periodIndex }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            timetablePhotoUri = uri.toString()
            onSavePhoto("Timetable - $selectedClass", "Timetable", uri.toString())
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingEntry = null
                    showDialog = true
                },
                containerColor = SchoolNavy,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_timetable_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Period")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("timetable_screen")
        ) {
            // Class Selector Chips
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

            // Top Action Bar for Timetable
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.06f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Timetable / اوقات کار",
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$selectedClass (${classEntries.size} Periods)",
                            fontSize = 11.sp,
                            color = Color.DarkGray
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Upload Photo", fontSize = 10.sp)
                        }

                        Button(
                            onClick = { onPreviewTimetable(selectedClass) },
                            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                            modifier = Modifier.height(34.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Preview, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Preview Sheet", fontSize = 10.sp)
                        }
                    }
                }
            }

            // Photo preview if attached
            if (timetablePhotoUri != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = timetablePhotoUri,
                            contentDescription = "Timetable Photo",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { showPhotoDialog = true },
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Timetable Reference Photo Attached", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                            Text("Table remains fully editable below", fontSize = 10.sp, color = Color.Gray)
                        }
                        TextButton(onClick = { showPhotoDialog = true }) {
                            Text("View", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Timetable Grid by Day
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(days) { day ->
                    val dayPeriods = entriesByDay[day] ?: emptyList()
                    DayTimetableCard(
                        day = day,
                        periods = dayPeriods,
                        onAddPeriod = {
                            editingEntry = TimetableEntry(
                                className = selectedClass,
                                section = "A",
                                academicSession = "2026 - 2027",
                                day = day,
                                periodIndex = dayPeriods.size + 1,
                                periodName = "Period ${dayPeriods.size + 1}",
                                startTime = "08:00 AM",
                                endTime = "08:45 AM",
                                subject = "",
                                teacherName = ""
                            )
                            showDialog = true
                        },
                        onEditPeriod = { entry ->
                            editingEntry = entry
                            showDialog = true
                        },
                        onDeletePeriod = { entry ->
                            entryToDelete = entry
                        }
                    )
                }
            }
        }
    }

    if (showDialog) {
        TimetableEntryDialog(
            entry = editingEntry,
            selectedClass = selectedClass,
            onDismiss = { showDialog = false },
            onSave = { entry ->
                onSaveEntry(entry)
                showDialog = false
            }
        )
    }

    if (showPhotoDialog && timetablePhotoUri != null) {
        AlertDialog(
            onDismissRequest = { showPhotoDialog = false },
            title = { Text("Timetable Photo Preview") },
            text = {
                AsyncImage(
                    model = timetablePhotoUri,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Fit
                )
            },
            confirmButton = {
                TextButton(onClick = { showPhotoDialog = false }) { Text("Close") }
            }
        )
    }

    if (entryToDelete != null) {
        AlertDialog(
            onDismissRequest = { entryToDelete = null },
            title = { Text("Delete Period") },
            text = { Text("Remove ${entryToDelete?.periodName} (${entryToDelete?.subject}) on ${entryToDelete?.day}?") },
            confirmButton = {
                Button(
                    onClick = {
                        entryToDelete?.let { onDeleteEntry(it) }
                        entryToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { entryToDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DayTimetableCard(
    day: String,
    periods: List<TimetableEntry>,
    onAddPeriod: () -> Unit,
    onEditPeriod: (TimetableEntry) -> Unit,
    onDeletePeriod: (TimetableEntry) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Day Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(SchoolNavy)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = day,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                    )
                }

                TextButton(onClick = onAddPeriod) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Period", fontSize = 11.sp)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp), color = Color(0xFFF1F5F9))

            if (periods.isEmpty()) {
                Text(
                    text = "No periods scheduled for $day",
                    fontSize = 11.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    periods.forEach { p ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.width(75.dp)) {
                                Text(p.periodName, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = SchoolNavy)
                                Text("${p.startTime}\n${p.endTime}", fontSize = 9.sp, color = Color.Gray, lineHeight = 11.sp)
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(p.subject, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                                Text("Teacher: ${p.teacherName.ifBlank { "Unassigned" }}", fontSize = 11.sp, color = Color.DarkGray)
                            }

                            IconButton(onClick = { onEditPeriod(p) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = SchoolNavy, modifier = Modifier.size(16.dp))
                            }

                            IconButton(onClick = { onDeletePeriod(p) }, modifier = Modifier.size(30.dp)) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaid, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TimetableEntryDialog(
    entry: TimetableEntry?,
    selectedClass: String,
    onDismiss: () -> Unit,
    onSave: (TimetableEntry) -> Unit
) {
    var day by remember { mutableStateOf(entry?.day ?: "Monday") }
    var periodName by remember { mutableStateOf(entry?.periodName ?: "Period 1") }
    var periodIndex by remember { mutableStateOf(entry?.periodIndex?.toString() ?: "1") }
    var startTime by remember { mutableStateOf(entry?.startTime ?: "08:00 AM") }
    var endTime by remember { mutableStateOf(entry?.endTime ?: "08:45 AM") }
    var subject by remember { mutableStateOf(entry?.subject ?: "") }
    var teacherName by remember { mutableStateOf(entry?.teacherName ?: "") }
    var room by remember { mutableStateOf(entry?.room ?: "Classroom") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(if (entry == null || entry.id == 0L) "Add Timetable Period" else "Edit Timetable Period", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = day,
                    onValueChange = { day = it },
                    label = { Text("Day (Monday - Saturday)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = periodName,
                        onValueChange = { periodName = it },
                        label = { Text("Period Name") },
                        modifier = Modifier.weight(1.5f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = periodIndex,
                        onValueChange = { periodIndex = it },
                        label = { Text("Order #") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        label = { Text("End Time") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = subject,
                    onValueChange = { subject = it },
                    label = { Text("Subject * / مضمون") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = teacherName,
                    onValueChange = { teacherName = it },
                    label = { Text("Teacher Name / استاد کا نام") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = room,
                    onValueChange = { room = it },
                    label = { Text("Room / کمرا") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (subject.isNotBlank()) {
                        onSave(
                            TimetableEntry(
                                id = entry?.id ?: 0L,
                                className = selectedClass,
                                section = entry?.section ?: "A",
                                academicSession = entry?.academicSession ?: "2026 - 2027",
                                day = day.trim(),
                                periodIndex = periodIndex.toIntOrNull() ?: 1,
                                periodName = periodName.trim(),
                                startTime = startTime.trim(),
                                endTime = endTime.trim(),
                                subject = subject.trim(),
                                teacherName = teacherName.trim(),
                                room = room.trim()
                            )
                        )
                    }
                },
                enabled = subject.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
            ) {
                Text("Save Period")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
