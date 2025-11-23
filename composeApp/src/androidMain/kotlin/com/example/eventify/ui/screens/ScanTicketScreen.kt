package com.example.eventify.ui.screens.organizer

import android.Manifest
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border // <--- FIXED: Required for Modifier.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.eventify.di.AppModule
import com.example.eventify.model.TicketValidationResult
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.Executors

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ScanTicketScreen(
    navController: NavController
) {
    // Permission State
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)

    val scope = rememberCoroutineScope()
    val repository = remember { AppModule.eventRepository }

    // Validation States
    var scanResult by remember { mutableStateOf<TicketValidationResult?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Main UI
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        if (cameraPermissionState.status.isGranted) {
            // 1. CAMERA PREVIEW
            CameraPreview(
                onBarcodeScanned = { barcodeValue ->
                    if (!isProcessing && barcodeValue != null && barcodeValue.isNotEmpty()) {
                        isProcessing = true

                        // Validate via Firebase
                        scope.launch {
                            val result = repository.validateTicket(barcodeValue)
                            scanResult = result

                            // Reset after 3 seconds if invalid, or keep success shown
                            if (result != TicketValidationResult.VALID) {
                                delay(3000)
                                isProcessing = false
                                scanResult = null
                            }
                        }
                    }
                }
            )

            // 2. TARGET OVERLAY (Box in middle)
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .align(Alignment.Center)
                    .border(2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            )

        } else {
            // 3. REQUEST PERMISSION UI
            Column(
                modifier = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Camera permission needed to scan tickets", color = Color.White)
                Spacer(Modifier.height(16.dp))
                Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }

        // 4. RESULT OVERLAY
        // FIXED: Using explicit variables instead of destructuring to prevent ambiguity errors
        if (scanResult != null) {
            val resultColor = when (scanResult) {
                TicketValidationResult.VALID -> Color(0xFF00E096) // Green
                TicketValidationResult.ALREADY_USED -> Color(0xFFFFAB00) // Yellow
                else -> Color(0xFFFF3D71) // Red
            }

            val resultText = when (scanResult) {
                TicketValidationResult.VALID -> "VALID TICKET"
                TicketValidationResult.ALREADY_USED -> "ALREADY USED"
                else -> "INVALID / ERROR"
            }

            val resultIcon = when (scanResult) {
                TicketValidationResult.VALID -> Icons.Default.CheckCircle
                TicketValidationResult.ALREADY_USED -> Icons.Default.Warning
                else -> Icons.Default.Error
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = resultColor),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp)
                    .fillMaxWidth()
                    .height(150.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(resultIcon, null, tint = Color.White, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(resultText, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)

                    if (scanResult == TicketValidationResult.VALID) {
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                isProcessing = false
                                scanResult = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = resultColor)
                        ) {
                            Text("Scan Next")
                        }
                    }
                }
            }
        }

        // Close Button
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
        ) {
            Icon(Icons.Default.Close, "Close", tint = Color.White)
        }
    }
}

@Composable
fun CameraPreview(
    onBarcodeScanned: (String?) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    AndroidView(
        factory = { ctx ->
            val previewView = PreviewView(ctx).apply {
                this.scaleType = PreviewView.ScaleType.FILL_CENTER
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            val executor = ContextCompat.getMainExecutor(ctx)

            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()

                imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor()) { imageProxy ->
                    val mediaImage = imageProxy.image
                    if (mediaImage != null) {
                        val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                        val scanner = BarcodeScanning.getClient()

                        scanner.process(image)
                            .addOnSuccessListener { barcodes ->
                                for (barcode in barcodes) {
                                    barcode.rawValue?.let { value ->
                                        onBarcodeScanned(value)
                                    }
                                }
                            }
                            .addOnCompleteListener { imageProxy.close() }
                    } else {
                        imageProxy.close()
                    }
                }

                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview,
                        imageAnalysis
                    )
                } catch (exc: Exception) {
                    Log.e("CameraPreview", "Use case binding failed", exc)
                }
            }, executor)

            previewView
        },
        modifier = Modifier.fillMaxSize()
    )
}