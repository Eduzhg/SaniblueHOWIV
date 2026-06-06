package com.saniblue.app.presentation.screens.configuracoes

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.saniblue.app.presentation.theme.SaniblueBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfiguracoesScreen(onNavigateBack: () -> Unit) {
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
        }
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
                    subtitulo = "Versão 1.0.0 — Sistema de Verificação Metrológica de Hidrômetros"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Info,
                    titulo = "Desenvolvido para",
                    subtitulo = "SANIBLUE — Saneamento e Abastecimento"
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

            ConfigSection(titulo = "Padrões de Acesso") {
                ConfigItem(
                    icon = Icons.Default.Person,
                    titulo = "Usuário administrador",
                    subtitulo = "Login: admin  •  Senha: admin123"
                )
                HorizontalDivider()
                ConfigItem(
                    icon = Icons.Default.Person,
                    titulo = "Usuário técnico",
                    subtitulo = "Login: tecnico  •  Senha: tecnico123"
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
                    subtitulo = "Aprovação de instrumentos de medição — hidrômetros"
                )
            }
        }
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
