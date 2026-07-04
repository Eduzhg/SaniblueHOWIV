package com.saniblue.app.presentation.screens.novo_ensaio

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saniblue.app.domain.model.MetodoEnsaio
import com.saniblue.app.domain.model.MotivosNaoRealizado
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.domain.model.ResultadoFinal
import com.saniblue.app.domain.model.TipoVazao
import com.saniblue.app.presentation.components.MedicaoInputRow
import com.saniblue.app.presentation.components.SectionHeader
import com.saniblue.app.presentation.theme.AprovadoGreen
import com.saniblue.app.presentation.theme.AprovadoGreenContainer
import com.saniblue.app.presentation.theme.PendenteOrange
import com.saniblue.app.presentation.theme.PendenteOrangeContainer
import com.saniblue.app.presentation.theme.ReprovadoRed
import com.saniblue.app.presentation.theme.ReprovadoRedContainer
import com.saniblue.app.presentation.theme.SaniblueBlue

private val PASSOS = listOf("Cadastro", "Nominal", "Transição", "Mínima", "Resultado")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoEnsaioScreen(
    ensaioId: Long = 0L,
    onNavigateBack: () -> Unit,
    onEnsaioSalvo: (Long) -> Unit,
    viewModel: NovoEnsaioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.iniciar(ensaioId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onEnsaioSalvo(uiState.savedId)
    }

    // Avisos não-bloqueantes (pendências ao trocar de etapa / campos obrigatórios)
    LaunchedEffect(uiState.mensagemAviso) {
        uiState.mensagemAviso?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.limparAviso()
        }
    }

    val titulo = if (ensaioId == 0L) "Novo Ensaio" else "Editar Ensaio"
    val passo = uiState.passoAtual
    val ultimoPasso = passo == TOTAL_PASSOS - 1

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaniblueBlue)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            WizardBottomBar(
                passo = passo,
                ultimoPasso = ultimoPasso,
                isLoading = uiState.isLoading,
                onVoltar = { viewModel.passoAnterior() },
                onProximo = { viewModel.proximoPasso() },
                onSalvar = { viewModel.salvar(ensaioId) }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            StepIndicator(
                passoAtual = passo,
                onPassoClick = { viewModel.irParaPasso(it) }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (passo) {
                    0 -> PassoCadastro(uiState, viewModel)
                    1 -> PassoVazao(TipoVazao.NOMINAL, uiState, viewModel)
                    2 -> PassoVazao(TipoVazao.TRANSICAO, uiState, viewModel)
                    3 -> PassoVazao(TipoVazao.MINIMA, uiState, viewModel)
                    else -> PassoResultado(uiState, viewModel)
                }
            }
        }
    }

    // Diálogo de confirmação de leitura suspeita (erro alto ou leitura "retrocedida")
    uiState.alertaLeitura?.let { alerta ->
        AlertDialog(
            onDismissRequest = { viewModel.descartarAlerta() },
            title = { Text("Confirmar leitura") },
            text = {
                when (alerta.tipoAlerta) {
                    TipoAlertaLeitura.ERRO_ALTO -> Text(
                        "A medição ${alerta.indice} resultou em um erro de " +
                            "${"%.3f".format(alerta.erroPct)}%, fora do esperado. " +
                            "Confira se os valores digitados estão corretos."
                    )
                    TipoAlertaLeitura.LEITURA_RETROCEDIDA -> Text(
                        "A leitura inicial da medição ${alerta.indice} é menor que a leitura final " +
                            "anterior (${"%.3f".format(alerta.leituraAnterior)}). O hidrômetro não retrocede — " +
                            "confira se os valores digitados estão corretos."
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmarAlerta() }) { Text("Está correto") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.descartarAlerta() }) { Text("Revisar") }
            }
        )
    }
}

// ==================== NAVEGAÇÃO ====================

@Composable
private fun StepIndicator(passoAtual: Int, onPassoClick: (Int) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PASSOS.forEachIndexed { index, _ ->
                StepCircle(
                    numero = index + 1,
                    estado = when {
                        index == passoAtual -> StepEstado.ATUAL
                        index < passoAtual -> StepEstado.CONCLUIDO
                        else -> StepEstado.FUTURO
                    },
                    onClick = { onPassoClick(index) }
                )
                if (index < PASSOS.size - 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(2.dp)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (index < passoAtual) SaniblueBlue else MaterialTheme.colorScheme.outlineVariant)
                    )
                }
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Passo ${passoAtual + 1} de $TOTAL_PASSOS — ${PASSOS[passoAtual]}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private enum class StepEstado { ATUAL, CONCLUIDO, FUTURO }

@Composable
private fun StepCircle(numero: Int, estado: StepEstado, onClick: () -> Unit) {
    val bg = when (estado) {
        StepEstado.ATUAL, StepEstado.CONCLUIDO -> SaniblueBlue
        StepEstado.FUTURO -> MaterialTheme.colorScheme.surfaceVariant
    }
    val fg = when (estado) {
        StepEstado.ATUAL, StepEstado.CONCLUIDO -> Color.White
        StepEstado.FUTURO -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (estado == StepEstado.CONCLUIDO) {
            Icon(Icons.Default.Check, contentDescription = null, tint = fg, modifier = Modifier.size(18.dp))
        } else {
            Text(text = numero.toString(), color = fg, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
private fun WizardBottomBar(
    passo: Int,
    ultimoPasso: Boolean,
    isLoading: Boolean,
    onVoltar: () -> Unit,
    onProximo: () -> Unit,
    onSalvar: () -> Unit
) {
    Surface(shadowElevation = 8.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                // Mantém os botões acima da barra de navegação do sistema (edge-to-edge)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onVoltar,
                enabled = passo > 0,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Voltar")
            }

            Button(
                onClick = if (ultimoPasso) onSalvar else onProximo,
                enabled = !isLoading,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SaniblueBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (ultimoPasso) "SALVAR" else "Próximo",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ==================== PASSO 1 — CADASTRO ====================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PassoCadastro(uiState: NovoEnsaioUiState, viewModel: NovoEnsaioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // === AVISO DE RASCUNHO RESTAURADO ===
        if (uiState.rascunhoRestaurado) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PendenteOrangeContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Rascunho recuperado da sessão anterior.",
                        style = MaterialTheme.typography.bodySmall,
                        color = PendenteOrange,
                        modifier = Modifier.weight(1f)
                    )
                    Row {
                        TextButton(onClick = { viewModel.dispensarAvisoRascunho() }) { Text("OK") }
                        TextButton(onClick = { viewModel.descartarRascunho() }) { Text("Descartar") }
                    }
                }
            }
        }

        // === NORMA ===
        SectionHeader(title = "Norma do Ensaio")
        Column {
            Text("Norma", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                NormaEnsaio.entries.forEach { norma ->
                    FilterChip(
                        selected = uiState.norma == norma,
                        onClick = { viewModel.selectNorma(norma) },
                        label = { Text(norma.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SaniblueBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Text(
                text = uiState.norma.descricao,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            )
        }

        // Método + maleta vêm do login (só leitura)
        Card(
            colors = CardDefaults.cardColors(containerColor = SaniblueBlue.copy(alpha = 0.08f)),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Configuração do turno (definida no login)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Método: ${uiState.metodoEnsaio.label}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = SaniblueBlue)
                Text("Maleta: ${uiState.maletaNome}  •  Erro padrão: ${uiState.erroPadrao}%", style = MaterialTheme.typography.bodySmall, color = SaniblueBlue)
            }
        }

        // === DADOS CADASTRAIS ===
        SectionHeader(title = "Dados Cadastrais")

        // Modelo de hidrômetro (define as vazões de referência)
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = uiState.modeloSelecionado?.nome ?: "Selecione o modelo",
                onValueChange = {},
                readOnly = true,
                label = { Text("Modelo do Hidrômetro *") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                uiState.modelos.forEach { modelo ->
                    DropdownMenuItem(
                        text = { Text(modelo.nome) },
                        onClick = {
                            viewModel.selectModelo(modelo.id)
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = uiState.nomeCompanhia,
            onValueChange = viewModel::updateNomeCompanhia,
            label = { Text("Nome da Companhia") },
            placeholder = { Text("Ex.: Samae - Blumenau (SC)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.numeroHidrometro,
                onValueChange = viewModel::updateNumeroHidrometro,
                label = { Text("Nº Hidrômetro *") },
                isError = uiState.validationErrors.containsKey("numeroHidrometro"),
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("A99A999999") },
                supportingText = {
                    Text(
                        uiState.validationErrors["numeroHidrometro"] ?: "Letra, 2 nº, letra, 6 nº",
                        color = if (uiState.validationErrors.containsKey("numeroHidrometro"))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )
            OutlinedTextField(
                value = uiState.matricula,
                onValueChange = viewModel::updateMatricula,
                label = { Text("Matrícula") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = uiState.idadeHidrometro,
            onValueChange = viewModel::updateIdadeHidrometro,
            label = { Text("Idade do Hidrômetro (automática pelo nº de série)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.cliente,
            onValueChange = viewModel::updateCliente,
            label = { Text("Cliente *") },
            isError = uiState.validationErrors.containsKey("cliente"),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = uiState.endereco,
            onValueChange = viewModel::updateEndereco,
            label = { Text("Endereço") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.bairro,
                onValueChange = viewModel::updateBairro,
                label = { Text("Bairro") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.cidade,
                onValueChange = viewModel::updateCidade,
                label = { Text("Cidade") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        // === DADOS DO ENSAIO ===
        SectionHeader(title = "Dados do Ensaio")

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.dataEnsaio,
                onValueChange = viewModel::updateDataEnsaio,
                label = { Text("Data *") },
                isError = uiState.validationErrors.containsKey("dataEnsaio"),
                modifier = Modifier.weight(1f),
                singleLine = true,
                placeholder = { Text("DD/MM/AAAA") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = DateVisualTransformation(),
                supportingText = uiState.validationErrors["dataEnsaio"]?.let {
                    { Text(it, color = MaterialTheme.colorScheme.error) }
                }
            )
            OutlinedTextField(
                value = uiState.temperaturaAgua,
                onValueChange = viewModel::updateTemperaturaAgua,
                label = { Text("Temp. Água (°C)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = uiState.tecnicoResponsavel,
                onValueChange = viewModel::updateTecnico,
                label = { Text("Técnico Responsável *") },
                isError = uiState.validationErrors.containsKey("tecnicoResponsavel"),
                modifier = Modifier.weight(2f),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.pressaoMedia,
                onValueChange = viewModel::updatePressaoMedia,
                label = { Text("Pressão Média (mca)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        OutlinedTextField(
            value = uiState.observacoes,
            onValueChange = viewModel::updateObservacoes,
            label = { Text("Observações") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2,
            maxLines = 4
        )

        // === ENSAIO NÃO REALIZADO ===
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Ensaio não realizado", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Switch(
                checked = !uiState.realizado,
                onCheckedChange = { viewModel.setRealizado(!it) }
            )
        }

        if (!uiState.realizado) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text("Motivo *", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                MotivosNaoRealizado.lista.forEach { motivo ->
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = uiState.motivoNaoRealizado == motivo ||
                                (motivo == MotivosNaoRealizado.OUTRO && uiState.motivoNaoRealizado.isNotBlank() && uiState.motivoNaoRealizado !in MotivosNaoRealizado.lista),
                            onClick = { viewModel.updateMotivoNaoRealizado(motivo) }
                        )
                        Text(motivo, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                val outroSelecionado = uiState.motivoNaoRealizado == MotivosNaoRealizado.OUTRO ||
                    (uiState.motivoNaoRealizado.isNotBlank() && uiState.motivoNaoRealizado !in MotivosNaoRealizado.lista)
                if (outroSelecionado) {
                    OutlinedTextField(
                        value = if (uiState.motivoNaoRealizado == MotivosNaoRealizado.OUTRO) "" else uiState.motivoNaoRealizado,
                        onValueChange = { viewModel.updateMotivoNaoRealizado(it) },
                        label = { Text("Descreva o motivo") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                if (uiState.validationErrors.containsKey("motivoNaoRealizado")) {
                    Text(
                        uiState.validationErrors["motivoNaoRealizado"]!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ==================== PASSOS 2-4 — VAZÕES ====================

@Composable
private fun PassoVazao(tipo: TipoVazao, uiState: NovoEnsaioUiState, viewModel: NovoEnsaioViewModel) {
    val vazaoState = when (tipo) {
        TipoVazao.NOMINAL -> uiState.nominal
        TipoVazao.TRANSICAO -> uiState.transicao
        TipoVazao.MINIMA -> uiState.minima
    }
    val titulo = uiState.norma.labelPara(tipo)
    val vazaoRef = uiState.modeloSelecionado?.let {
        when (tipo) {
            TipoVazao.NOMINAL -> it.vazaoNominal.toInt()
            TipoVazao.TRANSICAO -> it.vazaoTransicao.toInt()
            TipoVazao.MINIMA -> it.vazaoMinima.toInt()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        SectionHeader(title = titulo)

        if (!uiState.realizado) {
            Card(
                colors = CardDefaults.cardColors(containerColor = PendenteOrangeContainer),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Text(
                    "Ensaio marcado como não realizado — nenhuma medição é necessária.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PendenteOrange,
                    modifier = Modifier.padding(12.dp)
                )
            }
        } else {
            // Card com a norma + limites + vazão de referência
            Card(
                colors = CardDefaults.cardColors(containerColor = SaniblueBlue.copy(alpha = 0.08f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        text = "${uiState.modeloSelecionado?.nome ?: "Sem modelo"}  •  ${uiState.norma.label}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SaniblueBlue
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = buildString {
                            append("Limite: ${uiState.norma.limiteLabel(tipo)}")
                            if (vazaoRef != null) append("  •  Vazão: $vazaoRef L/h")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            listOf(1 to vazaoState.m1, 2 to vazaoState.m2, 3 to vazaoState.m3).forEach { (indice, m) ->
                MedicaoInputRow(
                    numero = indice,
                    metodo = uiState.metodoEnsaio,
                    escoamento = m.escoamento,
                    leituraInicial = m.leituraInicial,
                    leituraFinal = m.leituraFinal,
                    padraoInicial = m.padraoInicial,
                    padraoFinal = m.padraoFinal,
                    erro = m.erro,
                    aprovado = m.aprovado,
                    erroPadrao = uiState.erroPadrao,
                    onEscoamentoChange = { viewModel.updateMedicao(tipo, indice, escoamento = it) },
                    onLeituraInicialChange = { viewModel.updateMedicao(tipo, indice, inicial = it) },
                    onLeituraFinalChange = { viewModel.updateMedicao(tipo, indice, final = it) },
                    onPadraoInicialChange = { viewModel.updateMedicao(tipo, indice, padraoInicial = it) },
                    onPadraoFinalChange = { viewModel.updateMedicao(tipo, indice, padraoFinal = it) },
                    onLeituraInicialBlur = { viewModel.verificarLeituraInicialSuspeita(tipo, indice) },
                    onLeituraFinalBlur = { viewModel.verificarLeituraSuspeita(tipo, indice) }
                )
            }

            vazaoState.erroMedio?.let { medio ->
                ResultadoVazaoCard(
                    erroMedio = medio,
                    aprovado = vazaoState.aprovado ?: false,
                    label = titulo
                )
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// ==================== PASSO 5 — RESULTADO ====================

@Composable
private fun PassoResultado(uiState: NovoEnsaioUiState, viewModel: NovoEnsaioViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SectionHeader(title = "Resumo do Ensaio")

        if (uiState.realizado) {
            ResumoVazaoLinha(uiState.norma.labelNominal, uiState.nominal)
            ResumoVazaoLinha(uiState.norma.labelTransicao, uiState.transicao)
            ResumoVazaoLinha(uiState.norma.labelMinima, uiState.minima)
            Spacer(Modifier.height(4.dp))
        }

        ResultadoFinalCard(resultado = uiState.resultadoFinal)

        // === DADOS DE SUBSTITUIÇÃO (apenas se REPROVADO) ===
        if (uiState.resultadoFinal == ResultadoFinal.REPROVADO) {
            SectionHeader(title = "Substituição do Hidrômetro Reprovado")
            OutlinedTextField(
                value = uiState.leituraFinalReprovado,
                onValueChange = viewModel::updateLeituraFinalReprovado,
                label = { Text("Leitura Final do Hidrômetro Reprovado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.numeroSerieNovo,
                onValueChange = viewModel::updateNumeroSerieNovo,
                label = { Text("Nº de Série do Novo Hidrômetro") },
                placeholder = { Text("A99A999999") },
                isError = uiState.validationErrors.containsKey("numeroSerieNovo"),
                supportingText = {
                    Text(
                        uiState.validationErrors["numeroSerieNovo"] ?: "Letra, 2 nº, letra, 6 nº",
                        color = if (uiState.validationErrors.containsKey("numeroSerieNovo"))
                            MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            OutlinedTextField(
                value = uiState.leituraInicialNovo,
                onValueChange = viewModel::updateLeituraInicialNovo,
                label = { Text("Leitura Inicial do Hidrômetro Instalado") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        uiState.error?.let { erro ->
            Text(
                text = erro,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ResumoVazaoLinha(label: String, vazao: VazaoState) {
    val medio = vazao.erroMedio
    val aprovado = vazao.aprovado
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        if (medio != null && aprovado != null) {
            val fg = if (aprovado) AprovadoGreen else ReprovadoRed
            Text(
                text = "${"%.2f".format(medio)}%  •  ${if (aprovado) "APROVADO" else "REPROVADO"}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        } else {
            Text(
                text = "Pendente",
                style = MaterialTheme.typography.labelMedium,
                color = PendenteOrange
            )
        }
    }
}

// ==================== COMPONENTES COMPARTILHADOS ====================

@Composable
private fun ResultadoVazaoCard(erroMedio: Double, aprovado: Boolean, label: String) {
    val bg = if (aprovado) AprovadoGreenContainer else ReprovadoRedContainer
    val fg = if (aprovado) AprovadoGreen else ReprovadoRed
    val icon = if (aprovado) Icons.Default.CheckCircle else Icons.Default.Error
    val status = if (aprovado) "APROVADO" else "REPROVADO"

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = label, style = MaterialTheme.typography.labelMedium, color = fg)
                Text(
                    text = "Erro médio: ${"%.3f".format(erroMedio)}%",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(20.dp))
                Text(
                    text = " $status",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = fg
                )
            }
        }
    }
}

@Composable
private fun ResultadoFinalCard(resultado: ResultadoFinal) {
    val (bg, fg, icon, label) = when (resultado) {
        ResultadoFinal.APROVADO -> Quadruplet(
            AprovadoGreenContainer, AprovadoGreen, Icons.Default.CheckCircle, "APROVADO"
        )
        ResultadoFinal.REPROVADO -> Quadruplet(
            ReprovadoRedContainer, ReprovadoRed, Icons.Default.Error, "REPROVADO"
        )
        ResultadoFinal.PENDENTE -> Quadruplet(
            PendenteOrangeContainer, PendenteOrange, Icons.Default.HourglassEmpty, "PENDENTE"
        )
        ResultadoFinal.NAO_REALIZADO -> Quadruplet(
            PendenteOrangeContainer, PendenteOrange, Icons.Default.HourglassEmpty, "NÃO REALIZADO"
        )
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = bg),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "RESULTADO FINAL DO ENSAIO",
                style = MaterialTheme.typography.labelLarge,
                color = fg
            )
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = fg, modifier = Modifier.size(32.dp))
                Text(
                    text = "  $label",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = fg
                )
            }
        }
    }
}

private data class Quadruplet<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

/**
 * Máscara visual DD/MM/AAAA para TextField.
 * O campo armazena apenas dígitos ("08052026"); a barra é inserida só na exibição.
 */
private class DateVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.take(8)
        val out = buildString {
            digits.forEachIndexed { i, c ->
                if (i == 2 || i == 4) append('/')
                append(c)
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val mapped = when {
                    offset <= 1 -> offset
                    offset <= 3 -> offset + 1
                    else        -> offset + 2
                }
                return minOf(mapped, out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                val mapped = when {
                    offset <= 2 -> offset
                    offset <= 5 -> offset - 1
                    else        -> offset - 2
                }
                return minOf(mapped, digits.length)
            }
        }

        return TransformedText(AnnotatedString(out), offsetMapping)
    }
}
