package com.example.ui.screens

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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.ReceiptLong
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.Student
import com.example.ui.components.StatusChip
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusUnpaid
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun FeeScreen(
    feeRecords: List<FeeRecord>,
    students: List<Student>,
    selectedMonth: String,
    onMonthChange: (String) -> Unit,
    selectedStatus: String,
    onStatusChange: (String) -> Unit,
    onSaveFeeRecord: (
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
        id: Long
    ) -> Unit,
    onDeleteFeeRecord: (FeeRecord) -> Unit,
    onPreviewReceipt: (FeeRecord) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<FeeRecord?>(null) }
    var recordToDelete by remember { mutableStateOf<FeeRecord?>(null) }

    val distinctMonths = listOf("All") + feeRecords.map { it.month }.distinct()
    val statuses = listOf("All", "Paid", "Unpaid", "Partially Paid")

    val filteredRecords = feeRecords.filter { record ->
        val matchesMonth = selectedMonth == "All" || record.month == selectedMonth
        val matchesStatus = selectedStatus == "All" || record.paymentStatus.equals(selectedStatus, ignoreCase = true)
        matchesMonth && matchesStatus
    }

    val totalBilled = filteredRecords.sumOf { it.totalFee }
    val totalCollected = filteredRecords.sumOf { it.paidAmount }
    val totalRemaining = filteredRecords.sumOf { it.remainingAmount }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    showDialog = true
                },
                containerColor = SchoolEmerald,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_fee_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Fee Record")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .testTag("fee_screen")
        ) {
            // Month Filters
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(distinctMonths) { m ->
                    FilterChip(
                        selected = selectedMonth == m,
                        onClick = { onMonthChange(m) },
                        label = { Text(m) }
                    )
                }
            }

            // Status Filter Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(statuses) { st ->
                    FilterChip(
                        selected = selectedStatus == st,
                        onClick = { onStatusChange(st) },
                        label = { Text(st) }
                    )
                }
            }

            // Summary Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.06f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Payable", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "Rs. ${String.format(Locale.US, "%,.0f", totalBilled)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Collected", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "Rs. ${String.format(Locale.US, "%,.0f", totalCollected)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusPaid
                        )
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Remaining", fontSize = 11.sp, color = Color.Gray)
                        Text(
                            "Rs. ${String.format(Locale.US, "%,.0f", totalRemaining)}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = StatusUnpaid
                        )
                    }
                }
            }

            // Fee Records List
            if (filteredRecords.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No fee records found for the selected filter.\nTap '+' to generate or add a fee record.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredRecords, key = { it.id }) { record ->
                        val stu = students.find { it.id == record.studentId }
                        FeeRecordCard(
                            record = record,
                            studentPhotoUri = stu?.photoUri,
                            onEdit = {
                                editingRecord = record
                                showDialog = true
                            },
                            onDelete = { recordToDelete = record },
                            onPreview = { onPreviewReceipt(record) }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        FeeFormDialog(
            record = editingRecord,
            students = students,
            onDismiss = { showDialog = false },
            onSave = { sId, sName, fName, cls, sec, roll, month, mFee, aFee, oFee, disc, paid, date, id ->
                onSaveFeeRecord(sId, sName, fName, cls, sec, roll, month, mFee, aFee, oFee, disc, paid, date, id)
                showDialog = false
            }
        )
    }

    if (recordToDelete != null) {
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text("Delete Fee Record") },
            text = { Text("Are you sure you want to delete the fee record for ${recordToDelete?.studentName} (${recordToDelete?.month})?") },
            confirmButton = {
                Button(
                    onClick = {
                        recordToDelete?.let { onDeleteFeeRecord(it) }
                        recordToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusUnpaid)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun FeeRecordCard(
    record: FeeRecord,
    studentPhotoUri: String?,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onPreview: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("fee_card_${record.id}"),
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
                        StatusChip(record.paymentStatus)
                    }

                    Text(
                        text = "Roll #${record.rollNumber} • ${record.className} (${record.section})",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                    Text(
                        text = "Month: ${record.month}",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = SchoolNavyLight
                        )
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = Color(0xFFF1F5F9))

            // Financial Summary row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Total Fee", fontSize = 10.sp, color = Color.Gray)
                    Text("Rs. ${String.format(Locale.US, "%.0f", record.totalFee)}", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Paid Amount", fontSize = 10.sp, color = Color.Gray)
                    Text("Rs. ${String.format(Locale.US, "%.0f", record.paidAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusPaid)
                }
                Column {
                    Text("Remaining", fontSize = 10.sp, color = Color.Gray)
                    Text("Rs. ${String.format(Locale.US, "%.0f", record.remainingAmount)}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = StatusUnpaid)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Edit, Preview, Print/Receipt
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
                    Icon(Icons.Default.ReceiptLong, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Receipt / رسید", fontSize = 11.sp)
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(onClick = onEdit, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Fee", tint = SchoolNavy)
                }

                IconButton(onClick = onDelete, modifier = Modifier.size(34.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Fee", tint = StatusUnpaid)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeeFormDialog(
    record: FeeRecord?,
    students: List<Student>,
    onDismiss: () -> Unit,
    onSave: (
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
        id: Long
    ) -> Unit
) {
    var selectedStudent by remember {
        mutableStateOf(
            if (record != null) students.find { it.id == record.studentId }
            else students.firstOrNull()
        )
    }
    var expandedDropdown by remember { mutableStateOf(false) }

    var month by remember { mutableStateOf(record?.month ?: "October 2026") }
    var monthlyFeeStr by remember { mutableStateOf(record?.monthlyFee?.toString() ?: "3500") }
    var admissionFeeStr by remember { mutableStateOf(record?.admissionFee?.toString() ?: "0") }
    var otherChargesStr by remember { mutableStateOf(record?.otherCharges?.toString() ?: "300") }
    var discountStr by remember { mutableStateOf(record?.discount?.toString() ?: "0") }
    var paidAmountStr by remember { mutableStateOf(record?.paidAmount?.toString() ?: "3800") }
    var paymentDate by remember {
        mutableStateOf(
            record?.paymentDate ?: SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        )
    }

    // Auto formula calculation
    val mFee = monthlyFeeStr.toDoubleOrNull() ?: 0.0
    val aFee = admissionFeeStr.toDoubleOrNull() ?: 0.0
    val oFee = otherChargesStr.toDoubleOrNull() ?: 0.0
    val disc = discountStr.toDoubleOrNull() ?: 0.0
    val autoTotal = (mFee + aFee + oFee - disc).coerceAtLeast(0.0)

    val paid = paidAmountStr.toDoubleOrNull() ?: 0.0
    val autoRemaining = (autoTotal - paid).coerceAtLeast(0.0)

    val autoStatus = when {
        paid >= autoTotal && autoTotal > 0 -> "Paid"
        paid > 0 -> "Partially Paid"
        else -> "Unpaid"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (record == null) "Add Fee Record / اندراج فیس" else "Edit Fee Record / تدوین فیس",
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
                // Select Student Dropdown
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    OutlinedTextField(
                        value = selectedStudent?.let { "${it.name} (Roll #${it.rollNumber} - ${it.className})" } ?: "Select Student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Student *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        students.forEach { s ->
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
                    value = month,
                    onValueChange = { month = it },
                    label = { Text("Fee Month * (e.g. October 2026)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = monthlyFeeStr,
                        onValueChange = { monthlyFeeStr = it },
                        label = { Text("Monthly Fee") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = admissionFeeStr,
                        onValueChange = { admissionFeeStr = it },
                        label = { Text("Admission Fee") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = otherChargesStr,
                        onValueChange = { otherChargesStr = it },
                        label = { Text("Other Charges") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = discountStr,
                        onValueChange = { discountStr = it },
                        label = { Text("Discount (-)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                // Live Auto Calculation Banner
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SchoolNavy.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(
                            "Formula: Total = Monthly + Admission + Other - Discount",
                            fontSize = 10.sp,
                            color = Color.DarkGray
                        )
                        Text(
                            "Auto Calculated Total Fee: Rs. ${String.format(Locale.US, "%.0f", autoTotal)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                    }
                }

                OutlinedTextField(
                    value = paidAmountStr,
                    onValueChange = { paidAmountStr = it },
                    label = { Text("Paid Amount (PKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = paymentDate,
                    onValueChange = { paymentDate = it },
                    label = { Text("Payment Date") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Balance banner
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Remaining: Rs. ${String.format(Locale.US, "%.0f", autoRemaining)}",
                        fontWeight = FontWeight.Bold,
                        color = if (autoRemaining == 0.0) StatusPaid else StatusUnpaid
                    )
                    StatusChip(status = autoStatus)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent
                    if (s != null) {
                        onSave(
                            s.id,
                            s.name,
                            s.fatherName,
                            s.className,
                            s.section,
                            s.rollNumber,
                            month.trim(),
                            mFee,
                            aFee,
                            oFee,
                            disc,
                            paid,
                            paymentDate.trim(),
                            record?.id ?: 0L
                        )
                    }
                },
                enabled = selectedStudent != null,
                colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
            ) {
                Text("Save Fee Record")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
