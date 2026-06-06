package com.saniblue.app.util

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import javax.inject.Inject

class QrCodeGenerator @Inject constructor() {

    fun gerar(conteudo: String, tamanho: Int = 300): Bitmap {
        val hints = mapOf(EncodeHintType.MARGIN to 1)
        val writer = QRCodeWriter()
        val matrix = writer.encode(conteudo, BarcodeFormat.QR_CODE, tamanho, tamanho, hints)
        val bitmap = Bitmap.createBitmap(tamanho, tamanho, Bitmap.Config.RGB_565)
        for (x in 0 until tamanho) {
            for (y in 0 until tamanho) {
                bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bitmap
    }

    fun gerarUrlValidacao(ensaioId: Long, numeroHidrometro: String): String =
        "https://metrologia.saniblue.com.br/validar?id=$ensaioId&h=$numeroHidrometro"
}
