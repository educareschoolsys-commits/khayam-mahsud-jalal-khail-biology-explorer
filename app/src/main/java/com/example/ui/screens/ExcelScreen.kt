package com.example.ui.screens

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.export.ExcelManager
import com.example.export.ExcelRow
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import java.util.Locale

@Composable
fun ExcelScreen(
    importRows: List<ExcelRow>,
    isImporting: Boolean,
    onUploadExcelUri: (Context, Uri) -> Unit,
    onLoadSampleTemplate: (Context) -> Unit,
    onUpdateRow: (Int, ExcelRow) -> Unit,
    onRemoveRow: (Int) -> Unit,
    onClearRows: () -> Unit,
    onConfirmImport: () -> Unit
) {
    val context = LocalContext.current
    var editingRowIndex by remember { mutableStateOf<Int?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            onUploadExcelUri(context, uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("excel_screen"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Upload & Template Actions Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.06f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Excel & Spreadsheet Import / ایکسل درآمد",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SchoolNavy)
                )
                Text(
                    text = "Import '.xlsx' or '.csv' files with Student Details, Marks, and Fees. The system automatically calculates Total Marks, Percentage, Grade, Position, Pass/Fail, and places them into Students, Results, and Fee systems.",
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { filePicker.launch(arrayOf("*/*")) },
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                        modifier = Modifier.weight(1f).testTag("choose_excel_file_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Upload Excel (.xlsx/.csv)", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = { onLoadSampleTemplate(context) },
                        modifier = Modifier.weight(1f).testTag("load_sample_template_button"),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Load Sample Template", fontSize = 11.sp)
                    }
                }
            }
        }

        if (isImporting) {
            Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = SchoolNavy)
            }
        } else if (importRows.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(54.dp), tint = Color.LightGray)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        "No file currently loaded for preview.\nTap 'Upload Excel' to pick your school file\nor tap 'Load Sample Template' to test.",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            }
        } else {
            // Preview Banner and Confirm Bar
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Import Preview: ${importRows.size} Records", fontWeight = FontWeight.Bold, color = SchoolNavy, fontSize = 13.sp)
                        Text("Edit or verify any row before confirming", fontSize = 10.sp, color = Color.Gray)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(onClick = onClearRows) {
                            Text("Cancel", color = StatusUnpaid, fontSize = 11.sp)
                        }

                        Button(
                            onClick = onConfirmImport,
                            colors = ButtonDefaults.buttonColors(containerColor = SchoolEmerald),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("confirm_import_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Confirm Import", fontSize = 11.sp)
                        }
                    }
                }
            }

            // Editable Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SchoolNavy, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Roll", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(36.dp))
                Text("Name", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.3f))
                Text("Class", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp))
                Text("Subject", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                Text("Marks", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                Text("Fee", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)
                Spacer(modifier = Modifier.width(28.dp))
            }

            // Editable Rows List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(importRows) { index, row ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { editingRowIndex = index },
                        shape = RoundedCornerShape(4.dp),
                        colors = CardDefaults.cardColors(containerColor = if (index % 2 == 1) Color(0xFFF8FAFC) else Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(row.rollNumber, fontSize = 11.sp, modifier = Modifier.width(36.dp))
                            Text(row.studentName, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1.3f))
                            Text(row.className, fontSize = 10.sp, modifier = Modifier.width(55.dp))
                            Text(row.subject, fontSize = 10.sp, modifier = Modifier.weight(1f))
                            Text("${row.obtainedMarks.toInt()}/${row.totalMarks.toInt()}", fontSize = 10.sp, modifier = Modifier.width(55.dp), textAlign = TextAlign.Center)
                            Text("${row.paidFee.toInt()}", fontSize = 10.sp, modifier = Modifier.width(50.dp), textAlign = TextAlign.End)

                            IconButton(
                                onClick = { onRemoveRow(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Remove", tint = StatusUnpaid, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (editingRowIndex != null) {
        val idx = editingRowIndex!!
        if (idx in importRows.indices) {
            EditExcelRowDialog(
                row = importRows[idx],
                onDismiss = { editingRowIndex = null },
                onSave = { updated ->
                    onUpdateRow(idx, updated)
                    editingRowIndex = null
                }
            )
        }
    }
}

@Composable
fun EditExcelRowDialog(
    row: ExcelRow,
    onDismiss: () -> Unit,
    onSave: (ExcelRow) -> Unit
) {
    var name by remember { mutableStateOf(row.studentName) }
    var father by remember { mutableStateOf(row.fatherName) }
    var cls by remember { mutableStateOf(row.className) }
    var sec by remember { mutableStateOf(row.section) }
    var roll by remember { mutableStateOf(row.rollNumber) }
    var sub by remember { mutableStateOf(row.subject) }
    var totMarks by remember { mutableStateOf(row.totalMarks.toString()) }
    var obtMarks by remember { mutableStateOf(row.obtainedMarks.toString()) }
    var monthlyFee by remember { mutableStateOf(row.monthlyFee.toString()) }
    var paidFee by remember { mutableStateOf(row.paidFee.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Spreadsheet Row", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Student Name") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = father, onValueChange = { father = it }, label = { Text("Father Name") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = cls, onValueChange = { cls = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = sec, onValueChange = { sec = it }, label = { Text("Section") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = roll, onValueChange = { roll = it }, label = { Text("Roll") }, modifier = Modifier.weight(1f))
                }
                OutlinedTextField(value = sub, onValueChange = { sub = it }, label = { Text("Subject") }, modifier = Modifier.fillMaxWidth())
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = totMarks, onValueChange = { totMarks = it }, label = { Text("Total Marks") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = obtMarks, onValueChange = { obtMarks = it }, label = { Text("Obtained Marks") }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(value = monthlyFee, onValueChange = { monthlyFee = it }, label = { Text("Monthly Fee") }, modifier = Modifier.weight(1f))
                    OutlinedTextField(value = paidFee, onValueChange = { paidFee = it }, label = { Text("Paid Fee") }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        ExcelRow(
                            studentName = name,
                            fatherName = father,
                            className = cls,
                            section = sec,
                            rollNumber = roll,
                            subject = sub,
                            totalMarks = totMarks.toDoubleOrNull() ?: 100.0,
                            obtainedMarks = obtMarks.toDoubleOrNull() ?: 0.0,
                            monthlyFee = monthlyFee.toDoubleOrNull() ?: 3500.0,
                            paidFee = paidFee.toDoubleOrNull() ?: 0.0
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
            ) {
                Text("Update Row")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
