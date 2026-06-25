package com.revzion.siitglobe.data.repository

import com.revzion.siitglobe.data.api.SiitApi
import com.revzion.siitglobe.data.model.*
import com.revzion.siitglobe.data.storage.TokenStorage
import com.revzion.siitglobe.domain.model.*
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class FormsRepository(
    private val api: SiitApi,
    private val storage: TokenStorage,
) {
    private fun token(): String = storage.getToken() ?: throw Exception("Not authenticated")

    suspend fun getForms(): Result<List<FormTemplate>> =
        api.getForms(token()).map { it.map { dto -> dto.toDomain() } }

    suspend fun createForm(title: String, description: String, fields: List<FormField>): Result<FormTemplate> =
        api.createForm(token(), CreateFormRequest(
            title = title,
            description = description,
            fields = fields.mapIndexed { i, f -> CreateFormFieldRequest(
                label = f.label,
                type = f.type.name,
                required = f.required,
                options = f.options,
                order = i,
            )}
        )).map { it.toDomain() }

    suspend fun updateForm(id: String, title: String, description: String, fields: List<FormField>): Result<FormTemplate> =
        api.updateForm(token(), id, CreateFormRequest(
            title = title,
            description = description,
            fields = fields.mapIndexed { i, f -> CreateFormFieldRequest(
                label = f.label,
                type = f.type.name,
                required = f.required,
                options = f.options,
                order = i,
            )}
        )).map { it.toDomain() }

    suspend fun deleteForm(id: String): Result<Unit> =
        api.deleteForm(token(), id)

    suspend fun getResponses(formId: String): Result<List<FormResponse>> =
        api.getResponses(token(), formId).map { it.map { dto -> dto.toDomain() } }

    suspend fun submitResponse(formId: String, answers: List<FormAnswer>): Result<FormResponse> {
        val today = try {
            Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.toString()
        } catch (_: Exception) { "2024-01-01" }

        return api.submitResponse(token(), formId, SubmitResponseRequest(
            submittedAt = today,
            answers = answers.map { SubmitAnswerRequest(fieldId = it.fieldId, value = it.value) }
        )).map { it.toDomain() }
    }

    suspend fun deleteResponse(formId: String, responseId: String): Result<Unit> =
        api.deleteResponse(token(), formId, responseId)
}
