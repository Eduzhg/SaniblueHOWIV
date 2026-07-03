package com.saniblue.app.presentation.screens.hidrometros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.presentation.theme.SaniblueBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroHidrometrosScreen(
    onNavigateBack: () -> Unit,
    viewModel: CadastroHidrometrosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var modeloParaDeletar by remember { mutableStateOf<HidrometroModelo?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hidrômetros Cadastrados", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SaniblueBlue)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::iniciarNovoModelo, containerColor = SaniblueBlue) {
                Icon(Icons.Default.Add, null, tint = Color.White)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(uiState.modelos, key = { it.id }) { modelo ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = modelo.nome, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Nominal: ${modelo.vazaoNominal.toInt()} L/h | Trans: ${modelo.vazaoTransicao.toInt()} L/h | Mín: ${modelo.vazaoMinima.toInt()} L/h",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Row {
                            IconButton(onClick = { viewModel.editarModelo(modelo) }) {
                                Icon(Icons.Default.Edit, "Editar", tint = SaniblueBlue)
                            }
                            IconButton(onClick = { modeloParaDeletar = modelo }) {
                                Icon(Icons.Default.Delete, "Deletar", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }

    // Bottom sheet para edição
    if (uiState.isEditing) {
        ModalBottomSheet(
            onDismissRequest = viewModel::cancelar,
            sheetState = sheetState
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (uiState.modeloEmEdicao == null) "Novo Modelo" else "Editar Modelo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = uiState.nome,
                    onValueChange = viewModel::onNomeChange,
                    label = { Text("Nome do Modelo *") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                OutlinedTextField(
                    value = uiState.descricao,
                    onValueChange = viewModel::onDescricaoChange,
                    label = { Text("Descrição") },
                    modifier = Modifier.fillMaxWidth(), singleLine = true
                )
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.vazaoNominal,
                        onValueChange = viewModel::onVazaoNominalChange,
                        label = { Text("Vazão Nominal (L/h) *") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = uiState.vazaoTransicao,
                        onValueChange = viewModel::onVazaoTransicaoChange,
                        label = { Text("Transição (L/h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                    OutlinedTextField(
                        value = uiState.vazaoMinima,
                        onValueChange = viewModel::onVazaoMinimaChange,
                        label = { Text("Mínima (L/h)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f), singleLine = true
                    )
                }
                if (uiState.error != null) {
                    Text(uiState.error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = viewModel::cancelar, modifier = Modifier.weight(1f)) {
                        Text("Cancelar")
                    }
                    androidx.compose.material3.Button(
                        onClick = viewModel::salvar,
                        modifier = Modifier.weight(1f),
                        colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = SaniblueBlue)
                    ) {
                        Text("Salvar")
                    }
                }
            }
        }
    }

    // Confirmação de exclusão
    modeloParaDeletar?.let { modelo ->
        AlertDialog(
            onDismissRequest = { modeloParaDeletar = null },
            title = { Text("Confirmar exclusão") },
            text = { Text("Deseja excluir o modelo \"${modelo.nome}\"?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletar(modelo)
                    modeloParaDeletar = null
                }) { Text("Excluir", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { modeloParaDeletar = null }) { Text("Cancelar") }
            }
        )
    }
}
