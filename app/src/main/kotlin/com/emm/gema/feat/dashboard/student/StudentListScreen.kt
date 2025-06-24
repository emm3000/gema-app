package com.emm.gema.feat.dashboard.student

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PersonSearch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.domain.student.Student
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentListScreen(
    modifier: Modifier = Modifier,
    onAddStudent: () -> Unit = {},
    students: List<Student>,
    onStudentClick: (studentId: String) -> Unit = {},
    onBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Estudiantes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddStudent,
                icon = { Icon(Icons.Default.Add, contentDescription = "Agregar estudiante") },
                text = { Text("Agregar Estudiante") }
            )
        },
    ) { innerPadding ->
        if (students.isEmpty()) {
            EmptyState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp)
            ) {
                items(students, key = Student::id) { student ->
                    StudentListItem(student = student, onClick = { onStudentClick(student.id) })
                }
            }
        }
    }
}

@Composable
private fun StudentListItem(student: Student, onClick: () -> Unit) {
    var isMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudentAvatar(name = student.fullName)
            Spacer(modifier = Modifier.width(16.dp))
            Text(student.fullName, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
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

@Composable
fun StudentAvatar(name: String, modifier: Modifier = Modifier) {
    val avatarColor = remember(name) {
        val hash = name.hashCode()
        Color(
            red = (hash and 0xFF0000) shr 16,
            green = (hash and 0x00FF00) shr 8,
            blue = hash and 0x0000FF,
            alpha = 180
        )
    }
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(avatarColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name.firstOrNull()?.uppercase() ?: "",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .padding(top = 70.dp),
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Icon(
                imageVector = Icons.Default.PersonSearch,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = "No hay estudiantes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Parece que aún no has agregado ningún estudiante a este curso. ¡Usa el botón (+) para comenzar!",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun EmptyStatePreview() {
    GemaTheme {
        Surface {
            EmptyState()
        }
    }
}

@PreviewLightDark
@Composable
private fun StudentListScreenPreview() {
    GemaTheme {
        Surface {
            StudentListScreen(
                students = listOf(
                    Student(id = "1", fullName = "Alice Johnson", "", "", birthDate = "euismod", gender = "qui"),
                    Student(id = "2", fullName = "Bob Williams", "", "", birthDate = "erat", gender = "sed"),
                    Student(id = "3", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "4", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "5", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "6", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "7", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "8", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "9", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "10", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "11", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                    Student(id = "12", fullName = "Charlie Brown", "", "", birthDate = "moderatius", gender = "interdum"),
                )
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun StudentListScreenEmptyPreview() {
    GemaTheme {
        Surface {
            StudentListScreen(students = emptyList())
        }
    }
}