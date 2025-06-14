package com.emm.gema.feat.dashboard.course

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.ui.theme.GemaTheme

data class Course(val id: Int, val name: String, val studentId: String)

@Composable
fun CoursesScreen(
    modifier: Modifier = Modifier,
    createCourse: () -> Unit = {},
    toStudentList: (courseId: String) -> Unit = {}
) {
    val courses = listOf(
        Course(id = 1, name = "Matemáticas", studentId = "32"),
        Course(id = 2, name = "Historia", studentId = "28"),
        Course(id = 3, name = "Biología", studentId = "2"),
        Course(id = 4, name = "Biología", studentId = "25"),
        Course(id = 5, name = "Biología", studentId = "25"),
        Course(id = 6, name = "Biología", studentId = "25"),
    )

    val menuExpandedState = remember { mutableStateMapOf<Int, Boolean>() }
    val listState = rememberLazyListState()
    val showExtendedFab by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset < 100
        }
    }
    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = "Mis Cursos",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = 8.dp)
            )

            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(courses, key = { it.id }) { curso ->
                    CourseCard(
                        course = curso,
                        isMenuExpanded = menuExpandedState[curso.id] == true,
                        onMenuAction = { menuExpandedState[curso.id] = it },
                        toStudentList = toStudentList
                    )
                }

                item {
                    Spacer(Modifier.height(80.dp))
                }
            }

        }

        FloatingActionButtonContent(
            isExtended = showExtendedFab,
            onClick = createCourse,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        )
    }
}

@Composable
fun FloatingActionButtonContent(
    isExtended: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {

    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        expanded = isExtended,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        text = { Text("Crear Curso") },
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Agregar curso"
            )
        }
    )
}

@Composable
private fun CourseCard(
    course: Course,
    toStudentList: (String) -> Unit,
    isMenuExpanded: Boolean,
    onMenuAction: (Boolean) -> Unit
) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp) // Main content padding
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = course.name,
                    style = MaterialTheme.typography.headlineSmall, // Slightly larger and bolder for more prominence
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Box {
                    IconButton(onClick = { isMenuExpanded = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Más opciones para ${course.name}")
                    }
                    DropdownMenu(
                        expanded = isMenuExpanded,
                        onDismissRequest = { isMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            leadingIcon = {
                                Icon(
                                    Icons.Filled.Edit,
                                    contentDescription = null
                                )
                            }, // contentDescription for leadingIcon can often be null if redundant
                            onClick = {
                                isMenuExpanded = false
//                                onEditCourse(curso)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                            onClick = {
                                isMenuExpanded = false
//                                onDeleteCourse(curso)
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Group,
                    contentDescription = "Número de estudiantes",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${course.studentId} estudiantes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
            ) {
                OutlinedButton(
                    onClick = {
                        toStudentList(course.studentId)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Group, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Estudiantes")
                }
                Button(
                    onClick = {
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Assignment,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Evaluaciones", maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CoursesScreenPreview() {
    GemaTheme {
        CoursesScreen()
    }
}