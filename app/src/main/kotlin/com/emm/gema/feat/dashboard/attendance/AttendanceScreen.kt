package com.emm.gema.feat.dashboard.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.ui.theme.GemaTheme
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
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
) {

    val attendanceHistory = listOf("2024-06-12", "2024-06-11", "2024-06-10")

    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showHistorySheet by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackBarHostState) },
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Registro de Asistencia", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.height(16.dp))

                if (state.courses.isNotEmpty()) {
                    GemaDropdown(
                        modifier = Modifier.fillMaxWidth(),
                        textLabel = "Curso",
                        items = state.courses,
                        itemSelected = state.courseSelected,
                        onItemSelected = { onAction(AttendanceAction.OnCourseSelectedChange(it)) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = "Seleccionar fecha", modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(state.datePicker.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)))
                        }
                        TextButton(onClick = { showHistorySheet = true }) {
                            Text("Ver historial")
                        }
                    }
                }
            }
        },
        bottomBar = {
            Button(
                onClick = {
                    scope.launch {
                        snackBarHostState.showSnackbar("Asistencia guardada correctamente")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("Guardar Asistencia")
            }
        }
    ) { innerPadding ->
        when(state.screenState) {
            is ScreenState.EmptyCourses -> {
                Text("No hay cursos disponibles", modifier = Modifier.padding(innerPadding))
            }
            is ScreenState.EmptyStudents -> {
                Text("No hay estudiantes disponibles", modifier = Modifier.padding(innerPadding))
            }
            is ScreenState.HttpException -> {
                Text("Ocurrio Un error", modifier = Modifier.padding(innerPadding))
            }
            ScreenState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    CircularProgressIndicator()
                }
            }
            ScreenState.None -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.attendance) { studentName ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(studentName.student.fullName, modifier = Modifier.weight(1f))
                            Checkbox(
                                checked = studentName.status == "PRESENT",
                                onCheckedChange = {  }
                            )
                        }
                    }
                }
            }
        }

    }
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val localDate = LocalDate.ofInstant(Instant.ofEpochMilli(it), ZoneOffset.UTC)
                        onAction(AttendanceAction.OnDateChange(localDate))
                    }
                    showDatePicker = false
                }) {
                    Text("Aceptar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancelar")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showHistorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showHistorySheet = false },
            sheetState = sheetState
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Historial de Asistencia", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(bottom = 16.dp))
                LazyColumn {
                    items(attendanceHistory) { date ->
                        Text(date, modifier = Modifier.padding(vertical = 12.dp))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AttendanceScreenPreview() {
    GemaTheme {
        AttendanceScreen()
    }
}