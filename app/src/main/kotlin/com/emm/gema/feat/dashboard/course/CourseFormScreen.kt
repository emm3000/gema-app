package com.emm.gema.feat.dashboard.course

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.emm.gema.feat.dashboard.components.GemaDropdown
import com.emm.gema.feat.dashboard.gradeOptions
import com.emm.gema.feat.dashboard.levelOptions
import com.emm.gema.feat.dashboard.sectionsOptions
import com.emm.gema.feat.dashboard.shiftOptions
import com.emm.gema.ui.theme.GemaTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseFormScreen(
    modifier: Modifier = Modifier,
    state: CourseFormUiState = CourseFormUiState(),
    onAction: (CourseFormAction) -> Unit = {},
    onBack: () -> Unit = {},
) {
    val snackBarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(state.success) {
        if (state.success) {
            val result = snackBarHostState.showSnackbar(
                message = "Curso guardado con éxito!!",
                actionLabel = "Ok",
                duration = SnackbarDuration.Indefinite
            )
            if (result == SnackbarResult.ActionPerformed) {
                onBack()
            }
        }
    }

    Scaffold(
        modifier = Modifier,
        topBar = {
            CourseFormTopAppBar(courseId = null) { // Assuming new course for now
                keyboardController?.hide()
                onBack()
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) },
        bottomBar = {
            Button(
                onClick = { onAction(CourseFormAction.OnSave) },
                enabled = state.isValidFields,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(WindowInsets.navigationBars.asPaddingValues())
                    .padding(16.dp)
                    .height(50.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        },
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.courseName,
                onValueChange = { onAction(CourseFormAction.OnCourseNameChange(it)) },
                label = { Text("Nombre del Curso") },
                modifier = Modifier.fillMaxWidth()
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Grado",
                items = gradeOptions,
                itemSelected = state.grade,
                onItemSelected = { onAction(CourseFormAction.OnGradeChange(it)) }
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Sección",
                items = sectionsOptions,
                itemSelected = state.section,
                onItemSelected = { onAction(CourseFormAction.OnSectionChange(it)) }
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Nivel",
                items = levelOptions,
                itemSelected = state.level,
                onItemSelected = { onAction(CourseFormAction.OnLevelChange(it)) }
            )

            GemaDropdown(
                modifier = Modifier.fillMaxWidth(),
                textLabel = "Turno",
                items = shiftOptions,
                itemSelected = state.shift,
                onItemSelected = { onAction(CourseFormAction.OnShiftChange(it)) }
            )
        }
    }

    if (state.error != null) {
        ErrorDialog(
            error = state.error,
            onDismiss = { onAction(CourseFormAction.ClearErrorDialog) }
        )
    }
}

@Composable
private fun ErrorDialog(
    error: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Error") },
        text = { Text(error) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Ok")
            }
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CourseFormTopAppBar(courseId: String?, onBack: () -> Unit) {
    CenterAlignedTopAppBar(
        title = { Text(if (courseId == null) "Nuevo Curso" else "Editar Curso") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
    )
}

@PreviewLightDark
@Composable
private fun CourseFormScreenPreview() {
    GemaTheme {
        Surface {
            CourseFormScreen()
        }
    }
}