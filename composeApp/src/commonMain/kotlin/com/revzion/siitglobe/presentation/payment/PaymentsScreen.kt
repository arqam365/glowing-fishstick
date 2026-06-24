package com.revzion.siitglobe.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revzion.siitglobe.domain.model.Course
import com.revzion.siitglobe.domain.model.Payment
import com.revzion.siitglobe.domain.model.PaymentMode
import com.revzion.siitglobe.domain.model.Student
import com.revzion.siitglobe.viewmodel.SiitViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentsScreen(siitViewModel: SiitViewModel) {
    val state by siitViewModel.state.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(state.paymentSaveSuccess) {
        if (state.paymentSaveSuccess) {
            showAddDialog = false
            siitViewModel.resetPaymentSaveSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Payments", fontWeight = FontWeight.Bold)
                        Text(
                            "${state.payments.size} transactions",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Payment", fontWeight = FontWeight.SemiBold) }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.isLoading && state.payments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.payments.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(72.dp).clip(RoundedCornerShape(20.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Payments, null, modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.primary)
                    }
                    Text("No payments yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text("Tap + to record a payment", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        SummaryCard("Total Revenue", "₹${state.payments.sumOf { it.amount }.toInt()}", Color(0xFFE65100), modifier = Modifier.weight(1f))
                        SummaryCard("Transactions", state.payments.size.toString(), MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
                    }
                }
                items(state.payments, key = { it.id }) { payment ->
                    PaymentCard(payment = payment, student = state.students.find { it.id == payment.studentId })
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddPaymentDialog(
            students = state.students,
            courses = state.courses,
            isLoading = state.isLoading,
            error = state.error,
            onDismiss = { showAddDialog = false },
            onConfirm = { studentId, courseId, amount, mode, transactionId, remarks ->
                val now = try {
                    val dt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                    "${dt.year}-${dt.monthNumber.toString().padStart(2, '0')}-${dt.dayOfMonth.toString().padStart(2, '0')}"
                } catch (_: Exception) { "2024-01-01" }
                siitViewModel.createPayment(
                    receiptNumber = "RCP${System.currentTimeMillis()}",
                    studentId = studentId,
                    courseId = courseId,
                    amount = amount,
                    paymentDate = now,
                    paymentMode = mode,
                    transactionId = transactionId.ifBlank { null },
                    remarks = remarks.ifBlank { null },
                )
            }
        )
    }
}

@Composable
private fun SummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.shadow(2.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PaymentCard(payment: Payment, student: Student?) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(14.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp))
                    .background(modeColor(payment.paymentMode).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(modeIcon(payment.paymentMode), null, tint = modeColor(payment.paymentMode), modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(student?.fullName ?: "Unknown", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text("${payment.receiptNumber} • ${payment.paymentDate}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Surface(shape = RoundedCornerShape(4.dp), color = modeColor(payment.paymentMode).copy(alpha = 0.12f)) {
                    Text(payment.paymentMode.name, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = modeColor(payment.paymentMode), fontWeight = FontWeight.SemiBold)
                }
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("₹${payment.amount.toInt()}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color(0xFFE65100))
                val statusColor = when (payment.status.name) {
                    "COMPLETED" -> Color(0xFF1B8918)
                    "FAILED" -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
                Surface(shape = RoundedCornerShape(4.dp), color = statusColor.copy(alpha = 0.12f)) {
                    Text(payment.status.name.take(4), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private fun modeColor(mode: PaymentMode): Color = when (mode) {
    PaymentMode.CASH -> Color(0xFF1B8918)
    PaymentMode.UPI -> Color(0xFF5C35CC)
    PaymentMode.CARD -> Color(0xFF008093)
    PaymentMode.ONLINE -> Color(0xFF006DB7)
    PaymentMode.CHEQUE -> Color(0xFFE65100)
    PaymentMode.DD -> Color(0xFF795548)
}

private fun modeIcon(mode: PaymentMode) = when (mode) {
    PaymentMode.CASH -> Icons.Default.Money
    PaymentMode.UPI -> Icons.Default.PhoneAndroid
    PaymentMode.CARD -> Icons.Default.CreditCard
    PaymentMode.ONLINE -> Icons.Default.Language
    PaymentMode.CHEQUE, PaymentMode.DD -> Icons.Default.Receipt
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddPaymentDialog(
    students: List<Student>,
    courses: List<Course>,
    isLoading: Boolean,
    error: String?,
    onDismiss: () -> Unit,
    onConfirm: (studentId: String, courseId: String, amount: Double, mode: String, transactionId: String, remarks: String) -> Unit,
) {
    var selectedStudent by remember { mutableStateOf(students.firstOrNull()) }
    // Auto-fill amount from course fee when student changes
    val autoCourse = remember(selectedStudent) { courses.find { it.id == selectedStudent?.courseId } }
    var amount by remember(autoCourse) { mutableStateOf(autoCourse?.feeAmount?.toInt()?.toString() ?: "") }
    var selectedMode by remember { mutableStateOf(PaymentMode.CASH) }
    var transactionId by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Payments, null, tint = MaterialTheme.colorScheme.primary)
                Text("Add Payment", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                // Student picker
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    OutlinedTextField(
                        value = selectedStudent?.fullName ?: "Select student",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Student *") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        shape = RoundedCornerShape(10.dp)
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        students.forEach { student ->
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(student.fullName, style = MaterialTheme.typography.bodyMedium)
                                        val course = courses.find { it.id == student.courseId }
                                        if (course != null) {
                                            Text("${course.name} • ₹${course.feeAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                },
                                onClick = { selectedStudent = student; expanded = false }
                            )
                        }
                    }
                }

                // Course auto-filled info
                if (autoCourse != null) {
                    Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.School, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                            Column {
                                Text(autoCourse.name, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                                Text("Course fee: ₹${autoCourse.feeAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount (₹) *") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = { Icon(Icons.Default.CurrencyRupee, null) },
                    supportingText = if (autoCourse != null) {{ Text("Auto-filled from course fee", color = MaterialTheme.colorScheme.primary) }} else null,
                )

                Text("Payment Mode", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(PaymentMode.CASH, PaymentMode.UPI, PaymentMode.CARD, PaymentMode.ONLINE).forEach { mode ->
                        FilterChip(
                            selected = selectedMode == mode,
                            onClick = { selectedMode = mode },
                            label = { Text(mode.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                if (selectedMode != PaymentMode.CASH) {
                    OutlinedTextField(
                        value = transactionId,
                        onValueChange = { transactionId = it },
                        label = { Text("Transaction ID") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                if (error != null) {
                    Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val s = selectedStudent ?: return@Button
                    val amt = amount.toDoubleOrNull() ?: return@Button
                    onConfirm(s.id, s.courseId, amt, selectedMode.name, transactionId, remarks)
                },
                enabled = !isLoading && selectedStudent != null && amount.toDoubleOrNull() != null,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text("Save Payment", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
