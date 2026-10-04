package com.example.export

import android.content.Context
import android.net.Uri
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream

data class ExcelRow(
    val studentName: String,
    val fatherName: String,
    val className: String,
    val section: String,
    val rollNumber: String,
    val subject: String,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val monthlyFee: Double,
    val paidFee: Double
)

object ExcelManager {

    /**
     * Parses either .xlsx (OpenXML) or .csv text from an input stream
     */
    fun parseStream(inputStream: InputStream, fileName: String? = null): List<ExcelRow> {
        val lower = fileName?.lowercase() ?: ""
        return if (lower.endsWith(".csv") || lower.endsWith(".txt")) {
            parseCsv(inputStream)
        } else {
            // Try parsing as xlsx first, if fail fallback to CSV
            try {
                parseXlsx(inputStream)
            } catch (_: Exception) {
                try {
                    parseCsv(inputStream)
                } catch (_: Exception) {
                    emptyList()
                }
            }
        }
    }

    /**
     * Parses standard OpenXML .xlsx files by extracting sharedStrings.xml and sheet1.xml
     */
    fun parseXlsx(inputStream: InputStream): List<ExcelRow> {
        val sharedStrings = mutableListOf<String>()
        val sheetRows = mutableListOf<List<String>>()

        // Step 1: Read ZIP stream
        // Need to buffer or make copies of entries
        val tempBytes = inputStream.readBytes()

        // Read shared strings
        var zis = ZipInputStream(tempBytes.inputStream())
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.name.equals("xl/sharedStrings.xml", ignoreCase = true)) {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(zis, "UTF-8")
                var event = parser.eventType
                var currentText = StringBuilder()
                var inText = false
                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "t") inText = true
                        }
                        XmlPullParser.TEXT -> {
                            if (inText) currentText.append(parser.text)
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "t") inText = false
                            if (parser.name == "si") {
                                sharedStrings.add(currentText.toString())
                                currentText = StringBuilder()
                            }
                        }
                    }
                    event = parser.next()
                }
                break
            }
            entry = zis.nextEntry
        }
        zis.close()

        // Step 2: Read sheet1.xml
        zis = ZipInputStream(tempBytes.inputStream())
        entry = zis.nextEntry
        while (entry != null) {
            if (entry.name.contains("sheet1.xml", ignoreCase = true)) {
                val factory = XmlPullParserFactory.newInstance()
                val parser = factory.newPullParser()
                parser.setInput(zis, "UTF-8")
                var event = parser.eventType
                var currentRow = mutableListOf<String>()
                var currentVal = ""
                var cellType = ""
                var inValue = false

                while (event != XmlPullParser.END_DOCUMENT) {
                    when (event) {
                        XmlPullParser.START_TAG -> {
                            if (parser.name == "row") {
                                currentRow = mutableListOf()
                            } else if (parser.name == "c") {
                                cellType = parser.getAttributeValue(null, "t") ?: ""
                                currentVal = ""
                            } else if (parser.name == "v") {
                                inValue = true
                            }
                        }
                        XmlPullParser.TEXT -> {
                            if (inValue) currentVal += parser.text
                        }
                        XmlPullParser.END_TAG -> {
                            if (parser.name == "v") {
                                inValue = false
                            } else if (parser.name == "c") {
                                val resolved = if (cellType == "s") {
                                    val idx = currentVal.toIntOrNull() ?: -1
                                    if (idx in sharedStrings.indices) sharedStrings[idx] else currentVal
                                } else {
                                    currentVal
                                }
                                currentRow.add(resolved)
                            } else if (parser.name == "row") {
                                if (currentRow.isNotEmpty()) {
                                    sheetRows.add(currentRow)
                                }
                            }
                        }
                    }
                    event = parser.next()
                }
                break
            }
            entry = zis.nextEntry
        }
        zis.close()

        return convertRowsToExcelRows(sheetRows)
    }

    /**
     * Parses CSV or Tab-delimited text
     */
    fun parseCsv(inputStream: InputStream): List<ExcelRow> {
        val reader = BufferedReader(InputStreamReader(inputStream))
        val rawRows = mutableListOf<List<String>>()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            val trimmed = line!!.trim()
            if (trimmed.isEmpty()) continue
            val delimiter = if (trimmed.contains("\t")) "\t" else ","
            val parts = trimmed.split(delimiter).map { it.trim().trim('\"') }
            rawRows.add(parts)
        }
        return convertRowsToExcelRows(rawRows)
    }

    private fun convertRowsToExcelRows(rawRows: List<List<String>>): List<ExcelRow> {
        if (rawRows.isEmpty()) return emptyList()
        val list = mutableListOf<ExcelRow>()

        // Check if first row is header
        val startIdx = if (rawRows[0].any { it.contains("Name", ignoreCase = true) || it.contains("Roll", ignoreCase = true) || it.contains("نام", ignoreCase = true) }) 1 else 0

        for (i in startIdx until rawRows.size) {
            val r = rawRows[i]
            if (r.isEmpty() || r.all { it.isBlank() }) continue

            val name = r.getOrNull(0) ?: "Student $i"
            val father = r.getOrNull(1) ?: "Father $i"
            val cls = r.getOrNull(2) ?: "Class 5"
            val sec = r.getOrNull(3) ?: "A"
            val roll = r.getOrNull(4) ?: "$i"
            val sub = r.getOrNull(5) ?: "General"
            val tot = r.getOrNull(6)?.toDoubleOrNull() ?: 100.0
            val obt = r.getOrNull(7)?.toDoubleOrNull() ?: 0.0
            val fee = r.getOrNull(8)?.toDoubleOrNull() ?: 3500.0
            val paid = r.getOrNull(9)?.toDoubleOrNull() ?: fee

            list.add(
                ExcelRow(
                    studentName = name,
                    fatherName = father,
                    className = cls,
                    section = sec,
                    rollNumber = roll,
                    subject = sub,
                    totalMarks = tot,
                    obtainedMarks = obt,
                    monthlyFee = fee,
                    paidFee = paid
                )
            )
        }
        return list
    }

    /**
     * Generates a ready-to-import template CSV
     */
    fun createSampleTemplateCsv(context: Context): File {
        val file = File(PdfGenerator.getDocumentsDir(context), "Iqra_School_Import_Template.csv")
        val content = """Student Name,Father Name,Class,Section,Roll Number,Subject,Total Marks,Obtained Marks,Monthly Fee,Paid Fee
محمد حمزہ,عبدالرشید,Class 5,A,1,Mathematics,100,92,3500,3500
عائشہ صدیقہ,محمد سلیم,Class 5,A,2,Mathematics,100,96,3500,3500
علی حسن,طارق محمود,Class 5,A,3,Mathematics,100,81,3500,2000
فاطمہ زہرا,نذیر احمد,Class 5,A,4,Mathematics,100,89,3500,3500
بلال احمد,محمد فاروق,Class 5,A,5,Mathematics,100,75,3500,0
زينب فاطمہ,احمد علی,Class 6,A,1,Science,100,88,3800,3800
حسن رضا,محسن خان,Class 6,A,2,Science,100,84,3800,3800
"""
        file.writeText(content, Charsets.UTF_8)
        return file
    }
}
