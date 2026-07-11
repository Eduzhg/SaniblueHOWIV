package com.saniblue.app.presentation.screens.hidrometros

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.saniblue.app.domain.model.HidrometroModelo
import com.saniblue.app.domain.model.NormaEnsaio
import com.saniblue.app.presentation.theme.SaniblueBlue
import com.saniblue.app.util.formatVazao

/**
 * Consulta do catálogo fixo de capacidades de hidrômetro (norma + letra + classe R).
 * Somente leitura: a norma e as vazões de cada ensaio são resolvidas automaticamente
 * pelo nº de série no Novo Ensaio — não há cadastro manual de modelos.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CadastroHidrometrosScreen(
    onNavigateBack: () -> Unit,
    viewModel: CadastroHidrometrosViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

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
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "Capacidades identificadas automaticamente pelo nº de série do hidrômetro no Novo Ensaio.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(uiState.modelos, key = { it.id }) { modelo ->
                ModeloCard(modelo)
            }
        }
    }
}

@Composable
private fun ModeloCard(modelo: HidrometroModelo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            // O nome já inclui a norma (ex.: "... — Portaria 246") — sem repetir ao lado
            Text(
                text = modelo.nome,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = buildString {
                    append("Letra: ${modelo.letra}")
                    modelo.classeR?.let { append("  •  Classe: ${it.label}") }
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            val labelNominal = if (modelo.norma == NormaEnsaio.PORTARIA_155) "Q3" else "QN"
            val labelTransicao = if (modelo.norma == NormaEnsaio.PORTARIA_155) "Q2" else "QT"
            val labelMinima = if (modelo.norma == NormaEnsaio.PORTARIA_155) "Q1" else "QM"
            Text(
                text = "$labelNominal: ${formatVazao(modelo.vazaoNominal)} L/h  •  " +
                    "$labelTransicao: ${formatVazao(modelo.vazaoTransicao)} L/h  •  " +
                    "$labelMinima: ${formatVazao(modelo.vazaoMinima)} L/h",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
