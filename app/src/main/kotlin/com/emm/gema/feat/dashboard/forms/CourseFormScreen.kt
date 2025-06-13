package com.emm.gema.feat.dashboard.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormScreen(
    modifier: Modifier = Modifier,
    courseId: String? = null, // Se puede pasar el ID para modo edición
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // --- STATE MANAGEMENT ---
    var courseName by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var shift by remember { mutableStateOf("") }

    // TODO: Si courseId no es nulo, cargar los datos del curso para editarlos.

    // --- UI COMPOSITION ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (courseId == null) "Nuevo Curso" else "Editar Curso") },
                // Opcional: Agregar un botón de navegación para volver atrás
                navigationIcon = {
                     IconButton(onClick = onBack) {
                         Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                     }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = courseName,
                onValueChange = { courseName = it },
                label = { Text("Nombre del Curso") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = section,
                onValueChange = { section = it },
                label = { Text("Sección") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Año") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = shift,
                onValueChange = { shift = it },
                label = { Text("Turno (Mañana/Tarde)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave, // La navegación se manejaría en el ViewModel o en el NavHost
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}

// --- IMPORTS ---


@Preview(showBackground = true)
@Composable
private fun CourseFormScreenPreview() {
    GemaTheme {
        CourseFormScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun EditCourseFormScreenPreview() {
    GemaTheme {
        CourseFormScreen(courseId = "123")
    }
}