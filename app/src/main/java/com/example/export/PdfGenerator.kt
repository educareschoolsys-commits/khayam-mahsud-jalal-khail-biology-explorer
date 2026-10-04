package com.example.export

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.example.data.model.ClassResultStats
import com.example.data.model.FeeRecord
import com.example.data.model.ResultRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.SubjectScore
import com.example.data.model.TimetableEntry
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object PdfGenerator {

    // Standard A4 dimensions in PostScript points: 595 x 842
    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun getDocumentsDir(context: Context): File {
        val dir = File(context.filesDir, "school_documents")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    private fun loadBitmapFromUriOrResource(context: Context, uriString: String?): Bitmap? {
        if (uriString.isNullOrBlank()) {
            return try {
                // Default school logo drawable
                val resId = context.resources.getIdentifier("ic_school_logo", "drawable", context.packageName)
                if (resId != 0) BitmapFactory.decodeResource(context.resources, resId) else null
            } catch (_: Exception) { null }
        }
        return try {
            val uri = Uri.parse(uriString)
            val inputStream = context.contentResolver.openInputStream(uri)
            val bmp = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            bmp
        } catch (_: Exception) {
            try {
                // If it's a file path
                val file = File(uriString)
                if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
            } catch (_: Exception) { null }
        }
    }

    /**
     * Generate A4 Student Result Sheet
     */
    fun generateStudentResultPdf(
        context: Context,
        profile: SchoolProfile,
        result: ResultRecord,
        subjects: List<SubjectScore>,
        stats: ClassResultStats,
        studentPhotoUri: String? = null
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Paints
        val primaryPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            isAntiAlias = true
        }
        val textPaint = Paint().apply {
            color = Color.rgb(20, 20, 20)
            textSize = 10f
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val subHeaderPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            style = Paint.Style.STROKE
            strokeWidth = 2f
            isAntiAlias = true
        }
        val linePaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            strokeWidth = 1f
        }

        // Outer Decorative Border
        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, PAGE_HEIGHT - 20f, borderPaint)
        canvas.drawRect(24f, 24f, PAGE_WIDTH - 24f, PAGE_HEIGHT - 24f, Paint().apply {
            color = Color.rgb(217, 119, 6) // Gold border
            style = Paint.Style.STROKE
            strokeWidth = 1f
        })

        // Header Section
        val schoolLogo = loadBitmapFromUriOrResource(context, profile.logoUri)
        if (schoolLogo != null) {
            val destRect = Rect(35, 35, 95, 95)
            canvas.drawBitmap(schoolLogo, null, destRect, null)
        }

        // Student Photo
        val studentPhoto = loadBitmapFromUriOrResource(context, studentPhotoUri)
        if (studentPhoto != null) {
            val destRect = Rect(PAGE_WIDTH - 95, 35, PAGE_WIDTH - 35, 95)
            canvas.drawBitmap(studentPhoto, null, destRect, null)
            canvas.drawRect(destRect, borderPaint)
        } else {
            // Draw placeholder frame
            val destRect = RectF(PAGE_WIDTH - 95f, 35f, PAGE_WIDTH - 35f, 95f)
            canvas.drawRoundRect(destRect, 4f, 4f, Paint().apply {
                color = Color.rgb(240, 240, 240)
                style = Paint.Style.FILL
            })
            canvas.drawRoundRect(destRect, 4f, 4f, borderPaint)
            val phPaint = Paint().apply {
                color = Color.GRAY
                textSize = 8f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Student", PAGE_WIDTH - 65f, 60f, phPaint)
            canvas.drawText("Photo", PAGE_WIDTH - 65f, 72f, phPaint)
        }

        // School Name in Urdu and English
        canvas.drawText(profile.name, (PAGE_WIDTH / 2).toFloat(), 55f, headerPaint)
        val engPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText(profile.englishName, (PAGE_WIDTH / 2).toFloat(), 70f, engPaint)
        canvas.drawText("${profile.tagline} | Phone: ${profile.phone}", (PAGE_WIDTH / 2).toFloat(), 83f, subHeaderPaint)
        canvas.drawText(profile.address, (PAGE_WIDTH / 2).toFloat(), 95f, subHeaderPaint)

        // Title Badge
        val badgeRect = RectF(150f, 108f, PAGE_WIDTH - 150f, 130f)
        canvas.drawRoundRect(badgeRect, 6f, 6f, primaryPaint)
        val badgeTextPaint = Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        canvas.drawText("STUDENT RESULT SHEET / نتیجہ کارڈ", (PAGE_WIDTH / 2).toFloat(), 123f, badgeTextPaint)

        // Student Info Grid
        var startY = 145f
        val infoBox = RectF(35f, startY, PAGE_WIDTH - 35f, startY + 65f)
        canvas.drawRoundRect(infoBox, 4f, 4f, Paint().apply {
            color = Color.rgb(245, 247, 250)
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(infoBox, 4f, 4f, borderPaint)

        val boldLabelPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val valPaint = Paint().apply {
            color = Color.BLACK
            textSize = 9.5f
            isAntiAlias = true
        }

        // Row 1
        canvas.drawText("Student Name:", 45f, startY + 18f, boldLabelPaint)
        canvas.drawText(result.studentName, 120f, startY + 18f, valPaint)
        canvas.drawText("Roll Number:", 320f, startY + 18f, boldLabelPaint)
        canvas.drawText(result.rollNumber, 390f, startY + 18f, valPaint)

        // Row 2
        canvas.drawText("Father Name:", 45f, startY + 36f, boldLabelPaint)
        canvas.drawText(result.fatherName, 120f, startY + 36f, valPaint)
        canvas.drawText("Class & Sec:", 320f, startY + 36f, boldLabelPaint)
        canvas.drawText("${result.className} - ${result.section}", 390f, startY + 36f, valPaint)

        // Row 3
        canvas.drawText("Examination:", 45f, startY + 54f, boldLabelPaint)
        canvas.drawText(result.examTitle, 120f, startY + 54f, valPaint)
        canvas.drawText("Date & Session:", 320f, startY + 54f, boldLabelPaint)
        canvas.drawText("${result.examDate} (${profile.academicSession})", 390f, startY + 54f, valPaint)

        // Table Header
        startY = 225f
        val tableLeft = 35f
        val tableRight = PAGE_WIDTH - 35f
        val rowHeight = 22f

        // Draw Table Header Bar
        canvas.drawRect(tableLeft, startY, tableRight, startY + rowHeight, primaryPaint)
        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 9.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        val col1 = tableLeft + 10f      // Sr #
        val col2 = tableLeft + 45f      // Subject
        val col3 = tableLeft + 230f     // Total Marks
        val col4 = tableLeft + 310f     // Obtained Marks
        val col5 = tableLeft + 400f     // Percentage
        val col6 = tableLeft + 465f     // Grade
        val col7 = tableLeft + 510f     // Status

        canvas.drawText("Sr#", col1, startY + 15f, thPaint)
        canvas.drawText("Subject / مضمون", col2, startY + 15f, thPaint)
        canvas.drawText("Total", col3, startY + 15f, thPaint)
        canvas.drawText("Obtained", col4, startY + 15f, thPaint)
        canvas.drawText("%age", col5, startY + 15f, thPaint)
        canvas.drawText("Grade", col6, startY + 15f, thPaint)
        canvas.drawText("Status", col7 - 5f, startY + 15f, thPaint)

        // Table Rows
        var currentY = startY + rowHeight
        val zebraPaint = Paint().apply { color = Color.rgb(250, 250, 252) }

        subjects.forEachIndexed { index, sub ->
            if (index % 2 == 1) {
                canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, zebraPaint)
            }
            canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, linePaint)

            canvas.drawText("${index + 1}", col1 + 5f, currentY + 15f, textPaint)
            canvas.drawText(sub.subjectName, col2, currentY + 15f, boldLabelPaint)
            canvas.drawText(String.format(Locale.US, "%.0f", sub.totalMarks), col3 + 5f, currentY + 15f, textPaint)
            canvas.drawText(String.format(Locale.US, "%.1f", sub.obtainedMarks), col4 + 5f, currentY + 15f, boldLabelPaint)
            canvas.drawText(String.format(Locale.US, "%.1f%%", sub.percentage), col5 + 5f, currentY + 15f, textPaint)
            canvas.drawText(sub.grade, col6 + 5f, currentY + 15f, boldLabelPaint)

            val statusColor = if (sub.passStatus == "Pass") Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
            val stPaint = Paint().apply {
                color = statusColor
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            canvas.drawText(sub.passStatus, col7 - 5f, currentY + 15f, stPaint)

            currentY += rowHeight
        }

        // Grand Total Row
        canvas.drawRect(tableLeft, currentY, tableRight, currentY + rowHeight, Paint().apply { color = Color.rgb(230, 240, 250) })
        canvas.drawLine(tableLeft, currentY, tableRight, currentY, borderPaint)
        canvas.drawLine(tableLeft, currentY + rowHeight, tableRight, currentY + rowHeight, borderPaint)

        canvas.drawText("Grand Total / کل نمبرات", col2, currentY + 15f, boldLabelPaint)
        canvas.drawText(String.format(Locale.US, "%.0f", result.totalMarks), col3 + 5f, currentY + 15f, boldLabelPaint)
        canvas.drawText(String.format(Locale.US, "%.1f", result.obtainedMarks), col4 + 5f, currentY + 15f, boldLabelPaint)
        canvas.drawText(String.format(Locale.US, "%.2f%%", result.percentage), col5 + 5f, currentY + 15f, boldLabelPaint)
        canvas.drawText(result.grade, col6 + 5f, currentY + 15f, boldLabelPaint)
        val passCol = if (result.passStatus == "Pass") Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
        canvas.drawText(result.passStatus, col7 - 5f, currentY + 15f, Paint().apply {
            color = passCol
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        })

        // Draw Table Outer Frame & Vertical Dividers
        canvas.drawRect(tableLeft, startY, tableRight, currentY + rowHeight, borderPaint)

        // Summary Badges Box
        currentY += rowHeight + 15f
        val summaryBox = RectF(tableLeft, currentY, tableRight, currentY + 50f)
        canvas.drawRoundRect(summaryBox, 6f, 6f, Paint().apply {
            color = Color.rgb(243, 244, 246)
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(summaryBox, 6f, 6f, borderPaint)

        val statTitlePaint = Paint().apply {
            color = Color.rgb(100, 100, 100)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val statValPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }

        val boxW = (tableRight - tableLeft) / 4f
        // Item 1: Percentage
        canvas.drawText("Percentage", tableLeft + boxW * 0.5f, currentY + 18f, statTitlePaint)
        canvas.drawText(String.format(Locale.US, "%.1f%%", result.percentage), tableLeft + boxW * 0.5f, currentY + 38f, statValPaint)

        // Item 2: Grade
        canvas.drawText("Overall Grade", tableLeft + boxW * 1.5f, currentY + 18f, statTitlePaint)
        canvas.drawText(result.grade, tableLeft + boxW * 1.5f, currentY + 38f, statValPaint)

        // Item 3: Position
        canvas.drawText("Class Position", tableLeft + boxW * 2.5f, currentY + 18f, statTitlePaint)
        val posPaint = Paint(statValPaint).apply { color = Color.rgb(217, 119, 6) }
        canvas.drawText(result.position.ifBlank { "-" }, tableLeft + boxW * 2.5f, currentY + 38f, posPaint)

        // Item 4: Result Status
        canvas.drawText("Final Status", tableLeft + boxW * 3.5f, currentY + 18f, statTitlePaint)
        val finalStatusPaint = Paint(statValPaint).apply { color = passCol }
        canvas.drawText(result.passStatus, tableLeft + boxW * 3.5f, currentY + 38f, finalStatusPaint)

        // Class Benchmarks
        currentY += 65f
        val benchBox = RectF(tableLeft, currentY, tableRight, currentY + 45f)
        canvas.drawRoundRect(benchBox, 4f, 4f, Paint().apply {
            color = Color.rgb(249, 250, 251)
            style = Paint.Style.FILL
        })
        canvas.drawRoundRect(benchBox, 4f, 4f, Paint().apply {
            color = Color.rgb(209, 213, 219)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        })

        val bLabel = Paint().apply {
            color = Color.rgb(75, 85, 99)
            textSize = 8.5f
            isAntiAlias = true
        }
        val bVal = Paint().apply {
            color = Color.BLACK
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        canvas.drawText("Class Statistics: ", tableLeft + 10f, currentY + 18f, boldLabelPaint)
        canvas.drawText("Class Avg: ", tableLeft + 100f, currentY + 18f, bLabel)
        canvas.drawText(String.format(Locale.US, "%.1f%%", stats.classAverage), tableLeft + 145f, currentY + 18f, bVal)

        canvas.drawText("Highest Marks: ", tableLeft + 200f, currentY + 18f, bLabel)
        canvas.drawText(String.format(Locale.US, "%.1f", stats.highestMarks), tableLeft + 265f, currentY + 18f, bVal)

        canvas.drawText("Lowest Marks: ", tableLeft + 310f, currentY + 18f, bLabel)
        canvas.drawText(String.format(Locale.US, "%.1f", stats.lowestMarks), tableLeft + 375f, currentY + 18f, bVal)

        canvas.drawText("Total Students: ${stats.totalStudents}  |  Passed: ${stats.passedStudents}  |  Failed: ${stats.failedStudents}  |  Pass %age: ${String.format(Locale.US, "%.1f%%", stats.passPercentage)}", tableLeft + 10f, currentY + 34f, bLabel)

        // Signatures & Remarks Area
        val sigY = PAGE_HEIGHT - 80f
        canvas.drawLine(tableLeft + 30f, sigY, tableLeft + 160f, sigY, borderPaint)
        canvas.drawText("Class Teacher's Signature", tableLeft + 40f, sigY + 14f, bLabel)

        canvas.drawLine(PAGE_WIDTH / 2f - 60f, sigY, PAGE_WIDTH / 2f + 60f, sigY, borderPaint)
        canvas.drawText("Examination Incharge", PAGE_WIDTH / 2f - 45f, sigY + 14f, bLabel)

        canvas.drawLine(tableRight - 160f, sigY, tableRight - 30f, sigY, borderPaint)
        canvas.drawText(profile.principalName, tableRight - 160f, sigY + 14f, boldLabelPaint)

        // Footer Date and System mark
        val footerPaint = Paint().apply {
            color = Color.GRAY
            textSize = 7.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val timeStamp = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        canvas.drawText("اقراۃ روضۃ العلم پبلک سکول - Automated School Management System | Generated on: $timeStamp", (PAGE_WIDTH / 2).toFloat(), PAGE_HEIGHT - 28f, footerPaint)

        pdfDocument.finishPage(page)

        val fileName = "Result_${result.studentName.replace(" ", "_")}_Roll_${result.rollNumber}.pdf"
        val file = File(getDocumentsDir(context), fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()

        return file
    }

    /**
     * Generate A4 Class Result Master Sheet
     */
    fun generateClassResultMasterPdf(
        context: Context,
        profile: SchoolProfile,
        className: String,
        examTitle: String,
        results: List<ResultRecord>,
        stats: ClassResultStats
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val primaryPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            isAntiAlias = true
        }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 8.5f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val textPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            isAntiAlias = true
        }
        val boldTextPaint = Paint().apply {
            color = Color.BLACK
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }

        // Outer border
        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, PAGE_HEIGHT - 20f, borderPaint)

        // School Header
        canvas.drawText(profile.name, (PAGE_WIDTH / 2).toFloat(), 45f, headerPaint)
        canvas.drawText("${profile.englishName} | Session: ${profile.academicSession}", (PAGE_WIDTH / 2).toFloat(), 60f, subPaint)
        canvas.drawText("CLASS RESULT MASTER SHEET - $className ($examTitle)", (PAGE_WIDTH / 2).toFloat(), 75f, Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })

        // Class Stats Bar
        val statRect = RectF(30f, 85f, PAGE_WIDTH - 30f, 115f)
        canvas.drawRect(statRect, Paint().apply { color = Color.rgb(240, 245, 250) })
        canvas.drawRect(statRect, borderPaint)

        val statText = "Total Students: ${stats.totalStudents}  |  Passed: ${stats.passedStudents}  |  Failed: ${stats.failedStudents}  |  Pass: ${String.format(Locale.US, "%.1f%%", stats.passPercentage)}  |  Avg: ${String.format(Locale.US, "%.1f%%", stats.classAverage)}"
        canvas.drawText(statText, (PAGE_WIDTH / 2).toFloat(), 103f, Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 8.5f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })

        // Table
        val startY = 125f
        val tableLeft = 30f
        val tableRight = PAGE_WIDTH - 30f
        val rowH = 18f

        // Table header
        canvas.drawRect(tableLeft, startY, tableRight, startY + rowH, primaryPaint)
        val thPaint = Paint().apply {
            color = Color.WHITE
            textSize = 8f
            typeface = Typeface.DEFAULT_BOLD
        }

        val cRoll = tableLeft + 5f
        val cName = tableLeft + 35f
        val cFather = tableLeft + 145f
        val cTotal = tableLeft + 250f
        val cObtained = tableLeft + 300f
        val cPct = tableLeft + 355f
        val cGrade = tableLeft + 410f
        val cPos = tableLeft + 450f
        val cStatus = tableLeft + 490f

        canvas.drawText("Roll#", cRoll, startY + 12f, thPaint)
        canvas.drawText("Student Name", cName, startY + 12f, thPaint)
        canvas.drawText("Father Name", cFather, startY + 12f, thPaint)
        canvas.drawText("Total", cTotal, startY + 12f, thPaint)
        canvas.drawText("Obtained", cObtained, startY + 12f, thPaint)
        canvas.drawText("%age", cPct, startY + 12f, thPaint)
        canvas.drawText("Grade", cGrade, startY + 12f, thPaint)
        canvas.drawText("Position", cPos, startY + 12f, thPaint)
        canvas.drawText("Status", cStatus, startY + 12f, thPaint)

        var curY = startY + rowH
        results.forEachIndexed { i, r ->
            if (i % 2 == 1) {
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, Paint().apply { color = Color.rgb(250, 250, 252) })
            }
            canvas.drawLine(tableLeft, curY + rowH, tableRight, curY + rowH, Paint().apply { color = Color.rgb(220, 220, 220) })

            canvas.drawText(r.rollNumber, cRoll, curY + 12f, textPaint)
            canvas.drawText(r.studentName, cName, curY + 12f, boldTextPaint)
            canvas.drawText(r.fatherName, cFather, curY + 12f, textPaint)
            canvas.drawText(String.format(Locale.US, "%.0f", r.totalMarks), cTotal, curY + 12f, textPaint)
            canvas.drawText(String.format(Locale.US, "%.1f", r.obtainedMarks), cObtained, curY + 12f, boldTextPaint)
            canvas.drawText(String.format(Locale.US, "%.1f%%", r.percentage), cPct, curY + 12f, textPaint)
            canvas.drawText(r.grade, cGrade, curY + 12f, boldTextPaint)
            canvas.drawText(r.position, cPos, curY + 12f, Paint().apply {
                color = Color.rgb(217, 119, 6)
                textSize = 8f
                typeface = Typeface.DEFAULT_BOLD
            })
            val stColor = if (r.passStatus == "Pass") Color.rgb(22, 163, 74) else Color.rgb(220, 38, 38)
            canvas.drawText(r.passStatus, cStatus, curY + 12f, Paint().apply {
                color = stColor
                textSize = 8f
                typeface = Typeface.DEFAULT_BOLD
            })

            curY += rowH
        }

        canvas.drawRect(tableLeft, startY, tableRight, curY, borderPaint)

        // Signatures at bottom
        val sigY = PAGE_HEIGHT - 60f
        canvas.drawLine(50f, sigY, 180f, sigY, borderPaint)
        canvas.drawText("Class Teacher", 90f, sigY + 12f, textPaint)

        canvas.drawLine(PAGE_WIDTH - 180f, sigY, PAGE_WIDTH - 50f, sigY, borderPaint)
        canvas.drawText("Principal / ناظمِ اعلیٰ", PAGE_WIDTH - 145f, sigY + 12f, boldTextPaint)

        pdfDocument.finishPage(page)

        val fileName = "Class_Result_${className.replace(" ", "_")}.pdf"
        val file = File(getDocumentsDir(context), fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()
        return file
    }

    /**
     * Generate A4 Fee Receipt & Sheet PDF
     */
    fun generateFeeReceiptPdf(
        context: Context,
        profile: SchoolProfile,
        fee: FeeRecord,
        studentPhotoUri: String? = null
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val primaryPaint = Paint().apply { color = Color.rgb(15, 50, 84); isAntiAlias = true }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 18f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val subPaint = Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 9f
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }

        // Double Border
        canvas.drawRect(25f, 25f, PAGE_WIDTH - 25f, PAGE_HEIGHT - 25f, borderPaint)
        canvas.drawRect(28f, 28f, PAGE_WIDTH - 28f, PAGE_HEIGHT - 28f, Paint().apply {
            color = Color.rgb(217, 119, 6)
            style = Paint.Style.STROKE
            strokeWidth = 1f
        })

        // School Crest
        val schoolLogo = loadBitmapFromUriOrResource(context, profile.logoUri)
        if (schoolLogo != null) {
            canvas.drawBitmap(schoolLogo, null, Rect(40, 40, 95, 95), null)
        }

        // Student Photo
        val studentPhoto = loadBitmapFromUriOrResource(context, studentPhotoUri)
        if (studentPhoto != null) {
            val dest = Rect(PAGE_WIDTH - 95, 40, PAGE_WIDTH - 40, 95)
            canvas.drawBitmap(studentPhoto, null, dest, null)
            canvas.drawRect(dest, borderPaint)
        }

        // School Names
        canvas.drawText(profile.name, (PAGE_WIDTH / 2).toFloat(), 55f, headerPaint)
        canvas.drawText(profile.englishName, (PAGE_WIDTH / 2).toFloat(), 70f, Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })
        canvas.drawText("${profile.address} | Contact: ${profile.phone}", (PAGE_WIDTH / 2).toFloat(), 85f, subPaint)

        // Receipt Banner
        val bannerRect = RectF(160f, 105f, PAGE_WIDTH - 160f, 130f)
        canvas.drawRoundRect(bannerRect, 6f, 6f, primaryPaint)
        canvas.drawText("FEE RECEIPT / فیس رسید", (PAGE_WIDTH / 2).toFloat(), 122f, Paint().apply {
            color = Color.WHITE
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })

        // Student Info Box
        val infoRect = RectF(40f, 145f, PAGE_WIDTH - 40f, 215f)
        canvas.drawRoundRect(infoRect, 4f, 4f, Paint().apply { color = Color.rgb(248, 250, 252) })
        canvas.drawRoundRect(infoRect, 4f, 4f, borderPaint)

        val boldPaint = Paint().apply { color = Color.rgb(15, 50, 84); textSize = 10f; typeface = Typeface.DEFAULT_BOLD }
        val valPaint = Paint().apply { color = Color.BLACK; textSize = 10f }

        canvas.drawText("Receipt No:", 55f, 168f, boldPaint)
        canvas.drawText("REC-${fee.id + 1000}", 130f, 168f, valPaint)
        canvas.drawText("Payment Date:", 330f, 168f, boldPaint)
        canvas.drawText(fee.paymentDate, 420f, 168f, valPaint)

        canvas.drawText("Student Name:", 55f, 188f, boldPaint)
        canvas.drawText(fee.studentName, 130f, 188f, valPaint)
        canvas.drawText("Roll Number:", 330f, 188f, boldPaint)
        canvas.drawText(fee.rollNumber, 420f, 188f, valPaint)

        canvas.drawText("Father Name:", 55f, 208f, boldPaint)
        canvas.drawText(fee.fatherName, 130f, 208f, valPaint)
        canvas.drawText("Class & Sec:", 330f, 208f, boldPaint)
        canvas.drawText("${fee.className} - ${fee.section}", 420f, 208f, valPaint)

        // Itemized Table
        val startY = 235f
        val tableLeft = 40f
        val tableRight = PAGE_WIDTH - 40f
        val rowH = 26f

        canvas.drawRect(tableLeft, startY, tableRight, startY + rowH, primaryPaint)
        val thP = Paint().apply { color = Color.WHITE; textSize = 10f; typeface = Typeface.DEFAULT_BOLD }
        canvas.drawText("Fee Particulars / تفصیل فیس", tableLeft + 15f, startY + 17f, thP)
        canvas.drawText("Amount (PKR)", tableRight - 100f, startY + 17f, thP)

        val items = listOf(
            Pair("Monthly Tuition Fee (${fee.month})", fee.monthlyFee),
            Pair("Admission Fee", fee.admissionFee),
            Pair("Exam / Generator / Other Charges", fee.otherCharges),
            Pair("Special Concession / Discount", -fee.discount)
        )

        var curY = startY + rowH
        items.forEachIndexed { i, item ->
            if (i % 2 == 1) {
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, Paint().apply { color = Color.rgb(250, 250, 252) })
            }
            canvas.drawLine(tableLeft, curY + rowH, tableRight, curY + rowH, Paint().apply { color = Color.rgb(220, 220, 220) })
            canvas.drawText(item.first, tableLeft + 15f, curY + 17f, boldPaint)
            val amtStr = if (item.second < 0) "- ${String.format(Locale.US, "%.0f", -item.second)}" else String.format(Locale.US, "%.0f", item.second)
            canvas.drawText(amtStr, tableRight - 90f, curY + 17f, valPaint)
            curY += rowH
        }

        // Total Row
        canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, Paint().apply { color = Color.rgb(230, 240, 250) })
        canvas.drawLine(tableLeft, curY, tableRight, curY, borderPaint)
        canvas.drawText("Total Payable Fee / کل قابلِ ادا فیس", tableLeft + 15f, curY + 17f, boldPaint)
        canvas.drawText("Rs. ${String.format(Locale.US, "%.0f", fee.totalFee)}", tableRight - 90f, curY + 17f, boldPaint)
        curY += rowH

        // Paid Row
        canvas.drawText("Paid Amount / وصول شدہ رقم", tableLeft + 15f, curY + 17f, boldPaint)
        canvas.drawText("Rs. ${String.format(Locale.US, "%.0f", fee.paidAmount)}", tableRight - 90f, curY + 17f, Paint(boldPaint).apply { color = Color.rgb(22, 163, 74) })
        canvas.drawLine(tableLeft, curY + rowH, tableRight, curY + rowH, Paint().apply { color = Color.rgb(220, 220, 220) })
        curY += rowH

        // Remaining Row
        canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, Paint().apply { color = Color.rgb(254, 242, 242) })
        canvas.drawText("Remaining Balance / بقایا واجبات", tableLeft + 15f, curY + 17f, boldPaint)
        canvas.drawText("Rs. ${String.format(Locale.US, "%.0f", fee.remainingAmount)}", tableRight - 90f, curY + 17f, Paint(boldPaint).apply { color = Color.rgb(220, 38, 38) })
        canvas.drawLine(tableLeft, curY + rowH, tableRight, curY + rowH, borderPaint)
        curY += rowH

        canvas.drawRect(tableLeft, startY, tableRight, curY, borderPaint)

        // Status Stamp
        val stampRect = RectF(PAGE_WIDTH / 2f - 70f, curY + 25f, PAGE_WIDTH / 2f + 70f, curY + 65f)
        val stColor = when (fee.paymentStatus) {
            "Paid" -> Color.rgb(22, 163, 74)
            "Unpaid" -> Color.rgb(220, 38, 38)
            else -> Color.rgb(234, 88, 12)
        }
        canvas.drawRoundRect(stampRect, 8f, 8f, Paint().apply {
            color = stColor
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        })
        canvas.drawText(fee.paymentStatus.uppercase(Locale.US), (PAGE_WIDTH / 2).toFloat(), curY + 50f, Paint().apply {
            color = stColor
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })

        // Signatures
        val sigY = PAGE_HEIGHT - 90f
        canvas.drawLine(50f, sigY, 180f, sigY, borderPaint)
        canvas.drawText("Accountant / کلرک", 80f, sigY + 14f, valPaint)

        canvas.drawLine(PAGE_WIDTH - 190f, sigY, PAGE_WIDTH - 60f, sigY, borderPaint)
        canvas.drawText(profile.principalName, PAGE_WIDTH - 180f, sigY + 14f, boldPaint)

        pdfDocument.finishPage(page)

        val fileName = "Fee_Receipt_${fee.studentName.replace(" ", "_")}_${fee.month.replace(" ", "_")}.pdf"
        val file = File(getDocumentsDir(context), fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()
        return file
    }

    /**
     * Generate Timetable PDF
     */
    fun generateTimetablePdf(
        context: Context,
        profile: SchoolProfile,
        className: String,
        entries: List<TimetableEntry>
    ): File {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        val primaryPaint = Paint().apply { color = Color.rgb(15, 50, 84); isAntiAlias = true }
        val headerPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            textSize = 16f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
            isAntiAlias = true
        }
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 50, 84)
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        val textPaint = Paint().apply { color = Color.BLACK; textSize = 8f; isAntiAlias = true }
        val boldPaint = Paint().apply { color = Color.rgb(15, 50, 84); textSize = 8.5f; typeface = Typeface.DEFAULT_BOLD }

        canvas.drawRect(20f, 20f, PAGE_WIDTH - 20f, PAGE_HEIGHT - 20f, borderPaint)

        // Logo
        val schoolLogo = loadBitmapFromUriOrResource(context, profile.logoUri)
        if (schoolLogo != null) {
            canvas.drawBitmap(schoolLogo, null, Rect(35, 30, 80, 75), null)
        }

        canvas.drawText(profile.name, (PAGE_WIDTH / 2).toFloat(), 45f, headerPaint)
        canvas.drawText("${profile.englishName} | Session: ${profile.academicSession}", (PAGE_WIDTH / 2).toFloat(), 60f, Paint().apply {
            color = Color.rgb(80, 80, 80)
            textSize = 9f
            textAlign = Paint.Align.CENTER
        })
        canvas.drawText("CLASS TIMETABLE / ٹائم ٹیبل - $className", (PAGE_WIDTH / 2).toFloat(), 78f, Paint().apply {
            color = Color.rgb(217, 119, 6)
            textSize = 11f
            typeface = Typeface.DEFAULT_BOLD
            textAlign = Paint.Align.CENTER
        })

        val startY = 95f
        val tableLeft = 25f
        val tableRight = PAGE_WIDTH - 25f
        val rowH = 20f

        // Table Header
        canvas.drawRect(tableLeft, startY, tableRight, startY + rowH, primaryPaint)
        val thP = Paint().apply { color = Color.WHITE; textSize = 8f; typeface = Typeface.DEFAULT_BOLD }

        val colDay = tableLeft + 5f
        val colPeriod = tableLeft + 85f
        val colTime = tableLeft + 160f
        val colSubject = tableLeft + 270f
        val colTeacher = tableLeft + 410f

        canvas.drawText("Day", colDay, startY + 13f, thP)
        canvas.drawText("Period", colPeriod, startY + 13f, thP)
        canvas.drawText("Timing", colTime, startY + 13f, thP)
        canvas.drawText("Subject", colSubject, startY + 13f, thP)
        canvas.drawText("Teacher", colTeacher, startY + 13f, thP)

        var curY = startY + rowH
        entries.forEachIndexed { i, entry ->
            if (i % 2 == 1) {
                canvas.drawRect(tableLeft, curY, tableRight, curY + rowH, Paint().apply { color = Color.rgb(250, 250, 252) })
            }
            canvas.drawLine(tableLeft, curY + rowH, tableRight, curY + rowH, Paint().apply { color = Color.rgb(220, 220, 220) })

            canvas.drawText(entry.day, colDay, curY + 13f, boldPaint)
            canvas.drawText(entry.periodName, colPeriod, curY + 13f, textPaint)
            canvas.drawText("${entry.startTime} - ${entry.endTime}", colTime, curY + 13f, textPaint)
            canvas.drawText(entry.subject, colSubject, curY + 13f, boldPaint)
            canvas.drawText(entry.teacherName, colTeacher, curY + 13f, textPaint)

            curY += rowH
        }

        canvas.drawRect(tableLeft, startY, tableRight, curY, borderPaint)

        // Principal signature
        val sigY = PAGE_HEIGHT - 60f
        canvas.drawLine(PAGE_WIDTH - 180f, sigY, PAGE_WIDTH - 50f, sigY, borderPaint)
        canvas.drawText(profile.principalName, PAGE_WIDTH - 160f, sigY + 14f, boldPaint)

        pdfDocument.finishPage(page)

        val fileName = "Timetable_${className.replace(" ", "_")}.pdf"
        val file = File(getDocumentsDir(context), fileName)
        val fos = FileOutputStream(file)
        pdfDocument.writeTo(fos)
        fos.flush()
        fos.close()
        pdfDocument.close()
        return file
    }
}
