package com.saniblue.app.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.presentation.theme.AprovadoGreen
import com.saniblue.app.presentation.theme.AprovadoGreenContainer
import com.saniblue.app.presentation.theme.PendenteOrange
import com.saniblue.app.presentation.theme.PendenteOrangeContainer
import com.saniblue.app.presentation.theme.ReprovadoRed
import com.saniblue.app.presentation.theme.ReprovadoRedContainer

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun ResultadoBadge(
    resultado: ResultadoFinal,
    modifier: Modifier = Modifier,
    large: Boolean = false
) {
    val (backgroundColor, textColor, icon, label) = when (resultado) {
        ResultadoFinal.APROVADO -> Quadruplet(
            AprovadoGreenContainer, AprovadoGreen,
            Icons.Default.CheckCircle, "APROVADO"
        )
        ResultadoFinal.REPROVADO -> Quadruplet(
            ReprovadoRedContainer, ReprovadoRed,
            Icons.Default.Error, "REPROVADO"
        )
        ResultadoFinal.PENDENTE -> Quadruplet(
            PendenteOrangeContainer, PendenteOrange,
            Icons.Default.HourglassEmpty, "PENDENTE"
        )
        ResultadoFinal.NAO_REALIZADO -> Quadruplet(
            PendenteOrangeContainer, PendenteOrange,
            Icons.Default.HourglassEmpty, "NÃO REALIZADO"
        )
    }

    val padding = if (large) 12.dp else 6.dp
    val fontSize = if (large) 18.sp else 12.sp
    val iconSize = if (large) 28.dp else 16.dp

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(if (large) 12.dp else 8.dp)),
        color = backgroundColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = padding, vertical = padding / 2),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(iconSize)
            )
            Text(
                text = label,
                color = textColor,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
                .size(height = 2.dp, width = 40.dp)
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.45f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.55f)
        )
    }
}

@Composable
fun StatCard(
    titulo: String,
    valor: Int,
    cor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = cor.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = valor.toString(),
                style = MaterialTheme.typography.headlineMedium,
                color = cor,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = titulo,
                style = MaterialTheme.typography.bodySmall,
                color = cor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun ErroChip(
    erro: Double,
    aprovado: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = if (aprovado) AprovadoGreenContainer else ReprovadoRedContainer
    val fg = if (aprovado) AprovadoGreen else ReprovadoRed
    Surface(
        modifier = modifier.clip(RoundedCornerShape(4.dp)),
        color = bg
    ) {
        Text(
            text = "%.3f%%".format(erro),
            color = fg,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private data class Quadruplet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
