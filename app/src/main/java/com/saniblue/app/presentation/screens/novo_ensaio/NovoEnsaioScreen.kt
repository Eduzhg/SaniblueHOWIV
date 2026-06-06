package com.saniblue.app.presentation.screens.novo_ensaio

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.saniblue.app.domain.model.MetodoEnsaio
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovoEnsaioScreen(
    ensaioId: Long = 0L,
    onNavigateBack: () -> Unit,
    onEnsaioSalvo: (Long) -> Unit,
    viewModel: NovoEnsaioViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(ensaioId) {
        if (ensaioId != 0L) viewModel.carregarEnsaio(ensaioId)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) onEnsaioSalvo(uiState.savedId)
    }

    val titulo = if (ensaioId == 0L) "Novo Ensaio" else "Editar Ensaio"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaniblueBlue),
                actions = {
                    IconButton(
                        onClick = { viewModel.salvar(ensaioId) },
                        enabled = !uiState.isLoading
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Salvar", tint = Color.White)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
        ) {
            // === NORMA E MÉTODO ===
            item { SectionHeader(title = "Norma e Método do Ensaio") }

            item {
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

            item {
                Text("Método de ensaio", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MetodoEnsaio.entries.forEach { metodo ->
                        FilterChip(
                            selected = uiState.metodoEnsaio == metodo,
                            onClick = { viewModel.selectMetodo(metodo) },
                            label = { Text(metodo.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = SaniblueBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                Text(
                    text = uiState.metodoEnsaio.descricao,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            // === DADOS CADASTRAIS ===
            item { SectionHeader(title = "Dados Cadastrais") }

            item {
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
            }

            item {
                OutlinedTextField(
                    value = uiState.nomeCompanhia,
                    onValueChange = viewModel::updateNomeCompanhia,
                    label = { Text("Nome da Companhia") },
                    placeholder = { Text("Ex.: Samae - Blumenau (SC)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.numeroHidrometro,
                        onValueChange = viewModel::updateNumeroHidrometro,
                        label = { Text("Nº Hidrômetro *") },
                        isError = uiState.validationErrors.containsKey("numeroHidrometro"),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.matricula,
                        onValueChange = viewModel::updateMatricula,
                        label = { Text("Matrícula") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }

            item {
                OutlinedTextField(
                    value = uiState.idadeHidrometro,
                    onValueChange = viewModel::updateIdadeHidrometro,
                    label = { Text("Idade do Hidrômetro (automática pelo nº de série)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.cliente,
                    onValueChange = viewModel::updateCliente,
                    label = { Text("Cliente *") },
                    isError = uiState.validationErrors.containsKey("cliente"),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.endereco,
                    onValueChange = viewModel::updateEndereco,
                    label = { Text("Endereço") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
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
            }

            // === DADOS DO ENSAIO ===
            item { SectionHeader(title = "Dados do Ensaio") }

            item {
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
            }

            item {
                OutlinedTextField(
                    value = uiState.tecnicoResponsavel,
                    onValueChange = viewModel::updateTecnico,
                    label = { Text("Técnico Responsável *") },
                    isError = uiState.validationErrors.containsKey("tecnicoResponsavel"),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            item {
                OutlinedTextField(
                    value = uiState.observacoes,
                    onValueChange = viewModel::updateObservacoes,
                    label = { Text("Observações") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }

            // === MODELO INFO ===
            uiState.modeloSelecionado?.let { modelo ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SaniblueBlue.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(12.dp)) {
                            Text(
                                text = "${modelo.nome}  •  ${uiState.norma.label}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = SaniblueBlue
                            )
                            Row(
                                Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                InfoChip(uiState.norma.labelNominal.short(), "${modelo.vazaoNominal.toInt()} L/h")
                                InfoChip(uiState.norma.labelTransicao.short(), "${modelo.vazaoTransicao.toInt()} L/h")
                                InfoChip(uiState.norma.labelMinima.short(), "${modelo.vazaoMinima.toInt()} L/h")
                            }
                        }
                    }
                }
            }

            // === 3 VAZÕES ===
            vazaoSection(
                tipo = TipoVazao.NOMINAL,
                titulo = uiState.norma.labelNominal,
                vazaoState = uiState.nominal,
                norma = uiState.norma,
                metodo = uiState.metodoEnsaio,
                vazaoRef = uiState.modeloSelecionado?.vazaoNominal?.toInt(),
                viewModel = viewModel
            )
            vazaoSection(
                tipo = TipoVazao.TRANSICAO,
                titulo = uiState.norma.labelTransicao,
                vazaoState = uiState.transicao,
                norma = uiState.norma,
                metodo = uiState.metodoEnsaio,
                vazaoRef = uiState.modeloSelecionado?.vazaoTransicao?.toInt(),
                viewModel = viewModel
            )
            vazaoSection(
                tipo = TipoVazao.MINIMA,
                titulo = uiState.norma.labelMinima,
                vazaoState = uiState.minima,
                norma = uiState.norma,
                metodo = uiState.metodoEnsaio,
                vazaoRef = uiState.modeloSelecionado?.vazaoMinima?.toInt(),
                viewModel = viewModel
            )

            // === RESULTADO FINAL ===
            item {
                ResultadoFinalCard(resultado = uiState.resultadoFinal)
            }

            // === DADOS DE SUBSTITUIÇÃO (apenas se REPROVADO) ===
            if (uiState.resultadoFinal == ResultadoFinal.REPROVADO) {
                item { SectionHeader(title = "Substituição do Hidrômetro Reprovado") }
                item {
                    OutlinedTextField(
                        value = uiState.leituraFinalReprovado,
                        onValueChange = viewModel::updateLeituraFinalReprovado,
                        label = { Text("Leitura Final do Hidrômetro Reprovado") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.numeroSerieNovo,
                        onValueChange = viewModel::updateNumeroSerieNovo,
                        label = { Text("Nº de Série do Novo Hidrômetro") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
                item {
                    OutlinedTextField(
                        value = uiState.leituraInicialNovo,
                        onValueChange = viewModel::updateLeituraInicialNovo,
                        label = { Text("Leitura Inicial do Hidrômetro Instalado") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }

            // === BOTÃO SALVAR ===
            item {
                Button(
                    onClick = { viewModel.salvar(ensaioId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = SaniblueBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (ensaioId == 0L) "SALVAR ENSAIO" else "ATUALIZAR ENSAIO",
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (uiState.error != null) {
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

/** Encurta um label "Vazão Nominal (QN)" → "QN" para o chip de referência. */
private fun String.short(): String =
    Regex("\\(([^)]+)\\)").find(this)?.groupValues?.get(1) ?: this

private fun androidx.compose.foundation.lazy.LazyListScope.vazaoSection(
    tipo: TipoVazao,
    titulo: String,
    vazaoState: VazaoState,
    norma: NormaEnsaio,
    metodo: MetodoEnsaio,
    vazaoRef: Int?,
    viewModel: NovoEnsaioViewModel
) {
    item { SectionHeader(title = titulo) }
    item {
        Text(
            text = buildString {
                append("Limite: ${norma.limiteLabel(tipo)}")
                if (vazaoRef != null) append("  •  Vazão: $vazaoRef L/h")
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
    item {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(1 to vazaoState.m1, 2 to vazaoState.m2, 3 to vazaoState.m3).forEach { (indice, m) ->
                MedicaoInputRow(
                    numero = indice,
                    metodo = metodo,
                    escoamento = m.escoamento,
                    leituraInicial = m.leituraInicial,
                    leituraFinal = m.leituraFinal,
                    padraoInicial = m.padraoInicial,
                    padraoFinal = m.padraoFinal,
                    erro = m.erro,
                    aprovado = m.aprovado,
                    onEscoamentoChange = { viewModel.updateMedicao(tipo, indice, escoamento = it) },
                    onLeituraInicialChange = { viewModel.updateMedicao(tipo, indice, inicial = it) },
                    onLeituraFinalChange = { viewModel.updateMedicao(tipo, indice, final = it) },
                    onPadraoInicialChange = { viewModel.updateMedicao(tipo, indice, padraoInicial = it) },
                    onPadraoFinalChange = { viewModel.updateMedicao(tipo, indice, padraoFinal = it) }
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
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = SaniblueBlue)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

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
                    text = "Erro médio: ${"%.2f".format(erroMedio)}%",
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
