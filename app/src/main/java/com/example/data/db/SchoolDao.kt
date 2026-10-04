package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete
import com.example.data.model.FeeRecord
import com.example.data.model.GradingRule
import com.example.data.model.ResultRecord
import com.example.data.model.SavedDocument
import com.example.data.model.SchoolPhoto
import com.example.data.model.SchoolProfile
import com.example.data.model.Student
import com.example.data.model.TimetableEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface SchoolDao {
    // School Profile
    @Query("SELECT * FROM school_profile WHERE id = 1 LIMIT 1")
    fun getSchoolProfile(): Flow<SchoolProfile?>

    @Query("SELECT * FROM school_profile WHERE id = 1 LIMIT 1")
    suspend fun getSchoolProfileSync(): SchoolProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSchoolProfile(profile: SchoolProfile)

    // Grading Rules
    @Query("SELECT * FROM grading_rules ORDER BY minPercentage DESC")
    fun getAllGradingRules(): Flow<List<GradingRule>>

    @Query("SELECT * FROM grading_rules ORDER BY minPercentage DESC")
    suspend fun getAllGradingRulesSync(): List<GradingRule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradingRule(rule: GradingRule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGradingRules(rules: List<GradingRule>)

    @Update
    suspend fun updateGradingRule(rule: GradingRule)

    @Delete
    suspend fun deleteGradingRule(rule: GradingRule)

    // Students
    @Query("SELECT * FROM students ORDER BY className ASC, section ASC, CAST(rollNumber AS INTEGER) ASC")
    fun getAllStudents(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id LIMIT 1")
    suspend fun getStudentById(id: Long): Student?

    @Query("SELECT * FROM students WHERE className = :className ORDER BY CAST(rollNumber AS INTEGER) ASC")
    fun getStudentsByClass(className: String): Flow<List<Student>>

    @Query("SELECT DISTINCT className FROM students ORDER BY className ASC")
    fun getAllClasses(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: Student): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudents(students: List<Student>)

    @Update
    suspend fun updateStudent(student: Student)

    @Delete
    suspend fun deleteStudent(student: Student)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudentById(id: Long)

    // Fees
    @Query("SELECT * FROM fee_records ORDER BY id DESC")
    fun getAllFeeRecords(): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getFeesForStudent(studentId: Long): Flow<List<FeeRecord>>

    @Query("SELECT * FROM fee_records WHERE month = :month ORDER BY id DESC")
    fun getFeesByMonth(month: String): Flow<List<FeeRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecord(record: FeeRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFeeRecords(records: List<FeeRecord>)

    @Update
    suspend fun updateFeeRecord(record: FeeRecord)

    @Delete
    suspend fun deleteFeeRecord(record: FeeRecord)

    @Query("DELETE FROM fee_records WHERE id = :id")
    suspend fun deleteFeeRecordById(id: Long)

    // Results
    @Query("SELECT * FROM result_records ORDER BY id DESC")
    fun getAllResults(): Flow<List<ResultRecord>>

    @Query("SELECT * FROM result_records WHERE className = :className AND examTitle = :examTitle ORDER BY CAST(rollNumber AS INTEGER) ASC")
    fun getResultsByClassAndExam(className: String, examTitle: String): Flow<List<ResultRecord>>

    @Query("SELECT * FROM result_records WHERE className = :className AND examTitle = :examTitle")
    suspend fun getResultsByClassAndExamSync(className: String, examTitle: String): List<ResultRecord>

    @Query("SELECT * FROM result_records WHERE studentId = :studentId ORDER BY id DESC")
    fun getResultsForStudent(studentId: Long): Flow<List<ResultRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: ResultRecord): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResults(results: List<ResultRecord>)

    @Update
    suspend fun updateResult(result: ResultRecord)

    @Delete
    suspend fun deleteResult(result: ResultRecord)

    @Query("DELETE FROM result_records WHERE id = :id")
    suspend fun deleteResultById(id: Long)

    // Timetable
    @Query("SELECT * FROM timetable_entries ORDER BY day ASC, periodIndex ASC")
    fun getAllTimetableEntries(): Flow<List<TimetableEntry>>

    @Query("SELECT * FROM timetable_entries WHERE className = :className ORDER BY day ASC, periodIndex ASC")
    fun getTimetableForClass(className: String): Flow<List<TimetableEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEntry(entry: TimetableEntry): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimetableEntries(entries: List<TimetableEntry>)

    @Update
    suspend fun updateTimetableEntry(entry: TimetableEntry)

    @Delete
    suspend fun deleteTimetableEntry(entry: TimetableEntry)

    @Query("DELETE FROM timetable_entries WHERE className = :className")
    suspend fun clearTimetableForClass(className: String)

    // School Photos
    @Query("SELECT * FROM school_photos ORDER BY id DESC")
    fun getAllPhotos(): Flow<List<SchoolPhoto>>

    @Query("SELECT * FROM school_photos WHERE category = :category ORDER BY id DESC")
    fun getPhotosByCategory(category: String): Flow<List<SchoolPhoto>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhoto(photo: SchoolPhoto): Long

    @Delete
    suspend fun deletePhoto(photo: SchoolPhoto)

    @Query("DELETE FROM school_photos WHERE id = :id")
    suspend fun deletePhotoById(id: Long)

    // Saved Documents
    @Query("SELECT * FROM saved_documents ORDER BY id DESC")
    fun getAllSavedDocuments(): Flow<List<SavedDocument>>

    @Query("SELECT * FROM saved_documents WHERE docType = :docType ORDER BY id DESC")
    fun getSavedDocumentsByType(docType: String): Flow<List<SavedDocument>>

    @Query("SELECT * FROM saved_documents WHERE fileFormat = :format ORDER BY id DESC")
    fun getSavedDocumentsByFormat(format: String): Flow<List<SavedDocument>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedDocument(doc: SavedDocument): Long

    @Update
    suspend fun updateSavedDocument(doc: SavedDocument)

    @Delete
    suspend fun deleteSavedDocument(doc: SavedDocument)

    @Query("DELETE FROM saved_documents WHERE id = :id")
    suspend fun deleteSavedDocumentById(id: Long)
}
