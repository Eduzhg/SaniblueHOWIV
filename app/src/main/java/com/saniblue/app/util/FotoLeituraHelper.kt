package com.saniblue.app.util

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.saniblue.app.domain.model.CampoLeitura
import com.saniblue.app.domain.model.TipoVazao
import java.io.File

/**
 * Fotos das leituras de cada medição — uma por campo (padrão inicial/final e
 * hidrômetro inicial/final). Servem para o técnico auditar depois uma leitura que
 * saiu estranha, então NÃO vão para o laudo nem para a galeria pública: são muitas
 * (até 36 por ensaio) e só interessam dentro do app.
 *
 * A foto é gravada direto no arquivo definitivo — não há etapa de confirmar/descartar
 * como na foto do local; refazer simplesmente gera um arquivo novo e apaga o anterior.
 */
object FotoLeituraHelper {

    private fun diretorio(context: Context): File =
        File(
            context.getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: context.filesDir,
            "fotos_leituras"
        ).apply { mkdirs() }

    /**
     * Cria o arquivo de destino (vazio) para a câmera escrever. O nome identifica a
     * leitura, e o timestamp garante um arquivo novo a cada foto — assim nenhum cache
     * de imagem mostra a foto antiga depois de refazer.
     */
    fun criarArquivo(
        context: Context,
        numeroHidrometro: String,
        tipo: TipoVazao,
        indiceMedicao: Int,
        campo: CampoLeitura
    ): File {
        val serie = numeroHidrometro.replace(Regex("[^A-Za-z0-9_-]"), "_").ifBlank { "sem_numero" }
        val nome = "Leitura_${serie}_${tipo.name.lowercase()}_m${indiceMedicao}_" +
            "${campo.curto}_${System.currentTimeMillis()}.jpg"
        return File(diretorio(context), nome)
    }

    /** URI do FileProvider para a câmera escrever no arquivo. */
    fun uriPara(context: Context, arquivo: File): Uri =
        FileProvider.getUriForFile(context, "${context.packageName}.provider", arquivo)

    /** Apaga a foto (o técnico refez ou a câmera foi cancelada). Falha silenciosa. */
    fun apagar(path: String) {
        if (path.isBlank()) return
        runCatching { File(path).takeIf { it.exists() }?.delete() }
    }

    /** Apaga vários arquivos de uma vez (limpeza da Manutenção). */
    fun apagarVarias(paths: List<String>) = paths.forEach { apagar(it) }

    /**
     * Varre a pasta e apaga tudo que sobrou. Usado na limpeza total para levar junto
     * fotos órfãs — de ensaios já excluídos ou de capturas que o banco não registrou.
     *
     * @return quantos arquivos foram apagados.
     */
    fun apagarTudoDoDisco(context: Context): Int = runCatching {
        diretorio(context).listFiles()?.count { it.delete() } ?: 0
    }.getOrDefault(0)

    /** Quantas fotos existem na pasta e quanto ocupam, para mostrar na Manutenção. */
    fun usoEmDisco(context: Context): UsoDisco = runCatching {
        val arquivos = diretorio(context).listFiles().orEmpty()
        UsoDisco(arquivos.size, arquivos.sumOf { it.length() })
    }.getOrDefault(UsoDisco(0, 0L))

    data class UsoDisco(val quantidade: Int, val bytes: Long) {
        /** Tamanho legível (ex.: "312 MB"), já arredondado. */
        val tamanhoLegivel: String
            get() = when {
                bytes >= 1024L * 1024 * 1024 -> "%.1f GB".format(bytes / (1024.0 * 1024 * 1024))
                bytes >= 1024L * 1024 -> "%.0f MB".format(bytes / (1024.0 * 1024))
                bytes > 0 -> "%.0f KB".format(bytes / 1024.0)
                else -> "0 KB"
            }
    }
}
