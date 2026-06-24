package com.revzion.siitglobe.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.revzion.siitglobe.data.repository.SiitRepository
import com.revzion.siitglobe.domain.model.Attendance
import com.revzion.siitglobe.domain.model.AttendanceStatus
import com.revzion.siitglobe.domain.model.Course
import com.revzion.siitglobe.domain.model.Payment
import com.revzion.siitglobe.domain.model.Student
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate

data class SiitUiState(
    val isLoading: Boolean = false,
    val students: List<Student> = emptyList(),
    val courses: List<Course> = emptyList(),
    val payments: List<Payment> = emptyList(),
    val attendanceByDate: Map<String, List<Attendance>> = emptyMap(),
    val error: String? = null,
    val studentSaveSuccess: Boolean = false,
    val paymentSaveSuccess: Boolean = false,
    val courseSaveSuccess: Boolean = false,
)

class SiitViewModel(private val repository: SiitRepository) : ViewModel() {

    private val _state = MutableStateFlow(SiitUiState())
    val state: StateFlow<SiitUiState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            val students = repository.getStudents().getOrElse { emptyList() }
            val courses = repository.getCourses().getOrElse { emptyList() }
            val payments = repository.getPayments().getOrElse { emptyList() }
            _state.value = _state.value.copy(
                isLoading = false,
                students = students,
                courses = courses,
                payments = payments,
            )
        }
    }

    fun createStudent(
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
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, studentSaveSuccess = false)
            repository.createStudent(
                enrollmentNumber, firstName, lastName, fatherName, motherName,
                dateOfBirth, gender, email, phone, alternatePhone,
                addressStreet, addressCity, addressState, addressPinCode,
                courseId, enrollmentDate,
            ).onSuccess { newStudent ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    students = _state.value.students + newStudent,
                    studentSaveSuccess = true,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to save student")
            }
        }
    }

    fun deleteStudent(id: String) {
        viewModelScope.launch {
            repository.deleteStudent(id).onSuccess {
                _state.value = _state.value.copy(
                    students = _state.value.students.filter { it.id != id }
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Failed to delete student")
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun resetStudentSaveSuccess() {
        _state.value = _state.value.copy(studentSaveSuccess = false)
    }

    fun resetPaymentSaveSuccess() {
        _state.value = _state.value.copy(paymentSaveSuccess = false)
    }

    fun resetCourseSaveSuccess() {
        _state.value = _state.value.copy(courseSaveSuccess = false)
    }

    fun createCourse(
        name: String,
        code: String,
        description: String?,
        durationValue: Int,
        durationUnit: String,
        feeAmount: Double,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, courseSaveSuccess = false)
            repository.createCourse(name, code, description, durationValue, durationUnit, feeAmount)
                .onSuccess { newCourse ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        courses = _state.value.courses + newCourse,
                        courseSaveSuccess = true,
                    )
                }.onFailure { e ->
                    _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to save course")
                }
        }
    }

    fun createPayment(
        receiptNumber: String,
        studentId: String,
        courseId: String,
        amount: Double,
        paymentDate: String,
        paymentMode: String,
        transactionId: String?,
        remarks: String?,
    ) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null, paymentSaveSuccess = false)
            repository.createPayment(
                receiptNumber, studentId, courseId, amount, paymentDate, paymentMode, transactionId, remarks
            ).onSuccess { newPayment ->
                _state.value = _state.value.copy(
                    isLoading = false,
                    payments = listOf(newPayment) + _state.value.payments,
                    paymentSaveSuccess = true,
                )
            }.onFailure { e ->
                _state.value = _state.value.copy(isLoading = false, error = e.message ?: "Failed to save payment")
            }
        }
    }

    fun loadAttendanceForDate(date: LocalDate) {
        viewModelScope.launch {
            repository.getAttendanceByDate(date).onSuccess { records ->
                val key = date.toString()
                val updated = _state.value.attendanceByDate.toMutableMap()
                updated[key] = records
                _state.value = _state.value.copy(attendanceByDate = updated)
            }
        }
    }

    fun markAttendance(studentId: String, date: LocalDate, status: AttendanceStatus) {
        viewModelScope.launch {
            repository.markAttendance(studentId, date, status.name, null).onSuccess { record ->
                val key = date.toString()
                val current = _state.value.attendanceByDate[key]?.toMutableList() ?: mutableListOf()
                val idx = current.indexOfFirst { it.studentId == studentId }
                if (idx >= 0) current[idx] = record else current.add(record)
                val updated = _state.value.attendanceByDate.toMutableMap()
                updated[key] = current
                _state.value = _state.value.copy(attendanceByDate = updated)
            }.onFailure { e ->
                _state.value = _state.value.copy(error = e.message ?: "Failed to mark attendance")
            }
        }
    }

    fun getAttendanceForDate(date: LocalDate): List<Attendance> =
        _state.value.attendanceByDate[date.toString()] ?: emptyList()

    fun getStudentById(id: String): Student? = _state.value.students.find { it.id == id }

    fun getCourseById(id: String): Course? = _state.value.courses.find { it.id == id }

    fun getPaymentsForStudent(studentId: String): List<Payment> =
        _state.value.payments.filter { it.studentId == studentId }

    fun getTotalRevenue(): Double =
        _state.value.payments.filter { it.status.name == "COMPLETED" }.sumOf { it.amount }

    fun getActiveStudentCount(): Int =
        _state.value.students.count { it.status.name == "ACTIVE" }
}
