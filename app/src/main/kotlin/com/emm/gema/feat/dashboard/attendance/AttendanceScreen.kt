package com.emm.gema.feat.dashboard.attendance

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.domain.attendance.AttendanceStatus
import com.emm.gema.domain.attendance.StudentAttendanceStatus
import com.emm.gema.domain.student.Student
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.ui.theme.GemaTheme
import java.time.Instant
import java.time.ZoneId
import java.time.ZoneOffset

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
        modifier = modifier.fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeContent),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Asistencia") },
                actions = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Seleccionar Fecha")
                    }
                    IconButton(onClick = { showHistorySheet = true }) {
                        Icon(Icons.Default.History, contentDescription = "Ver Historial")
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onAction(AttendanceAction.OnSave) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Save, contentDescription = "Guardar Asistencia")
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
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Curso",
                items = state.courses,
                itemSelected = state.courseSelected,
                onItemSelected = { onAction(AttendanceAction.OnCourseSelectedChange(it)) }
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.attendance) { studentAttendance ->
                    StudentAttendanceItem(
                        student = studentAttendance.student,
                        status = studentAttendance.status,
                        onStatusChange = { newStatus ->
                             onAction(AttendanceAction.OnAttendanceStatusChange(studentAttendance))
                        }
                    )
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
    val options = listOf(AttendanceStatus.Present, AttendanceStatus.Absent)
    val icons = listOf("P", "T", "A")

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = student.fullName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            SingleChoiceSegmentedButtonRow {
                options.forEachIndexed { index, option ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size),
                        onClick = { onStatusChange(option) },
                        selected = status == option,
                        icon = {}
                    ) {
                        Text(text = icons[index])
                    }
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun AttendanceScreenPreview() {
    val sampleStudents = listOf(
        StudentAttendanceStatus(
            student = Student(
                id = "dictas",
                fullName = "Julius Mueller",
                dni = "dis",
                email = "stevie.sloan@example.com",
                birthDate = "facilisi",
                gender = "mnesarchum"
            ), status = AttendanceStatus.Present

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
                    dni = "adolescens",
                    email = "glenda.cobb@example.com",
                    birthDate = "nascetur",
                    gender = "te"
                ),
                status = AttendanceStatus.Present,
                onStatusChange = {}
            )
        }
    }
}