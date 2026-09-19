package com.saniblue.app.presentation.components

import android.graphics.Bitmap
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChangeIgnoreConsumed
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.saniblue.app.presentation.theme.AprovadoGreen
import com.saniblue.app.presentation.theme.SaniblueBlue

/** Espessura do traço da assinatura (convertida para pixels na densidade da tela). */
private val TRACO = 2.5.dp

/** Folga em volta do traçado ao recortar o PNG final, em múltiplos da espessura. */
private const val MARGEM_RECORTE = 3f

/**
 * Campo de assinatura do formulário: mostra a assinatura já coletada (ou o convite
 * para assinar) e abre a tela cheia de coleta ao ser tocado.
 *
 * @param path caminho do PNG já gravado ("" quando ainda não assinou)
 * @param onAssinado bitmap aceito pelo signatário (o chamador grava e devolve o path)
 * @param onLimpar remove a assinatura já gravada
 */
@Composable
fun AssinaturaCampo(
    titulo: String,
    subtitulo: String,
    path: String,
    onAssinado: (Bitmap) -> Unit,
    onLimpar: () -> Unit,
    modifier: Modifier = Modifier
) {
    var coletando by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(titulo, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(
                subtitulo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (path.isNotBlank()) {
                AssinaturaPreview(path, Modifier.fillMaxWidth().height(120.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { coletando = true }, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                        Text("  Refazer")
                    }
                    TextButton(onClick = onLimpar) { Text("Remover") }
                }
            } else {
                Button(
                    onClick = { coletando = true },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SaniblueBlue)
                ) {
                    Icon(Icons.Default.Draw, null, modifier = Modifier.size(18.dp))
                    Text("  Assinar na tela", fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (coletando) {
        AssinaturaDialog(
            titulo = titulo,
            instrucao = subtitulo,
            onDismiss = { coletando = false },
            onAceitar = { bitmap ->
                coletando = false
                onAssinado(bitmap)
            }
        )
    }
}

/** Assinatura gravada, sempre sobre fundo branco (o PNG é preto e transparente). */
@Composable
fun AssinaturaPreview(path: String, modifier: Modifier = Modifier) {
    AsyncImage(
        model = path,
        contentDescription = "Assinatura",
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(4.dp)
    )
}

/**
 * Tela cheia de coleta da assinatura: área ampla para assinar com o dedo,
 * "Refazer" (limpa o traçado) e "Aceitar" (devolve o bitmap).
 */
// PointerInputChange.historical é experimental, mas é o que dá os pontos intermediários
// do sistema — sem eles o traço fica "quebrado" quando o dedo anda rápido.
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AssinaturaDialog(
    titulo: String,
    instrucao: String,
    onDismiss: () -> Unit,
    onAceitar: (Bitmap) -> Unit
) {
    // Cada traço é a sequência de pontos de um toque (dedo encostado até levantar).
    // Listas de snapshot (inclusive as internas) para o Canvas redesenhar a cada ponto.
    val tracos = remember { mutableStateListOf<SnapshotStateList<Offset>>() }
    var tamanhoArea by remember { mutableStateOf(IntSize.Zero) }
    val temTraco = tracos.isNotEmpty()
    val tracoPx = with(LocalDensity.current) { TRACO.toPx() }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(titulo, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(
                        instrucao,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                TextButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null, modifier = Modifier.size(18.dp))
                    Text("  Cancelar")
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .onSizeChanged { tamanhoArea = it }
                        .pointerInput(Unit) {
                            awaitPointerEventScope {
                                while (true) {
                                    val down = awaitPointerEvent().changes.firstOrNull { it.pressed }
                                        ?: continue
                                    val traco = mutableStateListOf(down.position)
                                    tracos.add(traco)
                                    down.consume()
                                    // Acompanha o dedo até levantar, incluindo os pontos
                                    // intermediários do sistema (traço mais fiel)
                                    var ativo = true
                                    while (ativo) {
                                        val evento = awaitPointerEvent()
                                        val mudanca = evento.changes.firstOrNull { it.id == down.id }
                                        if (mudanca == null || !mudanca.pressed) {
                                            ativo = false
                                        } else {
                                            if (mudanca.positionChangeIgnoreConsumed() != Offset.Zero) {
                                                mudanca.historical.forEach { traco.add(it.position) }
                                                traco.add(mudanca.position)
                                            }
                                            mudanca.consume()
                                        }
                                    }
                                }
                            }
                        }
                ) {
                    // Linha-guia da assinatura (não entra no PNG gravado)
                    val yLinha = size.height * 0.78f
                    drawLine(
                        color = Color(0xFFBDBDBD),
                        start = Offset(size.width * 0.08f, yLinha),
                        end = Offset(size.width * 0.92f, yLinha),
                        strokeWidth = 1f
                    )
                    tracos.forEach { pontos ->
                        drawPath(
                            path = pontosParaPath(pontos),
                            color = Color.Black,
                            style = Stroke(
                                width = tracoPx,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                if (!temTraco) {
                    Text(
                        "Assine aqui com o dedo",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF9E9E9E),
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { tracos.clear() },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = temTraco
                ) {
                    Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                    Text("  Refazer")
                }
                Button(
                    onClick = {
                        gerarBitmap(tracos, tamanhoArea, tracoPx)?.let(onAceitar)
                    },
                    modifier = Modifier.weight(1f).height(52.dp),
                    enabled = temTraco && tamanhoArea.width > 0,
                    colors = ButtonDefaults.buttonColors(containerColor = AprovadoGreen)
                ) {
                    Icon(Icons.Default.Check, null, modifier = Modifier.size(18.dp))
                    Text("  Aceitar", fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

/** Liga os pontos de um traço; um toque isolado vira um ponto (linha de 1px). */
private fun pontosParaPath(pontos: List<Offset>): Path = Path().apply {
    if (pontos.isEmpty()) return@apply
    moveTo(pontos.first().x, pontos.first().y)
    if (pontos.size == 1) {
        lineTo(pontos.first().x, pontos.first().y + 0.1f)
        return@apply
    }
    pontos.drop(1).forEach { lineTo(it.x, it.y) }
}

/**
 * Rasteriza os traços em um PNG transparente recortado na área realmente assinada.
 *
 * Desenhar de novo (em vez de capturar a view) mantém o resultado idêntico ao que o
 * signatário viu, sem a linha-guia nem o fundo. O recorte importa: a área de coleta é
 * uma tela inteira, e sem ele a assinatura sairia minúscula ao ser encaixada no laudo.
 */
private fun gerarBitmap(tracos: List<List<Offset>>, tamanho: IntSize, tracoPx: Float): Bitmap? {
    if (tamanho.width <= 0 || tamanho.height <= 0) return null
    val pontos = tracos.flatten()
    if (pontos.isEmpty()) return null

    val margem = tracoPx * MARGEM_RECORTE
    val x0 = (pontos.minOf { it.x } - margem).coerceAtLeast(0f)
    val y0 = (pontos.minOf { it.y } - margem).coerceAtLeast(0f)
    val x1 = (pontos.maxOf { it.x } + margem).coerceAtMost(tamanho.width.toFloat())
    val y1 = (pontos.maxOf { it.y } + margem).coerceAtMost(tamanho.height.toFloat())
    val largura = (x1 - x0).toInt().coerceAtLeast(1)
    val altura = (y1 - y0).toInt().coerceAtLeast(1)

    val bitmap = Bitmap.createBitmap(largura, altura, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)
    val paint = android.graphics.Paint().apply {
        color = android.graphics.Color.BLACK
        style = android.graphics.Paint.Style.STROKE
        strokeWidth = tracoPx
        strokeCap = android.graphics.Paint.Cap.ROUND
        strokeJoin = android.graphics.Paint.Join.ROUND
        isAntiAlias = true
    }
    tracos.forEach { traco ->
        if (traco.isEmpty()) return@forEach
        val primeiro = traco.first()
        val path = android.graphics.Path().apply {
            moveTo(primeiro.x - x0, primeiro.y - y0)
            if (traco.size == 1) lineTo(primeiro.x - x0, primeiro.y - y0 + 0.1f)
            else traco.drop(1).forEach { lineTo(it.x - x0, it.y - y0) }
        }
        canvas.drawPath(path, paint)
    }
    return bitmap
}
