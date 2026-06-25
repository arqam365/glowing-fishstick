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

// ── Forms DTOs ───────────────────────────────────────────────────────────────

@Serializable
data class FormFieldDto(
    val id: String,
    val formId: String,
    val label: String,
    val type: String,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val order: Int = 0,
)

@Serializable
data class FormTemplateDto(
    val id: String,
    val title: String,
    val description: String = "",
    val fields: List<FormFieldDto> = emptyList(),
    val createdAt: String? = null,
)

@Serializable
data class FormAnswerDto(
    val id: String,
    val responseId: String,
    val fieldId: String,
    val value: String = "",
)

@Serializable
data class FormResponseDto(
    val id: String,
    val formId: String,
    val submittedAt: String,
    val answers: List<FormAnswerDto> = emptyList(),
)

@Serializable
data class CreateFormRequest(
    val title: String,
    val description: String = "",
    val fields: List<CreateFormFieldRequest> = emptyList(),
)

@Serializable
data class CreateFormFieldRequest(
    val label: String,
    val type: String,
    val required: Boolean = false,
    val options: List<String> = emptyList(),
    val order: Int = 0,
)

@Serializable
data class SubmitResponseRequest(
    val submittedAt: String,
    val answers: List<SubmitAnswerRequest> = emptyList(),
)

@Serializable
data class SubmitAnswerRequest(
    val fieldId: String,
    val value: String = "",
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

fun FormFieldDto.toDomain() = com.revzion.siitglobe.domain.model.FormField(
    id = id,
    label = label,
    type = runCatching { com.revzion.siitglobe.domain.model.FieldType.valueOf(type) }.getOrElse { com.revzion.siitglobe.domain.model.FieldType.TEXT },
    required = required,
    options = options,
)

fun FormTemplateDto.toDomain() = com.revzion.siitglobe.domain.model.FormTemplate(
    id = id,
    title = title,
    description = description,
    fields = fields.sortedBy { it.order }.map { it.toDomain() },
    createdAt = runCatching { LocalDate.parse(createdAt!!.take(10)) }.getOrElse { LocalDate(2024, 1, 1) },
)

fun FormResponseDto.toDomain() = com.revzion.siitglobe.domain.model.FormResponse(
    id = id,
    formId = formId,
    submittedAt = runCatching { LocalDate.parse(submittedAt.take(10)) }.getOrElse { LocalDate(2024, 1, 1) },
    answers = answers.map { com.revzion.siitglobe.domain.model.FormAnswer(fieldId = it.fieldId, value = it.value) },
)
