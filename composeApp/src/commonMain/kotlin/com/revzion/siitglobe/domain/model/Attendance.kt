package com.revzion.siitglobe.domain.model

import kotlinx.datetime.LocalDate

data class Attendance(
    val id: String,
    val studentId: String,
    val date: LocalDate,
    val status: AttendanceStatus,
    val note: String? = null,
    val createdAt: String = "",
)

enum class AttendanceStatus { PRESENT, ABSENT, LATE, HALF_DAY }
