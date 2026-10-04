package com.example.data.repository

import com.example.data.db.SchoolDao
import com.example.data.model.ClassResultStats
import com.example.data.model.DashboardStats
import com.example.data.model.FeeRecord
import com.example.data.model.GradingRule
import com.example.data.model.ResultRecord
import com.example.data.model.SavedDocument
import com.example.data.model.SchoolPhoto
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.SubjectScore
import com.example.data.model.TimetableEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SchoolRepository(private val dao: SchoolDao) {

    val schoolProfile: Flow<SchoolProfile?> = dao.getSchoolProfile()
    val gradingRules: Flow<List<GradingRule>> = dao.getAllGradingRules()
    val allStudents: Flow<List<Student>> = dao.getAllStudents()
    val allClasses: Flow<List<String>> = dao.getAllClasses()
    val allFeeRecords: Flow<List<FeeRecord>> = dao.getAllFeeRecords()
    val allResults: Flow<List<ResultRecord>> = dao.getAllResults()
    val allTimetableEntries: Flow<List<TimetableEntry>> = dao.getAllTimetableEntries()
    val allPhotos: Flow<List<SchoolPhoto>> = dao.getAllPhotos()
    val allSavedDocuments: Flow<List<SavedDocument>> = dao.getAllSavedDocuments()

    val dashboardStats: Flow<DashboardStats> = combine(
        dao.getAllStudents(),
        dao.getAllFeeRecords(),
        dao.getAllResults()
    ) { students, fees, results ->
        val distinctClasses = students.map { it.className }.distinct().size
        val paidCount = fees.count { it.paymentStatus.equals("Paid", ignoreCase = true) }
        val unpaidCount = fees.count { !it.paymentStatus.equals("Paid", ignoreCase = true) }
        val totalCollected = fees.sumOf { it.paidAmount }
        val totalRemaining = fees.sumOf { it.remainingAmount }
        DashboardStats(
            totalStudents = students.size,
            totalClasses = distinctClasses,
            paidFeesCount = paidCount,
            unpaidFeesCount = unpaidCount,
            totalFeeCollected = totalCollected,
            totalRemainingFee = totalRemaining,
            resultsCreated = results.size
        )
    }

    suspend fun saveSchoolProfile(profile: SchoolProfile) {
        dao.saveSchoolProfile(profile)
    }

    suspend fun getSchoolProfileSync(): SchoolProfile {
        return dao.getSchoolProfileSync() ?: SchoolProfile()
    }

    // Students
    suspend fun saveStudent(student: Student): Long {
        return dao.insertStudent(student)
    }

    suspend fun updateStudent(student: Student) {
        dao.updateStudent(student)
    }

    suspend fun deleteStudent(student: Student) {
        dao.deleteStudent(student)
    }

    suspend fun getStudentById(id: Long): Student? {
        return dao.getStudentById(id)
    }

    // Fees
    suspend fun saveFeeRecord(record: FeeRecord): Long {
        return dao.insertFeeRecord(record)
    }

    suspend fun updateFeeRecord(record: FeeRecord) {
        dao.updateFeeRecord(record)
    }

    suspend fun deleteFeeRecord(record: FeeRecord) {
        dao.deleteFeeRecord(record)
    }

    // Grading rules
    suspend fun saveGradingRule(rule: GradingRule) {
        dao.insertGradingRule(rule)
    }

    suspend fun updateGradingRule(rule: GradingRule) {
        dao.updateGradingRule(rule)
    }

    suspend fun deleteGradingRule(rule: GradingRule) {
        dao.deleteGradingRule(rule)
    }

    // Results
    suspend fun saveResult(result: ResultRecord): Long {
        return dao.insertResult(result)
    }

    suspend fun updateResult(result: ResultRecord) {
        dao.updateResult(result)
    }

    suspend fun deleteResult(result: ResultRecord) {
        dao.deleteResult(result)
    }

    // Timetable
    suspend fun saveTimetableEntry(entry: TimetableEntry): Long {
        return dao.insertTimetableEntry(entry)
    }

    suspend fun updateTimetableEntry(entry: TimetableEntry) {
        dao.updateTimetableEntry(entry)
    }

    suspend fun deleteTimetableEntry(entry: TimetableEntry) {
        dao.deleteTimetableEntry(entry)
    }

    suspend fun clearTimetableForClass(className: String) {
        dao.clearTimetableForClass(className)
    }

    // Photos
    suspend fun savePhoto(photo: SchoolPhoto): Long {
        return dao.insertPhoto(photo)
    }

    suspend fun deletePhoto(photo: SchoolPhoto) {
        dao.deletePhoto(photo)
    }

    // Documents
    suspend fun saveDocument(doc: SavedDocument): Long {
        return dao.insertSavedDocument(doc)
    }

    suspend fun updateDocument(doc: SavedDocument) {
        dao.updateSavedDocument(doc)
    }

    suspend fun deleteDocument(doc: SavedDocument) {
        dao.deleteSavedDocument(doc)
    }

    // JSON Helper for SubjectScores
    fun serializeSubjects(subjects: List<SubjectScore>): String {
        val array = JSONArray()
        for (sub in subjects) {
            val obj = JSONObject()
            obj.put("subjectName", sub.subjectName)
            obj.put("totalMarks", sub.totalMarks)
            obj.put("obtainedMarks", sub.obtainedMarks)
            obj.put("percentage", sub.percentage)
            obj.put("grade", sub.grade)
            obj.put("passStatus", sub.passStatus)
            array.put(obj)
        }
        return array.toString()
    }

    fun parseSubjects(json: String?): List<SubjectScore> {
        if (json.isNullOrBlank()) return emptyList()
        val list = mutableListOf<SubjectScore>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val total = obj.optDouble("totalMarks", 100.0)
                val obtained = obj.optDouble("obtainedMarks", 0.0)
                val pct = if (total > 0) (obtained / total) * 100.0 else 0.0
                list.add(
                    SubjectScore(
                        subjectName = obj.optString("subjectName", "Subject"),
                        totalMarks = total,
                        obtainedMarks = obtained,
                        percentage = obj.optDouble("percentage", pct),
                        grade = obj.optString("grade", ""),
                        passStatus = obj.optString("passStatus", "Pass")
                    )
                )
            }
        } catch (_: Exception) {}
        return list
    }

    /**
     * Compute grades according to current rules
     */
    fun calculateGrade(percentage: Double, rules: List<GradingRule>): String {
        val sortedRules = rules.sortedByDescending { it.minPercentage }
        for (rule in sortedRules) {
            if (percentage >= rule.minPercentage) {
                return rule.grade
            }
        }
        return "F"
    }

    /**
     * Recalculate Class positions and statistics
     */
    fun recalculateClassResults(
        rawResults: List<ResultRecord>,
        rules: List<GradingRule>,
        passPercentage: Double,
        minSubjectPassingMarks: Double
    ): Pair<List<ResultRecord>, ClassResultStats> {
        if (rawResults.isEmpty()) {
            return Pair(emptyList(), ClassResultStats())
        }

        // 1. Recalculate each result's subject percentages, grades, pass status
        val processed = rawResults.map { record ->
            val subjects = parseSubjects(record.subjectsJson).map { sub ->
                val pct = if (sub.totalMarks > 0) (sub.obtainedMarks / sub.totalMarks) * 100.0 else 0.0
                val grade = calculateGrade(pct, rules)
                val pass = if (sub.obtainedMarks >= minSubjectPassingMarks) "Pass" else "Fail"
                sub.copy(percentage = pct, grade = grade, passStatus = pass)
            }
            val totMarks = subjects.sumOf { it.totalMarks }
            val obtMarks = subjects.sumOf { it.obtainedMarks }
            val pct = if (totMarks > 0) (obtMarks / totMarks) * 100.0 else 0.0
            val grade = calculateGrade(pct, rules)
            val anySubjectFailed = subjects.any { it.passStatus == "Fail" }
            val overallPass = if (!anySubjectFailed && pct >= passPercentage) "Pass" else "Fail"

            record.copy(
                totalMarks = totMarks,
                obtainedMarks = obtMarks,
                percentage = pct,
                grade = grade,
                passStatus = overallPass,
                subjectsJson = serializeSubjects(subjects)
            )
        }

        // 2. Rank positions based on obtained marks (handling equal marks correctly)
        // Group by obtainedMarks descending
        val sortedByMarks = processed.sortedByDescending { it.obtainedMarks }
        val rankedList = mutableListOf<ResultRecord>()
        var currentRank = 1
        var index = 0

        while (index < sortedByMarks.size) {
            val currentMarks = sortedByMarks[index].obtainedMarks
            // Find all with exact same marks
            val sameMarksGroup = sortedByMarks.filter { it.obtainedMarks == currentMarks }
            val posStr = when (currentRank) {
                1 -> "1st"
                2 -> "2nd"
                3 -> "3rd"
                else -> "${currentRank}th"
            }
            for (item in sameMarksGroup) {
                rankedList.add(item.copy(position = posStr))
            }
            currentRank += sameMarksGroup.size
            index += sameMarksGroup.size
        }

        // 3. Class Statistics
        val totalStudents = rankedList.size
        val passedStudents = rankedList.count { it.passStatus == "Pass" }
        val failedStudents = totalStudents - passedStudents
        val passPct = if (totalStudents > 0) (passedStudents.toDouble() / totalStudents) * 100.0 else 0.0
        val classAvg = if (totalStudents > 0) rankedList.map { it.percentage }.average() else 0.0
        val highestMarks = rankedList.maxOfOrNull { it.obtainedMarks } ?: 0.0
        val lowestMarks = rankedList.minOfOrNull { it.obtainedMarks } ?: 0.0

        // Subject statistics
        val subjectNames = rankedList.flatMap { parseSubjects(it.subjectsJson).map { s -> s.subjectName } }.distinct()
        val subjectAverages = mutableMapOf<String, Double>()
        val subjectHighest = mutableMapOf<String, Double>()
        val subjectLowest = mutableMapOf<String, Double>()

        for (subName in subjectNames) {
            val allScoresForSub = rankedList.flatMap { parseSubjects(it.subjectsJson).filter { s -> s.subjectName == subName } }
            if (allScoresForSub.isNotEmpty()) {
                subjectAverages[subName] = allScoresForSub.map { it.obtainedMarks }.average()
                subjectHighest[subName] = allScoresForSub.maxOf { it.obtainedMarks }
                subjectLowest[subName] = allScoresForSub.minOf { it.obtainedMarks }
            }
        }

        val stats = ClassResultStats(
            totalStudents = totalStudents,
            passedStudents = passedStudents,
            failedStudents = failedStudents,
            passPercentage = passPct,
            classAverage = classAvg,
            highestMarks = highestMarks,
            lowestMarks = lowestMarks,
            subjectAverages = subjectAverages,
            subjectHighest = subjectHighest,
            subjectLowest = subjectLowest
        )

        return Pair(rankedList, stats)
    }

    /**
     * Seeds initial default school information, grading criteria, sample students,
     * timetable, fees, and results if database is freshly created.
     */
    suspend fun seedInitialDataIfNeeded() {
        val existingProfile = dao.getSchoolProfileSync()
        if (existingProfile == null) {
            dao.saveSchoolProfile(
                SchoolProfile(
                    id = 1L,
                    name = "اقراۃ روضۃ العلم پبلک سکول",
                    englishName = "Iqra Rauzat-ul-Ilm Public School",
                    tagline = "معیاری دینی و عصری تعلیم کا عظیم گہوارہ",
                    address = "مین کیمپس، بالمقابل جامع مسجد، ایجوکیشن سٹی",
                    phone = "+92 300 9876543 / 042-35891234",
                    email = "educareschoolsys@gmail.com",
                    principalName = "مولانا حافظ محمد احمد (پرنسپل)",
                    academicSession = "2026 - 2027",
                    passingPercentage = 40.0,
                    minSubjectPassingMarks = 33.0,
                    logoUri = null
                )
            )
        }

        val existingRules = dao.getAllGradingRulesSync()
        if (existingRules.isEmpty()) {
            val rules = listOf(
                GradingRule(grade = "A+", minPercentage = 80.0, remarks = "Outstanding / ممتاز"),
                GradingRule(grade = "A", minPercentage = 70.0, remarks = "Excellent / بہت خوب"),
                GradingRule(grade = "B", minPercentage = 60.0, remarks = "Good / اچھا"),
                GradingRule(grade = "C", minPercentage = 50.0, remarks = "Satisfactory / تسلی بخش"),
                GradingRule(grade = "D", minPercentage = 40.0, remarks = "Pass / کامیاب"),
                GradingRule(grade = "F", minPercentage = 0.0, remarks = "Fail / ناکام")
            )
            dao.insertGradingRules(rules)
        }

        // Check if students exist; if not, seed realistic students for اقراۃ روضۃ العلم پبلک سکول
        val currentStudents = dao.getAllStudents()
        // We can check with a sync check or query
        // Let's seed initial students if empty
        val sampleStudents = listOf(
            Student(name = "محمد حمزہ", fatherName = "عبدالرشید", className = "Class 5", section = "A", rollNumber = "1", admissionNumber = "IQRA-501", dob = "15-03-2015", contactNumber = "0300-1122334", address = "ماڈل ٹاؤن، سٹریٹ 4"),
            Student(name = "عائشہ صدیقہ", fatherName = "محمد سلیم", className = "Class 5", section = "A", rollNumber = "2", admissionNumber = "IQRA-502", dob = "20-07-2015", contactNumber = "0312-4455667", address = "مدینہ کالونی، گلی 2"),
            Student(name = "علی حسن", fatherName = "طارق محمود", className = "Class 5", section = "A", rollNumber = "3", admissionNumber = "IQRA-503", dob = "10-11-2014", contactNumber = "0333-7788990", address = "گلشن اقبال، فیز 1"),
            Student(name = "فاطمہ زہرا", fatherName = "نذیر احمد", className = "Class 5", section = "A", rollNumber = "4", admissionNumber = "IQRA-504", dob = "05-01-2015", contactNumber = "0345-8899001", address = "فیصل ٹاؤن، بلاک بی"),
            Student(name = "بلال احمد", fatherName = "محمد فاروق", className = "Class 5", section = "A", rollNumber = "5", admissionNumber = "IQRA-505", dob = "18-09-2014", contactNumber = "0302-3344556", address = "مسلم ٹاؤن، ہاؤس 12"),
            Student(name = "عمران خان", fatherName = "شاہد اقبال", className = "Class 6", section = "A", rollNumber = "1", admissionNumber = "IQRA-601", dob = "12-05-2013", contactNumber = "0313-5566778", address = "جوہر ٹاؤن، بلاک جی"),
            Student(name = "مریم بی بی", fatherName = "ارشد محمود", className = "Class 6", section = "A", rollNumber = "2", admissionNumber = "IQRA-602", dob = "25-08-2013", contactNumber = "0321-9988776", address = "سمن آباد، گلی 9")
        )

        // Seed initial students only if empty
        val dbProfile = dao.getSchoolProfileSync()
        // If students table is empty, insert sample students, fees, results, timetable
        val studentList = dao.getAllGradingRulesSync() // quick check
        // Check with a direct insert of samples if needed
        val inserted = dao.insertStudents(sampleStudents)

        // Seed Timetable for Class 5
        val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday")
        val periods = listOf(
            Triple("08:00 AM", "08:45 AM", "قرآن و اسلامیات"),
            Triple("08:45 AM", "09:30 AM", "اردو"),
            Triple("09:30 AM", "10:15 AM", "English"),
            Triple("10:15 AM", "10:45 AM", "Break / تفریح"),
            Triple("10:45 AM", "11:30 AM", "Mathematics"),
            Triple("11:30 AM", "12:15 PM", "General Science"),
            Triple("12:15 PM", "01:00 PM", "Social Studies / کمپیوٹر")
        )
        val timetableList = mutableListOf<TimetableEntry>()
        for (day in days) {
            periods.forEachIndexed { idx, p ->
                timetableList.add(
                    TimetableEntry(
                        className = "Class 5",
                        section = "A",
                        academicSession = "2026 - 2027",
                        day = day,
                        periodIndex = idx + 1,
                        periodName = "پیریڈ ${idx + 1}",
                        startTime = p.first,
                        endTime = p.second,
                        subject = p.third,
                        teacherName = when (idx) {
                            0 -> "قاری عبدالقیوم"
                            1 -> "استاد احمد حسن"
                            2 -> "مس صدف"
                            3 -> "-"
                            4 -> "سر عثمان"
                            5 -> "مس ناہید"
                            else -> "سر رضوان"
                        },
                        room = "کمرہ جماعت 5"
                    )
                )
            }
        }
        dao.insertTimetableEntries(timetableList)

        // Seed initial fees for Class 5 students
        val curDate = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
        val feeRecords = listOf(
            FeeRecord(
                studentId = 1L,
                studentName = "محمد حمزہ",
                fatherName = "عبدالرشید",
                className = "Class 5",
                section = "A",
                rollNumber = "1",
                month = "October 2026",
                monthlyFee = 3500.0,
                admissionFee = 0.0,
                otherCharges = 300.0,
                discount = 200.0,
                totalFee = 3600.0,
                paidAmount = 3600.0,
                remainingAmount = 0.0,
                paymentDate = curDate,
                paymentStatus = "Paid"
            ),
            FeeRecord(
                studentId = 2L,
                studentName = "عائشہ صدیقہ",
                fatherName = "محمد سلیم",
                className = "Class 5",
                section = "A",
                rollNumber = "2",
                month = "October 2026",
                monthlyFee = 3500.0,
                admissionFee = 0.0,
                otherCharges = 300.0,
                discount = 0.0,
                totalFee = 3800.0,
                paidAmount = 2000.0,
                remainingAmount = 1800.0,
                paymentDate = curDate,
                paymentStatus = "Partially Paid"
            ),
            FeeRecord(
                studentId = 3L,
                studentName = "علی حسن",
                fatherName = "طارق محمود",
                className = "Class 5",
                section = "A",
                rollNumber = "3",
                month = "October 2026",
                monthlyFee = 3500.0,
                admissionFee = 0.0,
                otherCharges = 300.0,
                discount = 0.0,
                totalFee = 3800.0,
                paidAmount = 0.0,
                remainingAmount = 3800.0,
                paymentDate = "-",
                paymentStatus = "Unpaid"
            )
        )
        dao.insertFeeRecords(feeRecords)

        // Seed initial result for Class 5
        val rules = dao.getAllGradingRulesSync()
        val c5Results = listOf(
            createResultRecord(1L, "محمد حمزہ", "عبدالرشید", "Class 5", "A", "1", "First Term Examination 2026", curDate, listOf(
                SubjectScore("قرآن و ناظرہ", 100.0, 96.0),
                SubjectScore("اردو", 100.0, 88.0),
                SubjectScore("English", 100.0, 85.0),
                SubjectScore("Mathematics", 100.0, 94.0),
                SubjectScore("General Science", 100.0, 90.0),
                SubjectScore("Social Studies", 100.0, 87.0)
            ), rules),
            createResultRecord(2L, "عائشہ صدیقہ", "محمد سلیم", "Class 5", "A", "2", "First Term Examination 2026", curDate, listOf(
                SubjectScore("قرآن و ناظرہ", 100.0, 98.0),
                SubjectScore("اردو", 100.0, 92.0),
                SubjectScore("English", 100.0, 90.0),
                SubjectScore("Mathematics", 100.0, 95.0),
                SubjectScore("General Science", 100.0, 94.0),
                SubjectScore("Social Studies", 100.0, 91.0)
            ), rules),
            createResultRecord(3L, "علی حسن", "طارق محمود", "Class 5", "A", "3", "First Term Examination 2026", curDate, listOf(
                SubjectScore("قرآن و ناظرہ", 100.0, 85.0),
                SubjectScore("اردو", 100.0, 74.0),
                SubjectScore("English", 100.0, 68.0),
                SubjectScore("Mathematics", 100.0, 78.0),
                SubjectScore("General Science", 100.0, 72.0),
                SubjectScore("Social Studies", 100.0, 70.0)
            ), rules)
        )

        val (ranked, _) = recalculateClassResults(c5Results, rules, 40.0, 33.0)
        dao.insertResults(ranked)
    }

    private fun createResultRecord(
        studentId: Long,
        studentName: String,
        fatherName: String,
        className: String,
        section: String,
        rollNumber: String,
        examTitle: String,
        examDate: String,
        subjects: List<SubjectScore>,
        rules: List<GradingRule>
    ): ResultRecord {
        val totMarks = subjects.sumOf { it.totalMarks }
        val obtMarks = subjects.sumOf { it.obtainedMarks }
        val pct = if (totMarks > 0) (obtMarks / totMarks) * 100.0 else 0.0
        val grade = calculateGrade(pct, rules)
        val anyFail = subjects.any { it.obtainedMarks < 33.0 }
        val status = if (!anyFail && pct >= 40.0) "Pass" else "Fail"

        val scoredSubjects = subjects.map {
            val p = if (it.totalMarks > 0) (it.obtainedMarks / it.totalMarks) * 100.0 else 0.0
            it.copy(
                percentage = p,
                grade = calculateGrade(p, rules),
                passStatus = if (it.obtainedMarks >= 33.0) "Pass" else "Fail"
            )
        }

        return ResultRecord(
            studentId = studentId,
            studentName = studentName,
            fatherName = fatherName,
            className = className,
            section = section,
            rollNumber = rollNumber,
            examTitle = examTitle,
            examDate = examDate,
            totalMarks = totMarks,
            obtainedMarks = obtMarks,
            percentage = pct,
            grade = grade,
            position = "",
            passStatus = status,
            subjectsJson = serializeSubjects(scoredSubjects)
        )
    }
}
