package com.emm.gema.feat.dashboard.grade

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.data.network.attendance.StudentResponse
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeEntryScreen(
    modifier: Modifier = Modifier,
    evaluationId: String, // Para saber a qué evaluación pertenecen las notas
    onBack: () -> Unit = {},
    onSave: (grades: Map<String, String>) -> Unit = {}
) {
    // --- STATE MANAGEMENT ---
    val studentGrades = remember { mutableStateMapOf<String, String>() }

    // --- DATA DE EJEMPLO ---
    val students = listOf<StudentResponse>()

    // --- UI COMPOSITION ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Notas") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = { onSave(studentGrades) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text("Guardar Notas")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(students, key = { it.id }) { student ->
                GradeEntryItem(
                    studentName = student.fullName,
                    grade = studentGrades[student.id] ?: "",
                    onGradeChange = {
                        // Validar que solo sean números y no más de 20
                        if (it.all { char -> char.isDigit() } && (it.toIntOrNull() ?: 0) <= 20) {
                            studentGrades[student.id] = it
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun GradeEntryItem(
    studentName: String,
    grade: String,
    onGradeChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = studentName, modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.width(16.dp))
            OutlinedTextField(
                value = grade,
                onValueChange = onGradeChange,
                label = { Text("Nota") },
                modifier = Modifier.width(80.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradeEntryScreenPreview() {
    GemaTheme {
        GradeEntryScreen(evaluationId = "1")
    }
}