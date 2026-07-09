package com.saniblue.app.presentation.screens.detalhes

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.saniblue.app.domain.model.Ensaio
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.VazaoEnsaio
import com.saniblue.app.presentation.components.ErroChip
import coil.compose.AsyncImage
import com.saniblue.app.presentation.components.InfoRow
import com.saniblue.app.presentation.components.LoadingContent
import com.saniblue.app.presentation.components.SectionHeader
import com.saniblue.app.presentation.theme.SaniblueBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalhesEnsaioScreen(
    ensaioId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEditar: (Long) -> Unit,
    viewModel: DetalhesEnsaioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(ensaioId) { viewModel.carregar(ensaioId) }

    LaunchedEffect(uiState.pdfFile) {
        uiState.pdfFile?.let { file ->
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(intent, "Abrir laudo PDF"))
            viewModel.clearPdf()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.ensaio?.let { "Ensaio Nº ${it.numeroHidrometro}" } ?: "Detalhes",
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaniblueBlue),
                actions = {
                    uiState.ensaio?.let {
                        IconButton(onClick = { onNavigateToEditar(it.id) }) {
                            Icon(Icons.Default.Edit, "Editar", tint = Color.White)
                        }
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> LoadingContent(Modifier.padding(padding))
            uiState.ensaio == null -> {
                Column(
                    Modifier.fillMaxSize().padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { Text("Ensaio não encontrado") }
            }
            else -> {
                val ensaio = uiState.ensaio!!
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Resultado em destaque
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(text = "Nº ${ensaio.numeroHidrometro}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text(text = ensaio.cliente, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(text = ensaio.dataEnsaio, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Dados cadastrais
                    SectionHeader("Dados do Cliente")
                    Card {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            InfoRow("Companhia", ensaio.nomeCompanhia.ifBlank { "-" })
                            InfoRow("Matrícula", ensaio.matricula.ifBlank { "-" })
                            InfoRow("Endereço", ensaio.endereco.ifBlank { "-" })
                            InfoRow("Bairro", ensaio.bairro.ifBlank { "-" })
                            InfoRow("Cidade", ensaio.cidade.ifBlank { "-" })
                        }
                    }

                    SectionHeader("Dados do Ensaio")
                    Card {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            InfoRow("Técnico", ensaio.tecnicoResponsavel)
                            InfoRow("Data", ensaio.dataEnsaio)
                            InfoRow("Idade", ensaio.idadeHidrometro.ifBlank { "-" })
                            InfoRow("Pressão Média", ensaio.pressaoMedia.let { if (it.isBlank()) "-" else "$it kg/cm²" })
                            InfoRow("Norma", ensaio.norma.descricao)
                            InfoRow("Método", ensaio.metodoEnsaio.label)
                            InfoRow("Maleta", ensaio.maletaNome.ifBlank { "-" })
                            InfoRow("Erro Padrão (Nominal)", "${ensaio.erroPadraoNominal}%")
                            InfoRow("Erro Padrão (Transição)", "${ensaio.erroPadraoTransicao}%")
                            InfoRow("Erro Padrão (Mínima)", "${ensaio.erroPadraoMinima}%")
                            if (ensaio.observacoes.isNotBlank()) {
                                InfoRow("Observações", ensaio.observacoes)
                            }
                        }
                    }

                    if (!ensaio.realizado) {
                        // Ensaio não realizado
                        SectionHeader("Ensaio Não Realizado")
                        Card {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                InfoRow("Motivo", ensaio.motivoNaoRealizado.ifBlank { "-" })
                                if (ensaio.fotoPath.isNotBlank()) {
                                    Text("Foto do local", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    AsyncImage(
                                        model = ensaio.fotoPath,
                                        contentDescription = "Foto do local",
                                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp))
                                    )
                                }
                            }
                        }
                    } else {
                        // Resultados por vazão
                        ensaio.vazoes.forEach { vazao ->
                            VazaoResultadoCard(vazao = vazao, norma = ensaio.norma)
                        }
                    }

                    // Dados de substituição (apenas reprovado)
                    if (ensaio.resultadoFinal == com.saniblue.app.domain.model.ResultadoFinal.REPROVADO) {
                        SectionHeader("Substituição do Hidrômetro Reprovado")
                        Card {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                InfoRow("Leitura Final (reprovado)", ensaio.leituraFinalReprovado.ifBlank { "-" })
                                InfoRow("Nº Série (novo)", ensaio.numeroSerieNovo.ifBlank { "-" })
                                InfoRow("Leitura Inicial (novo)", ensaio.leituraInicialNovo.ifBlank { "-" })
                            }
                        }
                    }

                    // Botões de ação
                    SectionHeader("Laudo")
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.gerarPdf(context) },
                            modifier = Modifier.weight(1f).height(52.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SaniblueBlue),
                            enabled = !uiState.isGeneratingPdf
                        ) {
                            if (uiState.isGeneratingPdf) {
                                CircularProgressIndicator(Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                            } else {
                                Icon(Icons.Default.PictureAsPdf, null, modifier = Modifier.size(18.dp))
                            }
                            Text("  Gerar PDF", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.error != null) {
                        Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun VazaoResultadoCard(vazao: VazaoEnsaio, norma: NormaEnsaio) {
    val tipoLabel = norma.labelPara(vazao.tipoVazao)
    val limiteLabel = norma.limiteLabel(vazao.tipoVazao)

    Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(1.dp)) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(text = tipoLabel, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(text = "Limite: $limiteLabel", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            HorizontalDivider()

            // Tabela de erros
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Med. 1", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ErroChip(vazao.erro1, vazao.aprovado)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Med. 2", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ErroChip(vazao.erro2, vazao.aprovado)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Med. 3", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ErroChip(vazao.erro3, vazao.aprovado)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Médio", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    ErroChip(vazao.erroMedio, vazao.aprovado)
                }
            }
        }
    }
}
