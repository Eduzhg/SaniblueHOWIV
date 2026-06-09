package com.saniblue.app.data.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.rascunhoDataStore by preferencesDataStore(name = "rascunho_ensaio")

/**
 * Guarda o rascunho do ensaio em andamento (JSON) para não perder os dados se o
 * app for minimizado/fechado/morto pelo sistema. É um único rascunho por vez.
 */
@Singleton
class RascunhoEnsaioStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val chaveJson = stringPreferencesKey("json")

    suspend fun salvar(json: String) {
        context.rascunhoDataStore.edit { it[chaveJson] = json }
    }

    suspend fun ler(): String? =
        context.rascunhoDataStore.data.map { it[chaveJson] }.first()

    suspend fun limpar() {
        context.rascunhoDataStore.edit { it.remove(chaveJson) }
    }
}
