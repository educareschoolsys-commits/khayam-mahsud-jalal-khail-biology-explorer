package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "school_profile")
data class SchoolProfile(
    @PrimaryKey val id: Long = 1L,
    val name: String = "اقراۃ روضۃ العلم پبلک سکول",
    val englishName: String = "Iqra Rauzat-ul-Ilm Public School",
    val tagline: String = "معیاری دینی و عصری تعلیم کا عظیم گہوارہ",
    val address: String = "Main Campus, Education City",
    val phone: String = "+92 300 1234567",
    val email: String = "educareschoolsys@gmail.com",
    val principalName: String = "Principal / ناظمِ اعلیٰ",
    val academicSession: String = "2026 - 2027",
    val passingPercentage: Double = 40.0,
    val minSubjectPassingMarks: Double = 33.0,
    val logoUri: String? = null
)

@Entity(tableName = "grading_rules")
data class GradingRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val grade: String,
    val minPercentage: Double,
    val remarks: String
)

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val fatherName: String,
    val className: String,
    val section: String = "A",
    val rollNumber: String,
    val admissionNumber: String,
    val dob: String = "",
    val contactNumber: String = "",
    val address: String = "",
    val photoUri: String? = null
)

@Entity(tableName = "fee_records")
data class FeeRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val fatherName: String,
    val className: String,
    val section: String,
    val rollNumber: String,
    val month: String, // e.g., "October 2026"
    val monthlyFee: Double,
    val admissionFee: Double = 0.0,
    val otherCharges: Double = 0.0,
    val discount: Double = 0.0,
    val totalFee: Double, // monthlyFee + admissionFee + otherCharges - discount
    val paidAmount: Double,
    val remainingAmount: Double, // totalFee - paidAmount
    val paymentDate: String,
    val paymentStatus: String // "Paid", "Unpaid", "Partially Paid"
)

data class SubjectScore(
    val subjectName: String,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val percentage: Double = if (totalMarks > 0) (obtainedMarks / totalMarks) * 100.0 else 0.0,
    val grade: String = "",
    val passStatus: String = "Pass"
)

@Entity(tableName = "result_records")
data class ResultRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val studentName: String,
    val fatherName: String,
    val className: String,
    val section: String,
    val rollNumber: String,
    val examTitle: String, // e.g. "Annual Examination 2026"
    val examDate: String,
    val totalMarks: Double,
    val obtainedMarks: Double,
    val percentage: Double,
    val grade: String,
    val position: String, // "1st", "2nd", "3rd", etc.
    val passStatus: String, // "Pass" or "Fail"
    val subjectsJson: String // Serialized List<SubjectScore>
)

@Entity(tableName = "timetable_entries")
data class TimetableEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val className: String,
    val section: String,
    val academicSession: String,
    val day: String, // "Monday", "Tuesday", etc.
    val periodIndex: Int, // 1, 2, 3...
    val periodName: String, // "Period 1"
    val startTime: String, // "08:00 AM"
    val endTime: String, // "08:45 AM"
    val subject: String,
    val teacherName: String,
    val room: String = "Classroom"
)

@Entity(tableName = "school_photos")
data class SchoolPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String, // "Student", "Logo", "Timetable", "Campus", "Event"
    val imageUri: String,
    val dateAdded: String,
    val associatedId: Long? = null
)

@Entity(tableName = "saved_documents")
data class SavedDocument(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val docType: String, // "RESULT", "CLASS_RESULT", "FEE_RECEIPT", "FEE_SHEET", "TIMETABLE", "STUDENT_RECORD"
    val fileFormat: String, // "PDF", "DOCX"
    val filePath: String,
    val fileSize: String,
    val dateCreated: String,
    val description: String = ""
)

// Statistics Models
data class DashboardStats(
    val totalStudents: Int = 0,
    val totalClasses: Int = 0,
    val paidFeesCount: Int = 0,
    val unpaidFeesCount: Int = 0,
    val totalFeeCollected: Double = 0.0,
    val totalRemainingFee: Double = 0.0,
    val resultsCreated: Int = 0
)

data class ClassResultStats(
    val totalStudents: Int = 0,
    val passedStudents: Int = 0,
    val failedStudents: Int = 0,
    val passPercentage: Double = 0.0,
    val classAverage: Double = 0.0,
    val highestMarks: Double = 0.0,
    val lowestMarks: Double = 0.0,
    val subjectAverages: Map<String, Double> = emptyMap(),
    val subjectHighest: Map<String, Double> = emptyMap(),
    val subjectLowest: Map<String, Double> = emptyMap()
)
