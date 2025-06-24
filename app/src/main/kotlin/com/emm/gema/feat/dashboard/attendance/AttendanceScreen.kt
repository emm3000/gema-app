package com.emm.gema.feat.dashboard.attendance

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.domain.attendance.AttendanceStatus
import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.student.Student
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.feat.dashboard.student.StudentAvatar
import com.emm.gema.ui.theme.GemaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    modifier: Modifier = Modifier,
    state: AttendanceUiState = AttendanceUiState(),
    onAction: (AttendanceAction) -> Unit = {},
    navigateToCreateCourse: () -> Unit = {},
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeContent),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Asistencia")
                        Text(
                            text = state.datePicker.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar Fecha")
                    }
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(Icons.Default.History, contentDescription = "Ver Historial")
                    }
                },
            )
        },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Button(
                    onClick = { onAction(AttendanceAction.OnSave) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .height(50.dp),
                    enabled = state.isSubmitButtonEnabled,
                ) {
                    Icon(Icons.Default.Save, contentDescription = "Guardar Asistencia")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Guardar Asistencia", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            GemaDropdown(
                modifier = Modifier
                    .fillMaxWidth(),
                textLabel = "Curso",
                items = state.courses,
                itemSelected = state.selectedCourse,
                onItemSelected = { onAction(AttendanceAction.OnCourseSelected(it)) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (state.attendance.isEmpty()) {
                EmptyState()
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.attendance, key = { it.student.id }) { studentAttendance ->
                        StudentAttendanceItem(
                            student = studentAttendance.student,
                            status = studentAttendance.status,
                            onStatusChange = {
                                onAction(AttendanceAction.OnStatusChange(studentAttendance))
                            }
                        )
                    }
                }
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = state.datePicker.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val localDate = Instant.ofEpochMilli(it).atZone(ZoneOffset.UTC).toLocalDate()
                        onAction(AttendanceAction.OnDateChange(localDate))
                    }
                    showDatePicker = false
                }) { Text("Aceptar") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState
        ) {
            // TODO: Implement History Sheet UI
        }
    }
}

@Composable
fun StudentAttendanceItem(
    student: Student,
    status: AttendanceStatus,
    onStatusChange: (AttendanceStatus) -> Unit
) {
    val options = AttendanceStatus.entries

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StudentAvatar(name = student.fullName)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, option ->
                    val (icon, color) = when (option) {
                        AttendanceStatus.Present -> Icons.Default.Check to MaterialTheme.colorScheme.primary
                        AttendanceStatus.Absent -> Icons.Default.Close to Color.Gray
                    }
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = { onStatusChange(option) },
                        selected = status == option,
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = option.name,
                                modifier = Modifier.size(SegmentedButtonDefaults.IconSize)
                            )
                        },
                        colors = SegmentedButtonDefaults.colors(
                            activeContentColor = color,
                            inactiveContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                            activeBorderColor = color.copy(alpha = 0.2f),
                        ),
                    ) {
                        Text(text = option.description)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                imageVector = Icons.Default.People,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "No hay estudiantes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona un curso para ver los estudiantes y tomar asistencia.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun AttendanceScreenPreview() {
    val sampleStudents = listOf(
        StudentAttendanceStatus(
            student = Student(
                id = "1", fullName = "Julius Mueller",
                dni = "facilisi",
                email = "michel.hines@example.com",
                birthDate = "cubilia",
                gender = "aliquam"
            ),
            status = AttendanceStatus.Present,
            studentId = "bb"
        ),
        StudentAttendanceStatus(
            student = Student(
                id = "2", fullName = "Mariana Rodriguez",
                dni = "mattis",
                email = "ricky.kirby@example.com",
                birthDate = "sonet",
                gender = "per"
            ),
            status = AttendanceStatus.Present, studentId = "labores",
        ),
        StudentAttendanceStatus(
            student = Student(
                id = "3", fullName = "Carlos Estevez",
                dni = "eloquentiam",
                email = "pete.madden@example.com",
                birthDate = "dicta",
                gender = "sem"
            ),
            status = AttendanceStatus.Absent,
            studentId = "gaa"
        )
    )
    GemaTheme {
        Surface {
            AttendanceScreen(
                state = AttendanceUiState(
                    attendance = sampleStudents
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StudentAttendanceItemPreview() {
    GemaTheme {
        Surface(modifier = Modifier.padding(16.dp)) {
            StudentAttendanceItem(
                student = Student(
                    id = "1", fullName = "Ana Torres Díaz",
                    dni = "solet",
                    email = "lina.walton@example.com",
                    birthDate = "ridens",
                    gender = "mediocrem"
                ),
                status = AttendanceStatus.Present,
                onStatusChange = {}
            )
        }
    }
}