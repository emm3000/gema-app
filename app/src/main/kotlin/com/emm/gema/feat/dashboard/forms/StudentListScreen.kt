package com.emm.gema.feat.dashboard.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.data.network.attendance.StudentResponse
import com.emm.gema.feat.dashboard.components.RetryComponent
import com.emm.gema.feat.dashboard.components.shimmerEffect
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    modifier: Modifier = Modifier,
    state: StudentListUiState = StudentListUiState(),
    retryFetchStudents: () -> Unit = {},
    onAddStudent: () -> Unit = {},
    onStudentClick: (studentId: String) -> Unit = {},
    onBack: () -> Unit = {}
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estudiantes del Curso") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddStudent) {
                Icon(Icons.Default.Add, contentDescription = "Agregar estudiante")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
        ) {

            when {
                state.isLoading -> {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            (1..5).forEach {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(60.dp)
                                        .clip(RoundedCornerShape(16.dp))
                                        .shimmerEffect()
                                )
                            }
                        }
                    }
                }

                state.error != null -> {
                    item {
                        RetryComponent(
                            modifier = Modifier.fillMaxWidth(),
                            error = state.error,
                            retryFetchCourses = retryFetchStudents
                        )
                    }
                }

                state.students.isEmpty() -> {
                    item {
                        Text(
                            text = "No hay estudiantes en este curso",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                else -> {
                    items(state.students, key = StudentResponse::id) { student ->
                        StudentListItem(student = student, onClick = { onStudentClick(student.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun StudentListItem(student: StudentResponse, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(16.dp))
            Text(student.fullName, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenPreview() {
    GemaTheme {
        StudentListScreen(
            state = StudentListUiState(
                students = listOf(
                    StudentResponse(
                        id = "1", fullName = "John Doe",
                        dni = "eget",
                        email = "williams.barber@example.com",
                        birthDate = "habitant",
                        gender = "explicari",
                    ),
                )
            ),
            onAddStudent = {},
            onStudentClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenErrorPreview() {
    GemaTheme {
        StudentListScreen(
            state = StudentListUiState(
                error = "Error"
            ),
            onAddStudent = {},
            onStudentClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenEmptyPreview() {
    GemaTheme {
        StudentListScreen(
            state = StudentListUiState(),
            onAddStudent = {},
            onStudentClick = {},
            onBack = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentListScreenLoadingPreview() {
    GemaTheme {
        StudentListScreen(
            state = StudentListUiState(
                isLoading = true
            ),
            onAddStudent = {},
            onStudentClick = {},
            onBack = {}
        )
    }
}