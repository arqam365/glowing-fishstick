package com.revzion.siitglobe.data.repository

import com.revzion.siitglobe.domain.model.*
import kotlinx.datetime.LocalDate

object MockDataRepository {

    var currentUserName: String = "Admin"

    val courses = mutableListOf(
        Course(
            id = "C001",
            name = "Basic Computers",
            code = "BC001",
            description = "Introduction to computers and basic operations",
            duration = CourseDuration(3, DurationUnit.MONTH),
            feeAmount = 3000.0,
        ),
        Course(
            id = "C002",
            name = "CCC NIELIT",
            code = "CCC001",
            description = "Course on Computer Concepts by NIELIT",
            duration = CourseDuration(3, DurationUnit.MONTH),
            feeAmount = 2500.0,
        ),
        Course(
            id = "C003",
            name = "DCA",
            code = "DCA001",
            description = "Diploma in Computer Applications",
            duration = CourseDuration(6, DurationUnit.MONTH),
            feeAmount = 8000.0,
        ),
        Course(
            id = "C004",
            name = "Tally with GST",
            code = "TALLY001",
            description = "Tally with GST implementation",
            duration = CourseDuration(2, DurationUnit.MONTH),
            feeAmount = 5000.0,
        ),
    )

    val students = mutableListOf(
        Student(
            id = "S001",
            enrollmentNumber = "SIIT2024001",
            firstName = "Rahul",
            lastName = "Sharma",
            fatherName = "Raj Kumar Sharma",
            motherName = "Sunita Sharma",
            dateOfBirth = LocalDate(2005, 3, 15),
            gender = Gender.MALE,
            email = "rahul.sharma@email.com",
            phone = "9876543210",
            alternatePhone = null,
            address = Address("123 Main Street", "Delhi", "Delhi", "110001"),
            courseId = "C001",
            enrollmentDate = LocalDate(2024, 1, 15),
            status = StudentStatus.ACTIVE,
            photoUrl = null,
            createdAt = "2024-01-15T10:00:00Z",
        ),
        Student(
            id = "S002",
            enrollmentNumber = "SIIT2024002",
            firstName = "Priya",
            lastName = "Singh",
            fatherName = "Vikram Singh",
            motherName = "Anjali Singh",
            dateOfBirth = LocalDate(2004, 7, 22),
            gender = Gender.FEMALE,
            email = "priya.singh@email.com",
            phone = "9876543211",
            alternatePhone = "9876543212",
            address = Address("456 Park Avenue", "Delhi", "Delhi", "110002"),
            courseId = "C002",
            enrollmentDate = LocalDate(2024, 2, 1),
            status = StudentStatus.ACTIVE,
            photoUrl = null,
            createdAt = "2024-02-01T11:30:00Z",
        ),
        Student(
            id = "S003",
            enrollmentNumber = "SIIT2024003",
            firstName = "Amit",
            lastName = "Patel",
            fatherName = "Suresh Patel",
            motherName = "Geeta Patel",
            dateOfBirth = LocalDate(2003, 11, 10),
            gender = Gender.MALE,
            email = "amit.patel@email.com",
            phone = "9876543213",
            alternatePhone = null,
            address = Address("789 Lake View", "Delhi", "Delhi", "110003"),
            courseId = "C003",
            enrollmentDate = LocalDate(2024, 1, 20),
            status = StudentStatus.ACTIVE,
            photoUrl = null,
            createdAt = "2024-01-20T09:15:00Z",
        ),
    )

    val payments = mutableListOf(
        Payment(
            id = "P001",
            receiptNumber = "RCP2024001",
            studentId = "S001",
            courseId = "C001",
            amount = 5000.0,
            paymentDate = LocalDate(2024, 1, 15),
            paymentMode = PaymentMode.CASH,
            status = PaymentStatus.COMPLETED,
            remarks = "Registration fee",
            createdAt = "2024-01-15T10:30:00Z",
        ),
        Payment(
            id = "P002",
            receiptNumber = "RCP2024002",
            studentId = "S002",
            courseId = "C002",
            amount = 3500.0,
            paymentDate = LocalDate(2024, 2, 1),
            paymentMode = PaymentMode.UPI,
            status = PaymentStatus.COMPLETED,
            transactionId = "UPI123456789",
            remarks = "First installment",
            createdAt = "2024-02-01T12:00:00Z",
        ),
        Payment(
            id = "P003",
            receiptNumber = "RCP2024003",
            studentId = "S003",
            courseId = "C003",
            amount = 7500.0,
            paymentDate = LocalDate(2024, 1, 20),
            paymentMode = PaymentMode.CARD,
            status = PaymentStatus.COMPLETED,
            transactionId = "CARD987654321",
            remarks = "Full payment",
            createdAt = "2024-01-20T14:45:00Z",
        ),
    )

    fun getCourseById(id: String): Course? = courses.find { it.id == id }

    fun getStudentById(id: String): Student? = students.find { it.id == id }

    fun addStudent(student: Student) { students.add(student) }

    fun addPayment(payment: Payment) { payments.add(payment) }

    fun getTotalStudents(): Int = students.size

    fun getActiveStudents(): Int = students.count { it.status == StudentStatus.ACTIVE }

    fun getTotalRevenue(): Double = payments.filter { it.status == PaymentStatus.COMPLETED }.sumOf { it.amount }
}
