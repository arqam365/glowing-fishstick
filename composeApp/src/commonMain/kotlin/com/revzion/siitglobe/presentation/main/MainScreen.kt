package com.revzion.siitglobe.presentation.main

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.revzion.siitglobe.presentation.attendance.AttendanceScreen
import com.revzion.siitglobe.presentation.course.CoursesScreen
import com.revzion.siitglobe.presentation.dashboard.DashboardScreen
import com.revzion.siitglobe.presentation.payment.PaymentsScreen
import com.revzion.siitglobe.presentation.student.StudentDetailScreen
import com.revzion.siitglobe.presentation.student.StudentListScreen
import com.revzion.siitglobe.presentation.student.StudentRegistrationScreen
import com.revzion.siitglobe.viewmodel.SiitViewModel

private data class NavItem(
    val label: String,
    val icon: ImageVector,
)

private val navItems = listOf(
    NavItem("Dashboard", Icons.Default.Dashboard),
    NavItem("Students", Icons.Default.People),
    NavItem("Payments", Icons.Default.Payments),
    NavItem("Attendance", Icons.Default.CalendarToday),
    NavItem("Courses", Icons.Default.School),
)

private sealed class StudentsNav {
    data object List : StudentsNav()
    data class Detail(val id: String) : StudentsNav()
    data object Registration : StudentsNav()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(userName: String, siitViewModel: SiitViewModel, onLogout: () -> Unit) {
    var selectedTab by remember { mutableIntStateOf(0) }

    BoxWithConstraints {
        val isDesktop = maxWidth >= 840.dp

        if (isDesktop) {
            Row(Modifier.fillMaxSize()) {
                SiitNavigationRail(
                    selectedTab = selectedTab,
                    userName = userName,
                    onTabSelected = { selectedTab = it },
                    onLogout = onLogout,
                )
                VerticalDivider()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TabContent(selectedTab, userName, siitViewModel, onTabChange = { selectedTab = it }, onLogout = onLogout)
                }
            }
        } else {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        navItems.forEachIndexed { index, item ->
                            NavigationBarItem(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                icon = {
                                    Icon(
                                        item.icon,
                                        contentDescription = item.label,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        item.label,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                alwaysShowLabel = true
                            )
                        }
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    TabContent(selectedTab, userName, siitViewModel, onTabChange = { selectedTab = it }, onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
private fun SiitNavigationRail(
    selectedTab: Int,
    userName: String,
    onTabSelected: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surface,
        header = {
            Spacer(Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "SiiT",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "SiiT",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
        }
    ) {
        Spacer(Modifier.weight(1f))

        navItems.forEachIndexed { index, item ->
            NavigationRailItem(
                selected = selectedTab == index,
                onClick = { onTabSelected(index) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) },
            )
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.weight(1f))

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userName.take(1).uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
        Spacer(Modifier.height(8.dp))
        NavigationRailItem(
            selected = false,
            onClick = onLogout,
            icon = { Icon(Icons.Default.Logout, contentDescription = "Logout", modifier = Modifier.size(20.dp)) },
            label = { Text("Logout", style = MaterialTheme.typography.labelSmall) },
        )
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TabContent(
    selectedTab: Int,
    userName: String,
    siitViewModel: SiitViewModel,
    onTabChange: (Int) -> Unit,
    onLogout: () -> Unit,
) {
    AnimatedContent(
        targetState = selectedTab,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "tab_transition"
    ) { tab ->
        when (tab) {
            0 -> DashboardScreen(
                userName = userName,
                siitViewModel = siitViewModel,
                onNavigateToStudents = { onTabChange(1) },
                onNavigateToPayments = { onTabChange(2) },
                onNavigateToAttendance = { onTabChange(3) },
                onNavigateToCourses = { onTabChange(4) },
                onLogout = onLogout,
            )
            1 -> StudentsTabContent(siitViewModel)
            2 -> PaymentsScreen(siitViewModel)
            3 -> AttendanceScreen(siitViewModel)
            4 -> CoursesScreen(siitViewModel)
        }
    }
}

@Composable
private fun StudentsTabContent(siitViewModel: SiitViewModel) {
    var nav by remember { mutableStateOf<StudentsNav>(StudentsNav.List) }

    when (val screen = nav) {
        is StudentsNav.List -> StudentListScreen(
            onNavigateBack = null,
            siitViewModel = siitViewModel,
            onNavigateToDetail = { id -> nav = StudentsNav.Detail(id) },
            onNavigateToRegistration = { nav = StudentsNav.Registration },
        )
        is StudentsNav.Detail -> StudentDetailScreen(
            studentId = screen.id,
            siitViewModel = siitViewModel,
            onNavigateBack = { nav = StudentsNav.List },
        )
        is StudentsNav.Registration -> StudentRegistrationScreen(
            siitViewModel = siitViewModel,
            onNavigateBack = { nav = StudentsNav.List },
        )
    }
}

