package com.example.eventify.ui.components

import android.graphics.Bitmap
import android.graphics.Color // Usar android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size // Importante
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import java.util.EnumMap

@Composable
fun QRCodeImage(
    content: String,
    modifier: Modifier = Modifier
) {
    // Log para debug (vai aparecer no Logcat)
    println("Gerando QR Code para: $content")

    if (content.isBlank()) return

    val bitmap = remember(content) { generateQRCode(content) }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Ticket QR Code: $content",
            modifier = modifier, // O modifier deve ter tamanho fixo no pai
            contentScale = ContentScale.Fit
        )
    }
}

fun generateQRCode(content: String): Bitmap? {
    return try {
        val hints = EnumMap<EncodeHintType, Any>(EncodeHintType::class.java)
        hints[EncodeHintType.MARGIN] = 2 // Margem de segurança branca

        val writer = QRCodeWriter()
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height

        // Cria o Bitmap
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

        for (x in 0 until width) {
            for (y in 0 until height) {
                // QR Code PRETO sobre fundo BRANCO (Garante contraste)
                bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}