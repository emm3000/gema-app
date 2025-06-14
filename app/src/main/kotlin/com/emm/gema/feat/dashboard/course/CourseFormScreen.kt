package com.emm.gema.feat.dashboard.course

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.emm.gema.feat.dashboard.components.GemaDropdown
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
            val result = snackBarHostState
                .showSnackbar(
                    message = "Curso creado con éxito!!",
                    actionLabel = "Ok",
                    duration = SnackbarDuration.Indefinite
                )
            when (result) {
                SnackbarResult.ActionPerformed -> {
                    onBack()
                }

                SnackbarResult.Dismissed -> {}
            }
        }
    }

    Scaffold(
        topBar = {
            CourseFormTopAppBar(null, {
                keyboardController?.hide()
                onBack()
            })
        },
        snackbarHost = { SnackbarHost(hostState = snackBarHostState) }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                textLabel = "Primaria/Secundaria",
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

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { onAction(CourseFormAction.OnSave) },
                enabled = state.isValidFields && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Guardar", fontWeight = FontWeight.Bold)
            }
        }
    }


    if (state.isLoading) {
        CommonDialog(onAction) {
            CircularProgressIndicator(
                modifier = Modifier
                    .size(48.dp)
                    .animateContentSize()
            )
        }
    }

    if (state.error != null) {
        CommonDialog(onAction) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Error!!", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    modifier = Modifier.fillMaxWidth(0.7f),
                    onClick = { onAction(CourseFormAction.ClearErrorDialog) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("Ok", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CommonDialog(
    onAction: (CourseFormAction) -> Unit,
    content: @Composable () -> Unit,
) {
    BasicAlertDialog(
        onDismissRequest = { onAction(CourseFormAction.ClearErrorDialog) },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5))
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 12.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun CourseFormTopAppBar(courseId: String?, onBack: () -> Unit) {
    TopAppBar(
        title = { Text(if (courseId == null) "Nuevo Curso" else "Editar Curso") },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun CourseFormScreenPreview() {
    GemaTheme {
        CourseFormScreen()
    }
}