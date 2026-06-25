package com.revzion.siitglobe.domain.model

import kotlinx.datetime.LocalDate

enum class FieldType {
    TEXT, PARAGRAPH, NUMBER, DATE, DROPDOWN, CHECKBOX, RADIO
}

data class FormField(
    val id: String,
    val label: String,
    val type: FieldType,
    val required: Boolean = false,
    val options: List<String> = emptyList()
)

data class FormTemplate(
    val id: String,
    val title: String,
    val description: String = "",
    val fields: List<FormField>,
    val createdAt: LocalDate
)

data class FormAnswer(
    val fieldId: String,
    val value: String
)

data class FormResponse(
    val id: String,
    val formId: String,
    val submittedAt: LocalDate,
    val answers: List<FormAnswer>
)
