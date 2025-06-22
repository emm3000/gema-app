package com.emm.gema.feat.dashboard.evaluation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.feat.dashboard.examTypes
import com.emm.gema.ui.theme.GemaTheme
import java.time.Instant
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationFormScreen(
    modifier: Modifier = Modifier,
    state: EvaluationFormUiState = EvaluationFormUiState(),
    onAction: (EvaluationFormAction) -> Unit = {},
    onBack: () -> Unit = {},
) {

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nueva Evaluación") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Curso",
                items = state.courses,
                itemSelected = state.selectedCourse,
                onItemSelected = { onAction(EvaluationFormAction.CourseSelected(it)) }
            )

            OutlinedTextField(
                value = state.title,
                onValueChange = { onAction(EvaluationFormAction.TitleChanged(it)) },
                label = { Text("Título de la evaluación") },
                modifier = Modifier.fillMaxWidth()
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Tipo",
                items = examTypes,
                itemSelected = state.type,
                onItemSelected = { onAction(EvaluationFormAction.TypeChanged(it)) }
            )

            OutlinedTextField(
                value = state.date.toString(),
                onValueChange = {},
                readOnly = true,
                label = { Text("Fecha") },
                trailingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
            )

            Button(
                onClick = {
                    onAction(EvaluationFormAction.Save)
                    onBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar")
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val localDate = Instant.ofEpochMilli(it)
                            .atZone(ZoneOffset.UTC)
                            .toLocalDate()
                        onAction(EvaluationFormAction.DateChanged(localDate))
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
}

@Preview(showBackground = true)
@Composable
private fun EvaluationFormScreenPreview() {
    GemaTheme {
        EvaluationFormScreen()
    }
}