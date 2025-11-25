package com.example.eventify.ui.utils

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.eventify.model.Attendee
import kotlinx.serialization.InternalSerializationApi
import java.io.File
import java.io.FileOutputStream

@OptIn(InternalSerializationApi::class)
fun exportAttendeesToPdf(context: Context, eventTitle: String, attendees: List<Attendee>) {
    // 1. Criar o Documento
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint()

    // 2. Desenhar o Título
    paint.textSize = 24f
    paint.isFakeBoldText = true
    canvas.drawText("Eventify - Guest List", 50f, 50f, paint)

    paint.textSize = 18f
    paint.isFakeBoldText = false
    canvas.drawText("Event: $eventTitle", 50f, 80f, paint)
    canvas.drawText("Total Guests: ${attendees.size}", 50f, 105f, paint)

    // 3. Desenhar a Lista
    var y = 150f
    paint.textSize = 14f

    // Cabeçalho da Tabela
    paint.isFakeBoldText = true
    canvas.drawText("Name", 50f, y, paint)
    canvas.drawText("Email", 250f, y, paint)
    canvas.drawText("Status", 450f, y, paint)
    y += 20f
    paint.isFakeBoldText = false

    // Linhas
    for (attendee in attendees) {
        // Verifica se a página acabou (simples)
        if (y > 800f) break

        val status = if (attendee.isCheckedIn) "CHECKED-IN" else "PENDING"

        canvas.drawText(attendee.name.take(25), 50f, y, paint)
        canvas.drawText(attendee.email.take(30), 250f, y, paint)
        canvas.drawText(status, 450f, y, paint)

        y += 25f
    }

    document.finishPage(page)

    // 4. Gravar Ficheiro na Cache
    val file = File(context.cacheDir, "guest_list.pdf")
    try {
        document.writeTo(FileOutputStream(file))
    } catch (e: Exception) {
        e.printStackTrace()
    }
    document.close()

    // 5. Partilhar (Share Intent)
    sharePdf(context, file)
}

private fun sharePdf(context: Context, file: File) {
    try {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Export Guest List"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}