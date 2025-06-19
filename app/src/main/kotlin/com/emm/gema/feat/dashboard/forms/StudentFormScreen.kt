package com.emm.gema.feat.dashboard.forms

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.feat.dashboard.components.GemaDropdown
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

    var selectedSex by remember { mutableStateOf("") }
    val sexOptions = listOf("Masculino", "Femenino")

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
                label = { Text("Dni") },
                modifier = Modifier.fillMaxWidth()
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Sexo",
                items = sexOptions,
                itemSelected = selectedSex,
                onItemSelected = { selectedSex = it }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar")
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun StudentFormScreenNewPreview() {
    GemaTheme {
        StudentFormScreen()
    }
}

@PreviewLightDark
@Composable
private fun StudentFormScreenEditPreview() {
    GemaTheme {
        StudentFormScreen(studentId = "123")
    }
}