package com.emm.gema.feat.dashboard.evaluation

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.emm.gema.domain.course.model.Course
import com.emm.gema.domain.evaluation.Evaluation
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
    onEvaluationClicked: (String) -> Unit = {}
) {

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeContent),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Evaluaciones") }
            )
        },
        floatingActionButton = {
            if (state.courses.isNotEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = navigateToCreateEvaluation,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    text = { Text("Crear evaluación") },
                    icon = { Icon(Icons.Default.Add, contentDescription = "Nueva Evaluación") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            if (state.courses.isNotEmpty()) {
                GemaDropdown(
                    modifier = Modifier.fillMaxWidth(),
                    textLabel = "Curso",
                    items = state.courses,
                    itemSelected = state.courseSelected,
                    onItemSelected = onCourseSelected,
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // TODO: Handle empty states
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(state.evaluations, key = Evaluation::id) { evaluation ->
                    EvaluationItem(
                        evaluation = evaluation,
                        onEvaluationClicked = onEvaluationClicked
                    )
                }
            }
        }
    }
}

@Composable
private fun EvaluationItem(
    evaluation: Evaluation,
    onEvaluationClicked: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEvaluationClicked(evaluation.id) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = evaluation.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = evaluation.date,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    AssistChip(
                        onClick = {},
                        label = {
                            Text(text = evaluation.type, style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones")
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = { isMenuExpanded = false /* TODO */ }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = { isMenuExpanded = false /* TODO */ }
                    )
                }
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun EvaluationsScreenPreview() {
    val sampleEvaluations = listOf(
        Evaluation(
            id = "1",
            name = "Examen Parcial I",
            date = "20/06/2024",
            type = "Examen",
            courseId = "",
            maxScore = 3426,
            term = "gubergren"
        ),
        Evaluation(
            id = "2",
            name = "Práctica Calificada 2",
            date = "28/06/2024",
            type = "Práctica",
            courseId = "",
            maxScore = 5617,
            term = "dolores"
        ),
    )
    GemaTheme {
        Surface {
            EvaluationsScreen(
                state = EvaluationsUiState(
                    evaluations = sampleEvaluations,
                    courses = listOf(
                        Course(
                            id = "1",
                            name = "Cálculo Avanzado",
                            grade = "ne",
                            section = "lacinia",
                            level = "mandamus",
                            shift = "class",
                            academicYear = 1984
                        )
                    )
                )
            )
        }
    }
}