package com.emm.gema.feat.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.data.network.course.CourseResponse
import com.emm.gema.domain.evaluation.Evaluation
import com.emm.gema.ui.theme.GemaTheme
import kotlinx.serialization.json.JsonArray

@Composable
fun DashboardScreen(modifier: Modifier = Modifier) {
    // Sample data
    val activeCourses = listOf(
        CourseResponse(
            id = "1",
            name = "Cálculo Avanzado",
            grade = "5to",
            section = "A",
            level = "Secundaria",
            shift = "Mañana",
            academicYear = 2024,
            student = JsonArray(listOf())
        ),
        CourseResponse(
            id = "2",
            name = "Historia Universal",
            grade = "5to",
            section = "A",
            level = "Secundaria",
            shift = "Mañana",
            academicYear = 2024,
            student = JsonArray(listOf())
        )
    )
    val upcomingEvaluations = listOf(
        Evaluation(id = "1", name = "Examen Parcial 1", date = "25/06/2024", courseId = "1", maxScore = 4922, type = "cum", term = "graece"),
        Evaluation(id = "2", name = "Presentación de Proyecto", date = "28/06/2024", courseId = "2",
            maxScore = 5881,
            type = "errem",
            term = "consetetur"
        )
    )
    val courseAttendance = listOf(
        Attendance("Cálculo Avanzado", 0.95f),
        Attendance("Historia Universal", 0.88f)
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        item { 
            Header() 
        }
        item {
            ActiveCoursesSection(activeCourses)
        }
        item {
            UpcomingEvaluationsSection(upcomingEvaluations)
        }
        item {
            AttendanceSection(courseAttendance)
        }
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun Header() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            text = "¡Bienvenido de nuevo!",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Profesor",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun ActiveCoursesSection(courses: List<CourseResponse>) {
    Column {
        SectionHeader("Cursos Activos")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            courses.forEach { course ->
                CourseCard(course)
            }
        }
    }
}

@Composable
fun CourseCard(course: CourseResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = "Course Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(course.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(
                        "${course.grade} ${course.section} - ${course.level}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = { /* Navigate to course details */ }) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Ver Curso",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun UpcomingEvaluationsSection(evaluations: List<Evaluation>) {
    Column {
        SectionHeader("Próximas Evaluaciones")
        if (evaluations.isEmpty()) {
            Text(
                "No hay evaluaciones próximas.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(evaluations) { evaluation ->
                    EvaluationCard(evaluation)
                }
            }
        }
    }
}

@Composable
fun EvaluationCard(evaluation: Evaluation) {
    Card(
        modifier = Modifier.width(220.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = "Evaluation Icon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = evaluation.date,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                text = evaluation.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Cálculo Avanzado", // Placeholder, ideally from courseId
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AttendanceSection(attendanceList: List<Attendance>) {
    Column {
        SectionHeader("Asistencia del Día")
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            attendanceList.forEach { attendance ->
                AttendanceCard(attendance)
            }
        }
    }
}

@Composable
fun AttendanceCard(attendance: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(attendance.course, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                Text(
                    text = "${(attendance.percentage * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { attendance.percentage },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            )
        }
    }
}


data class Attendance(val course: String, val percentage: Float)

@PreviewLightDark
@Composable
private fun DashboardScreenPreview() {
    GemaTheme {
        Surface {
            DashboardScreen()
        }
    }
}