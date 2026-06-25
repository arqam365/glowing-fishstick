package com.revzion.siitglobe.viewmodel

import androidx.lifecycle.ViewModel
import com.revzion.siitglobe.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

data class FormsUiState(
    val forms: List<FormTemplate> = emptyList(),
    val responses: Map<String, List<FormResponse>> = emptyMap()
)

class FormsViewModel : ViewModel() {
    private val _state = MutableStateFlow(FormsUiState())
    val state: StateFlow<FormsUiState> = _state.asStateFlow()

    private fun today() = try {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    } catch (_: Exception) {
        kotlinx.datetime.LocalDate(2024, 1, 1)
    }

    fun createForm(title: String, description: String, fields: List<FormField>) {
        val form = FormTemplate(
            id = "form_${System.currentTimeMillis()}",
            title = title,
            description = description,
            fields = fields,
            createdAt = today()
        )
        _state.value = _state.value.copy(forms = _state.value.forms + form)
    }

    fun updateForm(formId: String, title: String, description: String, fields: List<FormField>) {
        _state.value = _state.value.copy(
            forms = _state.value.forms.map { f ->
                if (f.id == formId) f.copy(title = title, description = description, fields = fields) else f
            }
        )
    }

    fun deleteForm(formId: String) {
        val updatedResponses = _state.value.responses.toMutableMap().also { it.remove(formId) }
        _state.value = _state.value.copy(
            forms = _state.value.forms.filter { it.id != formId },
            responses = updatedResponses
        )
    }

    fun submitResponse(formId: String, answers: List<FormAnswer>) {
        val response = FormResponse(
            id = "resp_${System.currentTimeMillis()}",
            formId = formId,
            submittedAt = today(),
            answers = answers
        )
        val current = _state.value.responses[formId]?.toMutableList() ?: mutableListOf()
        current.add(response)
        val updated = _state.value.responses.toMutableMap()
        updated[formId] = current
        _state.value = _state.value.copy(responses = updated)
    }

    fun deleteResponse(formId: String, responseId: String) {
        val current = _state.value.responses[formId]?.filter { it.id != responseId } ?: emptyList()
        val updated = _state.value.responses.toMutableMap()
        updated[formId] = current
        _state.value = _state.value.copy(responses = updated)
    }

    fun getForm(formId: String) = _state.value.forms.find { it.id == formId }
    fun getResponses(formId: String) = _state.value.responses[formId] ?: emptyList()
}
