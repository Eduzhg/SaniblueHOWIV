package com.saniblue.app.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.util.toDoubleLocale

@Composable
fun MedicaoInputRow(
    numero: Int,
    metodo: MetodoEnsaio,
    escoamento: String,
    leituraInicial: String,
    leituraFinal: String,
    padraoInicial: String,
    padraoFinal: String,
    erro: Double?,
    aprovado: Boolean? = null,
    erroPadrao: Double = 0.0,
    onEscoamentoChange: (String) -> Unit,
    onLeituraInicialChange: (String) -> Unit,
    onLeituraFinalChange: (String) -> Unit,
    onPadraoInicialChange: (String) -> Unit,
    onPadraoFinalChange: (String) -> Unit,
    onLeituraFinalBlur: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val comparativo = metodo == MetodoEnsaio.COMPARATIVO_LEITURA

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Medição $numero",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            if (comparativo) {
                // Método comparativo: leituras do padrão ultrassônico (a maleta não zera)
                Text(
                    text = "Padrão ultrassônico (maleta)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = padraoInicial,
                        onValueChange = onPadraoInicialChange,
                        label = { Text("Padrão Inicial", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = padraoFinal,
                        onValueChange = onPadraoFinalChange,
                        label = { Text("Padrão Final", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = escoamento,
                        onValueChange = {},
                        readOnly = true,
                        enabled = false,
                        label = { Text("Escoamento (L)", style = MaterialTheme.typography.labelSmall) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = "Hidrômetro em teste",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = leituraInicial,
                        onValueChange = onLeituraInicialChange,
                        label = { Text("Leit. Inicial", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = leituraFinal,
                        onValueChange = onLeituraFinalChange,
                        label = { Text("Leit. Final", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).aoSairDoCampo(onLeituraFinalBlur),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            } else {
                // Método escoamento direto: volume informado + leitura do hidrômetro
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = escoamento,
                        onValueChange = onEscoamentoChange,
                        label = { Text("Escoamento (L)", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = leituraInicial,
                        onValueChange = onLeituraInicialChange,
                        label = { Text("Leit. Inicial", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                    OutlinedTextField(
                        value = leituraFinal,
                        onValueChange = onLeituraFinalChange,
                        label = { Text("Leit. Final", style = MaterialTheme.typography.labelSmall) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f).aoSairDoCampo(onLeituraFinalBlur),
                        textStyle = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // Escoamento corrigido pelo erro padrão da maleta (volume de referência real)
            val escBruto = escoamento.toDoubleLocale()
            if (escBruto != null && escBruto > 0.0) {
                val corrigido = escBruto * (100.0 - erroPadrao) / 100.0
                Text(
                    text = "Escoamento corrigido (−$erroPadrao%): ${"%.3f".format(corrigido)} L",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (erro != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val ini = leituraInicial.toDoubleLocale()
                    val fin = leituraFinal.toDoubleLocale()
                    val totalizado = if (ini != null && fin != null) fin - ini else null

                    if (totalizado != null) {
                        Text(
                            text = "Totalizado: %.3f L  |  ".format(totalizado),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (aprovado != null) {
                        ErroChip(erro = erro, aprovado = aprovado)
                    } else {
                        Text(
                            text = "%.3f%%".format(erro),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Chama [onBlur] quando o campo PERDE o foco (depois de ter sido focado).
 * Usado para só validar a leitura quando o técnico termina de digitar e sai do campo.
 */
@Composable
private fun Modifier.aoSairDoCampo(onBlur: () -> Unit): Modifier {
    var teveFoco by remember { mutableStateOf(false) }
    return this.onFocusChanged { fs ->
        if (fs.isFocused) {
            teveFoco = true
        } else if (teveFoco) {
            teveFoco = false
            onBlur()
        }
    }
}
