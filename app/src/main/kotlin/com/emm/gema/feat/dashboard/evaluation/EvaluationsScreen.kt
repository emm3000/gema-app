package com.emm.gema.feat.dashboard.evaluation

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.Evaluation
import com.emm.gema.feat.dashboard.components.EmptyCourses
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EvaluationsScreen(
    modifier: Modifier = Modifier,
    state: EvaluationsUiState = EvaluationsUiState(),
    onCourseSelected: (Course) -> Unit = {},
    navigateToCreateCourse: () -> Unit = {},
    navigateToCreateEvaluation: () -> Unit = {},
) {

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.navigationBars),
        topBar = {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Mis Evaluaciones", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)

                Spacer(modifier = Modifier.height(16.dp))

                if (state.courses.isNotEmpty()) {
                    GemaDropdown(
                        modifier = Modifier.fillMaxWidth(),
                        textLabel = "Curso",
                        items = state.courses,
                        itemSelected = state.courseSelected,
                        onItemSelected = onCourseSelected,
                    )
                }
            }
        },
        floatingActionButton = {
            if (state.courses.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    text = { Text("Nueva Evaluación") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Nueva evaluación") },
                    onClick = navigateToCreateEvaluation
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            when (state.screenState) {
                is ScreenState.EmptyCourses -> {
                    EmptyCourses(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        navigateToCreateCourse()
                    }
                }
                is ScreenState.EmptyEvaluations -> {
                    Text("No hay evaluaciones")
                }
                ScreenState.None -> {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(state.evaluations, key = Evaluation::id) { evaluation ->
                            EvaluationCard(
                                evaluation = evaluation,
                                onRegisterGrades = { /* TODO: Navegar a GradeEntryScreen */ }
                            )
                        }
                    }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = MaterialTheme.shapes.large
            )
            .padding(16.dp)
    ) {
        Text(
            text = evaluation.name,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Tipo de evaluación",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = evaluation.type,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Fecha de evaluación",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = evaluation.date,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onRegisterGrades,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium
        ) {
            Text("Registrar notas")
        }
    }
}


@PreviewLightDark
@Composable
private fun EvaluationsScreenPreview() {
    GemaTheme {
        EvaluationsScreen()
    }
}

@PreviewLightDark
@Composable
private fun EvaluationsScreenEmptyPreview() {
    GemaTheme {
        EvaluationsScreen(
            state = EvaluationsUiState(
                screenState = ScreenState.EmptyCourses("gaaa")
            )
        )
    }
}