package com.emm.gema.feat.dashboard.forms

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentDetailScreen(
    modifier: Modifier = Modifier,
    studentId: String, // Para cargar los datos del estudiante
    onBack: () -> Unit = {},
    onEdit: (studentId: String) -> Unit = {}
) {
    // --- DATA DE EJEMPLO ---
    val student = StudentDetails(
        name = "Ana García",
        email = "ana.garcia@email.com",
        code = "202100123",
        attendance = 0.92f
    )
    val grades = listOf(
        StudentGrade("Examen Parcial 1", "18"),
        StudentGrade("Tarea Académica 1", "15"),
        StudentGrade("Proyecto Final", "19"),
        StudentGrade("Quiz Sorpresa", "16")
    )

    // --- UI COMPOSITION ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del Estudiante") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { onEdit(studentId) }) {
                Icon(Icons.Default.Edit, contentDescription = "Editar Estudiante")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PersonalInfoCard(student = student)
            AttendanceCard(attendance = student.attendance)
            GradesCard(grades = grades)
        }
    }
}

@Composable
private fun PersonalInfoCard(student: StudentDetails) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(student.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Divider()
            InfoRow(icon = Icons.Default.Email, text = student.email)
            InfoRow(icon = Icons.Default.Star, text = "Código: ${student.code}")
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun AttendanceCard(attendance: Float) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Asistencia General", style = MaterialTheme.typography.titleLarge)
                Text("${(attendance * 100).toInt()}%", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { attendance },
                    modifier = Modifier.size(64.dp),
                    strokeWidth = 6.dp,
                )
                Text("${(attendance * 100).toInt()}%", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun GradesCard(grades: List<StudentGrade>) {
    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Calificaciones", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            grades.forEach {
                GradeRow(grade = it)
                Divider()
            }
        }
    }
}

@Composable
private fun GradeRow(grade: StudentGrade) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(grade.evaluationName, style = MaterialTheme.typography.bodyLarge)
        Text(grade.score, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, fontSize = 18.sp)
    }
}

data class StudentDetails(val name: String, val email: String, val code: String, val attendance: Float)
data class StudentGrade(val evaluationName: String, val score: String)

@Preview(showBackground = true)
@Composable
private fun StudentDetailScreenPreview() {
    GemaTheme {
        StudentDetailScreen(studentId = "1")
    }
}