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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.feat.dashboard.sexOptions
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(
    modifier: Modifier = Modifier,
    state: StudentFormUiState = StudentFormUiState(),
    onAction: (StudentFormAction) -> Unit = {},
    studentId: String? = null,
    onBack: () -> Unit = {},
) {

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
                value = state.name,
                enabled = state.isLoading.not(),
                onValueChange = { onAction(StudentFormAction.NameChanged(it)) },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.email,
                enabled = state.isLoading.not(),
                onValueChange = { onAction(StudentFormAction.EmailChanged(it)) },
                label = { Text("Correo electrónico") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.dni,
                enabled = state.isLoading.not(),
                onValueChange = { onAction(StudentFormAction.DniChanged(it)) },
                label = { Text("Dni") },
                modifier = Modifier.fillMaxWidth()
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Sexo",
                items = sexOptions,
                itemSelected = state.sex,
                onItemSelected = { onAction(StudentFormAction.SexChanged(it)) }
            )

            when {
                state.isLoading -> {
                    Text(
                        text = "Cargando...",
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                state.error != null -> {
                    Text(
                        text = state.error,
                        modifier = Modifier.fillMaxWidth()
                            .padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(StudentFormAction.Save) },
                modifier = Modifier.fillMaxWidth()
                    .height(50.dp),
                enabled = state.isFormValid && !state.isLoading
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