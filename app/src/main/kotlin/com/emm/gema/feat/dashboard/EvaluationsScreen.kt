package com.emm.gema.feat.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.ui.theme.GemaTheme

data class Evaluation(val name: String, val type: String, val date: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsScreen(modifier: Modifier = Modifier) {

    // --- DATA DE EJEMPLO ---
    val courses = listOf("Matemáticas Avanzadas", "Historia del Arte", "Programación Móvil")
    val evaluations = listOf(
        Evaluation("Examen Parcial 1", "Examen", "2024-05-20"),
        Evaluation("Ensayo Final", "Proyecto", "2024-06-10"),
        Evaluation("Quiz de Unidad 3", "Quiz", "2024-06-15")
    )

    // --- STATE MANAGEMENT ---
    var selectedCourse by remember { mutableStateOf(courses.first()) }
    var isCourseMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize().consumeWindowInsets(WindowInsets.navigationBars),
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mis Evaluaciones", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

                ExposedDropdownMenuBox(
                    expanded = isCourseMenuExpanded,
                    onExpandedChange = { isCourseMenuExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCourse,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Curso") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isCourseMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = isCourseMenuExpanded,
                        onDismissRequest = { isCourseMenuExpanded = false }
                    ) {
                        courses.forEach { course ->
                            DropdownMenuItem(
                                text = { Text(course) },
                                onClick = {
                                    selectedCourse = course
                                    isCourseMenuExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                text = { Text("Nueva Evaluación") },
                icon = { Icon(Icons.Default.Add, contentDescription = "Nueva evaluación") },
                onClick = { /* TODO: Navegar a EvaluationFormScreen */ }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(evaluations) { evaluation ->
                    EvaluationCard(
                        evaluation = evaluation,
                        onRegisterGrades = { /* TODO: Navegar a GradeEntryScreen */ }
                    )
                }
            }
        }
    }
}

@Composable
private fun EvaluationCard(
    evaluation: Evaluation,
    onRegisterGrades: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(evaluation.name, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("Tipo: ${evaluation.type}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Fecha: ${evaluation.date}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onRegisterGrades,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Registrar notas")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun EvaluationsScreenPreview() {
    GemaTheme {
        EvaluationsScreen()
    }
}