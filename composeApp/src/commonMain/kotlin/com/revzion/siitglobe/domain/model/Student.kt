package com.revzion.siitglobe.domain.model

import kotlinx.datetime.LocalDate

data class Student(
    val id: String,
    val enrollmentNumber: String,
    val firstName: String,
    val lastName: String,
    val fatherName: String,
    val motherName: String,
    val dateOfBirth: LocalDate,
    val gender: Gender,
    val email: String?,
    val phone: String,
    val alternatePhone: String?,
    val address: Address,
    val courseId: String,
    val enrollmentDate: LocalDate,
    val status: StudentStatus,
    val photoUrl: String?,
    val createdAt: String,
) {
    val fullName: String get() = "$firstName $lastName"
}

data class Address(
    val street: String,
    val city: String,
    val state: String,
    val pinCode: String,
)

enum class Gender { MALE, FEMALE, OTHER }

enum class StudentStatus { ACTIVE, COMPLETED, DROPPED, ON_LEAVE }
