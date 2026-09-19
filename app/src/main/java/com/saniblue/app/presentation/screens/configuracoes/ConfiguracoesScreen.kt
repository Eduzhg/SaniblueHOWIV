package com.saniblue.app.presentation.screens.configuracoes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saniblue.app.BuildConfig
import com.saniblue.app.domain.model.ResumoFotosEnsaio
import com.saniblue.app.presentation.theme.SaniblueBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConfiguracoesViewModel = hiltViewModel()
) {
    val totalEnsaios by viewModel.totalEnsaios.collectAsStateWithLifecycle()
    val mensagem by viewModel.mensagem.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val usoFotos by viewModel.usoFotos.collectAsStateWithLifecycle()
    val fotosPorEnsaio by viewModel.fotosPorEnsaio.collectAsStateWithLifecycle()
    var confirmarLimpeza by remember { mutableStateOf(false) }
    var confirmarLimpezaFotos by remember { mutableStateOf(false) }
    var escolherEnsaioFotos by remember { mutableStateOf(false) }
    var ensaioParaLimpar by remember { mutableStateOf<ResumoFotosEnsaio?>(null) }

    // Recontar as fotos ao abrir a tela (podem ter mudado desde a última visita)
    LaunchedEffect(Unit) { viewModel.atualizarFotos() }

    LaunchedEffect(mensagem) {
        mensagem?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparMensagem()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configurações", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaniblueBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ConfigSection(titulo = "Sobre o Aplicativo") {
                ConfigItem(
                    icon = Icons.Default.WaterDrop,
                    titulo = "SANIBLUE Metrologia",
                    subtitulo = "Versão ${BuildConfig.VERSION_NAME} — Sistema de Verificação Metrológica de Hidrômetros"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Info,
                    titulo = "Desenvolvido para",
                    subtitulo = "SANIBLUE — Engenharia em Hidrometria"
                )
            }

            ConfigSection(titulo = "Dados") {
                ConfigItem(
                    icon = Icons.Default.Storage,
                    titulo = "Armazenamento Local",
                    subtitulo = "Room Database — 100% Offline"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Sync,
                    titulo = "Sincronização",
                    subtitulo = "Disponível em versão futura"
                )
            }

            ConfigSection(titulo = "Normas de Referência") {
                ConfigItem(
                    icon = Icons.Default.Info,
                    titulo = "ABNT NBR ISO 4064",
                    subtitulo = "Medição de água potável em sistemas de distribuição"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Info,
                    titulo = "INMETRO / Portaria 246",
                    subtitulo = "Hidrômetros — QN (nominal) / QT (transição) / QM (mínima)"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Info,
                    titulo = "INMETRO / Portaria 155",
                    subtitulo = "Hidrômetros — Q3 (permanente) / Q2 (transição) / Q1 (mínima)"
                )
            }

            // === Manutenção — apagar todos os ensaios do dispositivo ===
            ConfigSection(titulo = "Manutenção") {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Apaga os ensaios e todas as suas medições. Os laudos em PDF " +
                            "já gerados continuam no aparelho.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { confirmarLimpeza = true },
                        enabled = totalEnsaios > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (totalEnsaios > 0) "Limpar ensaios ($totalEnsaios)" else "Sem ensaios para limpar",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    HorizontalDivider()

                    // === Fotos das leituras ===
                    // São só para o técnico conferir a leitura na hora; não entram no
                    // laudo. Por isso podem ser descartadas sem perder nada do ensaio.
                    Text(
                        text = "Fotos das leituras",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "São as fotos tiradas em cada leitura das medições, usadas só " +
                            "para conferir um valor na hora do ensaio. Não aparecem no laudo, " +
                            "então apagá-las não altera nenhum resultado — só libera espaço.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (usoFotos.quantidade > 0)
                            "Guardadas agora: ${usoFotos.quantidade} fotos • ${usoFotos.tamanhoLegivel}"
                        else "Nenhuma foto de leitura guardada.",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = SaniblueBlue
                    )

                    Text(
                        text = "Apaga as fotos de TODOS os ensaios de uma vez.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = { confirmarLimpezaFotos = true },
                        enabled = usoFotos.quantidade > 0,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (usoFotos.quantidade > 0)
                                "Limpar todas as fotos (${usoFotos.quantidade})"
                            else "Sem fotos para limpar",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Text(
                        text = "Ou escolha um ensaio já conferido e apague só as fotos dele, " +
                            "mantendo as dos ensaios que ainda está acompanhando.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedButton(
                        onClick = { escolherEnsaioFotos = true },
                        enabled = fotosPorEnsaio.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (fotosPorEnsaio.isNotEmpty())
                                "Limpar fotos de um ensaio (${fotosPorEnsaio.size} com fotos)"
                            else "Nenhum ensaio com fotos",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Confirmação da limpeza (ação destrutiva, sem desfazer)
    if (confirmarLimpeza) {
        AlertDialog(
            onDismissRequest = { confirmarLimpeza = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Limpar todos os ensaios?") },
            text = {
                Text(
                    "Isso vai apagar TODOS os $totalEnsaios ensaios (e suas medições) deste dispositivo. " +
                        "Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limparEnsaios()
                        confirmarLimpeza = false
                    }
                ) {
                    Text("Apagar tudo", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpeza = false }) { Text("Cancelar") }
            }
        )
    }

    // Confirmação da limpeza total de fotos
    if (confirmarLimpezaFotos) {
        AlertDialog(
            onDismissRequest = { confirmarLimpezaFotos = false },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Limpar todas as fotos de leitura?") },
            text = {
                Text(
                    "Serão apagadas ${usoFotos.quantidade} fotos (${usoFotos.tamanhoLegivel}) de todos " +
                        "os ensaios. As medições, os resultados e os laudos não mudam — só as fotos " +
                        "de conferência somem. Esta ação não pode ser desfeita."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limparTodasAsFotos()
                        confirmarLimpezaFotos = false
                    }
                ) {
                    Text("Apagar fotos", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmarLimpezaFotos = false }) { Text("Cancelar") }
            }
        )
    }

    // Escolha do ensaio cujas fotos serão apagadas
    if (escolherEnsaioFotos) {
        AlertDialog(
            onDismissRequest = { escolherEnsaioFotos = false },
            icon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = SaniblueBlue) },
            title = { Text("Fotos de qual ensaio?") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        "Toque no ensaio para apagar as fotos dele. As medições continuam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    fotosPorEnsaio.forEach { resumo ->
                        HorizontalDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    ensaioParaLimpar = resumo
                                    escolherEnsaioFotos = false
                                }
                                .padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    text = resumo.numeroHidrometro.ifBlank { "Sem nº de série" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = resumo.dataEnsaio.ifBlank { "Sem data" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                text = "${resumo.quantidadeFotos} fotos",
                                style = MaterialTheme.typography.labelMedium,
                                color = SaniblueBlue
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { escolherEnsaioFotos = false }) { Text("Fechar") }
            }
        )
    }

    // Confirmação da limpeza de um ensaio específico
    ensaioParaLimpar?.let { resumo ->
        AlertDialog(
            onDismissRequest = { ensaioParaLimpar = null },
            icon = { Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Limpar as fotos deste ensaio?") },
            text = {
                Text(
                    "Serão apagadas ${resumo.quantidadeFotos} fotos do hidrômetro " +
                        "${resumo.numeroHidrometro.ifBlank { "sem nº de série" }} " +
                        "(${resumo.dataEnsaio.ifBlank { "sem data" }}). As medições e o resultado " +
                        "do ensaio continuam intactos."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.limparFotosDoEnsaio(resumo)
                        ensaioParaLimpar = null
                    }
                ) {
                    Text("Apagar fotos", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { ensaioParaLimpar = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ConfigSection(titulo: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(0.dp)) {
        Text(
            text = titulo.uppercase(),
            style = MaterialTheme.typography.labelMedium,
            color = SaniblueBlue,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(1.dp)
        ) {
            Column { content() }
        }
    }
}

@Composable
private fun ConfigItem(icon: ImageVector, titulo: String, subtitulo: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = SaniblueBlue)
        Column {
            Text(text = titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(text = subtitulo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
