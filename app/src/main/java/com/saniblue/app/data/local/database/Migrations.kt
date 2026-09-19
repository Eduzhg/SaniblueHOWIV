package com.saniblue.app.data.local.database

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Migrações do banco. Sem uma migração declarada, o Room cai no
 * fallbackToDestructiveMigration e APAGA os ensaios já gravados no tablet ao
 * atualizar o APK — por isso toda coluna nova precisa de um ALTER TABLE aqui.
 */

/** v11 → v12: assinaturas do cliente e do técnico coletadas na tela. */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE ensaios ADD COLUMN assinatura_cliente_path TEXT NOT NULL DEFAULT ''")
        db.execSQL("ALTER TABLE ensaios ADD COLUMN assinatura_tecnico_path TEXT NOT NULL DEFAULT ''")
    }
}

/** Todas as migrações conhecidas, na ordem. */
val MIGRATIONS = arrayOf(MIGRATION_11_12)
