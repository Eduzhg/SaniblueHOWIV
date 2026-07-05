package com.saniblue.app.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.domain.model.VazaoEnsaio
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

class PdfGenerator @Inject constructor(private val context: Context) {

    // ── Layout A4 (todos Float) ────────────────────────────────
    private val PW  = 595f   // largura A4 em pontos (72dpi)
    private val PH  = 842f   // altura  A4
    private val ML  = 36f    // margem lateral
    private val CW  = PW - ML * 2f  // largura útil = 523f

    // ── Cores ──────────────────────────────────────────────────
    private val C_AZUL      = Color.rgb(21, 101, 192)
    private val C_AZUL_CLR  = Color.rgb(227, 238, 255)
    private val C_CINZA_LIN = Color.rgb(248, 248, 248)
    private val C_VERDE     = Color.rgb(27, 94, 32)
    private val C_VERDE_BG  = Color.rgb(200, 230, 201)
    private val C_VERM      = Color.rgb(183, 28, 28)
    private val C_VERM_BG   = Color.rgb(255, 205, 210)

    // ── Estado da página atual ─────────────────────────────────
    private lateinit var document: PdfDocument
    private lateinit var canvas:   Canvas
    private lateinit var page:     PdfDocument.Page
    private var pageNum = 0
    private var y       = 0f

    // ─────────────────────────────────────────────────────────────────
    // Ponto de entrada
    // ─────────────────────────────────────────────────────────────────

    fun gerarLaudo(context: Context, ensaio: Ensaio, modelo: HidrometroModelo): File {
        document = PdfDocument()
        pageNum  = 0
        novaPagina()

        drawCabecalho(ensaio)
        drawSecaoDadosCadastrais(ensaio, modelo)
        if (!ensaio.realizado) {
            drawEnsaioNaoRealizado(ensaio)
        } else {
            drawSecaoResultados(ensaio, modelo)
            drawResultadoFinal(ensaio.resultadoFinal)
            if (ensaio.resultadoFinal == ResultadoFinal.REPROVADO) {
                drawDadosSubstituicao(ensaio)
            }
        }
        drawAcompanhamentoCliente(ensaio)
        drawAssinatura(ensaio)
        drawRodape(ensaio)

        document.finishPage(page)

        val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) ?: context.filesDir
        dir.mkdirs()
        val arquivo = File(dir, "Laudo_${ensaio.numeroHidrometro}_${System.currentTimeMillis()}.pdf")
        arquivo.outputStream().use { document.writeTo(it) }
        document.close()
        return arquivo
    }

    // ─────────────────────────────────────────────────────────────────
    // Controle de páginas
    // ─────────────────────────────────────────────────────────────────

    private fun novaPagina() {
        if (pageNum > 0) document.finishPage(page)
        pageNum++
        val info = PdfDocument.PageInfo.Builder(PW.toInt(), PH.toInt(), pageNum).create()
        page   = document.startPage(info)
        canvas = page.canvas
        y      = 0f
    }

    /** Verifica se há espaço suficiente antes do rodapé (últimos 55 pt); se não, nova página. */
    private fun checkSpace(needed: Float) {
        if (y + needed > PH - 55f) {
            novaPagina()
            y = 16f
            // Faixa de continuação
            canvas.drawRect(0f, 0f, PW.toFloat(), 5f, fillPaint(C_AZUL))
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Cabeçalho  (pág. 1)
    // ─────────────────────────────────────────────────────────────────

    private fun drawCabecalho(ensaio: Ensaio) {
        canvas.drawRect(0f, 0f, PW.toFloat(), 76f, fillPaint(C_AZUL))

        canvas.drawText("SANIBLUE",
            ML, 30f, textPaint(Color.WHITE, 22f, bold = true))
        canvas.drawText("Engenharia em Hidrometria",
            ML, 46f, textPaint(Color.WHITE, 10f))
        canvas.drawText("LAUDO METROLÓGICO DE HIDRÔMETRO",
            ML, 62f, textPaint(Color.WHITE, 10f, bold = true))

        val rp = textPaint(Color.WHITE, 9f, align = Paint.Align.RIGHT)
        canvas.drawText("Companhia: ${ensaio.nomeCompanhia.ifBlank { "-" }}", PW - ML, 30f, rp)
        canvas.drawText("Data: ${ensaio.dataEnsaio}", PW - ML, 46f, rp)
        canvas.drawText("Técnico: ${ensaio.tecnicoResponsavel}", PW - ML, 62f, rp)

        // Linha separadora
        canvas.drawLine(0f, 76f, PW.toFloat(), 76f, strokePaint(Color.LTGRAY, 1f))
        y = 84f
    }

    // ─────────────────────────────────────────────────────────────────
    // Dados cadastrais
    // ─────────────────────────────────────────────────────────────────

    private fun drawSecaoDadosCadastrais(ensaio: Ensaio, modelo: HidrometroModelo) {
        drawTitulo("DADOS DO CLIENTE E DO ENSAIO")

        val col = CW / 2f
        val pares = listOf(
            "Nº Hidrômetro" to ensaio.numeroHidrometro,
            "Matrícula"     to ensaio.matricula.ifBlank { "-" },
            "Cliente"       to ensaio.cliente,
            "Companhia"     to ensaio.nomeCompanhia.ifBlank { "-" },
            "Endereço"      to ensaio.endereco.ifBlank { "-" },
            "Bairro"        to ensaio.bairro.ifBlank { "-" },
            "Cidade"        to ensaio.cidade.ifBlank { "-" },
            "Idade do Hidrômetro" to ensaio.idadeHidrometro.ifBlank { "-" },
            "Data do Ensaio" to ensaio.dataEnsaio,
            "Pressão Média" to ensaio.pressaoMedia.let { if (it.isBlank()) "-" else "$it mca" },
            "Norma"         to ensaio.norma.descricao,
            "Método de Ensaio" to ensaio.metodoEnsaio.label
        )

        pares.chunked(2).forEach { par ->
            checkSpace(28f)
            canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
            par.getOrNull(0)?.let { (lbl, v) ->
                canvas.drawText(lbl, ML + 2f, y + 9f,  textPaint(Color.GRAY, 7.5f))
                canvas.drawText(trunc(v, 36), ML + 2f, y + 20f, textPaint(Color.BLACK, 9f, bold = true))
            }
            par.getOrNull(1)?.let { (lbl, v) ->
                canvas.drawText(lbl, ML + col + 2f, y + 9f,  textPaint(Color.GRAY, 7.5f))
                canvas.drawText(trunc(v, 36), ML + col + 2f, y + 20f, textPaint(Color.BLACK, 9f, bold = true))
            }
            y += 26f
        }

        if (ensaio.observacoes.isNotBlank()) {
            checkSpace(28f)
            canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
            canvas.drawText("Observações", ML + 2f, y + 9f, textPaint(Color.GRAY, 7.5f))
            canvas.drawText(trunc(ensaio.observacoes, 80), ML + 2f, y + 20f, textPaint(Color.BLACK, 9f))
            y += 26f
        }

        canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
        y += 8f
    }

    // ─────────────────────────────────────────────────────────────────
    // Seção de resultados — 3 tabelas (Nominal, Transição, Mínima)
    // ─────────────────────────────────────────────────────────────────

    private fun drawSecaoResultados(ensaio: Ensaio, modelo: HidrometroModelo) {
        drawTitulo("RESULTADOS DOS ENSAIOS METROLÓGICOS")
        y += 4f

        listOf(TipoVazao.NOMINAL, TipoVazao.TRANSICAO, TipoVazao.MINIMA).forEach { tipo ->
            val vazao = ensaio.vazoes.find { it.tipoVazao == tipo }
            drawTabelaVazao(tipo, vazao, modelo, ensaio.norma, ensaio.erroPadrao)
            y += 10f
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Tabela de uma Vazão (3 medições + linha de erro médio)
    // ─────────────────────────────────────────────────────────────────

    private fun drawTabelaVazao(tipo: TipoVazao, vazao: VazaoEnsaio?, modelo: HidrometroModelo, norma: NormaEnsaio, erroPadrao: Double) {
        // Altura estimada: 18(hdr vazão)+22(hdr colunas)+3×16(linhas)+18(erro médio) = 106
        checkSpace(110f)

        // Limites vêm da NORMA; vazão de referência vem do modelo
        val nomeVazao = norma.labelPara(tipo).uppercase()
        val limMin = norma.limiteMin(tipo)
        val limMax = norma.limiteMax(tipo)
        val vazaoRef = when (tipo) {
            TipoVazao.NOMINAL   -> modelo.vazaoNominal
            TipoVazao.TRANSICAO -> modelo.vazaoTransicao
            TipoVazao.MINIMA    -> modelo.vazaoMinima
        }
        val limLabel = "${limMin}% a +${limMax}%"

        // ── Cabeçalho do bloco de vazão ────────────────────────
        canvas.drawRect(ML, y, PW - ML, y + 18f, fillPaint(C_AZUL_CLR))
        canvas.drawText(
            "$nomeVazao   |   Referência: ${vazaoRef.toInt()} L/h   |   Limite aceitável: $limLabel",
            ML + 4f, y + 13f, textPaint(C_AZUL, 8.5f, bold = true)
        )
        y += 18f

        // ── Cabeçalho das colunas ─────────────────────────────
        // Larguras totais: 60+95+90+90+95+93 = 523 = CW  ✓ (sem coluna "Resultado")
        val CW_COLS = floatArrayOf(60f, 95f, 90f, 90f, 95f, 93f)
        val HDRS    = arrayOf("Medição", "Escoam.corr.(L)", "Leit. Inicial", "Leit. Final",
                              "Totalizado (L)", "Erro (%)")

        canvas.drawRect(ML, y, PW - ML, y + 20f, fillPaint(Color.rgb(235, 235, 235)))

        var cx = ML + 3f
        HDRS.forEachIndexed { i, h ->
            canvas.drawText(h, cx, y + 14f, textPaint(Color.DKGRAY, 7.5f, bold = true))
            cx += CW_COLS[i]
        }
        y += 20f

        // ── Linhas de medições ────────────────────────────────
        val medicoesDados = if (vazao != null) listOf(
            arrayOf("Medição 1", vazao.m1Escoamento, vazao.m1LeituraInicial, vazao.m1LeituraFinal, vazao.erro1),
            arrayOf("Medição 2", vazao.m2Escoamento, vazao.m2LeituraInicial, vazao.m2LeituraFinal, vazao.erro2),
            arrayOf("Medição 3", vazao.m3Escoamento, vazao.m3LeituraInicial, vazao.m3LeituraFinal, vazao.erro3)
        ) else emptyList()

        if (medicoesDados.isEmpty()) {
            // Sem dados
            repeat(3) {
                canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.3f))
                canvas.drawText("—", ML + 3f, y + 12f, textPaint(Color.GRAY, 8.5f))
                y += 16f
            }
        } else {
            medicoesDados.forEachIndexed { idx, med ->
                val nomeMed  = med[0] as String
                val esc      = med[1] as Double
                val li       = med[2] as Double
                val lf       = med[3] as Double
                val erroPct  = med[4] as Double

                // Fundo alternado
                if (idx % 2 == 1) {
                    canvas.drawRect(ML, y, PW - ML, y + 16f, fillPaint(C_CINZA_LIN))
                }
                canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.3f))

                val totalizado = lf - li
                val temDados  = esc > 0.0
                val dentroLim = temDados && (erroPct in limMin..limMax)

                // Escoamento corrigido pelo erro padrão da maleta (volume de referência)
                val escCorrigido = esc * (100.0 - erroPadrao) / 100.0

                // Formata os valores
                val escStr  = if (temDados) "%.3f".format(escCorrigido)  else "—"
                val liStr   = if (temDados) "%.3f".format(li)   else "—"
                val lfStr   = if (temDados) "%.3f".format(lf)   else "—"
                val totStr  = if (temDados) "%.3f".format(totalizado) else "—"
                val errStr  = if (temDados) "%.3f%%".format(erroPct)  else "—"

                // Cor do erro mantém referência visual (verde/vermelho), mas o veredito
                // individual (APROVADO/REPROVADO) não é mais exibido — só o resultado final.
                val erroCor = if (!temDados) Color.GRAY else if (dentroLim) C_VERDE else C_VERM

                cx = ML + 3f
                val valores = arrayOf(nomeMed, escStr, liStr, lfStr, totStr, errStr)
                valores.forEachIndexed { i, v ->
                    val p = when (i) {
                        5    -> textPaint(erroCor, 8.5f, bold = true)   // Erro
                        else -> textPaint(Color.BLACK, 8.5f)
                    }
                    canvas.drawText(v, cx, y + 12f, p)
                    cx += CW_COLS[i]
                }
                y += 16f
            }
        }

        // ── Linha de Erro Médio (sem veredito individual) ─────
        canvas.drawRect(ML, y, PW - ML, y + 18f, fillPaint(C_AZUL_CLR))

        val temDados   = vazao != null && vazao.m1Escoamento > 0 && vazao.m2Escoamento > 0 && vazao.m3Escoamento > 0
        val erroMedStr = if (temDados) "%.3f%%".format(vazao!!.erroMedio) else "Dados incompletos"

        canvas.drawText(
            "Erro Médio: $erroMedStr",
            ML + 4f, y + 13f,
            textPaint(Color.rgb(21, 60, 120), 9f, bold = true)
        )
        y += 18f
    }

    // ─────────────────────────────────────────────────────────────────
    // Resultado Final
    // ─────────────────────────────────────────────────────────────────

    private fun drawResultadoFinal(resultado: ResultadoFinal) {
        checkSpace(46f)
        y += 4f

        val texto = when (resultado) {
            ResultadoFinal.APROVADO      -> "APROVADO"
            ResultadoFinal.REPROVADO     -> "REPROVADO"
            ResultadoFinal.PENDENTE      -> "PENDENTE"
            ResultadoFinal.NAO_REALIZADO -> "ENSAIO NÃO REALIZADO"
        }

        val bgNeutro    = Color.rgb(244, 244, 244)
        val stripeColor = Color.rgb(51, 51, 51)
        val textColor   = Color.rgb(17, 17, 17)

        canvas.drawRect(ML, y, ML + 5f, y + 38f, fillPaint(stripeColor))
        canvas.drawRect(ML + 5f, y, PW - ML, y + 38f, fillPaint(bgNeutro))

        canvas.drawText("RESULTADO FINAL DO ENSAIO METROLÓGICO",
            ML + 12f, y + 11f, textPaint(Color.rgb(100, 100, 100), 8.5f))
        canvas.drawText(texto,
            ML + 12f, y + 30f, textPaint(textColor, 18f, bold = true))

        y += 44f
    }

    // ─────────────────────────────────────────────────────────────────
    // Ensaio não realizado
    // ─────────────────────────────────────────────────────────────────

    private fun drawEnsaioNaoRealizado(ensaio: Ensaio) {
        y += 8f
        checkSpace(60f)

        canvas.drawRect(ML, y, ML + 5f, y + 50f, fillPaint(Color.rgb(51, 51, 51)))
        canvas.drawRect(ML + 5f, y, PW - ML, y + 50f, fillPaint(Color.rgb(244, 244, 244)))
        canvas.drawText("RESULTADO DO ENSAIO",
            ML + 12f, y + 16f, textPaint(Color.rgb(100, 100, 100), 8.5f))
        canvas.drawText("ENSAIO NÃO REALIZADO",
            ML + 12f, y + 40f, textPaint(Color.rgb(17, 17, 17), 16f, bold = true))
        y += 56f

        drawTitulo("MOTIVO")
        checkSpace(26f)
        canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
        canvas.drawText(trunc(ensaio.motivoNaoRealizado.ifBlank { "-" }, 80),
            ML + 2f, y + 14f, textPaint(Color.BLACK, 9f, bold = true))
        y += 24f

        if (ensaio.fotoPath.isNotBlank()) {
            drawFotoNaoRealizado(ensaio.fotoPath)
        }
    }

    private fun drawFotoNaoRealizado(fotoPath: String) {
        val bitmap = FotoEnsaioHelper.carregarBitmap(context, fotoPath) ?: return
        drawTitulo("FOTO DO LOCAL")

        // Dimensões respeitando largura útil E altura máxima (evita estourar a página
        // com fotos em retrato). Altura máx. ≈ página menos margens/rodapé.
        val maxH = PH - 55f - 60f
        var imgW = CW
        var imgH = CW * bitmap.height.toFloat() / bitmap.width.toFloat()
        if (imgH > maxH) {
            imgH = maxH
            imgW = maxH * bitmap.width.toFloat() / bitmap.height.toFloat()
        }

        checkSpace(imgH + 12f)
        y += 4f
        val destino = android.graphics.RectF(ML, y, ML + imgW, y + imgH)
        canvas.drawBitmap(bitmap, null, destino, Paint().apply { isFilterBitmap = true })
        y += imgH + 8f
        bitmap.recycle()
    }

    // ─────────────────────────────────────────────────────────────────
    // Dados de substituição do hidrômetro reprovado
    // ─────────────────────────────────────────────────────────────────

    private fun drawDadosSubstituicao(ensaio: Ensaio) {
        checkSpace(70f)
        y += 4f

        drawTitulo("SUBSTITUIÇÃO DO HIDRÔMETRO REPROVADO")

        val pares = listOf(
            "Leitura Final do Hidrômetro Reprovado" to ensaio.leituraFinalReprovado.ifBlank { "-" },
            "Nº de Série do Novo Hidrômetro"        to ensaio.numeroSerieNovo.ifBlank { "-" },
            "Leitura Inicial do Novo Hidrômetro" to ensaio.leituraInicialNovo.ifBlank { "-" }
        )
        pares.forEach { (lbl, v) ->
            checkSpace(26f)
            canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
            canvas.drawText(lbl, ML + 2f, y + 9f, textPaint(Color.GRAY, 7.5f))
            canvas.drawText(trunc(v, 60), ML + 2f, y + 20f, textPaint(Color.BLACK, 9f, bold = true))
            y += 26f
        }
        canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
        y += 8f
    }

    // ─────────────────────────────────────────────────────────────────
    // Acompanhamento do ensaio pelo cliente
    // ─────────────────────────────────────────────────────────────────

    private fun drawAcompanhamentoCliente(ensaio: Ensaio) {
        checkSpace(60f)
        y += 4f
        drawTitulo("ACOMPANHAMENTO DO CLIENTE")

        if (!ensaio.clienteAcompanhou) {
            checkSpace(22f)
            canvas.drawText("O cliente não acompanhou o ensaio.",
                ML + 2f, y + 12f, textPaint(Color.BLACK, 9f))
            y += 22f
            return
        }

        if (ensaio.clienteRecusouDados) {
            checkSpace(22f)
            canvas.drawText("O cliente acompanhou o ensaio, mas recusou fornecer seus dados.",
                ML + 2f, y + 12f, textPaint(Color.BLACK, 9f, bold = true))
            y += 22f
            return
        }

        val pares = listOf(
            "Nome"              to ensaio.acompanhanteNome.ifBlank { "-" },
            "Documento (CPF/RG)" to ensaio.acompanhanteDocumento.ifBlank { "-" },
            "Telefone"          to ensaio.acompanhanteTelefone.ifBlank { "-" }
        )
        pares.forEach { (lbl, v) ->
            checkSpace(26f)
            canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
            canvas.drawText(lbl, ML + 2f, y + 9f, textPaint(Color.GRAY, 7.5f))
            canvas.drawText(trunc(v, 60), ML + 2f, y + 20f, textPaint(Color.BLACK, 9f, bold = true))
            y += 26f
        }
        canvas.drawLine(ML, y, PW - ML, y, strokePaint(Color.LTGRAY, 0.4f))
        y += 8f
    }

    // ─────────────────────────────────────────────────────────────────
    // Assinatura do técnico responsável
    // ─────────────────────────────────────────────────────────────────

    private fun drawAssinatura(ensaio: Ensaio) {
        // Espaço em branco para assinar + linha + identificação
        checkSpace(96f)
        y += 44f

        val cx   = PW / 2f
        val meia = 120f
        canvas.drawLine(cx - meia, y, cx + meia, y, strokePaint(Color.BLACK, 0.8f))
        canvas.drawText(ensaio.tecnicoResponsavel,
            cx, y + 13f, textPaint(Color.BLACK, 9f, bold = true, align = Paint.Align.CENTER))
        canvas.drawText("Assinatura do Técnico Responsável",
            cx, y + 25f, textPaint(Color.GRAY, 7.5f, align = Paint.Align.CENTER))
        y += 34f
    }

    // ─────────────────────────────────────────────────────────────────
    // Rodapé (posição fixa na última página)
    // ─────────────────────────────────────────────────────────────────

    private fun drawRodape(ensaio: Ensaio) {
        val ry = PH - 46f
        canvas.drawLine(ML, ry - 2f, PW - ML, ry - 2f, strokePaint(Color.LTGRAY, 0.5f))

        canvas.drawText("SANIBLUE Metrologia  •  Técnico: ${ensaio.tecnicoResponsavel}",
            ML, ry + 9f, textPaint(Color.GRAY, 7.5f))
        canvas.drawText("Emitido em: ${dataAtualFormatada()}  •  Documento gerado eletronicamente",
            ML, ry + 21f, textPaint(Color.GRAY, 7.5f))
        canvas.drawText("Pág. $pageNum",
            PW - ML, ry + 9f,
            textPaint(Color.GRAY, 7.5f, align = Paint.Align.RIGHT))

    }

    // ─────────────────────────────────────────────────────────────────
    // Título de seção (barra azul)
    // ─────────────────────────────────────────────────────────────────

    private fun drawTitulo(titulo: String) {
        checkSpace(20f)
        canvas.drawRect(ML, y, PW - ML, y + 16f, fillPaint(C_AZUL))
        canvas.drawText(titulo, ML + 4f, y + 11f, textPaint(Color.WHITE, 8.5f, bold = true))
        y += 18f
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers de Paint
    // ─────────────────────────────────────────────────────────────────

    private fun fillPaint(color: Int) = Paint().apply {
        this.color = color
        style = Paint.Style.FILL
        isAntiAlias = false
    }

    private fun strokePaint(color: Int, strokeWidth: Float) = Paint().apply {
        this.color = color
        style = Paint.Style.STROKE
        this.strokeWidth = strokeWidth
        isAntiAlias = false
    }

    private fun textPaint(
        color: Int,
        size: Float,
        bold: Boolean = false,
        align: Paint.Align = Paint.Align.LEFT
    ) = Paint().apply {
        this.color     = color
        this.textSize  = size
        this.textAlign = align
        this.typeface  = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        isAntiAlias    = true
    }

    // ─────────────────────────────────────────────────────────────────
    // Utilitários
    // ─────────────────────────────────────────────────────────────────

    private fun trunc(s: String, max: Int) =
        if (s.length > max) s.take(max - 1) + "…" else s
}
