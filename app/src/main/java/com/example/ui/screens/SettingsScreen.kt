package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.mutableStateListOf
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
import com.example.data.model.GradingRule
import com.example.data.model.SchoolProfile
import com.example.ui.components.SchoolLogoImage
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusUnpaid

@Composable
fun SettingsScreen(
    profile: SchoolProfile?,
    gradingRules: List<GradingRule>,
    onSaveProfile: (SchoolProfile) -> Unit,
    onSaveGradingRule: (GradingRule) -> Unit,
    onDeleteGradingRule: (GradingRule) -> Unit,
    onAddGradingRule: (GradingRule) -> Unit
) {
    var schoolName by remember(profile) { mutableStateOf(profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول") }
    var englishName by remember(profile) { mutableStateOf(profile?.englishName ?: "Iqra Rauzat-ul-Ilm Public School") }
    var tagline by remember(profile) { mutableStateOf(profile?.tagline ?: "معیاری دینی و عصری تعلیم کا عظیم گہوارہ") }
    var address by remember(profile) { mutableStateOf(profile?.address ?: "") }
    var phone by remember(profile) { mutableStateOf(profile?.phone ?: "") }
    var email by remember(profile) { mutableStateOf(profile?.email ?: "") }
    var principalName by remember(profile) { mutableStateOf(profile?.principalName ?: "") }
    var session by remember(profile) { mutableStateOf(profile?.academicSession ?: "2026 - 2027") }
    var passingPct by remember(profile) { mutableStateOf(profile?.passingPercentage?.toString() ?: "40") }
    var minSubMarks by remember(profile) { mutableStateOf(profile?.minSubjectPassingMarks?.toString() ?: "33") }
    var logoUri by remember(profile) { mutableStateOf(profile?.logoUri) }

    var newRuleDialog by remember { mutableStateOf(false) }

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            logoUri = uri.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("settings_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // School Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "School Profile & Details / سکول معلومات",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SchoolNavy)
                )

                // Logo selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SchoolLogoImage(logoUri = logoUri, modifier = Modifier.size(64.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Button(
                            onClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = SchoolNavyLight),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Upload / Change Logo", fontSize = 12.sp)
                        }
                        if (logoUri != null) {
                            TextButton(onClick = { logoUri = null }) {
                                Text("Reset to Default Crest", color = StatusUnpaid, fontSize = 11.sp)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = schoolName,
                    onValueChange = { schoolName = it },
                    label = { Text("School Name (Urdu) *") },
                    modifier = Modifier.fillMaxWidth().testTag("input_school_name"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = englishName,
                    onValueChange = { englishName = it },
                    label = { Text("School Name (English)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = tagline,
                    onValueChange = { tagline = it },
                    label = { Text("School Tagline / نعرہ") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("School Address / پتہ") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = principalName,
                        onValueChange = { principalName = it },
                        label = { Text("Principal Name") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = session,
                        onValueChange = { session = it },
                        label = { Text("Academic Session") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = passingPct,
                        onValueChange = { passingPct = it },
                        label = { Text("Passing Percentage (%)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minSubMarks,
                        onValueChange = { minSubMarks = it },
                        label = { Text("Min Subject Pass Marks") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                Button(
                    onClick = {
                        onSaveProfile(
                            SchoolProfile(
                                id = 1L,
                                name = schoolName.trim(),
                                englishName = englishName.trim(),
                                tagline = tagline.trim(),
                                address = address.trim(),
                                phone = phone.trim(),
                                email = email.trim(),
                                principalName = principalName.trim(),
                                academicSession = session.trim(),
                                passingPercentage = passingPct.toDoubleOrNull() ?: 40.0,
                                minSubjectPassingMarks = minSubMarks.toDoubleOrNull() ?: 33.0,
                                logoUri = logoUri
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().testTag("save_school_profile_button"),
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save School Profile")
                }
            }
        }

        // Grading Scale Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Grading System & Ranges / گریڈنگ کا نظام",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = SchoolNavy)
                    )
                    TextButton(onClick = { newRuleDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Rule", fontSize = 11.sp)
                    }
                }

                Text(
                    text = "Rules are automatically used for all result sheets, class positions, and reports. Editing ranges automatically recalculates existing results.",
                    fontSize = 10.sp,
                    color = Color.Gray
                )

                // Table Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Grade", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(50.dp))
                    Text("Min %", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(60.dp))
                    Text("Remarks", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(36.dp))
                }
                HorizontalDivider(color = Color(0xFFE2E8F0))

                gradingRules.forEach { rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(rule.grade, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = SchoolNavy, modifier = Modifier.width(50.dp))
                        Text("${rule.minPercentage.toInt()}%", fontSize = 12.sp, modifier = Modifier.width(60.dp))
                        Text(rule.remarks, fontSize = 11.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                        IconButton(onClick = { onDeleteGradingRule(rule) }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = StatusUnpaid, modifier = Modifier.size(16.dp))
                        }
                    }
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                }
            }
        }
    }

    if (newRuleDialog) {
        var grade by remember { mutableStateOf("") }
        var minPercentage by remember { mutableStateOf("") }
        var remarks by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { newRuleDialog = false },
            title = { Text("Add Grading Rule") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = grade, onValueChange = { grade = it }, label = { Text("Grade (e.g. A+)") })
                    OutlinedTextField(value = minPercentage, onValueChange = { minPercentage = it }, label = { Text("Minimum Percentage (%)") })
                    OutlinedTextField(value = remarks, onValueChange = { remarks = it }, label = { Text("Remarks (e.g. Excellent)") })
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val minP = minPercentage.toDoubleOrNull() ?: 0.0
                        if (grade.isNotBlank()) {
                            onAddGradingRule(
                                GradingRule(
                                    grade = grade.trim(),
                                    minPercentage = minP,
                                    remarks = remarks.trim()
                                )
                            )
                            newRuleDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SchoolNavy)
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { newRuleDialog = false }) { Text("Cancel") }
            }
        )
    }
}
