package com.revzion.siitglobe.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.revzion.siitglobe.domain.model.Student
import com.revzion.siitglobe.viewmodel.SiitViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    studentId: String,
    siitViewModel: SiitViewModel,
    onNavigateBack: () -> Unit,
) {
    val student = siitViewModel.getStudentById(studentId)
    val course = student?.let { siitViewModel.getCourseById(it.courseId) }
    val payments = siitViewModel.getPaymentsForStudent(studentId)

    if (student == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Student Not Found") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Student not found")
            }
        }
        return
    }

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            shape = RoundedCornerShape(20.dp),
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Student?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will permanently delete ${student.fullName} and all their data. This cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        siitViewModel.deleteStudent(studentId)
                        showDeleteConfirm = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") }
            }
        )
    }

    BoxWithConstraints {
        val isCompact = maxWidth < 600.dp
        val horizontalPadding = if (isCompact) 16.dp else 24.dp
        val headerAvatarSize = if (isCompact) 64.dp else 80.dp
        val sectionSpacing = if (isCompact) 12.dp else 16.dp

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Student Details") },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete student", tint = MaterialTheme.colorScheme.error)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(sectionSpacing)
            ) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(4.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        if (isCompact) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                StudentAvatar(student, headerAvatarSize)
                                StudentHeaderInfo(student, isCompact)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(20.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StudentAvatar(student, headerAvatarSize)
                                StudentHeaderInfo(student, isCompact)
                            }
                        }
                    }
                }

                item {
                    DetailSection(title = "Personal Information", icon = Icons.Default.Person, isCompact = isCompact) {
                        DetailRow("Father's Name", student.fatherName, isCompact)
                        DetailRow("Mother's Name", student.motherName, isCompact)
                        DetailRow("Date of Birth", student.dateOfBirth.toString(), isCompact)
                        DetailRow("Gender", student.gender.name, isCompact)
                    }
                }

                item {
                    DetailSection(title = "Contact Information", icon = Icons.Default.Phone, isCompact = isCompact) {
                        DetailRow("Phone", student.phone, isCompact)
                        if (student.email != null) {
                            DetailRow("Email", student.email, isCompact)
                        }
                        if (student.alternatePhone != null) {
                            DetailRow("Alternate Phone", student.alternatePhone, isCompact)
                        }
                    }
                }

                item {
                    DetailSection(title = "Address", icon = Icons.Default.Home, isCompact = isCompact) {
                        val address = student.address
                        Text(
                            text = "${address.street}, ${address.city}\n${address.state} - ${address.pinCode}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                item {
                    DetailSection(title = "Course Information", icon = Icons.Default.School, isCompact = isCompact) {
                        DetailRow("Course Name", course?.name ?: "Unknown", isCompact)
                        DetailRow("Course Code", course?.code ?: "N/A", isCompact)
                        DetailRow("Duration", course?.duration?.toString() ?: "N/A", isCompact)
                        DetailRow("Enrollment Date", student.enrollmentDate.toString(), isCompact)
                    }
                }

                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp)),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Payment, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                                Text(text = "Payment History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            }

                            HorizontalDivider()

                            if (payments.isEmpty()) {
                                Text(
                                    text = "No payments recorded yet",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(vertical = 16.dp)
                                )
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    payments.forEach { payment ->
                                        PaymentHistoryItem(
                                            receiptNumber = payment.receiptNumber,
                                            amount = payment.amount,
                                            date = payment.paymentDate.toString(),
                                            mode = payment.paymentMode.name,
                                            isCompact = isCompact
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun StudentAvatar(student: Student, size: Dp) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "${student.firstName.first()}${student.lastName.first()}",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onPrimary,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun StudentHeaderInfo(student: Student, isCompact: Boolean) {
    Column(
        horizontalAlignment = if (isCompact) Alignment.CenterHorizontally else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = student.fullName,
            style = if (isCompact) MaterialTheme.typography.headlineSmall else MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )

        Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)) {
            Text(
                text = student.enrollmentNumber,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        }

        Surface(
            shape = RoundedCornerShape(8.dp),
            color = when (student.status.name) {
                "ACTIVE" -> MaterialTheme.colorScheme.secondaryContainer
                "COMPLETED" -> MaterialTheme.colorScheme.primaryContainer
                "DROPPED" -> MaterialTheme.colorScheme.errorContainer
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ) {
            Text(
                text = student.status.name,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                style = MaterialTheme.typography.labelMedium,
                color = when (student.status.name) {
                    "ACTIVE" -> MaterialTheme.colorScheme.onSecondaryContainer
                    "COMPLETED" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "DROPPED" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun DetailSection(
    title: String,
    icon: ImageVector,
    isCompact: Boolean,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            HorizontalDivider()

            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String, isCompact: Boolean) {
    if (isCompact) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(text = value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PaymentHistoryItem(
    receiptNumber: String,
    amount: Double,
    date: String,
    mode: String,
    isCompact: Boolean,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    Icons.Default.Payment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(text = receiptNumber, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text(text = "$date • $mode", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Text(
                text = "₹$amount",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
