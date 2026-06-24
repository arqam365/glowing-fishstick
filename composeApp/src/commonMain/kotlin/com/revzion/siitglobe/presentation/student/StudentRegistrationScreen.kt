package com.revzion.siitglobe.presentation.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.revzion.siitglobe.domain.model.Gender
import com.revzion.siitglobe.viewmodel.SiitViewModel
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// ── Validators ────────────────────────────────────────────────────────────────

private fun isValidName(s: String) = s.trim().length >= 2
private fun isValidPhone(s: String) = s.trim().length == 10 && s.trim().all { it.isDigit() }
private fun isValidEmail(s: String) =
    s.isBlank() || s.matches(Regex("[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,}"))
private fun isValidPin(s: String) = s.trim().length == 6 && s.trim().all { it.isDigit() }
private fun isValidDob(s: String): Boolean {
    if (!s.matches(Regex("\\d{4}-\\d{2}-\\d{2}"))) return false
    val parts = s.split("-")
    val year = parts[0].toIntOrNull() ?: return false
    val month = parts[1].toIntOrNull() ?: return false
    val day = parts[2].toIntOrNull() ?: return false
    return year in 1950..2015 && month in 1..12 && day in 1..31
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentRegistrationScreen(
    siitViewModel: SiitViewModel,
    onNavigateBack: () -> Unit,
) {
    val vmState by siitViewModel.state.collectAsState()

    var currentStep by remember { mutableStateOf(0) }
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var fatherName by remember { mutableStateOf("") }
    var motherName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var addressState by remember { mutableStateOf("") }
    var pinCode by remember { mutableStateOf("") }
    var selectedCourse by remember(vmState.courses) { mutableStateOf(vmState.courses.firstOrNull()) }
    var selectedGender by remember { mutableStateOf(Gender.MALE) }
    var dateOfBirth by remember { mutableStateOf("") }

    // Track whether user has attempted to advance; reveals errors on all fields
    var stepAttempted by remember { mutableStateOf(false) }

    LaunchedEffect(vmState.studentSaveSuccess) {
        if (vmState.studentSaveSuccess) {
            siitViewModel.resetStudentSaveSuccess()
            onNavigateBack()
        }
    }

    val steps = listOf("Personal", "Contact", "Address", "Course")

    val isStep1Valid = isValidName(firstName) && isValidName(lastName) &&
            isValidName(fatherName) && isValidName(motherName) && isValidDob(dateOfBirth)
    val isStep2Valid = isValidPhone(phone) && isValidEmail(email)
    val isStep3Valid = street.trim().length >= 5 && city.trim().length >= 2 &&
            addressState.trim().length >= 2 && isValidPin(pinCode)
    val isStep4Valid = selectedCourse != null

    val currentStepValid = when (currentStep) {
        0 -> isStep1Valid; 1 -> isStep2Valid; 2 -> isStep3Valid; 3 -> isStep4Valid; else -> false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Register Student") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header card
            Card(
                modifier = Modifier.fillMaxWidth().shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(80.dp).clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        if (firstName.isNotBlank() || lastName.isNotBlank()) {
                            Text(
                                text = "${firstName.firstOrNull() ?: ""}${lastName.firstOrNull() ?: ""}".uppercase(),
                                style = MaterialTheme.typography.headlineLarge,
                                color = Color.White, fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(Icons.Default.PersonAdd, null, tint = Color.White, modifier = Modifier.size(40.dp))
                        }
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = if (firstName.isNotBlank() || lastName.isNotBlank())
                                "$firstName $lastName".trim() else "New Student Registration",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "Step ${currentStep + 1} of ${steps.size}: ${steps[currentStep]}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            ProgressStepper(steps = steps, currentStep = currentStep, modifier = Modifier.fillMaxWidth())

            Card(
                modifier = Modifier.fillMaxWidth().weight(1f).shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (currentStep) {
                        0 -> PersonalInformationStep(
                            firstName = firstName, onFirstNameChange = { firstName = it },
                            lastName = lastName, onLastNameChange = { lastName = it },
                            fatherName = fatherName, onFatherNameChange = { fatherName = it },
                            motherName = motherName, onMotherNameChange = { motherName = it },
                            selectedGender = selectedGender, onGenderChange = { selectedGender = it },
                            dateOfBirth = dateOfBirth, onDateOfBirthChange = { dateOfBirth = it },
                            showErrors = stepAttempted,
                        )
                        1 -> ContactInformationStep(
                            phone = phone, onPhoneChange = { if (it.all { c -> c.isDigit() } && it.length <= 10) phone = it },
                            email = email, onEmailChange = { email = it },
                            showErrors = stepAttempted,
                        )
                        2 -> AddressInformationStep(
                            street = street, onStreetChange = { street = it },
                            city = city, onCityChange = { city = it },
                            state = addressState, onStateChange = { addressState = it },
                            pinCode = pinCode, onPinCodeChange = { if (it.all { c -> c.isDigit() } && it.length <= 6) pinCode = it },
                            showErrors = stepAttempted,
                        )
                        3 -> CourseSelectionStep(
                            courses = vmState.courses,
                            selectedCourse = selectedCourse,
                            onCourseSelect = { selectedCourse = it }
                        )
                    }
                }
            }

            if (vmState.error != null) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                        Text(vmState.error ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { if (currentStep > 0) { currentStep--; stepAttempted = false } else onNavigateBack() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(
                        if (currentStep == 0) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack,
                        null, modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(if (currentStep == 0) "Cancel" else "Back", style = MaterialTheme.typography.labelLarge)
                }

                Button(
                    onClick = {
                        if (!currentStepValid) {
                            stepAttempted = true
                            return@Button
                        }
                        stepAttempted = false
                        if (currentStep < steps.size - 1) {
                            currentStep++
                        } else {
                            val now = try {
                                Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
                            } catch (_: Exception) { null }
                            val year = now?.year ?: 2024
                            val seq = (vmState.students.size + 1).toString().padStart(3, '0')
                            val today = if (now != null)
                                "$year-${now.monthNumber.toString().padStart(2, '0')}-${now.dayOfMonth.toString().padStart(2, '0')}"
                            else "2024-01-01"
                            siitViewModel.createStudent(
                                enrollmentNumber = "SIIT$year$seq",
                                firstName = firstName.trim(),
                                lastName = lastName.trim(),
                                fatherName = fatherName.trim(),
                                motherName = motherName.trim(),
                                dateOfBirth = dateOfBirth.trim(),
                                gender = selectedGender.name,
                                email = email.trim().ifBlank { null },
                                phone = phone.trim(),
                                alternatePhone = null,
                                addressStreet = street.trim(),
                                addressCity = city.trim(),
                                addressState = addressState.trim(),
                                addressPinCode = pinCode.trim(),
                                courseId = selectedCourse?.id ?: "",
                                enrollmentDate = today,
                            )
                        }
                    },
                    modifier = Modifier.weight(1f).height(56.dp).shadow(6.dp, RoundedCornerShape(16.dp)),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !vmState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    if (vmState.isLoading && currentStep == steps.size - 1) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = MaterialTheme.colorScheme.onPrimary, strokeWidth = 2.dp)
                    } else {
                        Icon(
                            if (currentStep == steps.size - 1) Icons.Default.Check else Icons.Default.ArrowForward,
                            null, modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            if (currentStep == steps.size - 1) "Register Student" else "Next",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProgressStepper(steps: List<String>, currentStep: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, _ ->
            Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(
                        when {
                            index < currentStep -> MaterialTheme.colorScheme.secondary
                            index == currentStep -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    ),
                    contentAlignment = Alignment.Center
                ) {
                    if (index < currentStep) {
                        Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    } else {
                        Text(
                            "${index + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold,
                            color = if (index == currentStep) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (index < steps.size - 1) {
                    Box(
                        modifier = Modifier.weight(1f).height(3.dp).background(
                            if (index < currentStep) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        steps.forEachIndexed { index, step ->
            Text(
                step, style = MaterialTheme.typography.labelSmall,
                fontWeight = if (index == currentStep) FontWeight.Bold else FontWeight.Normal,
                color = if (index == currentStep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun PersonalInformationStep(
    firstName: String, onFirstNameChange: (String) -> Unit,
    lastName: String, onLastNameChange: (String) -> Unit,
    fatherName: String, onFatherNameChange: (String) -> Unit,
    motherName: String, onMotherNameChange: (String) -> Unit,
    selectedGender: Gender, onGenderChange: (Gender) -> Unit,
    dateOfBirth: String, onDateOfBirthChange: (String) -> Unit,
    showErrors: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text("Personal Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValidatedField(
                value = firstName, onValueChange = onFirstNameChange,
                label = "First Name *",
                isValid = isValidName(firstName),
                errorMessage = "Min 2 characters",
                showError = showErrors && firstName.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
            ValidatedField(
                value = lastName, onValueChange = onLastNameChange,
                label = "Last Name *",
                isValid = isValidName(lastName),
                errorMessage = "Min 2 characters",
                showError = showErrors && lastName.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        ValidatedField(
            value = fatherName, onValueChange = onFatherNameChange,
            label = "Father's Name *",
            isValid = isValidName(fatherName),
            errorMessage = "Min 2 characters",
            showError = showErrors && fatherName.isNotEmpty(),
        )
        ValidatedField(
            value = motherName, onValueChange = onMotherNameChange,
            label = "Mother's Name *",
            isValid = isValidName(motherName),
            errorMessage = "Min 2 characters",
            showError = showErrors && motherName.isNotEmpty(),
        )
        ValidatedField(
            value = dateOfBirth, onValueChange = onDateOfBirthChange,
            label = "Date of Birth *",
            placeholder = "YYYY-MM-DD  e.g. 2000-06-15",
            isValid = isValidDob(dateOfBirth),
            errorMessage = "Enter as YYYY-MM-DD (year 1950–2015)",
            showError = showErrors && dateOfBirth.isNotEmpty(),
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text("Gender *", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Gender.entries.forEach { gender ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedGender == gender) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = { onGenderChange(gender) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                gender.name, style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selectedGender == gender) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedGender == gender) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (selectedGender == gender) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ContactInformationStep(
    phone: String, onPhoneChange: (String) -> Unit,
    email: String, onEmailChange: (String) -> Unit,
    showErrors: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Phone, null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(28.dp))
            Text("Contact Information", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        OutlinedTextField(
            value = phone,
            onValueChange = onPhoneChange,
            label = { Text("Phone Number *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = showErrors && phone.isNotEmpty() && !isValidPhone(phone),
            supportingText = {
                if (showErrors && phone.isNotEmpty() && !isValidPhone(phone)) {
                    Text("Enter exactly 10 digits  (${phone.length}/10)", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("${phone.length}/10 digits", color = if (phone.length == 10) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            leadingIcon = { Icon(Icons.Default.Phone, null) },
        )

        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            label = { Text("Email (optional)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = showErrors && email.isNotEmpty() && !isValidEmail(email),
            supportingText = if (showErrors && email.isNotEmpty() && !isValidEmail(email)) {
                { Text("Enter a valid email address", color = MaterialTheme.colorScheme.error) }
            } else null,
            leadingIcon = { Icon(Icons.Default.Email, null) },
        )
    }
}

@Composable
fun AddressInformationStep(
    street: String, onStreetChange: (String) -> Unit,
    city: String, onCityChange: (String) -> Unit,
    state: String, onStateChange: (String) -> Unit,
    pinCode: String, onPinCodeChange: (String) -> Unit,
    showErrors: Boolean = false,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Home, null, tint = MaterialTheme.colorScheme.tertiary, modifier = Modifier.size(28.dp))
            Text("Address Details", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        OutlinedTextField(
            value = street,
            onValueChange = onStreetChange,
            label = { Text("Street Address *") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 2,
            isError = showErrors && street.isNotEmpty() && street.trim().length < 5,
            supportingText = if (showErrors && street.isNotEmpty() && street.trim().length < 5) {
                { Text("Street address is too short", color = MaterialTheme.colorScheme.error) }
            } else null,
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ValidatedField(
                value = city, onValueChange = onCityChange,
                label = "City *",
                isValid = city.trim().length >= 2,
                errorMessage = "Enter city name",
                showError = showErrors && city.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
            ValidatedField(
                value = state, onValueChange = onStateChange,
                label = "State *",
                isValid = state.trim().length >= 2,
                errorMessage = "Enter state name",
                showError = showErrors && state.isNotEmpty(),
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedTextField(
            value = pinCode,
            onValueChange = onPinCodeChange,
            label = { Text("PIN Code *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            isError = showErrors && pinCode.isNotEmpty() && !isValidPin(pinCode),
            supportingText = {
                if (showErrors && pinCode.isNotEmpty() && !isValidPin(pinCode)) {
                    Text("Enter exactly 6 digits  (${pinCode.length}/6)", color = MaterialTheme.colorScheme.error)
                } else {
                    Text("${pinCode.length}/6 digits", color = if (pinCode.length == 6) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            leadingIcon = { Icon(Icons.Default.PinDrop, null) },
        )
    }
}

@Composable
fun CourseSelectionStep(
    courses: List<Course>,
    selectedCourse: Course?,
    onCourseSelect: (Course) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.School, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
            Text("Select Course", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
        HorizontalDivider()

        if (courses.isEmpty()) {
            Text("Loading courses…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        } else {
            Text("Choose the course for enrollment", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                courses.forEach { course ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selectedCourse?.id == course.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        onClick = { onCourseSelect(course) }
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                                    color = if (selectedCourse?.id == course.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                Text("${course.code} • ${course.duration} • ₹${course.feeAmount.toInt()}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                if (course.description.isNotBlank()) {
                                    Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                                }
                            }
                            if (selectedCourse?.id == course.id) {
                                Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ValidatedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isValid: Boolean,
    errorMessage: String,
    showError: Boolean,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = if (placeholder != null) {{ Text(placeholder, style = MaterialTheme.typography.bodySmall) }} else null,
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        isError = showError && !isValid,
        supportingText = if (showError && !isValid) {
            { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
        } else null,
    )
}
