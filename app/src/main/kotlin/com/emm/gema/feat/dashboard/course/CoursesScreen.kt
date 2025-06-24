package com.emm.gema.feat.dashboard.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import com.emm.gema.feat.dashboard.components.EmptyCourses
import com.emm.gema.ui.theme.GemaTheme

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    courses: List<Course>,
    createCourse: () -> Unit = {},
    onCourseClicked: (courseId: String) -> Unit = {}
) {
    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .consumeWindowInsets(WindowInsets.safeContent),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = createCourse,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Crear Curso"
                    )
                },
                text = { Text("Crear Curso") }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                text = "Mis Cursos",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)
            )
            if (courses.isEmpty()) {
                Box(
                    modifier = Modifier,
                    contentAlignment = Alignment.Center
                ) {
                    EmptyCourses(modifier = Modifier, navigateToCreateCourse = createCourse)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(courses, key = { it.id }) { course ->
                        CourseItem(
                            course = course,
                            onCourseClicked = onCourseClicked
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CourseItem(
    course: Course,
    onCourseClicked: (String) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        onClick = { onCourseClicked(course.id) },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                val sectionText = if (course.section == "Sin sección") "" else course.section
                Text(
                    text = "${course.grade} $sectionText - ${course.level}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box {
                IconButton(onClick = { isMenuExpanded = true }) {
                    Icon(
                        Icons.Filled.MoreVert,
                        contentDescription = "Más opciones",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                DropdownMenu(
                    expanded = isMenuExpanded,
                    onDismissRequest = { isMenuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                        onClick = {
                            // TODO: Handle edit
                            isMenuExpanded = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                        onClick = {
                            // TODO: Handle delete
                            isMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}


@PreviewLightDark
@Composable
private fun CoursesScreenPreview() {
    GemaTheme {
        CoursesScreen(
            courses = listOf(
                Course(
                    id = "1",
                    name = "Cálculo Avanzado",
                    grade = "5to",
                    section = "A",
                    level = "Secundaria",
                    shift = "Mañana",
                    academicYear = 2024
                ),
                Course(
                    id = "2",
                    name = "Historia Universal",
                    grade = "3ro",
                    section = "B",
                    level = "Secundaria",
                    shift = "Tarde",
                    academicYear = 2024
                )
            )
        )
    }
}

@PreviewLightDark
@Composable
private fun CoursesScreenEmptyPreview() {
    GemaTheme {
        CoursesScreen(courses = emptyList())
    }
}