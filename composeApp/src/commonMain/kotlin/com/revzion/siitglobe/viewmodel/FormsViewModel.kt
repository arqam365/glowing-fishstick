package com.revzion.siitglobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revzion.siitglobe.data.repository.FormsRepository
import com.revzion.siitglobe.domain.model.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class FormsUiState(
    val forms: List<FormTemplate> = emptyList(),
    val responses: Map<String, List<FormResponse>> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
)

class FormsViewModel(private val repository: FormsRepository) : ViewModel() {
    private val _state = MutableStateFlow(FormsUiState())
    val state: StateFlow<FormsUiState> = _state.asStateFlow()

    init {
        loadForms()
    }

    fun loadForms() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            repository.getForms()
                .onSuccess { forms ->
                    _state.value = _state.value.copy(forms = forms, isLoading = false)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message)
                }
        }
    }

    fun createForm(title: String, description: String, fields: List<FormField>) {
        viewModelScope.launch {
            repository.createForm(title, description, fields)
                .onSuccess { form ->
                    _state.value = _state.value.copy(forms = _state.value.forms + form)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun updateForm(formId: String, title: String, description: String, fields: List<FormField>) {
        viewModelScope.launch {
            repository.updateForm(formId, title, description, fields)
                .onSuccess { updated ->
                    _state.value = _state.value.copy(
                        forms = _state.value.forms.map { if (it.id == formId) updated else it }
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun deleteForm(formId: String) {
        viewModelScope.launch {
            repository.deleteForm(formId)
                .onSuccess {
                    val updatedResponses = _state.value.responses.toMutableMap().also { it.remove(formId) }
                    _state.value = _state.value.copy(
                        forms = _state.value.forms.filter { it.id != formId },
                        responses = updatedResponses
                    )
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun loadResponses(formId: String) {
        viewModelScope.launch {
            repository.getResponses(formId)
                .onSuccess { responses ->
                    val updated = _state.value.responses.toMutableMap()
                    updated[formId] = responses
                    _state.value = _state.value.copy(responses = updated)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun submitResponse(formId: String, answers: List<FormAnswer>) {
        viewModelScope.launch {
            repository.submitResponse(formId, answers)
                .onSuccess { response ->
                    val current = _state.value.responses[formId]?.toMutableList() ?: mutableListOf()
                    current.add(0, response)
                    val updated = _state.value.responses.toMutableMap()
                    updated[formId] = current
                    _state.value = _state.value.copy(responses = updated)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun deleteResponse(formId: String, responseId: String) {
        viewModelScope.launch {
            repository.deleteResponse(formId, responseId)
                .onSuccess {
                    val current = _state.value.responses[formId]?.filter { it.id != responseId } ?: emptyList()
                    val updated = _state.value.responses.toMutableMap()
                    updated[formId] = current
                    _state.value = _state.value.copy(responses = updated)
                }
                .onFailure { e ->
                    _state.value = _state.value.copy(error = e.message)
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun getForm(formId: String): FormTemplate? = _state.value.forms.find { it.id == formId }
    fun getResponses(formId: String): List<FormResponse> = _state.value.responses[formId] ?: emptyList()
}
