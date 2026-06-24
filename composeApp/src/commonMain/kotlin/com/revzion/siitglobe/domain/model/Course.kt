package com.revzion.siitglobe.domain.model

data class Course(
    val id: String,
    val name: String,
    val code: String,
    val description: String,
    val duration: CourseDuration,
    val feeAmount: Double,
    val isActive: Boolean = true,
)

data class CourseDuration(
    val value: Int,
    val unit: DurationUnit,
) {
    override fun toString(): String = "$value ${unit.name.lowercase().replaceFirstChar { it.uppercase() }}${if (value > 1) "s" else ""}"
}

enum class DurationUnit { MONTH, YEAR }
