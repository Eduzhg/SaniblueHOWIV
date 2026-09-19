package com.saniblue.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import kotlin.math.abs

/** Zoom máximo do visualizador. */
private const val ZOOM_MAX = 6f

/** Zoom aplicado ao dar dois toques na foto. */
private const val ZOOM_DUPLO_TOQUE = 2.5f

/**
 * Atalho discreto de foto embaixo de um campo de leitura: um ícone pequeno e uma
 * palavra. Sem foto abre a câmera; com foto abre o visualizador. Fica de propósito
 * no tamanho de legenda para não competir com o campo de digitação.
 */
@Composable
fun BotaoFotoLeitura(
    temFoto: Boolean,
    onTirar: () -> Unit,
    onVer: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cor = if (temFoto) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = modifier
            .clickable { if (temFoto) onVer() else onTirar() }
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = if (temFoto) Icons.Filled.PhotoCamera else Icons.Outlined.PhotoCamera,
            contentDescription = null,
            tint = cor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = if (temFoto) "ver foto" else "foto",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (temFoto) FontWeight.SemiBold else FontWeight.Normal,
            color = cor
        )
    }
}

/**
 * Foto da leitura em tela cheia, com pinça para zoom e arraste para andar pela
 * imagem — é assim que o técnico confere um dígito do mostrador. Dois toques
 * alternam entre encaixada na tela e ampliada.
 */
@Composable
fun VisualizadorFotoDialog(
    titulo: String,
    path: String,
    onFechar: () -> Unit,
    onRefazer: () -> Unit
) {
    var escala by remember { mutableStateOf(1f) }
    var deslocamento by remember { mutableStateOf(Offset.Zero) }
    var area by remember { mutableStateOf(IntSize.Zero) }

    // Impede arrastar a imagem para fora da tela: o deslocamento máximo é a metade
    // do que "sobra" da imagem ampliada em cada eixo.
    fun limitar(bruto: Offset, escalaAtual: Float): Offset {
        if (escalaAtual <= 1f) return Offset.Zero
        val maxX = abs(area.width * (escalaAtual - 1f)) / 2f
        val maxY = abs(area.height * (escalaAtual - 1f)) / 2f
        return Offset(bruto.x.coerceIn(-maxX, maxX), bruto.y.coerceIn(-maxY, maxY))
    }

    Dialog(
        onDismissRequest = onFechar,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        titulo,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "Pinça para ampliar • dois toques para encaixar",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFBDBDBD)
                    )
                }
                TextButton(onClick = onFechar) {
                    Icon(Icons.Default.Close, contentDescription = "Fechar", tint = Color.White,
                        modifier = Modifier.size(18.dp))
                    Text("  Fechar", color = Color.White)
                }
            }

            Box(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .onSizeChanged { area = it }
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val nova = (escala * zoom).coerceIn(1f, ZOOM_MAX)
                            escala = nova
                            deslocamento = limitar(deslocamento + pan, nova)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                if (escala > 1f) {
                                    escala = 1f
                                    deslocamento = Offset.Zero
                                } else {
                                    escala = ZOOM_DUPLO_TOQUE
                                }
                            }
                        )
                    }
            ) {
                AsyncImage(
                    model = path,
                    contentDescription = titulo,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = escala
                            scaleY = escala
                            translationX = deslocamento.x
                            translationY = deslocamento.y
                        }
                )
            }

            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(onClick = onRefazer) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White,
                        modifier = Modifier.size(18.dp))
                    Text("  Refazer foto", color = Color.White)
                }
            }
        }
    }
}
