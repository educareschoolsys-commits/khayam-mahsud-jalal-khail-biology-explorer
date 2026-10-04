package com.example.ui.screens

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DashboardStats
import com.example.data.model.SchoolProfile
import com.example.ui.components.SchoolLogoImage
import com.example.ui.theme.SchoolEmerald
import com.example.ui.theme.SchoolGold
import com.example.ui.theme.SchoolNavy
import com.example.ui.theme.SchoolNavyDark
import com.example.ui.theme.SchoolNavyLight
import com.example.ui.theme.StatusPaid
import com.example.ui.theme.StatusPartial
import com.example.ui.theme.StatusUnpaid
import com.example.viewmodel.Screen
import java.util.Locale

data class DashboardItem(
    val title: String,
    val urduTitle: String,
    val icon: ImageVector,
    val screen: Screen,
    val color: Color,
    val testTag: String
)

@Composable
fun DashboardScreen(
    profile: SchoolProfile?,
    stats: DashboardStats,
    onNavigate: (Screen) -> Unit
) {
    val menuItems = listOf(
        DashboardItem("Students", "طلباء و طالبات", Icons.Default.School, Screen.STUDENTS, Color(0xFF1E88E5), "dashboard_students"),
        DashboardItem("Fee Management", "فیس مینجمنٹ", Icons.Default.AccountBalanceWallet, Screen.FEES, Color(0xFF43A047), "dashboard_fees"),
        DashboardItem("Timetable", "اوقات کار (ٹائم ٹیبل)", Icons.Default.CalendarMonth, Screen.TIMETABLE, Color(0xFFFB8C00), "dashboard_timetable"),
        DashboardItem("Results", "نتائج و پوزیشنز", Icons.Default.Assessment, Screen.RESULTS, Color(0xFF8E24AA), "dashboard_results"),
        DashboardItem("Photos", "تصاویر و لوگو", Icons.Default.PhotoLibrary, Screen.PHOTOS, Color(0xFF00ACC1), "dashboard_photos"),
        DashboardItem("Excel Upload", "ایکسل امپورٹ", Icons.Default.UploadFile, Screen.EXCEL_UPLOAD, Color(0xFF3949AB), "dashboard_excel"),
        DashboardItem("Saved Documents", "محفوظ دستاویزات", Icons.Default.FolderOpen, Screen.SAVED_DOCUMENTS, Color(0xFF546E7A), "dashboard_documents"),
        DashboardItem("Preview & Hub", "دستاویزات کا پیش نظارہ", Icons.Default.Preview, Screen.PREVIEW, Color(0xFFD81B60), "dashboard_preview"),
        DashboardItem("School Settings", "سکول سیٹنگز", Icons.Default.Settings, Screen.SETTINGS, Color(0xFF5E35B1), "dashboard_settings")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero School Header Banner
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("school_header_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SchoolNavy),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(SchoolNavy, SchoolNavyDark)
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, SchoolGold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            SchoolLogoImage(
                                logoUri = profile?.logoUri,
                                modifier = Modifier.size(72.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile?.name ?: "اقراۃ روضۃ العلم پبلک سکول",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = profile?.englishName ?: "Iqra Rauzat-ul-Ilm Public School",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp,
                                    color = SchoolGold
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = profile?.tagline ?: "معیاری دینی و عصری تعلیم کا عظیم گہوارہ",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Academic Session: ${profile?.academicSession ?: "2026 - 2027"}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }
        }

        // Live KPI Statistics Section
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Dashboard Statistics / اعداد و شمار",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = SchoolNavy
                    )
                )

                // Row 1: Students, Classes, Results
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Students",
                        urdu = "کل طلباء",
                        value = "${stats.totalStudents}",
                        color = Color(0xFF1E88E5),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Classes",
                        urdu = "کلاسز",
                        value = "${stats.totalClasses}",
                        color = Color(0xFF8E24AA),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Results",
                        urdu = "نتائج",
                        value = "${stats.resultsCreated}",
                        color = Color(0xFFFB8C00),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 2: Fees Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Paid Fees",
                        urdu = "ادا شدہ فیس",
                        value = "${stats.paidFeesCount}",
                        color = StatusPaid,
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Unpaid Fees",
                        urdu = "ناواجب فیس",
                        value = "${stats.unpaidFeesCount}",
                        color = StatusUnpaid,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Row 3: Revenue Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KpiCard(
                        title = "Fee Collected",
                        urdu = "کل وصولی",
                        value = "Rs. ${String.format(Locale.US, "%,.0f", stats.totalFeeCollected)}",
                        color = Color(0xFF0F6848),
                        modifier = Modifier.weight(1f)
                    )
                    KpiCard(
                        title = "Remaining",
                        urdu = "بقایا واجبات",
                        value = "Rs. ${String.format(Locale.US, "%,.0f", stats.totalRemainingFee)}",
                        color = StatusUnpaid,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section Title: Main Modules
        item {
            Text(
                text = "School Modules & Features / انتظامی شعبہ جات",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SchoolNavy
                )
            )
        }

        // 9 Main Dashboard Action Tiles
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                val chunkedItems = menuItems.chunked(3)
                chunkedItems.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { item ->
                            DashboardTile(
                                item = item,
                                modifier = Modifier.weight(1f),
                                onClick = { onNavigate(item.screen) }
                            )
                        }
                    }
                }
            }
        }

        // Quick School Contact & Address Footer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "اقراۃ روضۃ العلم پبلک سکول - سسٹم رابطہ",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = SchoolNavy
                        )
                    )
                    Text(
                        text = "پتہ: ${profile?.address ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                    Text(
                        text = "فون: ${profile?.phone ?: ""}  |  ای میل: ${profile?.email ?: ""}",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color.DarkGray)
                    )
                }
            }
        }
    }
}

@Composable
fun KpiCard(
    title: String,
    urdu: String,
    value: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = color
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.DarkGray
                ),
                maxLines = 1
            )
            Text(
                text = urdu,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 9.sp,
                    color = Color.Gray
                ),
                maxLines = 1
            )
        }
    }
}

@Composable
fun DashboardTile(
    item: DashboardItem,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier
            .testTag(item.testTag)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 14.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(item.color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.title,
                    tint = item.color,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = SchoolNavyDark
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = item.urduTitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 10.sp,
                    color = Color.Gray
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
