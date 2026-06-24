package com.revzion.siitglobe.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revzion.siitglobe.domain.model.Course
import com.revzion.siitglobe.domain.model.Payment
import com.revzion.siitglobe.domain.model.Student
import com.revzion.siitglobe.presentation.theme.Green40
import com.revzion.siitglobe.presentation.theme.Teal40
import com.revzion.siitglobe.viewmodel.SiitViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String,
    siitViewModel: SiitViewModel,
    onNavigateToStudents: () -> Unit,
    onNavigateToPayments: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToCourses: () -> Unit,
    onLogout: () -> Unit = {},
) {
    val state by siitViewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.School,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Text(
                            "SiiT",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.Default.Logout,
                            contentDescription = "Logout",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { scaffoldPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                WelcomeBanner(
                    userName = userName,
                    activeStudents = siitViewModel.getActiveStudentCount()
                )
            }
            item {
                StatsSection(
                    totalStudents = state.students.size,
                    activeStudents = siitViewModel.getActiveStudentCount(),
                    totalRevenue = siitViewModel.getTotalRevenue(),
                    courseCount = state.courses.size,
                    isLoading = state.isLoading,
                    onStudents = onNavigateToStudents,
                    onPayments = onNavigateToPayments,
                    onCourses = onNavigateToCourses,
                )
            }
            item {
                RecentStudentsSection(
                    students = state.students.take(3),
                    courses = state.courses,
                    onViewAll = onNavigateToStudents
                )
            }
            item {
                RecentPaymentsSection(
                    payments = state.payments.take(3),
                    students = state.students,
                    onViewAll = onNavigateToPayments
                )
            }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }
}

@Composable
private fun WelcomeBanner(userName: String, activeStudents: Int) {
    val (greeting, dateStr) = remember {
        try {
            val now = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
            val g = when {
                now.hour < 12 -> "Good morning"
                now.hour < 17 -> "Good afternoon"
                else -> "Good evening"
            }
            val monthName = now.month.name.lowercase().replaceFirstChar { it.uppercase() }
            val dayName = now.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }
            Pair(g, "$dayName, $monthName ${now.dayOfMonth}")
        } catch (_: Exception) {
            Pair("Welcome", "SiiT Dashboard")
        }
    }

    val gradientBrush = Brush.linearGradient(
        colors = listOf(Green40, Teal40),
        start = Offset(0f, 0f),
        end = Offset(1000f, 400f)
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(gradientBrush)
            .padding(horizontal = 24.dp, vertical = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .align(Alignment.TopEnd)
                .offset(x = 30.dp, y = (-30).dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(80.dp)
                .align(Alignment.BottomEnd)
                .offset(x = 10.dp, y = 20.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.06f))
        )

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
                text = "$greeting 👋",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(4.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BannerChip(icon = Icons.Default.CalendarToday, text = dateStr)
                BannerChip(icon = Icons.Default.People, text = "$activeStudents active")
            }
        }
    }
}

@Composable
private fun BannerChip(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
        Text(text, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun StatsSection(
    totalStudents: Int,
    activeStudents: Int,
    totalRevenue: Double,
    courseCount: Int,
    isLoading: Boolean,
    onStudents: () -> Unit,
    onPayments: () -> Unit,
    onCourses: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            "Overview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Students",
                value = if (isLoading) "…" else totalStudents.toString(),
                icon = Icons.Default.People,
                iconBg = Color(0xFF1B8918),
                modifier = Modifier.weight(1f),
                onClick = onStudents
            )
            StatCard(
                title = "Active",
                value = if (isLoading) "…" else activeStudents.toString(),
                icon = Icons.Default.CheckCircle,
                iconBg = Color(0xFF008093),
                modifier = Modifier.weight(1f),
                onClick = onStudents
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Revenue",
                value = if (isLoading) "…" else "₹${totalRevenue.toInt().formatAmount()}",
                icon = Icons.Default.AccountBalance,
                iconBg = Color(0xFFE65100),
                modifier = Modifier.weight(1f),
                onClick = onPayments
            )
            StatCard(
                title = "Courses",
                value = if (isLoading) "…" else courseCount.toString(),
                icon = Icons.Default.School,
                iconBg = Color(0xFF5C35CC),
                modifier = Modifier.weight(1f),
                onClick = onCourses
            )
        }
    }
}

private fun Int.formatAmount(): String =
    if (this >= 1000) "${this / 1000}k" else this.toString()

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    iconBg: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier.shadow(3.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RecentStudentsSection(
    students: List<Student>,
    courses: List<Course>,
    onViewAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Recent Students", onViewAll = onViewAll)

        if (students.isEmpty()) {
            Text(
                "No students yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            students.forEach { student ->
                val course = courses.find { it.id == student.courseId }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.linearGradient(colors = listOf(Green40, Teal40))),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${student.firstName.first()}${student.lastName.first()}",
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                student.fullName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${course?.name ?: "—"} • ${student.enrollmentNumber}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        StatusBadge(status = student.status.name)
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentPaymentsSection(
    payments: List<Payment>,
    students: List<Student>,
    onViewAll: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        SectionHeader(title = "Recent Payments", onViewAll = onViewAll)

        if (payments.isEmpty()) {
            Text(
                "No payments yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        } else {
            payments.forEach { payment ->
                val student = students.find { it.id == payment.studentId }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(2.dp, RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE65100).copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = null,
                                tint = Color(0xFFE65100),
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                student?.fullName ?: "Unknown",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                "${payment.receiptNumber} • ${payment.paymentDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            "₹${payment.amount.toInt()}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onViewAll: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onViewAll, contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)) {
            Text(
                "View all",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun StatusBadge(status: String) {
    val (bg, fg) = when (status) {
        "ACTIVE" -> Pair(Color(0xFF1B8918).copy(alpha = 0.12f), Color(0xFF1B8918))
        "COMPLETED" -> Pair(Color(0xFF008093).copy(alpha = 0.12f), Color(0xFF008093))
        "DROPPED" -> Pair(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error)
        "ON_LEAVE" -> Pair(Color(0xFFE65100).copy(alpha = 0.12f), Color(0xFFE65100))
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.onSurfaceVariant)
    }
    val label = when (status) {
        "ACTIVE" -> "Active"
        "COMPLETED" -> "Completed"
        "DROPPED" -> "Dropped"
        "ON_LEAVE" -> "On Leave"
        else -> status
    }
    Surface(shape = RoundedCornerShape(6.dp), color = bg) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = fg,
            fontWeight = FontWeight.Bold
        )
    }
}
