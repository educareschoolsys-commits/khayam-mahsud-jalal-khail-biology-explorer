package com.example.export

import android.content.Context
import com.example.data.model.ClassResultStats
import com.example.data.model.FeeRecord
import com.example.data.model.ResultRecord
import com.example.data.model.SchoolProfile
import com.example.data.model.SubjectScore
import com.example.data.model.TimetableEntry
import java.io.File
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

object DocxGenerator {

    private fun escapeXml(str: String): String {
        return str
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun createDocxFile(outFile: File, documentXmlBody: String) {
        val contentTypes = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
  <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
  <Default Extension="xml" ContentType="application/xml"/>
  <Override PartName="/word/document.xml" ContentType="application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"/>
</Types>"""

        val rels = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
  <Relationship Id="rId1" Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument" Target="word/document.xml"/>
</Relationships>"""

        val fullDocXml = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<w:document xmlns:w="http://schemas.openxmlformats.org/wordprocessingml/2006/main"
            xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
  <w:body>
    $documentXmlBody
    <w:sectPr>
      <w:pgSz w:w="11906" w:h="16838"/>
      <w:pgMar w:top="1134" w:right="1134" w:bottom="1134" w:left="1134"/>
    </w:sectPr>
  </w:body>
</w:document>"""

        ZipOutputStream(FileOutputStream(outFile)).use { zos ->
            // [Content_Types].xml
            zos.putNextEntry(ZipEntry("[Content_Types].xml"))
            zos.write(contentTypes.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // _rels/.rels
            zos.putNextEntry(ZipEntry("_rels/.rels"))
            zos.write(rels.toByteArray(Charsets.UTF_8))
            zos.closeEntry()

            // word/document.xml
            zos.putNextEntry(ZipEntry("word/document.xml"))
            zos.write(fullDocXml.toByteArray(Charsets.UTF_8))
            zos.closeEntry()
        }
    }

    private fun paragraph(text: String, isBold: Boolean = false, sizeHalfPt: Int = 22, colorHex: String = "000000", align: String = "left"): String {
        val bTag = if (isBold) "<w:b/>" else ""
        val jcTag = if (align != "left") "<w:jc w:val=\"$align\"/>" else ""
        return """<w:p>
          <w:pPr>$jcTag</w:pPr>
          <w:r>
            <w:rPr>
              $bTag
              <w:color w:val="$colorHex"/>
              <w:sz w:val="$sizeHalfPt"/>
            </w:rPr>
            <w:t>${escapeXml(text)}</w:t>
          </w:r>
        </w:p>"""
    }

    private fun tableRow(cells: List<String>, isHeader: Boolean = false, bgHex: String? = null): String {
        val trPr = if (isHeader) "<w:trPr><w:tblHeader/></w:trPr>" else ""
        val rowXml = StringBuilder("<w:tr>$trPr")
        for (cellText in cells) {
            val shd = if (bgHex != null) "<w:shd w:val=\"clear\" w:color=\"auto\" w:fill=\"$bgHex\"/>" else ""
            val bTag = if (isHeader) "<w:b/>" else ""
            val col = if (isHeader) "FFFFFF" else "000000"
            rowXml.append("""<w:tc>
              <w:tcPr>$shd<w:tcMar><w:top w:w="120"/><w:bottom w:w="120"/><w:left w:w="140"/><w:right w:w="140"/></w:tcMar></w:tcPr>
              <w:p>
                <w:r>
                  <w:rPr>$bTag<w:color w:val="$col"/><w:sz w:val="20"/></w:rPr>
                  <w:t>${escapeXml(cellText)}</w:t>
                </w:r>
              </w:p>
            </w:tc>""")
        }
        rowXml.append("</w:tr>")
        return rowXml.toString()
    }

    fun generateStudentResultDocx(
        context: Context,
        profile: SchoolProfile,
        result: ResultRecord,
        subjects: List<SubjectScore>,
        stats: ClassResultStats
    ): File {
        val sb = StringBuilder()
        sb.append(paragraph(profile.name, isBold = true, sizeHalfPt = 36, colorHex = "0F3254", align = "center"))
        sb.append(paragraph(profile.englishName, isBold = true, sizeHalfPt = 24, colorHex = "0F3254", align = "center"))
        sb.append(paragraph("${profile.tagline} | Phone: ${profile.phone}", sizeHalfPt = 18, colorHex = "555555", align = "center"))
        sb.append(paragraph(profile.address, sizeHalfPt = 18, colorHex = "555555", align = "center"))
        sb.append(paragraph("STUDENT RESULT SHEET / نتیجہ کارڈ", isBold = true, sizeHalfPt = 26, colorHex = "D97706", align = "center"))
        sb.append(paragraph(""))

        // Student Info
        sb.append(paragraph("Student Name: ${result.studentName}         Roll Number: ${result.rollNumber}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Father Name: ${result.fatherName}           Class & Sec: ${result.className} - ${result.section}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Examination: ${result.examTitle}             Session: ${profile.academicSession}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph(""))

        // Subject Table
        sb.append("""<w:tbl>
          <w:tblPr>
            <w:tblW w:w="0" w:type="auto"/>
            <w:tblBorders>
              <w:top w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:left w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:bottom w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:right w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
              <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
            </w:tblBorders>
          </w:tblPr>""")

        sb.append(tableRow(listOf("Sr#", "Subject / مضمون", "Total Marks", "Obtained Marks", "Percentage", "Grade", "Status"), isHeader = true, bgHex = "0F3254"))

        subjects.forEachIndexed { i, sub ->
            val bg = if (i % 2 == 1) "F8FAFC" else null
            sb.append(tableRow(listOf(
                "${i + 1}",
                sub.subjectName,
                String.format(Locale.US, "%.0f", sub.totalMarks),
                String.format(Locale.US, "%.1f", sub.obtainedMarks),
                String.format(Locale.US, "%.1f%%", sub.percentage),
                sub.grade,
                sub.passStatus
            ), bgHex = bg))
        }

        // Grand Total row
        sb.append(tableRow(listOf(
            "",
            "Grand Total / کل نمبرات",
            String.format(Locale.US, "%.0f", result.totalMarks),
            String.format(Locale.US, "%.1f", result.obtainedMarks),
            String.format(Locale.US, "%.2f%%", result.percentage),
            result.grade,
            result.passStatus
        ), isHeader = true, bgHex = "1E3A5F"))

        sb.append("</w:tbl>")
        sb.append(paragraph(""))

        // Summary details
        sb.append(paragraph("Result Summary:", isBold = true, sizeHalfPt = 22, colorHex = "0F3254"))
        sb.append(paragraph("Percentage: ${String.format(Locale.US, "%.1f%%", result.percentage)} | Overall Grade: ${result.grade} | Class Position: ${result.position} | Result: ${result.passStatus}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Class Statistics: Average: ${String.format(Locale.US, "%.1f%%", stats.classAverage)} | Highest: ${stats.highestMarks} | Lowest: ${stats.lowestMarks} | Total Students: ${stats.totalStudents} | Pass %: ${String.format(Locale.US, "%.1f%%", stats.passPercentage)}", sizeHalfPt = 20, colorHex = "555555"))
        sb.append(paragraph(""))
        sb.append(paragraph("Class Teacher Signature: __________________          Principal Signature: __________________", isBold = true, sizeHalfPt = 22))

        val fileName = "Result_${result.studentName.replace(" ", "_")}_Roll_${result.rollNumber}.docx"
        val file = File(PdfGenerator.getDocumentsDir(context), fileName)
        createDocxFile(file, sb.toString())
        return file
    }

    fun generateFeeReceiptDocx(
        context: Context,
        profile: SchoolProfile,
        fee: FeeRecord
    ): File {
        val sb = StringBuilder()
        sb.append(paragraph(profile.name, isBold = true, sizeHalfPt = 36, colorHex = "0F3254", align = "center"))
        sb.append(paragraph(profile.englishName, isBold = true, sizeHalfPt = 24, colorHex = "0F3254", align = "center"))
        sb.append(paragraph("${profile.address} | Phone: ${profile.phone}", sizeHalfPt = 18, colorHex = "555555", align = "center"))
        sb.append(paragraph("FEE RECEIPT / فیس رسید", isBold = true, sizeHalfPt = 26, colorHex = "D97706", align = "center"))
        sb.append(paragraph(""))

        sb.append(paragraph("Receipt No: REC-${fee.id + 1000}         Payment Date: ${fee.paymentDate}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Student Name: ${fee.studentName}         Roll Number: ${fee.rollNumber}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Father Name: ${fee.fatherName}           Class & Sec: ${fee.className} - ${fee.section}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph("Month: ${fee.month}                       Status: ${fee.paymentStatus}", isBold = true, sizeHalfPt = 22))
        sb.append(paragraph(""))

        sb.append("""<w:tbl>
          <w:tblPr>
            <w:tblW w:w="0" w:type="auto"/>
            <w:tblBorders>
              <w:top w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:left w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:bottom w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:right w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
              <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
            </w:tblBorders>
          </w:tblPr>""")

        sb.append(tableRow(listOf("Fee Particulars / تفصیل فیس", "Amount (PKR)"), isHeader = true, bgHex = "0F3254"))
        sb.append(tableRow(listOf("Monthly Tuition Fee (${fee.month})", String.format(Locale.US, "%.0f", fee.monthlyFee))))
        sb.append(tableRow(listOf("Admission Fee", String.format(Locale.US, "%.0f", fee.admissionFee))))
        sb.append(tableRow(listOf("Exam / Other Charges", String.format(Locale.US, "%.0f", fee.otherCharges))))
        sb.append(tableRow(listOf("Discount / رعایت", "- " + String.format(Locale.US, "%.0f", fee.discount))))
        sb.append(tableRow(listOf("Total Payable Fee / کل قابلِ ادا رقم", "Rs. " + String.format(Locale.US, "%.0f", fee.totalFee)), isHeader = true, bgHex = "1E3A5F"))
        sb.append(tableRow(listOf("Paid Amount / وصول شدہ رقم", "Rs. " + String.format(Locale.US, "%.0f", fee.paidAmount)), isHeader = true, bgHex = "15803D"))
        sb.append(tableRow(listOf("Remaining Balance / بقایا واجبات", "Rs. " + String.format(Locale.US, "%.0f", fee.remainingAmount)), isHeader = true, bgHex = "B91C1C"))
        sb.append("</w:tbl>")
        sb.append(paragraph(""))

        sb.append(paragraph("Accountant Signature: __________________          Principal Signature: __________________", isBold = true, sizeHalfPt = 22))

        val fileName = "Fee_Receipt_${fee.studentName.replace(" ", "_")}_${fee.month.replace(" ", "_")}.docx"
        val file = File(PdfGenerator.getDocumentsDir(context), fileName)
        createDocxFile(file, sb.toString())
        return file
    }

    fun generateTimetableDocx(
        context: Context,
        profile: SchoolProfile,
        className: String,
        entries: List<TimetableEntry>
    ): File {
        val sb = StringBuilder()
        sb.append(paragraph(profile.name, isBold = true, sizeHalfPt = 36, colorHex = "0F3254", align = "center"))
        sb.append(paragraph(profile.englishName, isBold = true, sizeHalfPt = 24, colorHex = "0F3254", align = "center"))
        sb.append(paragraph("TIMETABLE / ٹائم ٹیبل - $className (Session: ${profile.academicSession})", isBold = true, sizeHalfPt = 26, colorHex = "D97706", align = "center"))
        sb.append(paragraph(""))

        sb.append("""<w:tbl>
          <w:tblPr>
            <w:tblW w:w="0" w:type="auto"/>
            <w:tblBorders>
              <w:top w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:left w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:bottom w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:right w:val="single" w:sz="4" w:space="0" w:color="0F3254"/>
              <w:insideH w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
              <w:insideV w:val="single" w:sz="4" w:space="0" w:color="E2E8F0"/>
            </w:tblBorders>
          </w:tblPr>""")

        sb.append(tableRow(listOf("Day", "Period", "Timing", "Subject", "Teacher", "Room"), isHeader = true, bgHex = "0F3254"))

        entries.forEachIndexed { i, e ->
            val bg = if (i % 2 == 1) "F8FAFC" else null
            sb.append(tableRow(listOf(e.day, e.periodName, "${e.startTime} - ${e.endTime}", e.subject, e.teacherName, e.room), bgHex = bg))
        }

        sb.append("</w:tbl>")
        sb.append(paragraph(""))
        sb.append(paragraph("Principal Signature: __________________", isBold = true, sizeHalfPt = 22))

        val fileName = "Timetable_${className.replace(" ", "_")}.docx"
        val file = File(PdfGenerator.getDocumentsDir(context), fileName)
        createDocxFile(file, sb.toString())
        return file
    }
}
