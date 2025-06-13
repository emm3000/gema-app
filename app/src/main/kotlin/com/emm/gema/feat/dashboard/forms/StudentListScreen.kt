package com.emm.gema.feat.dashboard.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    modifier: Modifier = Modifier,
    courseId: String, // ID del curso para saber a dónde pertenecen los estudiantes
    onAddStudent: () -> Unit = {},
    onStudentClick: (studentId: String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    // --- DATA DE EJEMPLO ---
    val students = listOf(
        Student(id = "1", name = "Ana García"),
        Student(id = "2", name = "Luis Fernandez"),
        Student(id = "3", name = "Carla Rossi"),
        Student(id = "4", name = "David Martos"),
        Student(id = "5", name = "Elena Vidal")
    )

    // --- UI COMPOSITION ---
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
            items(students, key = { it.id }) { student ->
                StudentListItem(student = student, onClick = { onStudentClick(student.id) })
            }
        }
    }
}

@Composable
private fun StudentListItem(student: Student, onClick: () -> Unit) {
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
            Text(student.name, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

data class Student(val id: String, val name: String)

// --- IMPORTS ---


@Preview(showBackground = true)
@Composable
private fun StudentListScreenPreview() {
    GemaTheme {
        StudentListScreen(
            courseId = "1",
            onAddStudent = {},
            onStudentClick = {},
            onBack = {}
        )
    }
}