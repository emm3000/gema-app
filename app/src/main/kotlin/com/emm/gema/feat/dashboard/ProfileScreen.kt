package com.emm.gema.feat.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.emm.gema.ui.theme.GemaTheme

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {

    // --- STATE MANAGEMENT ---
    var name by remember { mutableStateOf("Emiliano Montes") }
    var email by remember { mutableStateOf("docente.ejemplo@gema.com") }
    val dni = "12345678A" // DNI no es editable en este ejemplo

    var showEditDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }

    // --- UI COMPOSITION ---
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Mi Perfil", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(24.dp))

            // Card con información del docente
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    ProfileInfoRow(Icons.Default.Person, "Nombre", name)
                    ProfileInfoRow(Icons.Default.Email, "Correo", email)
                    ProfileInfoRow(Icons.Default.AccountBox, "DNI", dni)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showEditDialog = true },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Editar")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Opción para cambiar contraseña
            TextButton(onClick = { showChangePasswordDialog = true }) {
                Text("Cambiar contraseña")
            }
        }

        // Botón de Cerrar Sesión
        OutlinedButton(
            onClick = { /* TODO: Lógica para cerrar sesión */ },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Cerrar Sesión")
        }
    }

    // --- DIALOGS ---
    if (showEditDialog) {
        EditProfileDialog(
            currentName = name,
            currentEmail = email,
            onDismiss = { showEditDialog = false },
            onConfirm = { newName, newEmail ->
                name = newName
                email = newEmail
                showEditDialog = false
            }
        )
    }

    if (showChangePasswordDialog) {
        ChangePasswordDialog(
            onDismiss = { showChangePasswordDialog = false },
            onConfirm = { /* TODO: Lógica para cambiar contraseña */
                showChangePasswordDialog = false
            }
        )
    }
}

@Composable
private fun ProfileInfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 8.dp)) {
        Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall)
            Text(value, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
private fun EditProfileDialog(
    currentName: String,
    currentEmail: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    var tempName by remember { mutableStateOf(currentName) }
    var tempEmail by remember { mutableStateOf(currentEmail) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Perfil") },
        text = {
            Column {
                OutlinedTextField(value = tempName, onValueChange = { tempName = it }, label = { Text("Nombre") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = tempEmail, onValueChange = { tempEmail = it }, label = { Text("Correo") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(tempName, tempEmail) }) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun ChangePasswordDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambiar Contraseña") },
        text = {
            Column {
                OutlinedTextField(value = currentPassword, onValueChange = { currentPassword = it }, label = { Text("Contraseña actual") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = newPassword, onValueChange = { newPassword = it }, label = { Text("Nueva contraseña") })
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Confirmar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// --- IMPORTS ---


@Preview(showBackground = true)
@Composable
private fun ProfileScreenPreview() {
    GemaTheme {
        ProfileScreen()
    }
}