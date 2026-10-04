package com.example.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FeeRecord
import com.example.data.model.ResultRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.ui.components.SchoolLogoImage
import com.example.ui.components.StudentAvatar
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusUnpaid
import com.example.viewmodel.PreviewState
import com.example.viewmodel.PreviewType
import java.util.Locale

@Composable
fun DocumentPreviewScreen(
    profile: SchoolProfile?,
    previewState: PreviewState,
    students: List<Student>,
    onEdit: () -> Unit,
    onSaveToMobile: (Context) -> Unit,
    onExportPdf: (Context) -> Unit,
    onExportDocx: (Context) -> Unit,
    onPrint: (Context) -> Unit,
    onShare: (Context, String) -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFE2E8F0))
            .testTag("document_preview_screen")
    ) {
        // Sticky Top Action Bar
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(bottomStart = 12.dp, bottomEnd = 12.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sheet Preview / پیش نظارہ",
                        fontWeight = FontWeight.Bold,
                        color = SchoolNavy,
                        fontSize = 14.sp
                    )

                    // Edit button
                    OutlinedButton(
                        onClick = onEdit,
                        modifier = Modifier.height(32.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", fontSize = 11.sp)
                    }
                }

                // Action Buttons: Save | PDF | Word | Print | Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Button(
                        onClick = { onSaveToMobile(context) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("preview_save_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Save", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { onExportPdf(context) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("preview_pdf_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("PDF", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { onExportDocx(context) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("preview_word_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1565C0)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Word", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { onPrint(context) },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("preview_print_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF455A64)),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Print", fontSize = 10.sp)
                    }

                    Button(
                        onClick = { onShare(context, "PDF") },
                        modifier = Modifier.weight(1f).height(36.dp).testTag("preview_share_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = SchoolEmerald),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(3.dp))
                        Text("Share", fontSize = 10.sp)
                    }
                }
            }
        }

        // Scrollable A4 Document Paper Canvas
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .verticalScroll(rememberScrollState()),
            contentAlignment = Alignment.TopCenter
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                shape = RoundedCornerShape(4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                // A4 Document Border Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, SchoolNavy, RoundedCornerShape(4.dp))
                        .padding(12.dp)
                ) {
                    when (previewState.type) {
                        PreviewType.STUDENT_RESULT -> {
                            StudentResultPaperView(
                                profile = profile,
                                result = previewState.resultRecord,
                                subjects = previewState.subjects,
                                stats = previewState.classStats,
                                studentPhotoUri = previewState.resultRecord?.let { res ->
                                    students.find { it.id == res.studentId }?.photoUri
                                }
                            )
                        }
                        PreviewType.FEE_RECEIPT -> {
                            FeeReceiptPaperView(
                                profile = profile,
                                fee = previewState.feeRecord,
                                studentPhotoUri = previewState.feeRecord?.let { f ->
                                    students.find { it.id == f.studentId }?.photoUri
                                }
                            )
                        }
                        PreviewType.CLASS_RESULT -> {
                            ClassResultPaperView(
                                profile = profile,
                                className = previewState.selectedClass,
                                stats = previewState.classStats
                            )
                        }
                        PreviewType.TIMETABLE -> {
                            TimetablePaperView(
                                profile = profile,
                                className = previewState.selectedClass,
                                entries = previewState.timetableEntries
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentResultPaperView(
    profile: SchoolProfile?,
    result: ResultRecord?,
    subjects: List<com.example.data.model.SubjectScore>,
    stats: com.example.data.model.ClassResultStats,
    studentPhotoUri: String?
) {
    if (result == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        // Document Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SchoolLogoImage(logoUri = profile?.logoUri, modifier = Modifier.size(54.dp))

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = SchoolNavy,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = profile?.englishName ?: "Iqra Rauzat-ul-Ilm Public School",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = SchoolNavyLight
                )
                Text(
                    text = "${profile?.tagline} | Phone: ${profile?.phone}",
                    fontSize = 9.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = profile?.address ?: "",
                    fontSize = 8.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            StudentAvatar(photoUri = studentPhotoUri, name = result.studentName, modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Badge
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SchoolNavy, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("STUDENT RESULT SHEET / نتیجہ کارڈ", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Student Info Grid
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Student Name: ${result.studentName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Roll No: ${result.rollNumber}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Father Name: ${result.fatherName}", fontSize = 11.sp)
                    Text("Class: ${result.className} (${result.section})", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Exam: ${result.examTitle}", fontSize = 10.sp, color = Color.DarkGray)
                    Text("Date: ${result.examDate} (${profile?.academicSession})", fontSize = 10.sp, color = Color.DarkGray)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Table
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SchoolNavy)
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Sr", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(22.dp))
            Text("Subject / مضمون", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f))
            Text("Total", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text("Obt", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text("%", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text("Grd", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            Text("Status", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(45.dp), textAlign = TextAlign.End)
        }

        subjects.forEachIndexed { i, s ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(if (i % 2 == 1) Color(0xFFF8FAFC) else Color.White)
                    .padding(vertical = 5.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${i + 1}", fontSize = 10.sp, modifier = Modifier.width(22.dp))
                Text(s.subjectName, fontSize = 10.sp, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1.5f))
                Text("${s.totalMarks.toInt()}", fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                Text(String.format(Locale.US, "%.1f", s.obtainedMarks), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                Text(String.format(Locale.US, "%.0f%%", s.percentage), fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
                Text(s.grade, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
                Text(
                    s.passStatus,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (s.passStatus == "Pass") StatusPaid else StatusUnpaid,
                    modifier = Modifier.width(45.dp),
                    textAlign = TextAlign.End
                )
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }

        // Grand Total Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0))
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Total", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(22.dp))
            Text("Grand Total / کل نمبرات", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.weight(1.5f))
            Text("${result.totalMarks.toInt()}", fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text(String.format(Locale.US, "%.1f", result.obtainedMarks), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text(String.format(Locale.US, "%.1f%%", result.percentage), fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(42.dp), textAlign = TextAlign.Center)
            Text(result.grade, fontWeight = FontWeight.Bold, fontSize = 10.sp, modifier = Modifier.width(32.dp), textAlign = TextAlign.Center)
            Text(
                result.passStatus,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = if (result.passStatus == "Pass") StatusPaid else StatusUnpaid,
                modifier = Modifier.width(45.dp),
                textAlign = TextAlign.End
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Summary Badges
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1F5F9), RoundedCornerShape(6.dp))
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Percentage", fontSize = 9.sp, color = Color.Gray)
                Text(String.format(Locale.US, "%.1f%%", result.percentage), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = StatusPaid)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Grade", fontSize = 9.sp, color = Color.Gray)
                Text(result.grade, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SchoolNavy)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Position", fontSize = 9.sp, color = Color.Gray)
                Text(result.position.ifBlank { "-" }, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SchoolGold)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Status", fontSize = 9.sp, color = Color.Gray)
                Text(result.passStatus, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = if (result.passStatus == "Pass") StatusPaid else StatusUnpaid)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Class Benchmarks
        Text(
            "Class Average: ${String.format(Locale.US, "%.1f%%", stats.classAverage)} | Highest: ${stats.highestMarks.toInt()} | Lowest: ${stats.lowestMarks.toInt()} | Pass Rate: ${String.format(Locale.US, "%.1f%%", stats.passPercentage)}",
            fontSize = 9.sp,
            color = Color.DarkGray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(26.dp))

        // Signatures
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(1.dp).background(Color.Black))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Class Teacher", fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(1.dp).background(Color.Black))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Exam Incharge", fontSize = 9.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(1.dp).background(Color.Black))
                Spacer(modifier = Modifier.height(2.dp))
                Text(profile?.principalName ?: "Principal", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun FeeReceiptPaperView(
    profile: SchoolProfile?,
    fee: FeeRecord?,
    studentPhotoUri: String?
) {
    if (fee == null) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SchoolLogoImage(logoUri = profile?.logoUri, modifier = Modifier.size(54.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول", fontWeight = FontWeight.Bold, fontSize = 17.sp, color = SchoolNavy)
                Text(profile?.englishName ?: "Iqra Rauzat-ul-Ilm Public School", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = SchoolNavyLight)
                Text("${profile?.address} | Ph: ${profile?.phone}", fontSize = 9.sp, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(8.dp))
            StudentAvatar(photoUri = studentPhotoUri, name = fee.studentName, modifier = Modifier.size(54.dp))
        }

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SchoolNavy, RoundedCornerShape(4.dp))
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("FEE RECEIPT / فیس رسید", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
        ) {
            Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Receipt No: REC-${fee.id + 1000}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Date: ${fee.paymentDate}", fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Student: ${fee.studentName}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    Text("Roll No: ${fee.rollNumber}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Father: ${fee.fatherName}", fontSize = 11.sp)
                    Text("Class: ${fee.className} (${fee.section})", fontSize = 11.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Breakdown Table
        val items = listOf(
            Pair("Monthly Tuition Fee (${fee.month})", fee.monthlyFee),
            Pair("Admission Fee", fee.admissionFee),
            Pair("Exam / Other Charges", fee.otherCharges),
            Pair("Special Discount", -fee.discount)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SchoolNavy)
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Particulars / تفصیل", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Text("Amount (PKR)", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        }

        items.forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(item.first, fontSize = 10.sp)
                Text(if (item.second < 0) "- ${String.format(Locale.US, "%.0f", -item.second)}" else String.format(Locale.US, "%.0f", item.second), fontSize = 10.sp)
            }
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFE2E8F0))
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Total Fee / کل واجب الادا", fontWeight = FontWeight.Bold, fontSize = 11.sp)
            Text("Rs. ${String.format(Locale.US, "%.0f", fee.totalFee)}", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Paid Amount / وصول شدہ رقم", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusPaid)
            Text("Rs. ${String.format(Locale.US, "%.0f", fee.paidAmount)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusPaid)
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFFEE2E2))
                .padding(vertical = 6.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Remaining Balance / بقایا واجبات", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusUnpaid)
            Text("Rs. ${String.format(Locale.US, "%.0f", fee.remainingAmount)}", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = StatusUnpaid)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(1.dp).background(Color.Black))
                Spacer(modifier = Modifier.height(2.dp))
                Text("Accountant / کلرک", fontSize = 9.sp)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(modifier = Modifier.width(110.dp).height(1.dp).background(Color.Black))
                Spacer(modifier = Modifier.height(2.dp))
                Text(profile?.principalName ?: "Principal", fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ClassResultPaperView(
    profile: SchoolProfile?,
    className: String,
    stats: com.example.data.model.ClassResultStats
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SchoolNavy, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("CLASS RESULT MASTER SHEET - $className", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolGold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Text("Total Students: ${stats.totalStudents} | Passed: ${stats.passedStudents} | Failed: ${stats.failedStudents} | Pass Rate: ${String.format(Locale.US, "%.1f%%", stats.passPercentage)}", fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(8.dp))
        Text("Class Average: ${String.format(Locale.US, "%.1f%%", stats.classAverage)} | Highest: ${stats.highestMarks.toInt()} | Lowest: ${stats.lowestMarks.toInt()}", fontSize = 10.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun TimetablePaperView(
    profile: SchoolProfile?,
    className: String,
    entries: List<com.example.data.model.TimetableEntry>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = SchoolNavy, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Text("CLASS TIMETABLE / اوقات کار - $className", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SchoolGold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        entries.forEach { e ->
            Text("${e.day} • ${e.periodName} (${e.startTime} - ${e.endTime}) : ${e.subject} - Teacher: ${e.teacherName}", fontSize = 10.sp)
            HorizontalDivider(color = Color(0xFFF1F5F9))
        }
    }
}
