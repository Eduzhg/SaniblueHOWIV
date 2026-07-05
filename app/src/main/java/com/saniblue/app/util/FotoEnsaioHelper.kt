package com.saniblue.app.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import java.io.File

object FotoEnsaioHelper {

    /** Referência ao arquivo temporário (sem criar nem apagar). */
    fun obterArquivoTemp(context: Context): File =
        File(File(context.cacheDir, "fotos_temp").apply { mkdirs() }, "foto_temp.jpg")

    /** Prepara arquivo temporário limpo para a câmera escrever (apaga o anterior). */
    fun prepararArquivoTemp(context: Context): File =
        obterArquivoTemp(context).also { if (it.exists()) it.delete() }

    /** Retorna URI do FileProvider para o arquivo temporário. */
    fun uriParaArquivoTemp(context: Context, arquivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", arquivo)

    /**
     * Persiste a foto do ensaio fazendo DUAS cópias a partir do arquivo temporário:
     *
     *  1. Uma no armazenamento do próprio app (getExternalFilesDir) — não depende de
     *     permissões nem do MediaStore, então sempre funciona. É este caminho que é
     *     devolvido e usado pelo laudo (PDF) e pela tela de detalhes.
     *  2. Uma na galeria pública (Pictures/Saniblue), para o usuário conseguir abrir
     *     a foto depois pela galeria. Best-effort: se falhar (ex.: permissão negada em
     *     Android antigo), o fluxo não quebra — a foto segue disponível pela cópia (1).
     *
     * @return caminho absoluto da cópia do app, ou "" se nem essa cópia foi possível.
     */
    fun salvarFoto(
        context: Context,
        arquivoTemp: File,
        numeroHidrometro: String,
        dataEnsaio: String
    ): String {
        val nomeSeguro = numeroHidrometro.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "sem_numero" }
        val dataSegura = dataEnsaio.replace("/", "").ifBlank { "sem_data" }
        val nomeArquivo = "NaoRealizado_${nomeSeguro}_$dataSegura.jpg"

        // (1) Cópia confiável no armazenamento do app — é este o path retornado.
        val caminhoApp = runCatching {
            val dir = File(
                context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
                "fotos_ensaio"
            ).apply { mkdirs() }
            val dest = File(dir, nomeArquivo)
            arquivoTemp.copyTo(dest, overwrite = true)
            dest.absolutePath
        }.getOrDefault("")

        // (2) Cópia na galeria pública (bônus para o usuário) — falha não interrompe.
        runCatching { copiarParaGaleria(context, arquivoTemp, nomeArquivo) }

        return caminhoApp
    }

    /** Copia o arquivo temporário para a galeria pública (Pictures/Saniblue). */
    private fun copiarParaGaleria(context: Context, arquivoTemp: File, nomeArquivo: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val cv = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, nomeArquivo)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/Saniblue")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, cv) ?: return
            resolver.openOutputStream(uri)?.use { out ->
                arquivoTemp.inputStream().use { it.copyTo(out) }
            }
            cv.clear()
            cv.put(MediaStore.Images.Media.IS_PENDING, 0)
            resolver.update(uri, cv, null, null)
        } else {
            val dir = File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                "Saniblue"
            ).apply { mkdirs() }
            val dest = File(dir, nomeArquivo)
            arquivoTemp.copyTo(dest, overwrite = true)
            MediaScannerConnection.scanFile(context, arrayOf(dest.absolutePath), null, null)
        }
    }

    /**
     * Carrega Bitmap a partir de URI content:// ou caminho de arquivo.
     * Usa subsampling para nunca carregar mais que ~1200px na maior dimensão,
     * evitando OOM com fotos de câmera em alta resolução.
     */
    fun carregarBitmap(context: Context, fotoPath: String): Bitmap? = runCatching {
        val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        if (fotoPath.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(fotoPath))
                ?.use { BitmapFactory.decodeStream(it, null, boundsOpts) }
        } else {
            BitmapFactory.decodeFile(fotoPath, boundsOpts)
        }
        val sampleSize = if (boundsOpts.outWidth > 0 && boundsOpts.outHeight > 0) {
            calcSampleSize(boundsOpts.outWidth, boundsOpts.outHeight, 1200)
        } else 1
        val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        if (fotoPath.startsWith("content://")) {
            context.contentResolver.openInputStream(Uri.parse(fotoPath))
                ?.use { BitmapFactory.decodeStream(it, null, decodeOpts) }
        } else {
            BitmapFactory.decodeFile(fotoPath, decodeOpts)
        }
    }.getOrNull()

    private fun calcSampleSize(width: Int, height: Int, maxDim: Int): Int {
        var size = 1
        while (maxOf(width / size, height / size) > maxDim) size *= 2
        return size
    }
}
