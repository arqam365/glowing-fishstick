package com.revzion.siitglobe.presentation.forms

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.revzion.siitglobe.domain.model.*
import com.revzion.siitglobe.viewmodel.FormsViewModel

private sealed class FormsNav {
    data object List : FormsNav()
    data class Builder(val formId: String? = null) : FormsNav()
    data class Fill(val formId: String) : FormsNav()
    data class Responses(val formId: String) : FormsNav()
}

@Composable
fun FormsScreen(formsViewModel: FormsViewModel) {
    var nav by remember { mutableStateOf<FormsNav>(FormsNav.List) }
    val state by formsViewModel.state.collectAsState()

    when (val screen = nav) {
        is FormsNav.List -> FormListScreen(
            forms = state.forms,
            responseCounts = state.responses.mapValues { it.value.size },
            onCreate = { nav = FormsNav.Builder() },
            onEdit = { nav = FormsNav.Builder(it) },
            onFill = { nav = FormsNav.Fill(it) },
            onResponses = { nav = FormsNav.Responses(it) },
            onDelete = { formsViewModel.deleteForm(it) }
        )
        is FormsNav.Builder -> FormBuilderScreen(
            existing = screen.formId?.let { formsViewModel.getForm(it) },
            onSave = { title, desc, fields ->
                if (screen.formId != null) formsViewModel.updateForm(screen.formId, title, desc, fields)
                else formsViewModel.createForm(title, desc, fields)
                nav = FormsNav.List
            },
            onBack = { nav = FormsNav.List }
        )
        is FormsNav.Fill -> {
            val form = formsViewModel.getForm(screen.formId)
            if (form != null) {
                FormFillScreen(
                    form = form,
                    onSubmit = { answers ->
                        formsViewModel.submitResponse(screen.formId, answers)
                        nav = FormsNav.List
                    },
                    onBack = { nav = FormsNav.List }
                )
            }
        }
        is FormsNav.Responses -> {
            val form = formsViewModel.getForm(screen.formId)
            if (form != null) {
                FormResponsesScreen(
                    form = form,
                    responses = formsViewModel.getResponses(screen.formId),
                    onDelete = { formsViewModel.deleteResponse(screen.formId, it) },
                    onBack = { nav = FormsNav.List }
                )
            }
        }
    }
}

// ─── Form List ───────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormListScreen(
    forms: kotlin.collections.List<FormTemplate>,
    responseCounts: Map<String, Int>,
    onCreate: () -> Unit,
    onEdit: (String) -> Unit,
    onFill: (String) -> Unit,
    onResponses: (String) -> Unit,
    onDelete: (String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Forms", fontWeight = FontWeight.Bold)
                        if (forms.isNotEmpty()) Text("${forms.size} form${if (forms.size != 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreate,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("New Form", fontWeight = FontWeight.SemiBold) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (forms.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Icon(Icons.Default.Assignment, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("No forms yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Tap + to create your first form", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(forms, key = { it.id }) { form ->
                    FormCard(
                        form = form,
                        responseCount = responseCounts[form.id] ?: 0,
                        onEdit = { onEdit(form.id) },
                        onFill = { onFill(form.id) },
                        onResponses = { onResponses(form.id) },
                        onDelete = { onDelete(form.id) }
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun FormCard(
    form: FormTemplate,
    responseCount: Int,
    onEdit: () -> Unit,
    onFill: () -> Unit,
    onResponses: () -> Unit,
    onDelete: () -> Unit,
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            icon = { Icon(Icons.Default.DeleteForever, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Form?", fontWeight = FontWeight.Bold) },
            text = { Text("\"${form.title}\" and all ${responseCount} response(s) will be permanently deleted.") },
            confirmButton = {
                Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Assignment, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(form.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 1)
                    if (form.description.isNotBlank()) Text(form.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
                Box {
                    IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Fill Form") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { showMenu = false; onFill() })
                        DropdownMenuItem(text = { Text("Edit Form") }, leadingIcon = { Icon(Icons.Default.Settings, null) }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text("View Responses") }, leadingIcon = { Icon(Icons.Default.List, null) }, onClick = { showMenu = false; onResponses() })
                        HorizontalDivider()
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; showDeleteConfirm = true })
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoChip(Icons.Default.FormatListBulleted, "${form.fields.size} fields")
                InfoChip(Icons.Default.BarChart, "$responseCount response${if (responseCount != 1) "s" else ""}")
                InfoChip(Icons.Default.CalendarToday, form.createdAt.toString())
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onFill, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Fill", fontWeight = FontWeight.SemiBold)
                }
                Button(onClick = onResponses, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.BarChart, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Responses", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(12.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(text, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// ─── Form Builder ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormBuilderScreen(
    existing: FormTemplate?,
    onSave: (String, String, kotlin.collections.List<FormField>) -> Unit,
    onBack: () -> Unit,
) {
    var title by remember { mutableStateOf(existing?.title ?: "") }
    var description by remember { mutableStateOf(existing?.description ?: "") }
    var fields by remember { mutableStateOf(existing?.fields ?: emptyList()) }
    var showAddField by remember { mutableStateOf(false) }
    var editingField by remember { mutableStateOf<FormField?>(null) }
    var attempted by remember { mutableStateOf(false) }

    if (showAddField || editingField != null) {
        AddFieldDialog(
            existing = editingField,
            onConfirm = { field ->
                fields = if (editingField != null) fields.map { if (it.id == field.id) field else it }
                else fields + field
                showAddField = false
                editingField = null
            },
            onDismiss = { showAddField = false; editingField = null }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (existing == null) "Create Form" else "Edit Form", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                actions = {
                    Button(
                        onClick = {
                            attempted = true
                            if (title.isNotBlank() && fields.isNotEmpty()) onSave(title.trim(), description.trim(), fields)
                        },
                        modifier = Modifier.padding(end = 8.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) { Text("Save", fontWeight = FontWeight.Bold) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(16.dp))) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("Form Details", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        OutlinedTextField(
                            value = title,
                            onValueChange = { title = it },
                            label = { Text("Form Title *") },
                            isError = attempted && title.isBlank(),
                            supportingText = if (attempted && title.isBlank()) {{ Text("Title is required") }} else null,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = description,
                            onValueChange = { description = it },
                            label = { Text("Description (optional)") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            minLines = 2,
                            maxLines = 3
                        )
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Fields", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                        if (attempted && fields.isEmpty()) Text("Add at least one field", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                    }
                    FilledTonalButton(onClick = { showAddField = true }, shape = RoundedCornerShape(10.dp)) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Add Field")
                    }
                }
            }

            itemsIndexed(fields, key = { _, f -> f.id }) { index, field ->
                FieldBuilderCard(
                    field = field,
                    index = index,
                    total = fields.size,
                    onMoveUp = { if (index > 0) fields = fields.toMutableList().also { it.add(index - 1, it.removeAt(index)) } },
                    onMoveDown = { if (index < fields.size - 1) fields = fields.toMutableList().also { it.add(index + 1, it.removeAt(index)) } },
                    onEdit = { editingField = field },
                    onDelete = { fields = fields.filter { it.id != field.id } }
                )
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FieldBuilderCard(
    field: FormField,
    index: Int,
    total: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("${index + 1}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.width(20.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(field.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = RoundedCornerShape(4.dp), color = fieldTypeColor(field.type).copy(alpha = 0.12f)) {
                        Text(fieldTypeLabel(field.type), modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = fieldTypeColor(field.type), fontWeight = FontWeight.Bold)
                    }
                    if (field.required) Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.errorContainer) {
                        Text("Required", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                    if (field.options.isNotEmpty()) Text("${field.options.size} options", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Column {
                IconButton(onClick = onMoveUp, enabled = index > 0, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowUp, null, modifier = Modifier.size(18.dp)) }
                IconButton(onClick = onMoveDown, enabled = index < total - 1, modifier = Modifier.size(28.dp)) { Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(18.dp)) }
            }
            IconButton(onClick = onEdit) { Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary) }
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
        }
    }
}

// ─── Add/Edit Field Dialog ────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddFieldDialog(existing: FormField?, onConfirm: (FormField) -> Unit, onDismiss: () -> Unit) {
    var label by remember { mutableStateOf(existing?.label ?: "") }
    var type by remember { mutableStateOf(existing?.type ?: FieldType.TEXT) }
    var required by remember { mutableStateOf(existing?.required ?: false) }
    var options by remember { mutableStateOf(existing?.options ?: emptyList()) }
    var newOption by remember { mutableStateOf("") }
    var attempted by remember { mutableStateOf(false) }
    var showTypePicker by remember { mutableStateOf(false) }

    val needsOptions = type in listOf(FieldType.DROPDOWN, FieldType.CHECKBOX, FieldType.RADIO)

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        title = { Text(if (existing == null) "Add Field" else "Edit Field", fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Field Label *") },
                    isError = attempted && label.isBlank(),
                    supportingText = if (attempted && label.isBlank()) {{ Text("Required") }} else null,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                // Type picker
                Box {
                    OutlinedTextField(
                        value = fieldTypeLabel(type),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Field Type") },
                        trailingIcon = { Icon(Icons.Default.ArrowDropDown, null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Surface(modifier = Modifier.matchParentSize(), color = Color.Transparent, onClick = { showTypePicker = true }) {}
                    DropdownMenu(expanded = showTypePicker, onDismissRequest = { showTypePicker = false }) {
                        FieldType.entries.forEach { ft ->
                            DropdownMenuItem(
                                text = { Text(fieldTypeLabel(ft)) },
                                leadingIcon = { Icon(fieldTypeIcon(ft), null, tint = fieldTypeColor(ft)) },
                                onClick = { type = ft; options = emptyList(); showTypePicker = false }
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text("Required field", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = required, onCheckedChange = { required = it })
                }

                if (needsOptions) {
                    HorizontalDivider()
                    Text("Options", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    if (attempted && options.isEmpty()) Text("Add at least one option", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

                    options.forEachIndexed { i, opt ->
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                            Text(opt, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { options = options.toMutableList().also { it.removeAt(i) } }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp))
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newOption,
                            onValueChange = { newOption = it },
                            placeholder = { Text("Add option...") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newOption.isNotBlank()) {
                                    options = options + newOption.trim()
                                    newOption = ""
                                }
                            }
                        ) { Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.primary) }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    attempted = true
                    val valid = label.isNotBlank() && (!needsOptions || options.isNotEmpty())
                    if (valid) onConfirm(FormField(id = existing?.id ?: "field_${System.currentTimeMillis()}", label = label.trim(), type = type, required = required, options = options))
                },
                shape = RoundedCornerShape(10.dp)
            ) { Text(if (existing == null) "Add" else "Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

// ─── Form Fill ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormFillScreen(form: FormTemplate, onSubmit: (kotlin.collections.List<FormAnswer>) -> Unit, onBack: () -> Unit) {
    val answers = remember { mutableStateMapOf<String, String>() }
    var attempted by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }

    fun isValid(): Boolean = form.fields.filter { it.required }.all { answers[it.id]?.isNotBlank() == true }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Submit Response?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to submit this form response?") },
            confirmButton = {
                Button(
                    onClick = {
                        onSubmit(answers.map { FormAnswer(it.key, it.value) })
                        showConfirm = false
                    },
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Submit") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(form.title, fontWeight = FontWeight.Bold, maxLines = 1) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (form.description.isNotBlank()) {
                item {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), modifier = Modifier.fillMaxWidth()) {
                        Text(form.description, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            items(form.fields, key = { it.id }) { field ->
                val error = attempted && field.required && answers[field.id].isNullOrBlank()
                FieldFillCard(
                    field = field,
                    value = answers[field.id] ?: "",
                    isError = error,
                    onValueChange = { answers[field.id] = it }
                )
            }

            item {
                Button(
                    onClick = {
                        attempted = true
                        if (isValid()) showConfirm = true
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) { Text("Submit Response", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
                Spacer(Modifier.height(80.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FieldFillCard(field: FormField, value: String, isError: Boolean, onValueChange: (String) -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().shadow(1.dp, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(field.label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                if (field.required) Text("*", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
            }
            if (isError) Text("This field is required", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)

            when (field.type) {
                FieldType.TEXT -> OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), isError = isError, singleLine = true, placeholder = { Text("Your answer") })
                FieldType.PARAGRAPH -> OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), isError = isError, minLines = 3, maxLines = 5, placeholder = { Text("Your answer") })
                FieldType.NUMBER -> OutlinedTextField(value = value, onValueChange = { if (it.all { c -> c.isDigit() || c == '.' || c == '-' }) onValueChange(it) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), isError = isError, singleLine = true, placeholder = { Text("0") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                FieldType.DATE -> OutlinedTextField(value = value, onValueChange = onValueChange, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), isError = isError, singleLine = true, placeholder = { Text("DD/MM/YYYY") }, leadingIcon = { Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(18.dp)) })
                FieldType.DROPDOWN -> {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                        OutlinedTextField(
                            value = value.ifBlank { "Select an option" },
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(MenuAnchorType.PrimaryNotEditable),
                            shape = RoundedCornerShape(10.dp),
                            isError = isError,
                            colors = if (value.isBlank()) OutlinedTextFieldDefaults.colors(unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant) else OutlinedTextFieldDefaults.colors()
                        )
                        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                            field.options.forEach { opt ->
                                DropdownMenuItem(text = { Text(opt) }, onClick = { onValueChange(opt); expanded = false })
                            }
                        }
                    }
                }
                FieldType.RADIO -> {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        field.options.forEach { opt ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                RadioButton(selected = value == opt, onClick = { onValueChange(opt) })
                                Text(opt, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
                FieldType.CHECKBOX -> {
                    val selected = value.split(",").filter { it.isNotBlank() }.toMutableSet()
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        field.options.forEach { opt ->
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                Checkbox(checked = opt in selected, onCheckedChange = { checked ->
                                    if (checked) selected.add(opt) else selected.remove(opt)
                                    onValueChange(selected.joinToString(","))
                                })
                                Text(opt, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─── Responses Viewer ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormResponsesScreen(
    form: FormTemplate,
    responses: kotlin.collections.List<FormResponse>,
    onDelete: (String) -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(form.title, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${responses.size} response${if (responses.size != 1) "s" else ""}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (responses.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Inbox, null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Text("No responses yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Fill the form to create a response", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(responses.sortedByDescending { it.submittedAt.toString() }, key = { _, r -> r.id }) { index, response ->
                    ResponseCard(
                        index = responses.size - index,
                        response = response,
                        fields = form.fields,
                        onDelete = { onDelete(response.id) }
                    )
                }
                item { Spacer(Modifier.height(24.dp)) }
            }
        }
    }
}

@Composable
private fun ResponseCard(index: Int, response: FormResponse, fields: kotlin.collections.List<FormField>, onDelete: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Response?", fontWeight = FontWeight.Bold) },
            text = { Text("This response will be permanently deleted.") },
            confirmButton = { Button(onClick = { onDelete(); showDeleteConfirm = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") } },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }

    Surface(modifier = Modifier.fillMaxWidth().shadow(2.dp, RoundedCornerShape(14.dp)), shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(modifier = Modifier.size(38.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                    Text("#$index", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text("Response #$index", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Text("Submitted: ${response.submittedAt}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                IconButton(onClick = { expanded = !expanded }) { Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, null) }
                IconButton(onClick = { showDeleteConfirm = true }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
            }

            if (expanded) {
                HorizontalDivider(modifier = Modifier.padding(horizontal = 14.dp))
                Column(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    fields.forEach { field ->
                        val answer = response.answers.find { it.fieldId == field.id }?.value
                        Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(field.label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (answer.isNullOrBlank()) {
                                Text("—", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            } else if (field.type == FieldType.CHECKBOX) {
                                answer.split(",").filter { it.isNotBlank() }.forEach { option ->
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary))
                                        Text(option, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            } else {
                                Text(answer, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        if (field != fields.last()) HorizontalDivider(thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun fieldTypeLabel(type: FieldType) = when (type) {
    FieldType.TEXT -> "Short Answer"
    FieldType.PARAGRAPH -> "Paragraph"
    FieldType.NUMBER -> "Number"
    FieldType.DATE -> "Date"
    FieldType.DROPDOWN -> "Dropdown"
    FieldType.CHECKBOX -> "Checkboxes"
    FieldType.RADIO -> "Multiple Choice"
}

private fun fieldTypeColor(type: FieldType): Color = when (type) {
    FieldType.TEXT -> Color(0xFF1565C0)
    FieldType.PARAGRAPH -> Color(0xFF00796B)
    FieldType.NUMBER -> Color(0xFFE65100)
    FieldType.DATE -> Color(0xFF6A1B9A)
    FieldType.DROPDOWN -> Color(0xFF37474F)
    FieldType.CHECKBOX -> Color(0xFF1B5E20)
    FieldType.RADIO -> Color(0xFFB71C1C)
}

private fun fieldTypeIcon(type: FieldType): ImageVector = when (type) {
    FieldType.TEXT -> Icons.Default.ShortText
    FieldType.PARAGRAPH -> Icons.Default.Notes
    FieldType.NUMBER -> Icons.Default.Pin
    FieldType.DATE -> Icons.Default.CalendarToday
    FieldType.DROPDOWN -> Icons.Default.ArrowDropDown
    FieldType.CHECKBOX -> Icons.Default.CheckBox
    FieldType.RADIO -> Icons.Default.RadioButtonChecked
}
