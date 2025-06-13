package com.emm.gema.feat.dashboard.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
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
fun StudentFormScreen(
    modifier: Modifier = Modifier,
    studentId: String? = null, // Para modo edición
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    // --- STATE MANAGEMENT ---
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    var isSexDropdownExpanded by remember { mutableStateOf(false) }
    var selectedSex by remember { mutableStateOf("") }
    val sexOptions = listOf("Masculino", "Femenino")

    var isGradeDropdownExpanded by remember { mutableStateOf(false) }
    var selectedGrade by remember { mutableStateOf("") }
    val gradeOptions = listOf("1ro", "2do", "3ro", "4to", "5to")

    // TODO: Si studentId no es nulo, cargar los datos del estudiante aquí

    // --- UI COMPOSITION ---
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (studentId == null) "Nuevo Estudiante" else "Editar Estudiante") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Código de estudiante") },
                modifier = Modifier.fillMaxWidth()
            )

            // Dropdown para Sexo
            ExposedDropdownMenuBox(
                expanded = isSexDropdownExpanded,
                onExpandedChange = { isSexDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedSex,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Sexo") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isSexDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isSexDropdownExpanded,
                    onDismissRequest = { isSexDropdownExpanded = false }
                ) {
                    sexOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedSex = option
                                isSexDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            // Dropdown para Grado
            ExposedDropdownMenuBox(
                expanded = isGradeDropdownExpanded,
                onExpandedChange = { isGradeDropdownExpanded = it }
            ) {
                OutlinedTextField(
                    value = selectedGrade,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Grado") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGradeDropdownExpanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = isGradeDropdownExpanded,
                    onDismissRequest = { isGradeDropdownExpanded = false }
                ) {
                    gradeOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                selectedGrade = option
                                isGradeDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun StudentFormScreenNewPreview() {
    GemaTheme {
        StudentFormScreen()
    }
}

@Preview(showBackground = true)
@Composable
private fun StudentFormScreenEditPreview() {
    GemaTheme {
        StudentFormScreen(studentId = "123")
    }
}