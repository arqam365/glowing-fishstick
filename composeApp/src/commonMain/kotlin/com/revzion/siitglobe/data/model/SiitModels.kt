package com.revzion.siitglobe.data.model

import com.revzion.siitglobe.domain.model.*
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class StudentDto(
    val id: String,
    val enrollmentNumber: String,
    val firstName: String,
    val lastName: String,
    val fatherName: String,
    val motherName: String,
    val dateOfBirth: String,
    val gender: String,
    val email: String? = null,
    val phone: String,
    val alternatePhone: String? = null,
    val addressStreet: String,
    val addressCity: String,
    val addressState: String,
    val addressPinCode: String,
    val courseId: String,
    val enrollmentDate: String,
    val status: String,
    val photoUrl: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreateStudentRequest(
    val enrollmentNumber: String,
    val firstName: String,
    val lastName: String,
    val fatherName: String,
    val motherName: String,
    val dateOfBirth: String,
    val gender: String,
    val email: String? = null,
    val phone: String,
    val alternatePhone: String? = null,
    val addressStreet: String,
    val addressCity: String,
    val addressState: String,
    val addressPinCode: String,
    val courseId: String,
    val enrollmentDate: String,
    val status: String = "ACTIVE",
    val photoUrl: String? = null,
)

@Serializable
data class CourseDto(
    val id: String,
    val name: String,
    val code: String,
    val description: String? = null,
    val durationValue: String,
    val durationUnit: String,
    val feeAmount: Double,
    val isActive: Boolean = true,
    val createdAt: String? = null,
)

@Serializable
data class PaymentDto(
    val id: String,
    val receiptNumber: String,
    val studentId: String,
    val courseId: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMode: String,
    val status: String,
    val transactionId: String? = null,
    val remarks: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class CreatePaymentRequest(
    val receiptNumber: String,
    val studentId: String,
    val courseId: String,
    val amount: Double,
    val paymentDate: String,
    val paymentMode: String,
    val status: String = "COMPLETED",
    val transactionId: String? = null,
    val remarks: String? = null,
)

@Serializable
data class CreateCourseRequest(
    val name: String,
    val code: String,
    val description: String? = null,
    val durationValue: Int,
    val durationUnit: String,
    val feeAmount: Double,
)

@Serializable
data class AttendanceDto(
    val id: String,
    val studentId: String,
    val date: String,
    val status: String,
    val note: String? = null,
    val createdAt: String? = null,
)

@Serializable
data class MarkAttendanceRequest(
    val studentId: String,
    val date: String,
    val status: String,
    val note: String? = null,
)

// ── Mappers ──────────────────────────────────────────────────────────────────

fun StudentDto.toDomain() = Student(
    id = id,
    enrollmentNumber = enrollmentNumber,
    firstName = firstName,
    lastName = lastName,
    fatherName = fatherName,
    motherName = motherName,
    dateOfBirth = runCatching { LocalDate.parse(dateOfBirth) }.getOrElse { LocalDate(2000, 1, 1) },
    gender = runCatching { Gender.valueOf(gender) }.getOrElse { Gender.MALE },
    email = email,
    phone = phone,
    alternatePhone = alternatePhone,
    address = Address(addressStreet, addressCity, addressState, addressPinCode),
    courseId = courseId,
    enrollmentDate = runCatching { LocalDate.parse(enrollmentDate) }.getOrElse { LocalDate(2024, 1, 1) },
    status = runCatching { StudentStatus.valueOf(status) }.getOrElse { StudentStatus.ACTIVE },
    photoUrl = photoUrl,
    createdAt = createdAt ?: "",
)

fun CourseDto.toDomain() = Course(
    id = id,
    name = name,
    code = code,
    description = description ?: "",
    duration = CourseDuration(
        value = durationValue.toIntOrNull() ?: 0,
        unit = runCatching { DurationUnit.valueOf(durationUnit) }.getOrElse { DurationUnit.MONTH },
    ),
    feeAmount = feeAmount,
    isActive = isActive,
)

fun AttendanceDto.toDomain() = com.revzion.siitglobe.domain.model.Attendance(
    id = id,
    studentId = studentId,
    date = runCatching { LocalDate.parse(date) }.getOrElse { LocalDate(2024, 1, 1) },
    status = runCatching { com.revzion.siitglobe.domain.model.AttendanceStatus.valueOf(status) }.getOrElse { com.revzion.siitglobe.domain.model.AttendanceStatus.ABSENT },
    note = note,
    createdAt = createdAt ?: "",
)

fun PaymentDto.toDomain() = Payment(
    id = id,
    receiptNumber = receiptNumber,
    studentId = studentId,
    courseId = courseId,
    amount = amount,
    paymentDate = runCatching { LocalDate.parse(paymentDate) }.getOrElse { LocalDate(2024, 1, 1) },
    paymentMode = runCatching { PaymentMode.valueOf(paymentMode) }.getOrElse { PaymentMode.CASH },
    status = runCatching { PaymentStatus.valueOf(status) }.getOrElse { PaymentStatus.COMPLETED },
    transactionId = transactionId,
    remarks = remarks,
    createdAt = createdAt ?: "",
)
