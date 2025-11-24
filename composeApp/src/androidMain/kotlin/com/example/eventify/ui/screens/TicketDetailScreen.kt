package com.example.eventify.ui.screens

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape // <--- IMPORT CORRIGIDO
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.ui.components.generateQRCode
import com.example.eventify.viewmodels.TicketDetailViewModel
import java.io.OutputStream

@Composable
fun TicketDetailScreen(
    ticketId: String,
    eventTitle: String,
    navController: NavController
) {
    val viewModel = remember { AppModule.provideTicketDetailViewModel(ticketId) }
    val transferStatus by viewModel.transferStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val context = LocalContext.current

    // Estado do Dialog
    var showTransferDialog by remember { mutableStateOf(false) }
    var recipientEmail by remember { mutableStateOf("") }

    // Reagir ao resultado da transferência
    LaunchedEffect(transferStatus) {
        when (transferStatus) {
            TicketDetailViewModel.Result.SUCCESS -> {
                Toast.makeText(context, "Bilhete transferido com sucesso!", Toast.LENGTH_LONG).show()
                navController.popBackStack()
            }
            TicketDetailViewModel.Result.USER_NOT_FOUND -> { // <--- CORREÇÃO: Caso que faltava
                Toast.makeText(context, "Utilizador não encontrado com esse email.", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
            TicketDetailViewModel.Result.ERROR -> {
                Toast.makeText(context, "Erro ao transferir. Verifica a ligação.", Toast.LENGTH_SHORT).show()
                viewModel.resetStatus()
            }
            null -> {}
        }
    }

    // QR Bitmap
    val qrBitmap = remember(ticketId) { generateQRCode(ticketId) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A12))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = eventTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Admit One", color = Color.Gray)

                Spacer(modifier = Modifier.height(32.dp))

                // QR Code
                if (qrBitmap != null) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(200.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(ticketId.take(8).uppercase(), style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            // 1. Voltar
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.background(Color.White.copy(alpha = 0.1f), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
            }

            // 2. Download
            IconButton(
                onClick = {
                    if (qrBitmap != null) saveToGallery(context, qrBitmap, "Ticket-$eventTitle")
                },
                modifier = Modifier.background(Color(0xFFD0BCFF), CircleShape)
            ) {
                Icon(Icons.Default.Download, "Save", tint = Color(0xFF381E72))
            }

            // 3. Transferir
            IconButton(
                onClick = { showTransferDialog = true },
                modifier = Modifier.background(Color(0xFF00E096), CircleShape)
            ) {
                Icon(Icons.Default.Send, "Transfer", tint = Color.Black)
            }
        }
    }

    // --- DIALOG DE TRANSFERÊNCIA ---
    if (showTransferDialog) {
        AlertDialog(
            onDismissRequest = { showTransferDialog = false },
            title = { Text("Transfer Ticket") },
            text = {
                Column {
                    Text("Enter the email address of the person you want to send this ticket to. This action cannot be undone.")
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = recipientEmail,
                        onValueChange = { recipientEmail = it },
                        label = { Text("Recipient Email") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.transferTicket(recipientEmail)
                        showTransferDialog = false
                    },
                    enabled = !isLoading && recipientEmail.isNotBlank()
                ) {
                    Text("Send Ticket")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTransferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// --- FUNÇÃO DE DOWNLOAD (Para resolver o Unresolved reference) ---
fun saveToGallery(context: Context, bitmap: Bitmap, title: String) {
    val filename = "$title.png"
    var fos: OutputStream? = null
    var imageUri: android.net.Uri? = null

    try {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Eventify")
                put(MediaStore.MediaColumns.IS_PENDING, 1)
            }
        }

        val resolver = context.contentResolver
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

        imageUri?.let { uri ->
            fos = resolver.openOutputStream(uri)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos!!)
            fos?.close()

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(uri, contentValues, null, null)
            }

            Toast.makeText(context, "Bilhete guardado na Galeria!", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        Toast.makeText(context, "Erro ao guardar: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}