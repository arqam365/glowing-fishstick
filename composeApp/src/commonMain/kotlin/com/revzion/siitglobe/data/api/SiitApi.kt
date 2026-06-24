package com.revzion.siitglobe.data.api

import com.revzion.siitglobe.data.model.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*

private const val BASE_URL = "https://api.siit.net.in"

class SiitApi(private val client: HttpClient) {

    // ── Courses ──────────────────────────────────────────────────────────────

    suspend fun getCourses(token: String): Result<List<CourseDto>> = runCatching {
        client.get("$BASE_URL/api/courses") { bearerAuth(token) }.body()
    }

    suspend fun createCourse(token: String, request: CreateCourseRequest): Result<CourseDto> = runCatching {
        client.post("$BASE_URL/api/courses") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ── Students ─────────────────────────────────────────────────────────────

    suspend fun getStudents(token: String): Result<List<StudentDto>> = runCatching {
        client.get("$BASE_URL/api/students") { bearerAuth(token) }.body()
    }

    suspend fun getStudent(token: String, id: String): Result<StudentDto> = runCatching {
        client.get("$BASE_URL/api/students/$id") { bearerAuth(token) }.body()
    }

    suspend fun createStudent(token: String, request: CreateStudentRequest): Result<StudentDto> = runCatching {
        client.post("$BASE_URL/api/students") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateStudent(token: String, id: String, request: CreateStudentRequest): Result<StudentDto> = runCatching {
        client.put("$BASE_URL/api/students/$id") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteStudent(token: String, id: String): Result<Unit> = runCatching {
        client.delete("$BASE_URL/api/students/$id") { bearerAuth(token) }
        Unit
    }

    // ── Payments ─────────────────────────────────────────────────────────────

    suspend fun getPayments(token: String): Result<List<PaymentDto>> = runCatching {
        client.get("$BASE_URL/api/payments") { bearerAuth(token) }.body()
    }

    suspend fun createPayment(token: String, request: CreatePaymentRequest): Result<PaymentDto> = runCatching {
        client.post("$BASE_URL/api/payments") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    // ── Attendance ───────────────────────────────────────────────────────────

    suspend fun getAttendance(token: String, date: String? = null, studentId: String? = null): Result<List<AttendanceDto>> = runCatching {
        client.get("$BASE_URL/api/attendance") {
            bearerAuth(token)
            if (date != null) parameter("date", date)
            if (studentId != null) parameter("studentId", studentId)
        }.body()
    }

    suspend fun markAttendance(token: String, request: MarkAttendanceRequest): Result<AttendanceDto> = runCatching {
        client.post("$BASE_URL/api/attendance") {
            bearerAuth(token)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun seedCourses(token: String): Result<Unit> = runCatching {
        client.post("$BASE_URL/api/seed") { bearerAuth(token) }
        Unit
    }
}
