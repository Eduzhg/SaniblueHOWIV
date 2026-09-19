package com.saniblue.app.util

import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import java.io.File

/**
 * Persistência das assinaturas coletadas na tela (cliente acompanhante e técnico).
 *
 * Diferente da foto do ensaio, a assinatura NÃO vai para a galeria pública — é um
 * dado do laudo, não uma foto do usuário. Fica apenas no armazenamento do app
 * (getExternalFilesDir), de onde o laudo (PDF) e a tela de detalhes a carregam.
 */
object AssinaturaHelper {

    enum class Tipo(val prefixo: String) { CLIENTE("Cliente"), TECNICO("Tecnico") }

    /**
     * Grava o bitmap da assinatura como PNG (fundo transparente) no armazenamento do app.
     * O nome leva um timestamp: refazer a assinatura gera um arquivo novo (o chamador
     * apaga o anterior), então nunca aparece a versão antiga vinda de cache de imagem.
     *
     * @return caminho absoluto do arquivo, ou "" se não foi possível gravar.
     */
    fun salvarAssinatura(
        context: Context,
        bitmap: Bitmap,
        tipo: Tipo,
        numeroHidrometro: String,
        dataEnsaio: String
    ): String = runCatching {
        val nomeSeguro = numeroHidrometro.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "sem_numero" }
        val dataSegura = dataEnsaio.replace("/", "").ifBlank { "sem_data" }
        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
            "assinaturas"
        ).apply { mkdirs() }
        val dest = File(
            dir,
            "Assinatura_${tipo.prefixo}_${nomeSeguro}_${dataSegura}_${System.currentTimeMillis()}.png"
        )
        dest.outputStream().use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }
        dest.absolutePath
    }.getOrDefault("")

    /** Apaga o arquivo da assinatura (técnico refez/limpou). Falha silenciosa. */
    fun apagarAssinatura(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    /** Carrega a assinatura gravada, ou null se o arquivo não existir mais. */
    fun carregarBitmap(context: Context, path: String): Bitmap? =
        if (path.isBlank()) null else FotoEnsaioHelper.carregarBitmap(context, path)
}
