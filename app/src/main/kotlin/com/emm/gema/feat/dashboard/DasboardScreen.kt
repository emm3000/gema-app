package com.emm.gema.feat.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.data.network.course.CourseResponse
import com.emm.gema.domain.evaluation.Evaluation
import com.emm.gema.ui.theme.GemaTheme
import kotlinx.serialization.json.JsonArray

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    val activeCourses = listOf(
        CourseResponse(
            id = "dolores",
            name = "Numbers Berger",
            grade = "movet",
            section = "an",
            level = "nonumy",
            shift = "singulis",
            academicYear = 2014,
            student = JsonArray(listOf())
        ),
    )
    val upcomingEvaluations = listOf<Evaluation>()
    val courseAttendance = listOf(
        Attendance("Matemáticas", 0.95f),
        Attendance("Historia", 0.88f)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        Text(
            text = "¡Bienvenido, Profesor!",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Cursos Activos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                activeCourses.forEach { curso ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(curso.name, style = MaterialTheme.typography.bodyLarge)
                            Text("Estudiantes: ${0}", style = MaterialTheme.typography.bodySmall)
                        }
                        Button(onClick = { }) {
                            Text("Ver")
                        }
                    }
                }
            }
        }
        Text("Próximas Evaluaciones", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(upcomingEvaluations) { evaluacion ->
                Card(
                    modifier = Modifier.size(width = 180.dp, height = 100.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(evaluacion.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                        Spacer(Modifier.height(8.dp))
                        Text(evaluacion.date, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                }
            }
        }
        Spacer(Modifier.height(24.dp))
        Text("Asistencia del día por curso", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            courseAttendance.forEach { asistencia ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Row(
                        Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(asistencia.course, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Asistencia del día",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        CircularProgressIndicator(
                            progress = { asistencia.percentage },
                            modifier = Modifier.size(48.dp),
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 6.dp
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(
                            "${(asistencia.percentage * 100).toInt()}%",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

data class Attendance(val course: String, val percentage: Float)

@Preview
@Composable
private fun DashboardScreenPreview() {
    GemaTheme {
        DashboardScreen()
    }
}