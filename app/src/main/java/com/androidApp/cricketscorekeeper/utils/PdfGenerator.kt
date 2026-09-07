package com.androidApp.cricketscorekeeper.utils

import android.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.androidApp.cricketscorekeeper.models.MatchState
import com.androidApp.cricketscorekeeper.viewmodel.ScoreViewModel

fun generateAndSavePdf(context: Context, state: MatchState, viewModel: ScoreViewModel) {
    val document = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(400, 1200, 1).create()
    val page = document.startPage(pageInfo)
    val canvas = page.canvas
    val paint = Paint().apply { textSize = 14f }
    val boldPaint = Paint().apply { textSize = 16f; isFakeBoldText = true }

    var yPos = 40f
    canvas.drawText("Cricket Match Summary", 20f, yPos, boldPaint)
    yPos += 25f

    canvas.drawText("Batting Team: ${state.battingTeamName} vs Bowling Team: ${state.bowlingTeamName}", 20f, yPos, boldPaint)
    yPos += 30f

    if (state.previousInningsSummary.isNotEmpty()) {
        state.previousInningsSummary.split("\n").forEach { line ->
            if (line.isNotBlank()) {
                canvas.drawText(line, 20f, yPos, paint)
                yPos += 25f
            }
        }
    }

    canvas.drawText("Innings ${state.innings} (${state.battingTeamName}): ${state.totalRuns}/${state.wickets} (${state.displayOvers} overs)", 20f, yPos, boldPaint)
    yPos += 30f

    if (state.targetScore != null) {
        val dlsTag = if (state.isDlsApplied) " (DLS Adjusted)" else ""
        canvas.drawText("Target: ${state.targetScore}$dlsTag", 20f, yPos, boldPaint)
        yPos += 30f
    }

    canvas.drawText("Batting:", 20f, yPos, boldPaint)
    yPos += 20f
    val allBatters = state.dismissedBatters + state.retiredHurtBatters + state.striker + state.nonStriker
    allBatters.forEach { b ->
        if (b.name.isNotBlank()) {
            val status = if (b.isOut) "" else if (state.retiredHurtBatters.contains(b)) " (Retired Hurt)" else " *"
            canvas.drawText("${b.name}$status: ${b.runs} (${b.balls}) | 4s: ${b.fours} | 6s: ${b.sixes}", 30f, yPos, paint)
            yPos += 20f
        }
    }

    yPos += 10f
    canvas.drawText("Bowling:", 20f, yPos, boldPaint)
    yPos += 20f
    val bowlerStats = viewModel.getAggregatedBowlerStats()
    bowlerStats.forEach { b ->
        canvas.drawText("${b.name}: ${b.displayFigures}", 30f, yPos, paint)
        yPos += 20f
    }

    document.finishPage(page)

    try {
        val fileName = "CricketScore_Innings${state.innings}_${System.currentTimeMillis()}.pdf"

        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                document.writeTo(outputStream)
            }

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                context.startActivity(intent)
            } catch (_: Exception) {
                // Fallback to notification if no handler app installed
            }

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val channelId = "pdf_downloads"
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(channelId, "PDF Downloads", NotificationManager.IMPORTANCE_DEFAULT)
                notificationManager.createNotificationChannel(channel)
            }

            val notification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_menu_save)
                .setContentTitle("Scorecard Saved")
                .setContentText("Tap to open $fileName")
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify(System.currentTimeMillis().toInt(), notification)
            Toast.makeText(context, "PDF saved! Check your notifications.", Toast.LENGTH_SHORT).show()

        } else {
            Toast.makeText(context, "Failed to create file.", Toast.LENGTH_SHORT).show()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
    } finally {
        document.close()
    }
}
