package com.revzion.siitglobe.data.repository

import com.revzion.siitglobe.data.api.SiitApi
import com.revzion.siitglobe.data.model.*
import com.revzion.siitglobe.data.storage.TokenStorage
import com.revzion.siitglobe.domain.model.*
import kotlinx.datetime.LocalDate

class SiitRepository(
    private val api: SiitApi,
    private val storage: TokenStorage,
) {
    private fun token(): String = storage.getToken() ?: throw Exception("Not authenticated")

    // ── Courses ──────────────────────────────────────────────────────────────

    suspend fun getCourses(): Result<List<Course>> =
        api.getCourses(token()).map { list -> list.map { it.toDomain() } }

    // ── Students ─────────────────────────────────────────────────────────────

    suspend fun getStudents(): Result<List<Student>> =
        api.getStudents(token()).map { list -> list.map { it.toDomain() } }

    suspend fun getStudent(id: String): Result<Student> =
        api.getStudent(token(), id).map { it.toDomain() }

    suspend fun createStudent(
        enrollmentNumber: String,
        firstName: String,
        lastName: String,
        fatherName: String,
        motherName: String,
        dateOfBirth: String,
        gender: String,
        email: String?,
        phone: String,
        alternatePhone: String?,
        addressStreet: String,
        addressCity: String,
        addressState: String,
        addressPinCode: String,
        courseId: String,
        enrollmentDate: String,
    ): Result<Student> = api.createStudent(
        token(),
        CreateStudentRequest(
            enrollmentNumber = enrollmentNumber,
            firstName = firstName,
            lastName = lastName,
            fatherName = fatherName,
            motherName = motherName,
            dateOfBirth = dateOfBirth,
            gender = gender,
            email = email,
            phone = phone,
            alternatePhone = alternatePhone,
            addressStreet = addressStreet,
            addressCity = addressCity,
            addressState = addressState,
            addressPinCode = addressPinCode,
            courseId = courseId,
            enrollmentDate = enrollmentDate,
        )
    ).map { it.toDomain() }

    suspend fun deleteStudent(id: String): Result<Unit> =
        api.deleteStudent(token(), id)

    suspend fun createCourse(
        name: String,
        code: String,
        description: String?,
        durationValue: Int,
        durationUnit: String,
        feeAmount: Double,
    ): Result<Course> = api.createCourse(
        token(),
        CreateCourseRequest(
            name = name,
            code = code,
            description = description,
            durationValue = durationValue,
            durationUnit = durationUnit,
            feeAmount = feeAmount,
        )
    ).map { it.toDomain() }

    // ── Payments ─────────────────────────────────────────────────────────────

    suspend fun getPayments(): Result<List<Payment>> =
        api.getPayments(token()).map { list -> list.map { it.toDomain() } }

    // ── Attendance ────────────────────────────────────────────────────────────

    suspend fun getAttendanceByDate(date: LocalDate): Result<List<Attendance>> =
        api.getAttendance(token(), date = date.toString()).map { list -> list.map { it.toDomain() } }

    suspend fun markAttendance(studentId: String, date: LocalDate, status: String, note: String?): Result<Attendance> =
        api.markAttendance(
            token(),
            MarkAttendanceRequest(
                studentId = studentId,
                date = date.toString(),
                status = status,
                note = note,
            )
        ).map { it.toDomain() }

    suspend fun createPayment(
        receiptNumber: String,
        studentId: String,
        courseId: String,
        amount: Double,
        paymentDate: String,
        paymentMode: String,
        transactionId: String?,
        remarks: String?,
    ): Result<Payment> = api.createPayment(
        token(),
        CreatePaymentRequest(
            receiptNumber = receiptNumber,
            studentId = studentId,
            courseId = courseId,
            amount = amount,
            paymentDate = paymentDate,
            paymentMode = paymentMode,
            transactionId = transactionId,
            remarks = remarks,
        )
    ).map { it.toDomain() }
}
