package com.revzion.siitglobe.presentation.attendance

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.sp
import com.revzion.siitglobe.domain.model.AttendanceStatus
import com.revzion.siitglobe.domain.model.Student
import com.revzion.siitglobe.viewmodel.SiitViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(siitViewModel: SiitViewModel) {
    // Read from state — this IS reactive, recomposes on every state change
    val state by siitViewModel.state.collectAsState()

    val today = remember {
        try { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
        catch (_: Exception) { LocalDate(2024, 1, 1) }
    }
    var selectedDate by remember { mutableStateOf(today) }

    // Optimistic map: immediate UI feedback before API response arrives
    // key = studentId, value = status being saved
    var optimistic by remember { mutableStateOf<Map<String, AttendanceStatus>>(emptyMap()) }

    // Load attendance from API when date changes
    LaunchedEffect(selectedDate) {
        optimistic = emptyMap()
        siitViewModel.loadAttendanceForDate(selectedDate)
    }

    // Derive attendanceMap DIRECTLY from state — reactive, auto-recomposes
    val confirmedMap = remember(state.attendanceByDate, selectedDate) {
        (state.attendanceByDate[selectedDate.toString()] ?: emptyList()).associateBy { it.studentId }
    }

    // Clear optimistic entries once confirmed state catches up
    LaunchedEffect(confirmedMap) {
        optimistic = optimistic.filter { (id, status) -> confirmedMap[id]?.status != status }
    }

    // Effective status: optimistic takes priority, fallback to confirmed
    fun effectiveStatus(studentId: String): AttendanceStatus? =
        optimistic[studentId] ?: confirmedMap[studentId]?.status

    val presentCount = state.students.count { effectiveStatus(it.id) == AttendanceStatus.PRESENT }
    val absentCount  = state.students.count { effectiveStatus(it.id) == AttendanceStatus.ABSENT }
    val lateCount    = state.students.count { effectiveStatus(it.id) == AttendanceStatus.LATE }
    val markedCount  = state.students.count { effectiveStatus(it.id) != null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Attendance", fontWeight = FontWeight.Bold)
                        if (state.students.isNotEmpty()) {
                            Text(
                                "$markedCount / ${state.students.size} marked",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (state.students.isEmpty() && state.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 24.dp),
            ) {
                item {
                    DateSelectorBar(
                        selectedDate = selectedDate,
                        onPrev = { selectedDate = selectedDate.minus(1, DateTimeUnit.DAY) },
                        onNext = { if (selectedDate < today) selectedDate = selectedDate.plus(1, DateTimeUnit.DAY) },
                        isToday = selectedDate == today
                    )
                }

                if (state.students.isNotEmpty()) {
                    item {
                        AttendanceSummaryRow(
                            total = state.students.size,
                            present = presentCount,
                            absent = absentCount,
                            late = lateCount,
                            marked = markedCount,
                        )
                    }
                }

                if (state.students.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.People, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("No students enrolled", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                } else {
                    item {
                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Student",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AttendanceStatus.entries.forEach { status ->
                                    Text(
                                        statusLabel(status),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = statusColor(status),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.width(32.dp),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    items(state.students, key = { it.id }) { student ->
                        val current = effectiveStatus(student.id)
                        val isSaving = optimistic.containsKey(student.id)
                        StudentAttendanceRow(
                            student = student,
                            currentStatus = current,
                            isSaving = isSaving,
                            courseName = siitViewModel.getCourseById(student.courseId)?.name,
                            onStatusChange = { status ->
                                // Optimistic update — instant feedback
                                optimistic = optimistic + (student.id to status)
                                siitViewModel.markAttendance(student.id, selectedDate, status)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DateSelectorBar(
    selectedDate: LocalDate,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    isToday: Boolean,
) {
    Surface(color = MaterialTheme.colorScheme.surface, shadowElevation = 2.dp) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrev) {
                Icon(Icons.Default.ChevronLeft, "Previous day")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(formatDate(selectedDate), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (isToday) {
                    Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)) {
                        Text("Today", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    val today = try { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date } catch (_: Exception) { LocalDate(2024, 1, 1) }
                    val diff = today.toEpochDays() - selectedDate.toEpochDays()
                    Text("$diff day${if (diff != 1) "s" else ""} ago", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onNext, enabled = !isToday) {
                Icon(Icons.Default.ChevronRight, "Next day", tint = if (isToday) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f) else LocalContentColor.current)
            }
        }
    }
}

@Composable
private fun AttendanceSummaryRow(total: Int, present: Int, absent: Int, late: Int, marked: Int) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        AttendanceStat("Present", present.toString(), Color(0xFF1B8918), Modifier.weight(1f))
        AttendanceStat("Absent", absent.toString(), Color(0xFFB71C1C), Modifier.weight(1f))
        AttendanceStat("Late", late.toString(), Color(0xFFE65100), Modifier.weight(1f))
        AttendanceStat("Marked", "$marked/$total", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
    }
}

@Composable
private fun AttendanceStat(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier.shadow(1.dp, RoundedCornerShape(10.dp)), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun StudentAttendanceRow(
    student: Student,
    currentStatus: AttendanceStatus?,
    isSaving: Boolean,
    courseName: String?,
    onStatusChange: (AttendanceStatus) -> Unit,
) {
    val rowBg = if (currentStatus != null)
        statusColor(currentStatus).copy(alpha = 0.04f)
    else
        MaterialTheme.colorScheme.surface

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = rowBg,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Avatar — tinted with current status color
            Box(
                modifier = Modifier.size(42.dp).clip(CircleShape)
                    .background(if (currentStatus != null) statusColor(currentStatus).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = currentStatus?.let { statusColor(it) } ?: MaterialTheme.colorScheme.primary)
                } else {
                    Text(
                        student.firstName.take(1).uppercase(),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (currentStatus != null) statusColor(currentStatus) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(student.fullName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                if (courseName != null) {
                    Text(courseName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            }

            // Single-select radio circles
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                AttendanceStatus.entries.forEach { status ->
                    StatusCircle(
                        status = status,
                        selected = currentStatus == status,
                        onClick = { onStatusChange(status) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusCircle(
    status: AttendanceStatus,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val color = statusColor(status)
    val bgColor by animateColorAsState(
        targetValue = if (selected) color else Color.Transparent,
        animationSpec = tween(150),
        label = "circle_bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) color else color.copy(alpha = 0.35f),
        animationSpec = tween(150),
        label = "circle_border"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Color.White else color.copy(alpha = 0.6f),
        animationSpec = tween(150),
        label = "circle_text"
    )

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = bgColor,
        modifier = Modifier
            .size(34.dp)
            .border(width = if (selected) 0.dp else 1.5.dp, color = borderColor, shape = CircleShape)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = statusLabel(status),
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp
            )
        }
    }
}

private fun statusColor(status: AttendanceStatus): Color = when (status) {
    AttendanceStatus.PRESENT  -> Color(0xFF1B8918)
    AttendanceStatus.ABSENT   -> Color(0xFFB71C1C)
    AttendanceStatus.LATE     -> Color(0xFFE65100)
    AttendanceStatus.HALF_DAY -> Color(0xFF1565C0)
}

private fun statusLabel(status: AttendanceStatus): String = when (status) {
    AttendanceStatus.PRESENT  -> "P"
    AttendanceStatus.ABSENT   -> "A"
    AttendanceStatus.LATE     -> "L"
    AttendanceStatus.HALF_DAY -> "H"
}

private fun formatDate(date: LocalDate): String {
    val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
    val days   = listOf("Mon","Tue","Wed","Thu","Fri","Sat","Sun")
    return "${days[date.dayOfWeek.ordinal]}, ${date.dayOfMonth} ${months[date.monthNumber - 1]} ${date.year}"
}
