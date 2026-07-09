package com.saniblue.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "ensaios",
    foreignKeys = [
        ForeignKey(
            entity = HidrometroModeloEntity::class,
            parentColumns = ["id"],
            childColumns = ["hidrometro_modelo_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("hidrometro_modelo_id"),
        Index("numero_hidrometro"),
        Index("data_ensaio")
    ]
)
data class EnsaioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // Referência ao modelo de hidrômetro
    @ColumnInfo("hidrometro_modelo_id") val hidrometroModeloId: Long,

    // Dados de identificação
    @ColumnInfo("numero_hidrometro") val numeroHidrometro: String,
    @ColumnInfo("cliente") val cliente: String,
    @ColumnInfo("matricula") val matricula: String = "",
    @ColumnInfo("endereco") val endereco: String = "",
    @ColumnInfo("cidade") val cidade: String = "",
    @ColumnInfo("bairro") val bairro: String = "",

    // Dados do ensaio
    @ColumnInfo("data_ensaio") val dataEnsaio: String,
    @ColumnInfo("tecnico_responsavel") val tecnicoResponsavel: String,
    @ColumnInfo("idade_hidrometro") val idadeHidrometro: String = "",
    @ColumnInfo("observacoes") val observacoes: String = "",

    // Nome da companhia de saneamento (ex.: "Samae - Blumenau (SC)")
    @ColumnInfo("nome_companhia") val nomeCompanhia: String = "",

    // Norma e método do ensaio (nomes dos enums NormaEnsaio / MetodoEnsaio)
    @ColumnInfo("norma") val norma: String = "PORTARIA_246",
    @ColumnInfo("metodo_ensaio") val metodoEnsaio: String = "ESCOAMENTO_DIRETO",

    // Maleta usada e erros padrão (por vazão) aplicados no cálculo
    @ColumnInfo("maleta_nome") val maletaNome: String = "",
    @ColumnInfo("erro_padrao_nominal") val erroPadraoNominal: Double = 0.0,
    @ColumnInfo("erro_padrao_transicao") val erroPadraoTransicao: Double = 0.0,
    @ColumnInfo("erro_padrao_minima") val erroPadraoMinima: Double = 0.0,

    // Pressão média (mca)
    @ColumnInfo("pressao_media") val pressaoMedia: String = "",

    // Ensaio não realizado + motivo
    @ColumnInfo("realizado") val realizado: Boolean = true,
    @ColumnInfo("motivo_nao_realizado") val motivoNaoRealizado: String = "",

    // Dados preenchidos apenas quando REPROVADO e substituído
    @ColumnInfo("leitura_final_reprovado") val leituraFinalReprovado: String = "",
    @ColumnInfo("numero_serie_novo") val numeroSerieNovo: String = "",
    @ColumnInfo("leitura_inicial_novo") val leituraInicialNovo: String = "",

    // Foto do local (apenas ensaios não realizados)
    @ColumnInfo("foto_path") val fotoPath: String = "",

    // Acompanhamento do ensaio pelo cliente (testemunha)
    @ColumnInfo("cliente_acompanhou") val clienteAcompanhou: Boolean = false,
    @ColumnInfo("cliente_recusou_dados") val clienteRecusouDados: Boolean = false,
    @ColumnInfo("acompanhante_nome") val acompanhanteNome: String = "",
    @ColumnInfo("acompanhante_documento") val acompanhanteDocumento: String = "",
    @ColumnInfo("acompanhante_telefone") val acompanhanteTelefone: String = "",

    // Resultado calculado e armazenado
    @ColumnInfo("resultado_final") val resultadoFinal: String = "PENDENTE",

    // Controle de sincronização para uso futuro
    @ColumnInfo("sync_status") val syncStatus: String = "PENDING",
    @ColumnInfo("created_at") val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo("updated_at") val updatedAt: Long = System.currentTimeMillis()
)
