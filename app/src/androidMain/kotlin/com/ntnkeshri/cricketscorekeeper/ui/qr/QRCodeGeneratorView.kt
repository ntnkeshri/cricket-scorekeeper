package com.ntnkeshri.cricketscorekeeper.ui.qr

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.ntnkeshri.cricketscorekeeper.models.MatchState
import com.ntnkeshri.cricketscorekeeper.utils.QRStateSerializer

@Composable
fun QRCodeGeneratorView(state: MatchState, modifier: Modifier = Modifier) {
    var qrBitmap by remember(state) { mutableStateOf<Bitmap?>(null) }
    var payloadSize by remember(state) { mutableIntStateOf(0) }

    LaunchedEffect(state) {
        val serialized = QRStateSerializer.compress(state)
        payloadSize = serialized.length
        qrBitmap = generateQRCodeBitmap(serialized, 512, 512)
    }

    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        qrBitmap?.let { bitmap ->
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "Match State QR Code",
                modifier = Modifier.size(250.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Payload Size: $payloadSize chars",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } ?: CircularProgressIndicator()
    }
}

private fun generateQRCodeBitmap(text: String, width: Int, height: Int): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, width, height)
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
        }
    }
    return bitmap
}
